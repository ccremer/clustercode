package controllers

import (
	"context"
	"path/filepath"
	"strconv"
	"time"

	"github.com/go-logr/logr"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	apierrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/utils/pointer"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/builder"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
	"sigs.k8s.io/controller-runtime/pkg/predicate"

	"github.com/ccremer/clustercode/api/v1alpha1"
)

type (
	// ClustercodeTaskReconciler reconciles ClustercodeTask objects
	ClustercodeTaskReconciler struct {
		Client client.Client
		Log    logr.Logger
		Scheme *runtime.Scheme
	}
	// ClustercodeTaskContext holds the parameters of a single reconciliation
	ClustercodeTaskContext struct {
		ctx  context.Context
		task *v1alpha1.ClustercodeTask
		plan *v1alpha1.ClustercodePlan
		log  logr.Logger
	}
)

func (r *ClustercodeTaskReconciler) SetupWithManager(mgr ctrl.Manager) error {
	pred, err := predicate.LabelSelectorPredicate(metav1.LabelSelector{MatchLabels: ClusterCodeLabels})
	if err != nil {
		return err
	}
	return ctrl.NewControllerManagedBy(mgr).
		For(&v1alpha1.ClustercodeTask{}, builder.WithPredicates(pred)).
		WithEventFilter(predicate.GenerationChangedPredicate{}).
		Complete(r)
}

// +kubebuilder:rbac:groups=clustercode.github.io,resources=clustercodetasks,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=clustercode.github.io,resources=clustercodetasks/status,verbs=get;update;patch

func (r *ClustercodeTaskReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	rc := &ClustercodeTaskContext{
		ctx:  ctx,
		task: &v1alpha1.ClustercodeTask{},
	}
	err := r.Client.Get(ctx, req.NamespacedName, rc.task)
	if err != nil {
		if apierrors.IsNotFound(err) {
			r.Log.Info("object not found, ignoring reconcile", "object", req.NamespacedName)
			return ctrl.Result{}, nil
		}
		r.Log.Error(err, "could not retrieve object", "object", req.NamespacedName)
		return ctrl.Result{Requeue: true, RequeueAfter: time.Minute}, err
	}
	rc.log = r.Log.WithValues("task", req.NamespacedName)

	if err := r.handleTask(rc); err != nil {
		return ctrl.Result{}, err
	}
	rc.log.Info("reconciled task")
	return ctrl.Result{}, nil
}

func (r *ClustercodeTaskReconciler) handleTask(rc *ClustercodeTaskContext) error {
	if rc.task.Status.SlicesPlanned == 0 {
		return r.createSplitJob(rc)
	}

	return nil
}

func (r *ClustercodeTaskReconciler) getOwner(rc *ClustercodeTaskContext) types.NamespacedName {
	for _, owner := range rc.task.GetOwnerReferences() {
		if pointer.BoolPtrDerefOr(owner.Controller, false) {
			return types.NamespacedName{Namespace: rc.task.Namespace, Name: owner.Name}
		}
	}
	return types.NamespacedName{}
}

func (r *ClustercodeTaskReconciler) createSplitJob(rc *ClustercodeTaskContext) error {

	rc.plan = &v1alpha1.ClustercodePlan{}
	if err := r.Client.Get(rc.ctx, r.getOwner(rc), rc.plan); err != nil {
		return err
	}
	sourceMountRoot := filepath.Join("/clustercode)", SourceSubMountPath)
	intermediateMountRoot := filepath.Join("/clustercode)", IntermediateSubMountPath)
	variables := map[string]string{
		"${INPUT}":      filepath.Join(sourceMountRoot, rc.task.Spec.SourceUrl.GetPath()),
		"${OUTPUT}":     getSegmentFileNameTemplate(rc, intermediateMountRoot),
		"${SLICE_SIZE}": strconv.Itoa(rc.task.Spec.EncodeSpec.SliceSize),
	}
	job := &batchv1.Job{
		ObjectMeta: metav1.ObjectMeta{
			Name:      rc.task.Name + "-split",
			Namespace: rc.task.Namespace,
			Labels:    mergeLabels(ClusterCodeLabels, ClusterCodeSplitLabels),
		},
		Spec: batchv1.JobSpec{
			BackoffLimit: pointer.Int32Ptr(0),
			Template: corev1.PodTemplateSpec{
				Spec: corev1.PodSpec{
					ServiceAccountName: rc.plan.GetServiceAccountName(),
					RestartPolicy:      corev1.RestartPolicyNever,
					Containers: []corev1.Container{
						{
							Name:            "ffmpeg",
							Image:           "docker.io/jrottenberg/ffmpeg:4.1-alpine",
							ImagePullPolicy: corev1.PullIfNotPresent,
							Args:            mergeArgsAndReplaceVariables(variables, rc.task.Spec.EncodeSpec.DefaultCommandArgs, rc.task.Spec.EncodeSpec.SplitCommandArgs),
							VolumeMounts: []corev1.VolumeMount{
								{Name: SourceSubMountPath, MountPath: sourceMountRoot, SubPath: rc.plan.Spec.Storage.SourcePvc.SubPath},
								{Name: IntermediateSubMountPath, MountPath: intermediateMountRoot, SubPath: rc.plan.Spec.Storage.IntermediatePvc.SubPath},
							},
						},
					},
					Volumes: []corev1.Volume{
						{
							Name: SourceSubMountPath,
							VolumeSource: corev1.VolumeSource{
								PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
									ClaimName: rc.plan.Spec.Storage.SourcePvc.ClaimName,
								},
							},
						},
						{
							Name: IntermediateSubMountPath,
							VolumeSource: corev1.VolumeSource{
								PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
									ClaimName: rc.plan.Spec.Storage.IntermediatePvc.ClaimName,
								},
							},
						},
					},
				},
			},
		},
	}
	if err := controllerutil.SetControllerReference(rc.task, job.GetObjectMeta(), r.Scheme); err != nil {
		rc.log.Info("could not set controller reference, deleting the task won't delete the job", "err", err.Error())
	}
	if err := r.Client.Create(rc.ctx, job); err != nil {
		if apierrors.IsAlreadyExists(err) {
			rc.log.Info("skip creating job, it already exists", "job", job.Name)
		} else {
			rc.log.Error(err, "could not create job", "job", job.Name)
		}
	} else {
		rc.log.Info("job created", "job", job.Name)
	}
	return nil
}

func getSegmentFileNameTemplate(rc *ClustercodeTaskContext, intermediateMountRoot string) string {
	return filepath.Join(intermediateMountRoot, rc.task.Name+"_%d"+filepath.Ext(rc.task.Spec.SourceUrl.GetPath()))
}
