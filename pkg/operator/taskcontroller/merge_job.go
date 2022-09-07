package taskcontroller

import (
	"fmt"
	"path/filepath"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	"github.com/ccremer/clustercode/pkg/internal/utils"
	batchv1 "k8s.io/api/batch/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

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
			args:              utils.MergeArgsAndReplaceVariables(variables, ctx.task.Spec.EncodeSpec.MergeCommandArgs),
			jobType:           internaltypes.JobTypeMerge,
			mountIntermediate: true,
			mountTarget:       true,
			mountConfig:       true,
		})
		return controllerutil.SetControllerReference(ctx.task, job, r.Client.Scheme())
	})
	return err
}
