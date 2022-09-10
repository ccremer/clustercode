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

func (r *TaskReconciler) createSplitJob(ctx *TaskContext) error {
	sourceMountRoot := filepath.Join("/clustercode", internaltypes.SourceSubMountPath)
	intermediateMountRoot := filepath.Join("/clustercode", internaltypes.IntermediateSubMountPath)
	variables := map[string]string{
		"${INPUT}":      filepath.Join(sourceMountRoot, ctx.task.Spec.SourceUrl.GetPath()),
		"${OUTPUT}":     getSegmentFileNameTemplatePath(ctx, intermediateMountRoot),
		"${SLICE_SIZE}": strconv.Itoa(ctx.task.Spec.Encode.SliceSize),
	}
	job := &batchv1.Job{ObjectMeta: metav1.ObjectMeta{
		Name:      fmt.Sprintf("%s-%s", ctx.task.Spec.TaskId, internaltypes.JobTypeSplit),
		Namespace: ctx.task.Namespace,
	}}

	_, err := controllerutil.CreateOrUpdate(ctx, r.Client, job, func() error {
		createClustercodeJobDefinition(job, ctx.task, TaskOpts{
			template:          ctx.task.Spec.Encode.PodTemplate,
			image:             DefaultFfmpegContainerImage,
			args:              utils.MergeArgsAndReplaceVariables(variables, ctx.task.Spec.Encode.SplitCommandArgs),
			jobType:           internaltypes.JobTypeSplit,
			mountSource:       true,
			mountIntermediate: true,
		})
		return controllerutil.SetControllerReference(ctx.task, job, r.Client.Scheme())
	})
	return err
}
