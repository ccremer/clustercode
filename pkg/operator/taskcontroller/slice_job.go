package taskcontroller

import (
	"fmt"
	"path/filepath"
	"strconv"

	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	"github.com/ccremer/clustercode/pkg/internal/utils"
	batchv1 "k8s.io/api/batch/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

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
