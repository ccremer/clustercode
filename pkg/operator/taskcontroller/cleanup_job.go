package taskcontroller

import (
	"fmt"

	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	"github.com/ccremer/clustercode/pkg/operator/blueprintcontroller"
	batchv1 "k8s.io/api/batch/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

func (r *TaskReconciler) ensureCleanupJob(ctx *TaskContext) error {
	taskId := ctx.task.Spec.TaskId
	job := &batchv1.Job{ObjectMeta: metav1.ObjectMeta{
		Name:      fmt.Sprintf("%.*s-%s", 62-len(internaltypes.JobTypeCleanup), taskId, internaltypes.JobTypeCleanup),
		Namespace: ctx.task.Namespace,
	}}

	_, err := controllerutil.CreateOrUpdate(ctx, r.Client, job, func() error {
		createClustercodeJobDefinition(job, ctx.task, TaskOpts{
			template: ctx.task.Spec.Cleanup.PodTemplate,
			jobType:  internaltypes.JobTypeCleanup,
			image:    blueprintcontroller.DefaultClusterCodeContainerImage,
			args: []string{
				"--log-level=1",
				"cleanup",
				"--task-name=" + ctx.task.Name,
				"--namespace=" + ctx.task.Namespace,
			},
			mountSource:       true,
			mountIntermediate: true,
		})
		return controllerutil.SetControllerReference(ctx.task, job, r.Client.Scheme())
	})
	return err
}
