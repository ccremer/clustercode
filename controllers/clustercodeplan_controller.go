package controllers

import (
	"context"
	"time"

	"github.com/go-logr/logr"
	batchv1 "k8s.io/api/batch/v1"
	"k8s.io/api/batch/v1beta1"
	corev1 "k8s.io/api/core/v1"
	rbacv1 "k8s.io/api/rbac/v1"
	apierrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/utils/pointer"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
	"sigs.k8s.io/controller-runtime/pkg/predicate"

	"github.com/ccremer/clustercode/api/v1alpha1"
	"github.com/ccremer/clustercode/cfg"
)

type (
	// ClustercodePlanReconciler reconciles ClustercodePlan objects
	ClustercodePlanReconciler struct {
		Client client.Client
		Log    logr.Logger
		Scheme *runtime.Scheme
	}
	// ReconciliationContext holds the parameters of a single reconciliation
	ReconciliationContext struct {
		ctx  context.Context
		plan *v1alpha1.ClustercodePlan
		log  logr.Logger
	}
)

func (r *ClustercodePlanReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&v1alpha1.ClustercodePlan{}).
		WithEventFilter(predicate.GenerationChangedPredicate{}).
		Complete(r)
}

// +kubebuilder:rbac:groups=clustercode.github.io,resources=clustercodeplans,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=clustercode.github.io,resources=clustercodeplans/status,verbs=get;update;patch
// +kubebuilder:rbac:groups=batch,resources=jobs,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=batch,resources=cronjobs,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=batch,resources=cronjobs/status,verbs=get;update;patch
// +kubebuilder:rbac:groups=core,resources=serviceaccounts,verbs=get;list;create;delete
// +kubebuilder:rbac:groups=rbac.authorization.k8s.io,resources=roles;rolebindings,verbs=get;list;create;delete

func (r *ClustercodePlanReconciler) Reconcile(ctx context.Context, req ctrl.Request) (result ctrl.Result, returnErr error) {
	rc := &ReconciliationContext{
		ctx:  ctx,
		plan: &v1alpha1.ClustercodePlan{},
	}
	err := r.Client.Get(ctx, req.NamespacedName, rc.plan)
	if err != nil {
		if apierrors.IsNotFound(err) {
			r.Log.Info("object not found, ignoring reconcile", "object", req.NamespacedName)
			return ctrl.Result{}, nil
		}
		r.Log.Error(err, "could not retrieve object", "object", req.NamespacedName)
		return ctrl.Result{Requeue: true, RequeueAfter: time.Minute}, err
	}
	rc.log = r.Log.WithValues("plan", req.NamespacedName)
	r.handlePlan(rc)
	return ctrl.Result{}, nil
}

func (r *ClustercodePlanReconciler) handlePlan(rc *ReconciliationContext) {

	saName, err := r.createServiceAccountAndBinding(rc)
	if err != nil {
		rc.log.Error(err, "cannot ensure that scanner job have necessary RBAC permissions")
	}

	cronJob := v1beta1.CronJob{
		ObjectMeta: metav1.ObjectMeta{
			Name:      rc.plan.Name + "-scan-job",
			Namespace: rc.plan.Namespace,
			Labels: map[string]string{
				"app.kubernetes.io/managed-by": "clustercode",
			},
		},
		Spec: v1beta1.CronJobSpec{
			Schedule:          rc.plan.Spec.ScanSchedule,
			ConcurrencyPolicy: v1beta1.ForbidConcurrent,
			Suspend:           &rc.plan.Spec.Suspend,

			JobTemplate: v1beta1.JobTemplateSpec{
				Spec: batchv1.JobSpec{
					BackoffLimit: pointer.Int32Ptr(1),
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
										"--scan.namespace=" + rc.plan.Namespace,
										"--scan.clustercode-plan-name=" + rc.plan.Name,
									},
									Image: "localhost:5000/clustercode/operator:e2e",
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
	if err := controllerutil.SetControllerReference(rc.plan, cronJob.GetObjectMeta(), r.Scheme); err != nil {
		rc.log.Error(err, "could not set controller reference, deleting the plan will not delete the cronjob", "cronjob", cronJob.Name)
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

func (r *ClustercodePlanReconciler) createServiceAccountAndBinding(rc *ReconciliationContext) (string, error) {
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

func (r *ClustercodePlanReconciler) newRbacDefinition(rc *ReconciliationContext) (rbacv1.RoleBinding, corev1.ServiceAccount) {
	saName := rc.plan.Name + "-clustercode"
	roleBinding := rbacv1.RoleBinding{
		ObjectMeta: metav1.ObjectMeta{
			Name:      saName + "-rolebinding",
			Namespace: rc.plan.Namespace,
		},
		Subjects: []rbacv1.Subject{
			{
				Kind:      "ServiceAccount",
				Namespace: rc.plan.Namespace,
				Name:      saName,
			},
		},
		RoleRef: rbacv1.RoleRef{
			Kind:     "ClusterRole",
			Name:     cfg.Config.Scan.ClusterRoleName,
			APIGroup: rbacv1.GroupName,
		},
	}

	account := corev1.ServiceAccount{
		ObjectMeta: metav1.ObjectMeta{
			Name:      saName,
			Namespace: rc.plan.Namespace,
		},
	}

	if err := controllerutil.SetControllerReference(rc.plan, roleBinding.GetObjectMeta(), r.Scheme); err != nil {
		rc.log.Error(err, "could not set controller reference on role", "roleBinding", roleBinding.Name)
	}
	if err := controllerutil.SetControllerReference(rc.plan, account.GetObjectMeta(), r.Scheme); err != nil {
		rc.log.Error(err, "could not set controller reference on service account", "sa", account.Name)
	}
	return roleBinding, account
}
