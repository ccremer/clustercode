package controllers

import (
	"context"
	"fmt"
	"path/filepath"
	"strconv"
	"time"

	"github.com/go-logr/logr"
	apierrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
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
	TaskOpts struct {
		args              []string
		jobType           ClusterCodeJobType
		mountSource       bool
		mountIntermediate bool
		mountTarget       bool
		mountConfig       bool
	}
)

func (r *ClustercodeTaskReconciler) SetupWithManager(mgr ctrl.Manager) error {
	pred, err := predicate.LabelSelectorPredicate(metav1.LabelSelector{MatchLabels: ClusterCodeLabels})
	if err != nil {
		return err
	}
	return ctrl.NewControllerManagedBy(mgr).
		For(&v1alpha1.ClustercodeTask{}, builder.WithPredicates(pred)).
		//Owns(&batchv1.Job{}).
		//WithEventFilter(predicate.GenerationChangedPredicate{}).
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
	rc.log = r.Log.WithValues("task", req.NamespacedName).WithValues("meta", rc.task.GetObjectMeta())

	if err := r.handleTask(rc); err != nil {
		rc.log.Error(err, "could not reconcile task")
		return ctrl.Result{}, err
	}
	rc.log.Info("reconciled task")
	return ctrl.Result{}, nil
}

func (r *ClustercodeTaskReconciler) handleTask(rc *ClustercodeTaskContext) error {
	if rc.task.Spec.SlicesPlannedCount == 0 {
		return r.createSplitJob(rc)
	}

	if len(rc.task.Status.SlicesFinished) >= rc.task.Spec.SlicesPlannedCount {
		rc.log.Info("no more slices to schedule")
		return r.createMergeJob(rc)
	}
	// Todo: Check condition whether more jobs are needed
	nextSliceIndex := r.determineNextSliceIndex(rc)
	if nextSliceIndex < 0 {
		return nil
	} else {
		rc.log.Info("scheduling next slice", "index", nextSliceIndex)
		return r.createSliceJob(rc, nextSliceIndex)
	}
}

func (r *ClustercodeTaskReconciler) determineNextSliceIndex(rc *ClustercodeTaskContext) int {
	status := rc.task.Status
	if rc.task.Spec.ConcurrencyStrategy.ConcurrentCountStrategy != nil {
		maxCount := rc.task.Spec.ConcurrencyStrategy.ConcurrentCountStrategy.MaxCount
		if len(status.SlicesScheduled) >= maxCount {
			rc.log.V(1).Info("reached concurrent max count, cannot schedule more", "max", maxCount)
			return -1
		}
	}
	for i := 0; i < rc.task.Spec.SlicesPlannedCount; i++ {
		if containsSliceIndex(status.SlicesScheduled, i) {
			continue
		}
		if containsSliceIndex(status.SlicesFinished, i) {
			continue
		}
		return i
	}
	return -1
}

func containsSliceIndex(list []v1alpha1.ClustercodeSliceRef, index int) bool {
	for _, t := range list {
		if t.SliceIndex == index {
			return true
		}
	}
	return false
}

func (r *ClustercodeTaskReconciler) createSplitJob(rc *ClustercodeTaskContext) error {
	sourceMountRoot := filepath.Join("/clustercode", SourceSubMountPath)
	intermediateMountRoot := filepath.Join("/clustercode", IntermediateSubMountPath)
	variables := map[string]string{
		"${INPUT}":      filepath.Join(sourceMountRoot, rc.task.Spec.SourceUrl.GetPath()),
		"${OUTPUT}":     getSegmentFileNameTemplatePath(rc, intermediateMountRoot),
		"${SLICE_SIZE}": strconv.Itoa(rc.task.Spec.EncodeSpec.SliceSize),
	}
	job := createFfmpegJobDefinition(rc.task, &TaskOpts{
		args:              mergeArgsAndReplaceVariables(variables, rc.task.Spec.EncodeSpec.DefaultCommandArgs, rc.task.Spec.EncodeSpec.SplitCommandArgs),
		jobType:           ClustercodeTypeSplit,
		mountSource:       true,
		mountIntermediate: true,
	})
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

func (r *ClustercodeTaskReconciler) createSliceJob(rc *ClustercodeTaskContext, index int) error {
	intermediateMountRoot := filepath.Join("/clustercode", IntermediateSubMountPath)
	variables := map[string]string{
		"${INPUT}":  getSourceSegmentFileNameIndexPath(rc, intermediateMountRoot, index),
		"${OUTPUT}": getTargetSegmentFileNameIndexPath(rc, intermediateMountRoot, index),
	}
	job := createFfmpegJobDefinition(rc.task, &TaskOpts{
		args:              mergeArgsAndReplaceVariables(variables, rc.task.Spec.EncodeSpec.DefaultCommandArgs, rc.task.Spec.EncodeSpec.TranscodeCommandArgs),
		jobType:           ClustercodeTypeSlice,
		mountIntermediate: true,
	})
	job.Name = fmt.Sprintf("%s-%d", job.Name, index)
	job.Labels[ClustercodeSliceIndexLabelKey] = strconv.Itoa(index)
	if err := controllerutil.SetControllerReference(rc.task, job.GetObjectMeta(), r.Scheme); err != nil {
		return fmt.Errorf("could not set controller reference: %w", err)
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
	rc.task.Status.SlicesScheduled = append(rc.task.Status.SlicesScheduled, v1alpha1.ClustercodeSliceRef{
		JobName:    job.Name,
		SliceIndex: index,
	})
	return r.Client.Status().Update(rc.ctx, rc.task)
}

func (r *ClustercodeTaskReconciler) createMergeJob(rc *ClustercodeTaskContext) error {
	configMountRoot := filepath.Join("/clustercode", ConfigSubMountPath)
	targetMountRoot := filepath.Join("/clustercode", TargetSubMountPath)
	variables := map[string]string{
		"${INPUT}":  filepath.Join(configMountRoot, v1alpha1.ConfigMapFileName),
		"${OUTPUT}": filepath.Join(targetMountRoot, rc.task.Spec.TargetUrl.GetPath()),
	}
	job := createFfmpegJobDefinition(rc.task, &TaskOpts{
		args:              mergeArgsAndReplaceVariables(variables, rc.task.Spec.EncodeSpec.DefaultCommandArgs, rc.task.Spec.EncodeSpec.MergeCommandArgs),
		jobType:           ClustercodeTypeMerge,
		mountIntermediate: true,
		mountTarget:       true,
		mountConfig:       true,
	})
	if err := controllerutil.SetControllerReference(rc.task, job.GetObjectMeta(), r.Scheme); err != nil {
		return fmt.Errorf("could not set controller reference: %w", err)
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

func getSegmentFileNameTemplatePath(rc *ClustercodeTaskContext, intermediateMountRoot string) string {
	return filepath.Join(intermediateMountRoot, rc.task.Name+"_%d"+filepath.Ext(rc.task.Spec.SourceUrl.GetPath()))
}

func getSourceSegmentFileNameIndexPath(rc *ClustercodeTaskContext, intermediateMountRoot string, index int) string {
	return filepath.Join(intermediateMountRoot, fmt.Sprintf("%s_%d%s", rc.task.Name, index, filepath.Ext(rc.task.Spec.SourceUrl.GetPath())))
}

func getTargetSegmentFileNameIndexPath(rc *ClustercodeTaskContext, intermediateMountRoot string, index int) string {
	return filepath.Join(intermediateMountRoot, fmt.Sprintf("%s_%d%s%s", rc.task.Name, index, v1alpha1.MediaFileDoneSuffix, filepath.Ext(rc.task.Spec.TargetUrl.GetPath())))
}
