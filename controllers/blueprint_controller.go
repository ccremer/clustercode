package controllers

import (
	"context"
	"path/filepath"
	"time"

	"github.com/go-logr/logr"
	batchv1 "k8s.io/api/batch/v1"
	"k8s.io/api/batch/v1beta1"
	corev1 "k8s.io/api/core/v1"
	rbacv1 "k8s.io/api/rbac/v1"
	apierrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/utils/pointer"
	"k8s.io/utils/strings"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
	"sigs.k8s.io/controller-runtime/pkg/predicate"

	"github.com/ccremer/clustercode/api/v1alpha1"
	"github.com/ccremer/clustercode/cfg"
)

type (
	// BlueprintReconciler reconciles Blueprint objects
	BlueprintReconciler struct {
		Client client.Client
		Log    logr.Logger
		Scheme *runtime.Scheme
	}
	// BlueprintContext holds the parameters of a single reconciliation
	BlueprintContext struct {
		ctx       context.Context
		blueprint *v1alpha1.Blueprint
		log       logr.Logger
	}
)

func (r *BlueprintReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&v1alpha1.Blueprint{}).
		WithEventFilter(predicate.GenerationChangedPredicate{}).
		Complete(r)
}

// +kubebuilder:rbac:groups=clustercode.github.io,resources=blueprints,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=clustercode.github.io,resources=blueprints/status,verbs=get;update;patch
// +kubebuilder:rbac:groups=batch,resources=jobs,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=batch,resources=cronjobs,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=batch,resources=cronjobs/status,verbs=get;update;patch
// +kubebuilder:rbac:groups=core,resources=serviceaccounts,verbs=get;list;create;delete
// +kubebuilder:rbac:groups=rbac.authorization.k8s.io,resources=roles;rolebindings,verbs=get;list;create;delete

func (r *BlueprintReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	rc := &BlueprintContext{
		ctx:       ctx,
		blueprint: &v1alpha1.Blueprint{},
	}
	err := r.Client.Get(ctx, req.NamespacedName, rc.blueprint)
	if err != nil {
		if apierrors.IsNotFound(err) {
			r.Log.Info("object not found, ignoring reconcile", "object", req.NamespacedName)
			return ctrl.Result{}, nil
		}
		r.Log.Error(err, "could not retrieve object", "object", req.NamespacedName)
		return ctrl.Result{Requeue: true, RequeueAfter: time.Minute}, err
	}
	rc.log = r.Log.WithValues("blueprint", req.NamespacedName)
	r.handleBlueprint(rc)
	rc.log.Info("reconciled blueprint")
	return ctrl.Result{}, nil
}

func (r *BlueprintReconciler) handleBlueprint(rc *BlueprintContext) {

	saName, err := r.createServiceAccountAndBinding(rc)
	if err != nil {
		rc.log.Error(err, "cannot ensure that scanner job have necessary RBAC permissions")
	}

	cronJob := v1beta1.CronJob{
		ObjectMeta: metav1.ObjectMeta{
			Name:      rc.blueprint.Name + "-scan-job",
			Namespace: rc.blueprint.Namespace,
			Labels:    labels.Merge(ClusterCodeLabels, ClustercodeTypeScan.AsLabels()),
		},
		Spec: v1beta1.CronJobSpec{
			Schedule:          rc.blueprint.Spec.ScanSchedule,
			ConcurrencyPolicy: v1beta1.ForbidConcurrent,
			Suspend:           &rc.blueprint.Spec.Suspend,

			JobTemplate: v1beta1.JobTemplateSpec{
				Spec: batchv1.JobSpec{
					BackoffLimit: pointer.Int32Ptr(0),
					Template: corev1.PodTemplateSpec{
						Spec: corev1.PodSpec{
							ServiceAccountName: saName,
							RestartPolicy:      corev1.RestartPolicyNever,
							Containers: []corev1.Container{
								{
									Name: "scanner",
									Env: []corev1.EnvVar{
										{
											Name:  "CC_LOG__DEBUG",
											Value: "true",
										},
									},
									Args: []string{
										"scan",
										"--namespace=" + rc.blueprint.Namespace,
										"--scan.blueprint-name=" + rc.blueprint.Name,
									},
									Image: cfg.Config.Operator.ClustercodeContainerImage,
									VolumeMounts: []corev1.VolumeMount{
										{
											Name:      SourceSubMountPath,
											MountPath: filepath.Join("/clustercode", SourceSubMountPath),
											SubPath:   rc.blueprint.Spec.Storage.SourcePvc.SubPath,
										},
										{
											Name:      IntermediateSubMountPath,
											MountPath: filepath.Join("/clustercode", IntermediateSubMountPath),
											SubPath:   rc.blueprint.Spec.Storage.SourcePvc.SubPath,
										},
									},
								},
							},
							Volumes: []corev1.Volume{
								{
									Name: SourceSubMountPath,
									VolumeSource: corev1.VolumeSource{
										PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
											ClaimName: rc.blueprint.Spec.Storage.SourcePvc.ClaimName,
										},
									},
								},
								{
									Name: IntermediateSubMountPath,
									VolumeSource: corev1.VolumeSource{
										PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
											ClaimName: rc.blueprint.Spec.Storage.IntermediatePvc.ClaimName,
										},
									},
								},
							},
						},
					},
				},
			},
			SuccessfulJobsHistoryLimit: pointer.Int32Ptr(1),
			FailedJobsHistoryLimit:     pointer.Int32Ptr(1),
		},
	}
	if err := controllerutil.SetControllerReference(rc.blueprint, cronJob.GetObjectMeta(), r.Scheme); err != nil {
		rc.log.Error(err, "could not set controller reference, deleting the blueprint will not delete the cronjob", "cronjob", cronJob.Name)
	}

	if err := r.Client.Create(rc.ctx, &cronJob); err != nil {
		if apierrors.IsAlreadyExists(err) {
			rc.log.Info("cronjob already exists, updating it")
			err = r.Client.Update(rc.ctx, &cronJob)
			if err != nil {
				rc.log.Error(err, "could not update cronjob")
			}
			return
		}
		if !apierrors.IsNotFound(err) {
			rc.log.Error(err, "could not create cronjob")
			return
		}
	} else {
		rc.log.Info("created cronjob")
	}
}

func (r *BlueprintReconciler) createServiceAccountAndBinding(rc *BlueprintContext) (string, error) {
	binding, sa := r.newRbacDefinition(rc)

	err := r.Client.Create(rc.ctx, &sa)
	if err != nil {
		if !apierrors.IsAlreadyExists(err) {
			return sa.Name, err
		}
	} else {
		rc.log.Info("service account created", "sa", sa.Name)
	}
	err = r.Client.Create(rc.ctx, &binding)
	if err != nil {
		if !apierrors.IsAlreadyExists(err) {
			return sa.Name, err
		}
	} else {
		rc.log.Info("rolebinding created", "roleBinding", binding.Name)
	}
	return sa.Name, nil
}

func (r *BlueprintReconciler) newRbacDefinition(rc *BlueprintContext) (rbacv1.RoleBinding, corev1.ServiceAccount) {
	saName := rc.blueprint.GetServiceAccountName()
	roleBinding := rbacv1.RoleBinding{
		ObjectMeta: metav1.ObjectMeta{
			Name:      strings.ShortenString(saName, 51) + "-rolebinding",
			Namespace: rc.blueprint.Namespace,
			Labels:    ClusterCodeLabels,
		},
		Subjects: []rbacv1.Subject{
			{
				Kind:      "ServiceAccount",
				Namespace: rc.blueprint.Namespace,
				Name:      saName,
			},
		},
		RoleRef: rbacv1.RoleRef{
			Kind:     cfg.Config.Scan.RoleKind,
			Name:     cfg.Config.Scan.RoleName,
			APIGroup: rbacv1.GroupName,
		},
	}

	account := corev1.ServiceAccount{
		ObjectMeta: metav1.ObjectMeta{
			Name:      saName,
			Namespace: rc.blueprint.Namespace,
			Labels:    ClusterCodeLabels,
		},
	}

	if err := controllerutil.SetControllerReference(rc.blueprint, roleBinding.GetObjectMeta(), r.Scheme); err != nil {
		rc.log.Error(err, "could not set controller reference on role", "roleBinding", roleBinding.Name)
	}
	if err := controllerutil.SetControllerReference(rc.blueprint, account.GetObjectMeta(), r.Scheme); err != nil {
		rc.log.Error(err, "could not set controller reference on service account", "sa", account.Name)
	}
	return roleBinding, account
}
