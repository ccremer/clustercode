package jobcontroller

import (
	"fmt"
	"strconv"

	"github.com/ccremer/clustercode/pkg/api/conditions"
	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	"k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func (r *JobProvisioner) determineSliceIndex(ctx *JobContext) error {
	indexStr, found := ctx.job.Labels[internaltypes.ClustercodeSliceIndexLabelKey]
	if !found {
		return fmt.Errorf("cannot determine slice index, missing label '%s'", internaltypes.ClustercodeSliceIndexLabelKey)
	}
	index, err := strconv.Atoi(indexStr)
	if err != nil {
		return fmt.Errorf("cannot determine slice index from label '%s': %w", internaltypes.ClustercodeSliceIndexLabelKey, err)
	}
	ctx.sliceIndex = index
	return err
}

func (r *JobProvisioner) updateStatusWithSlicesFinished(ctx *JobContext) error {
	ctx.resolver.MustRequireDependencyByFuncName(r.fetchTask, r.determineSliceIndex)

	finishedList := ctx.task.Status.SlicesFinished
	finishedList = append(finishedList, v1alpha1.ClustercodeSliceRef{
		SliceIndex: ctx.sliceIndex,
		JobName:    ctx.job.Name,
	})
	ctx.task.Status.SlicesFinished = finishedList
	ctx.task.Status.SlicesFinishedCount = len(finishedList)

	scheduled := make([]v1alpha1.ClustercodeSliceRef, 0)
	for _, ref := range ctx.task.Status.SlicesScheduled {
		if ref.SliceIndex != ctx.sliceIndex {
			scheduled = append(scheduled, ref)
		}
	}
	ctx.task.Status.SlicesScheduled = scheduled
	if len(ctx.task.Status.SlicesFinished) >= ctx.task.Spec.SlicesPlannedCount {
		meta.SetStatusCondition(&ctx.task.Status.Conditions, conditions.ProgressingSuccessful())
	}
	return r.Client.Status().Update(ctx, ctx.task)
}

func (r *JobProvisioner) updateStatusWithCondition(condition metav1.Condition) func(ctx *JobContext) error {
	return func(ctx *JobContext) error {
		ctx.resolver.MustRequireDependencyByFuncName(r.fetchTask)
		meta.SetStatusCondition(&ctx.task.Status.Conditions, condition)
		return r.Client.Status().Update(ctx, ctx.task)
	}
}

func (r *JobProvisioner) updateStatusWithCountComplete(ctx *JobContext) error {
	ctx.resolver.MustRequireDependencyByFuncName(r.fetchTask)
	meta.SetStatusCondition(&ctx.task.Status.Conditions, conditions.CountComplete(ctx.task.Spec.SlicesPlannedCount))
	return r.Client.Status().Update(ctx, ctx.task)
}
