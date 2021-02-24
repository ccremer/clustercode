package task

import (
	"context"
	"fmt"
	"path/filepath"
	"strconv"
	"time"

	"github.com/go-logr/logr"
	apierrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/tools/record"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/builder"
	"sigs.k8s.io/controller-runtime/pkg/predicate"

	"github.com/ccremer/clustercode/api/v1alpha1"
	"github.com/ccremer/clustercode/controllers"
	"github.com/ccremer/clustercode/controllers/pipeline"
)

type (
	// Reconciler reconciles Task objects
	Reconciler struct {
		Recorder record.EventRecorder
		*pipeline.ResourceAction
	}
	// ReconciliationContext holds the parameters of a single reconciliation
	ReconciliationContext struct {
		ctx            context.Context
		task           *v1alpha1.Task
		blueprint      *v1alpha1.Blueprint
		Log            logr.Logger
		nextSliceIndex int
	}
	TaskOpts struct {
		Args              []string
		JobType           controllers.ClusterCodeJobType
		MountSource       bool
		MountIntermediate bool
		MountTarget       bool
		MountConfig       bool
	}
)

func (r *Reconciler) SetupWithManager(mgr ctrl.Manager, l logr.Logger) error {
	r.ResourceAction = &pipeline.ResourceAction{
		Log:    l,
		Client: mgr.GetClient(),
		Scheme: mgr.GetScheme(),
	}
	r.Recorder = mgr.GetEventRecorderFor("task-controller")
	pred, err := predicate.LabelSelectorPredicate(metav1.LabelSelector{MatchLabels: controllers.ClusterCodeLabels})
	if err != nil {
		return err
	}
	return ctrl.NewControllerManagedBy(mgr).
		For(&v1alpha1.Task{}, builder.WithPredicates(pred)).
		//Owns(&batchv1.Job{}).
		WithEventFilter(predicate.GenerationChangedPredicate{}).
		Complete(r)
}

// +kubebuilder:rbac:groups=clustercode.github.io,resources=tasks,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=clustercode.github.io,resources=tasks/status,verbs=get;update;patch

func (r *Reconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	rc := &ReconciliationContext{
		ctx: ctx,
		task: &v1alpha1.Task{
			ObjectMeta: metav1.ObjectMeta{
				Name:      req.Name,
				Namespace: req.Namespace,
			},
		},
		Log: r.Log.WithValues("task", req.NamespacedName.String()),
	}

	splitJobPipeline := pipeline.NewPipeline(rc.Log).
		WithSteps(
			pipeline.NewStep("create split job", r.createSplitJob(rc)),
		)

	mergeJobPipeline := pipeline.NewPipeline(rc.Log).
		WithSteps(
			pipeline.NewStep("create merge job", r.createMergeJob(rc)),
		)
	sliceJobPipeline := pipeline.NewPipeline(rc.Log).
		WithSteps(
			pipeline.NewStep("create slice job", r.createSliceJob(rc)),
		)

	result := pipeline.NewPipeline(rc.Log).
		WithSteps(
			pipeline.NewStep("get reconcile object", r.GetOrAbort(ctx, rc.task)),
			splitJobPipeline.AsNestedStep("split-job", splitJobPredicate(rc)),
			mergeJobPipeline.AsNestedStep("merge-job", mergeJobPredicate(rc)),
			sliceJobPipeline.AsNestedStep("slice-job", sliceJobPredicate(rc)),
			pipeline.NewStep("update status", r.UpdateStatus(rc.ctx, rc.task)),
		).Run()
	if result.Err != nil {
		r.Log.Error(result.Err, "pipeline failed with error")
	}

	if result.Requeue {
		return ctrl.Result{RequeueAfter: 1 * time.Minute}, result.Err
	}
	rc.Log.Info("reconciled task")
	return ctrl.Result{}, result.Err
}

func sliceJobPredicate(rc *ReconciliationContext) pipeline.Predicate {
	return func(step pipeline.Step) bool {
		status := rc.task.Status
		if rc.task.Spec.ConcurrencyStrategy.ConcurrentCountStrategy != nil {
			maxCount := rc.task.Spec.ConcurrencyStrategy.ConcurrentCountStrategy.MaxCount
			if len(status.SlicesScheduled) >= maxCount {
				rc.Log.V(1).Info("reached concurrent max count, cannot schedule more", "max", maxCount)
				rc.nextSliceIndex = -1
				return false
			}
		}
		for i := 0; i < rc.task.Spec.SlicesPlannedCount; i++ {
			if containsSliceIndex(status.SlicesScheduled, i) || containsSliceIndex(status.SlicesFinished, i) {
				continue
			}
			rc.nextSliceIndex = i
			return true
		}
		rc.nextSliceIndex = -1
		return false
	}
}

func containsSliceIndex(list []v1alpha1.ClustercodeSliceRef, index int) bool {
	for _, t := range list {
		if t.SliceIndex == index {
			return true
		}
	}
	return false
}

func (r *Reconciler) createSplitJob(rc *ReconciliationContext) pipeline.ActionFunc {
	return func() pipeline.Result {
		sourceMountRoot := filepath.Join("/clustercode", controllers.SourceSubMountPath)
		intermediateMountRoot := filepath.Join("/clustercode", controllers.IntermediateSubMountPath)
		variables := map[string]string{
			"${INPUT}":      filepath.Join(sourceMountRoot, rc.task.Spec.SourceUrl.GetPath()),
			"${OUTPUT}":     getSegmentFileNameTemplatePath(rc, intermediateMountRoot),
			"${SLICE_SIZE}": strconv.Itoa(rc.task.Spec.EncodeSpec.SliceSize),
		}
		job := r.CreateFfmpegJobDefinition(rc.task, &TaskOpts{
			Args:              MergeArgsAndReplaceVariables(variables, rc.task.Spec.EncodeSpec.DefaultCommandArgs, rc.task.Spec.EncodeSpec.SplitCommandArgs),
			JobType:           controllers.ClustercodeTypeSplit,
			MountSource:       true,
			MountIntermediate: true,
		})
		if err := r.Client.Create(rc.ctx, job); err != nil {
			if apierrors.IsAlreadyExists(err) {
				rc.Log.Info("skip creating job, it already exists", "job", job.Name)
			} else {
				rc.Log.Error(err, "could not create job", "job", job.Name)
			}
		} else {
			rc.Log.Info("job created", "job", job.Name)
		}
		return pipeline.Result{}
	}
}

func (r *Reconciler) createSliceJob(rc *ReconciliationContext) pipeline.ActionFunc {
	return func() pipeline.Result {
		index := rc.nextSliceIndex
		rc.Log.Info("scheduling next slice", "index", index)
		intermediateMountRoot := filepath.Join("/clustercode", controllers.IntermediateSubMountPath)
		variables := map[string]string{
			"${INPUT}":  getSourceSegmentFileNameIndexPath(rc, intermediateMountRoot, index),
			"${OUTPUT}": getTargetSegmentFileNameIndexPath(rc, intermediateMountRoot, index),
		}
		job := r.CreateFfmpegJobDefinition(rc.task, &TaskOpts{
			Args:              MergeArgsAndReplaceVariables(variables, rc.task.Spec.EncodeSpec.DefaultCommandArgs, rc.task.Spec.EncodeSpec.TranscodeCommandArgs),
			JobType:           controllers.ClustercodeTypeSlice,
			MountIntermediate: true,
		})
		job.Name = fmt.Sprintf("%s-%d", job.Name, index)
		job.Labels[controllers.ClustercodeSliceIndexLabelKey] = strconv.Itoa(index)
		if err := r.Client.Create(rc.ctx, job); err != nil {
			if apierrors.IsAlreadyExists(err) {
				rc.Log.Info("skip creating job, it already exists", "job", job.Name)
			} else {
				rc.Log.Error(err, "could not create job", "job", job.Name)
			}
		} else {
			rc.Log.Info("job created", "job", job.Name)
		}
		rc.task.Status.SlicesScheduled = append(rc.task.Status.SlicesScheduled, v1alpha1.ClustercodeSliceRef{
			JobName:    job.Name,
			SliceIndex: index,
		})
		return pipeline.Result{}
	}
}

func (r *Reconciler) createMergeJob(rc *ReconciliationContext) pipeline.ActionFunc {
	return func() pipeline.Result {
		configMountRoot := filepath.Join("/clustercode", controllers.ConfigSubMountPath)
		targetMountRoot := filepath.Join("/clustercode", controllers.TargetSubMountPath)
		variables := map[string]string{
			"${INPUT}":  filepath.Join(configMountRoot, v1alpha1.ConfigMapFileName),
			"${OUTPUT}": filepath.Join(targetMountRoot, rc.task.Spec.TargetUrl.GetPath()),
		}
		job := r.CreateFfmpegJobDefinition(rc.task, &TaskOpts{
			Args:              MergeArgsAndReplaceVariables(variables, rc.task.Spec.EncodeSpec.DefaultCommandArgs, rc.task.Spec.EncodeSpec.MergeCommandArgs),
			JobType:           controllers.ClustercodeTypeMerge,
			MountIntermediate: true,
			MountTarget:       true,
			MountConfig:       true,
		})
		if err := r.Client.Create(rc.ctx, job); err != nil {
			if apierrors.IsAlreadyExists(err) {
				rc.Log.Info("skip creating job, it already exists", "job", job.Name)
			} else {
				return pipeline.Result{Err: fmt.Errorf("could not create job: %w", err)}
			}
		} else {
			rc.Log.Info("job created", "job", job.Name)
		}
		return pipeline.Result{}
	}
}

func getSegmentFileNameTemplatePath(rc *ReconciliationContext, intermediateMountRoot string) string {
	return filepath.Join(intermediateMountRoot, rc.task.Name+"_%d"+filepath.Ext(rc.task.Spec.SourceUrl.GetPath()))
}

func getSourceSegmentFileNameIndexPath(rc *ReconciliationContext, intermediateMountRoot string, index int) string {
	return filepath.Join(intermediateMountRoot, fmt.Sprintf("%s_%d%s", rc.task.Name, index, filepath.Ext(rc.task.Spec.SourceUrl.GetPath())))
}

func getTargetSegmentFileNameIndexPath(rc *ReconciliationContext, intermediateMountRoot string, index int) string {
	return filepath.Join(intermediateMountRoot, fmt.Sprintf("%s_%d%s%s", rc.task.Name, index, v1alpha1.MediaFileDoneSuffix, filepath.Ext(rc.task.Spec.TargetUrl.GetPath())))
}
