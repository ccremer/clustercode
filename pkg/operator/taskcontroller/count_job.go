package taskcontroller

import (
	"fmt"
	"path/filepath"

	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	"github.com/ccremer/clustercode/pkg/internal/utils"
	"github.com/ccremer/clustercode/pkg/operator/blueprintcontroller"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/utils/pointer"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

func (r *TaskReconciler) ensureCountJob(ctx *TaskContext) error {
	taskId := ctx.task.Spec.TaskId
	intermediateMountRoot := filepath.Join("/clustercode", internaltypes.IntermediateSubMountPath)
	job := &batchv1.Job{
		ObjectMeta: metav1.ObjectMeta{
			Name:      fmt.Sprintf("%.*s-%s", 62-len(internaltypes.JobTypeCount), taskId, internaltypes.JobTypeCount),
			Namespace: ctx.task.Namespace,
		},
	}
	_, err := controllerutil.CreateOrUpdate(ctx, r.Client, job, func() error {
		job.Labels = labels.Merge(job.Labels, labels.Merge(internaltypes.ClusterCodeLabels, labels.Merge(internaltypes.JobTypeCount.AsLabels(), taskId.AsLabels())))
		job.Spec.BackoffLimit = pointer.Int32Ptr(0)
		job.Spec.Template.Spec.ServiceAccountName = ctx.task.Spec.ServiceAccountName
		job.Spec.Template.Spec.RestartPolicy = corev1.RestartPolicyNever
		if len(job.Spec.Template.Spec.Containers) == 0 {
			job.Spec.Template.Spec.Containers = []corev1.Container{
				{
					Name:            "clustercode",
					Image:           blueprintcontroller.DefaultClusterCodeContainerImage,
					ImagePullPolicy: corev1.PullIfNotPresent,
					Args: []string{
						"--log-level=1",
						"count",
						"--task-name=" + ctx.task.Name,
						"--namespace=" + ctx.task.Namespace,
					},
				},
			}
			utils.EnsurePVCVolume(job, internaltypes.IntermediateSubMountPath, intermediateMountRoot, ctx.task.Spec.Storage.IntermediatePvc)
		}
		return controllerutil.SetControllerReference(ctx.task, job, r.Client.Scheme())
	})
	return err
}
