package jobcontroller

import (
	"fmt"
	"path/filepath"
	"strconv"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	"github.com/ccremer/clustercode/pkg/operator/blueprintcontroller"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/utils/pointer"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

func (r *JobProvisioner) ensureCountJob(ctx *JobContext) error {
	ctx.resolver.MustRequireDependencyByFuncName(r.fetchTask)

	taskId := ctx.task.Spec.TaskId
	intermediateMountRoot := filepath.Join("/clustercode", internaltypes.IntermediateSubMountPath)
	job := &batchv1.Job{
		ObjectMeta: metav1.ObjectMeta{
			Name:      fmt.Sprintf("%.*s-%s", 62-len(internaltypes.JobTypeCount), taskId, internaltypes.JobTypeCount),
			Namespace: ctx.job.Namespace,
		},
	}
	_, err := controllerutil.CreateOrUpdate(ctx, r.Client, job, func() error {
		job.Labels = labels.Merge(job.Labels, labels.Merge(internaltypes.ClusterCodeLabels, labels.Merge(internaltypes.JobTypeCount.AsLabels(), taskId.AsLabels())))
		job.Spec = batchv1.JobSpec{
			BackoffLimit: pointer.Int32Ptr(0),
			Template: corev1.PodTemplateSpec{
				Spec: corev1.PodSpec{
					ServiceAccountName: ctx.job.Spec.Template.Spec.ServiceAccountName,
					RestartPolicy:      corev1.RestartPolicyNever,
					Containers: []corev1.Container{
						{
							Name:            "clustercode",
							Image:           blueprintcontroller.DefaultClusterCodeContainerImage,
							ImagePullPolicy: corev1.PullIfNotPresent,
							Args: []string{
								"-d",
								"count",
								"--task-name=" + ctx.task.Name,
								"--namespace=" + ctx.job.Namespace,
							},
							VolumeMounts: []corev1.VolumeMount{
								{Name: internaltypes.IntermediateSubMountPath, MountPath: intermediateMountRoot, SubPath: ctx.task.Spec.Storage.IntermediatePvc.SubPath}},
						},
					},
					Volumes: []corev1.Volume{
						{
							Name: internaltypes.IntermediateSubMountPath,
							VolumeSource: corev1.VolumeSource{
								PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
									ClaimName: ctx.task.Spec.Storage.IntermediatePvc.ClaimName}},
						},
					},
				},
			},
		}
		return controllerutil.SetControllerReference(ctx.task, job, r.Client.Scheme())
	})
	return err
}

func (r *JobProvisioner) ensureCleanupJob(ctx *JobContext) error {
	ctx.resolver.MustRequireDependencyByFuncName(r.fetchTask)

	taskId := ctx.task.Spec.TaskId
	job := &batchv1.Job{ObjectMeta: metav1.ObjectMeta{
		Name:      fmt.Sprintf("%.*s-%s", 62-len(internaltypes.JobTypeCleanup), taskId, internaltypes.JobTypeCleanup),
		Namespace: ctx.job.Namespace,
	}}

	_, err := controllerutil.CreateOrUpdate(ctx, r.Client, job, func() error {
		job.Labels = labels.Merge(job.Labels, labels.Merge(internaltypes.ClusterCodeLabels, labels.Merge(internaltypes.JobTypeCleanup.AsLabels(), taskId.AsLabels())))
		job.Spec = batchv1.JobSpec{
			BackoffLimit: pointer.Int32Ptr(0),
			Template: corev1.PodTemplateSpec{
				Spec: corev1.PodSpec{
					SecurityContext: &corev1.PodSecurityContext{
						RunAsUser:  pointer.Int64Ptr(1000),
						RunAsGroup: pointer.Int64Ptr(0),
						FSGroup:    pointer.Int64Ptr(0),
					},
					ServiceAccountName: ctx.job.Spec.Template.Spec.ServiceAccountName,
					RestartPolicy:      corev1.RestartPolicyNever,
					Containers: []corev1.Container{
						{
							Name:            "clustercode",
							Image:           blueprintcontroller.DefaultClusterCodeContainerImage,
							ImagePullPolicy: corev1.PullIfNotPresent,
							Args: []string{
								"-d",
								"--namespace=" + ctx.job.Namespace,
								"cleanup",
								"--task-name=" + ctx.task.Name,
							},
						},
					},
				},
			},
		}
		job.Spec.Template.Spec.Containers[0].VolumeMounts = []corev1.VolumeMount{
			{Name: internaltypes.SourceSubMountPath, MountPath: filepath.Join("/clustercode"), SubPath: internaltypes.SourceSubMountPath},
			{Name: internaltypes.IntermediateSubMountPath, MountPath: filepath.Join("/clustercode"), SubPath: internaltypes.IntermediateSubMountPath},
		}
		job.Spec.Template.Spec.Volumes = []corev1.Volume{
			{
				Name: internaltypes.SourceSubMountPath,
				VolumeSource: corev1.VolumeSource{
					PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
						ClaimName: ctx.task.Spec.Storage.SourcePvc.ClaimName}}},
			{
				Name: internaltypes.IntermediateSubMountPath,
				VolumeSource: corev1.VolumeSource{
					PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
						ClaimName: ctx.task.Spec.Storage.IntermediatePvc.ClaimName}}},
		}
		return controllerutil.SetOwnerReference(ctx.task, job, r.Client.Scheme())
	})
	return err
}

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

func (r *JobProvisioner) updateStatus(ctx *JobContext) error {
	ctx.resolver.MustRequireDependencyByFuncName(r.fetchTask, r.determineSliceIndex)

	finishedList := ctx.task.Status.SlicesFinished
	finishedList = append(finishedList, v1alpha1.ClustercodeSliceRef{
		SliceIndex: ctx.sliceIndex,
		JobName:    ctx.job.Name,
	})
	ctx.task.Status.SlicesFinished = finishedList
	ctx.task.Status.SlicesFinishedCount = len(finishedList)

	var scheduled []v1alpha1.ClustercodeSliceRef
	for _, ref := range ctx.task.Status.SlicesScheduled {
		if ref.SliceIndex != ctx.sliceIndex {
			scheduled = append(scheduled, ref)
		}
	}
	ctx.task.Status.SlicesScheduled = scheduled
	return r.Client.Status().Update(ctx, ctx.task)
}
