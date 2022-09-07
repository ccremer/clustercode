package taskcontroller

import (
	"context"
	"fmt"
	"path/filepath"

	"github.com/ccremer/clustercode/pkg/api/conditions"
	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"github.com/ccremer/clustercode/pkg/internal/pipe"
	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	pipeline "github.com/ccremer/go-command-pipeline"
	"github.com/go-logr/logr"
	batchv1 "k8s.io/api/batch/v1"
	"k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
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

	p := pipeline.NewPipeline[*TaskContext]().WithBeforeHooks(pipe.DebugLogger[*TaskContext](r.Log), tc.resolver.Record)
	p.WithSteps(
		p.When(r.hasNoSlicesPlanned, "create split job", r.createSplitJob),
		p.When(r.hasCondition(conditions.SplitComplete()), "ensure count job", r.ensureCountJob),
		p.When(r.allSlicesFinished, "create merge job", r.createMergeJob),
		p.WithNestedSteps("schedule next slice job", r.partialSlicesFinished,
			p.NewStep("determine next slice index", r.determineNextSliceIndex),
			p.When(r.nextSliceDetermined, "create slice job", r.createSliceJob),
			p.When(r.nextSliceDetermined, "update status", r.updateStatusWithProgressing),
		),
		p.When(r.hasCondition(conditions.MergeComplete()), "create cleanup job", r.ensureCleanupJob),
		p.When(r.hasCondition(conditions.Ready()), "cleanup jobs", r.cleanupJobs),
	).WithFinalizer(r.setFailureCondition)
	err := p.RunWithContext(tc)
	return reconcile.Result{}, err
}

func (r *TaskReconciler) Deprovision(_ context.Context, _ *v1alpha1.Task) (reconcile.Result, error) {
	return reconcile.Result{}, nil
}

func (r *TaskReconciler) setFailureCondition(ctx *TaskContext, err error) error {
	if updateErr := pipe.UpdateFailedCondition(ctx, r.Client, &ctx.task.Status.Conditions, ctx.task, err); updateErr != nil {
		r.Log.Error(updateErr, "could not update Failed status condition")
	}
	return err
}

func (r *TaskReconciler) hasNoSlicesPlanned(ctx *TaskContext) bool {
	return ctx.task.Spec.SlicesPlannedCount == 0
}

func (r *TaskReconciler) allSlicesFinished(ctx *TaskContext) bool {
	cond := meta.FindStatusCondition(ctx.task.Status.Conditions, conditions.Progressing().Type)
	if cond != nil && cond.Status == metav1.ConditionFalse {
		// "progressing" is false once all slices are completed.
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

func (r *TaskReconciler) hasCondition(condition metav1.Condition) func(*TaskContext) bool {
	return func(ctx *TaskContext) bool {
		return meta.IsStatusConditionTrue(ctx.task.Status.Conditions, condition.Type)
	}
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

func (r *TaskReconciler) updateStatusWithProgressing(ctx *TaskContext) error {
	ctx.resolver.MustRequireDependencyByFuncName(r.createSliceJob)

	meta.SetStatusCondition(&ctx.task.Status.Conditions, conditions.Progressing())
	ctx.task.Status.SlicesScheduled = append(ctx.task.Status.SlicesScheduled, v1alpha1.ClustercodeSliceRef{
		JobName:    ctx.job.Name,
		SliceIndex: ctx.nextSliceIndex,
	})
	return r.Client.Status().Update(ctx, ctx.task)
}

func (r *TaskReconciler) cleanupJobs(ctx *TaskContext) error {
	return r.Client.DeleteAllOf(ctx, &batchv1.Job{},
		client.MatchingLabels(ctx.task.Spec.TaskId.AsLabels()),
		client.InNamespace(ctx.task.Namespace),
		client.PropagationPolicy(metav1.DeletePropagationBackground),
	)
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
