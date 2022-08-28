package taskcontroller

import (
	"context"
	"fmt"
	"path/filepath"
	"strconv"

	"github.com/ccremer/clustercode/pkg/api/conditions"
	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"github.com/ccremer/clustercode/pkg/internal/pipe"
	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	"github.com/ccremer/clustercode/pkg/internal/utils"
	pipeline "github.com/ccremer/go-command-pipeline"
	"github.com/go-logr/logr"
	batchv1 "k8s.io/api/batch/v1"
	"k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
)

type (
	// TaskReconciler reconciles Task objects
	TaskReconciler struct {
		Client client.Client
		Log    logr.Logger
	}
	// TaskContext holds the parameters of a single reconciliation
	TaskContext struct {
		context.Context
		resolver       pipeline.DependencyResolver[*TaskContext]
		task           *v1alpha1.Task
		blueprint      *v1alpha1.Blueprint
		job            *batchv1.Job
		nextSliceIndex int
	}
	TaskOpts struct {
		args              []string
		jobType           internaltypes.ClusterCodeJobType
		mountSource       bool
		mountIntermediate bool
		mountTarget       bool
		mountConfig       bool
	}
)

func (r *TaskReconciler) NewObject() *v1alpha1.Task {
	return &v1alpha1.Task{}
}

func (r *TaskReconciler) Provision(ctx context.Context, obj *v1alpha1.Task) (reconcile.Result, error) {
	tc := &TaskContext{
		Context:  ctx,
		task:     obj,
		resolver: pipeline.NewDependencyRecorder[*TaskContext](),
	}

	p := pipeline.NewPipeline[*TaskContext]().WithBeforeHooks(pipe.DebugLogger[*TaskContext](tc), tc.resolver.Record)
	p.WithSteps(
		p.When(r.hasNoSlicesPlanned, "create split job", r.createSplitJob),
		p.When(r.isSplitComplete, "ensure count job", r.ensureCountJob),
		p.When(r.allSlicesFinished, "create merge job", r.createMergeJob),
		p.WithNestedSteps("schedule next slice job", r.partialSlicesFinished,
			p.NewStep("determine next slice index", r.determineNextSliceIndex),
			p.When(r.nextSliceDetermined, "create slice job", r.createSliceJob),
			p.When(r.nextSliceDetermined, "update status", r.updateStatus),
		),
		p.When(r.isMergeFinished, "create cleanup job", r.ensureCleanupJob),
	)
	err := p.RunWithContext(tc)
	return reconcile.Result{}, err
}

func (r *TaskReconciler) Deprovision(_ context.Context, _ *v1alpha1.Task) (reconcile.Result, error) {
	return reconcile.Result{}, nil
}

func (r *TaskReconciler) hasNoSlicesPlanned(ctx *TaskContext) bool {
	return ctx.task.Spec.SlicesPlannedCount == 0
}

func (r *TaskReconciler) allSlicesFinished(ctx *TaskContext) bool {
	cond := meta.FindStatusCondition(ctx.task.Status.Conditions, conditions.Progressing().Type)
	if cond != nil {
		return len(ctx.task.Status.SlicesFinished) >= ctx.task.Spec.SlicesPlannedCount
	}
	return false
}

func (r *TaskReconciler) partialSlicesFinished(ctx *TaskContext) bool {
	return len(ctx.task.Status.SlicesFinished) < ctx.task.Spec.SlicesPlannedCount
}

func (r *TaskReconciler) nextSliceDetermined(ctx *TaskContext) bool {
	return ctx.nextSliceIndex >= 0
}

func (r *TaskReconciler) isSplitComplete(ctx *TaskContext) bool {
	cond := meta.FindStatusCondition(ctx.task.Status.Conditions, conditions.SplitComplete().Type)
	if cond != nil {
		return cond.Status == metav1.ConditionTrue
	}
	return false
}

func (r *TaskReconciler) isMergeFinished(ctx *TaskContext) bool {
	cond := meta.FindStatusCondition(ctx.task.Status.Conditions, conditions.MergeComplete().Type)
	if cond != nil {
		return cond.Status == metav1.ConditionTrue
	}
	return false
}

func (r *TaskReconciler) determineNextSliceIndex(ctx *TaskContext) error {
	// Todo: Check condition whether more jobs are needed
	status := ctx.task.Status
	if ctx.task.Spec.ConcurrencyStrategy.ConcurrentCountStrategy != nil {
		maxCount := ctx.task.Spec.ConcurrencyStrategy.ConcurrentCountStrategy.MaxCount
		if len(status.SlicesScheduled) >= maxCount {
			r.Log.V(1).Info("reached concurrent max count, cannot schedule more", "max", maxCount)
			ctx.nextSliceIndex = -1
			return nil
		}
	}
	total := len(status.SlicesScheduled) + len(status.SlicesFinished)
	toSkipIndexes := make(map[int]bool, total)
	for i := 0; i < len(status.SlicesScheduled); i++ {
		toSkipIndexes[status.SlicesScheduled[i].SliceIndex] = true
	}
	for i := 0; i < len(status.SlicesFinished); i++ {
		toSkipIndexes[status.SlicesFinished[i].SliceIndex] = true
	}

	for i := 0; i < ctx.task.Spec.SlicesPlannedCount; i++ {
		if _, exists := toSkipIndexes[i]; exists {
			continue
		}
		ctx.nextSliceIndex = i
		return nil
	}
	ctx.nextSliceIndex = -1
	return nil
}

func (r *TaskReconciler) createSplitJob(ctx *TaskContext) error {
	sourceMountRoot := filepath.Join("/clustercode", internaltypes.SourceSubMountPath)
	intermediateMountRoot := filepath.Join("/clustercode", internaltypes.IntermediateSubMountPath)
	variables := map[string]string{
		"${INPUT}":      filepath.Join(sourceMountRoot, ctx.task.Spec.SourceUrl.GetPath()),
		"${OUTPUT}":     getSegmentFileNameTemplatePath(ctx, intermediateMountRoot),
		"${SLICE_SIZE}": strconv.Itoa(ctx.task.Spec.EncodeSpec.SliceSize),
	}
	job := &batchv1.Job{ObjectMeta: metav1.ObjectMeta{
		Name:      fmt.Sprintf("%s-%s", ctx.task.Spec.TaskId, internaltypes.JobTypeSplit),
		Namespace: ctx.task.Namespace,
	}}

	_, err := controllerutil.CreateOrUpdate(ctx, r.Client, job, func() error {
		createFfmpegJobDefinition(job, ctx.task, &TaskOpts{
			args:              utils.MergeArgsAndReplaceVariables(variables, ctx.task.Spec.EncodeSpec.DefaultCommandArgs, ctx.task.Spec.EncodeSpec.SplitCommandArgs),
			jobType:           internaltypes.JobTypeSplit,
			mountSource:       true,
			mountIntermediate: true,
		})
		return controllerutil.SetControllerReference(ctx.task, job, r.Client.Scheme())
	})
	return err
}

func (r *TaskReconciler) createSliceJob(ctx *TaskContext) error {
	ctx.resolver.MustRequireDependencyByFuncName(r.determineNextSliceIndex)

	intermediateMountRoot := filepath.Join("/clustercode", internaltypes.IntermediateSubMountPath)
	index := ctx.nextSliceIndex
	variables := map[string]string{
		"${INPUT}":  getSourceSegmentFileNameIndexPath(ctx, intermediateMountRoot, index),
		"${OUTPUT}": getTargetSegmentFileNameIndexPath(ctx, intermediateMountRoot, index),
	}
	job := &batchv1.Job{ObjectMeta: metav1.ObjectMeta{
		Name:      fmt.Sprintf("%s-%s-%d", ctx.task.Spec.TaskId, internaltypes.JobTypeSlice, index),
		Namespace: ctx.task.Namespace,
	}}
	_, err := controllerutil.CreateOrUpdate(ctx, r.Client, job, func() error {
		createFfmpegJobDefinition(job, ctx.task, &TaskOpts{
			args:              utils.MergeArgsAndReplaceVariables(variables, ctx.task.Spec.EncodeSpec.DefaultCommandArgs, ctx.task.Spec.EncodeSpec.TranscodeCommandArgs),
			jobType:           internaltypes.JobTypeSlice,
			mountIntermediate: true,
		})
		job.Labels[internaltypes.ClustercodeSliceIndexLabelKey] = strconv.Itoa(index)

		return controllerutil.SetControllerReference(ctx.task, job, r.Client.Scheme())
	})
	ctx.job = job
	return err

}

func (r *TaskReconciler) updateStatus(ctx *TaskContext) error {
	ctx.resolver.MustRequireDependencyByFuncName(r.createSliceJob)

	meta.SetStatusCondition(&ctx.task.Status.Conditions, conditions.Progressing())
	ctx.task.Status.SlicesScheduled = append(ctx.task.Status.SlicesScheduled, v1alpha1.ClustercodeSliceRef{
		JobName:    ctx.job.Name,
		SliceIndex: ctx.nextSliceIndex,
	})
	return r.Client.Status().Update(ctx, ctx.task)
}

func (r *TaskReconciler) createMergeJob(ctx *TaskContext) error {
	configMountRoot := filepath.Join("/clustercode", internaltypes.ConfigSubMountPath)
	targetMountRoot := filepath.Join("/clustercode", internaltypes.TargetSubMountPath)
	variables := map[string]string{
		"${INPUT}":  filepath.Join(configMountRoot, v1alpha1.ConfigMapFileName),
		"${OUTPUT}": filepath.Join(targetMountRoot, ctx.task.Spec.TargetUrl.GetPath()),
	}
	job := &batchv1.Job{ObjectMeta: metav1.ObjectMeta{
		Name:      fmt.Sprintf("%s-%s", ctx.task.Spec.TaskId, internaltypes.JobTypeMerge),
		Namespace: ctx.task.Namespace,
	}}

	_, err := controllerutil.CreateOrUpdate(ctx, r.Client, job, func() error {
		createFfmpegJobDefinition(job, ctx.task, &TaskOpts{
			args:              utils.MergeArgsAndReplaceVariables(variables, ctx.task.Spec.EncodeSpec.DefaultCommandArgs, ctx.task.Spec.EncodeSpec.MergeCommandArgs),
			jobType:           internaltypes.JobTypeMerge,
			mountIntermediate: true,
			mountTarget:       true,
			mountConfig:       true,
		})
		return controllerutil.SetControllerReference(ctx.task, job, r.Client.Scheme())
	})
	return err
}

func getSegmentFileNameTemplatePath(ctx *TaskContext, intermediateMountRoot string) string {
	return filepath.Join(intermediateMountRoot, ctx.task.Name+"_%d"+filepath.Ext(ctx.task.Spec.SourceUrl.GetPath()))
}

func getSourceSegmentFileNameIndexPath(ctx *TaskContext, intermediateMountRoot string, index int) string {
	return filepath.Join(intermediateMountRoot, fmt.Sprintf("%s_%d%s", ctx.task.Name, index, filepath.Ext(ctx.task.Spec.SourceUrl.GetPath())))
}

func getTargetSegmentFileNameIndexPath(ctx *TaskContext, intermediateMountRoot string, index int) string {
	return filepath.Join(intermediateMountRoot, fmt.Sprintf("%s_%d%s%s", ctx.task.Name, index, v1alpha1.MediaFileDoneSuffix, filepath.Ext(ctx.task.Spec.TargetUrl.GetPath())))
}
