package taskcontroller

import (
	"path/filepath"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	"github.com/ccremer/clustercode/pkg/internal/utils"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/utils/pointer"
)

var DefaultFfmpegContainerImage string

func createFfmpegJobDefinition(job *batchv1.Job, task *v1alpha1.Task, opts *TaskOpts) *batchv1.Job {

	job.Labels = labels.Merge(job.Labels, labels.Merge(internaltypes.ClusterCodeLabels, labels.Merge(opts.jobType.AsLabels(), task.Spec.TaskId.AsLabels())))
	job.Spec.BackoffLimit = pointer.Int32Ptr(0)
	job.Spec.Template.Spec.SecurityContext = &corev1.PodSecurityContext{
		RunAsUser:  pointer.Int64Ptr(1000),
		RunAsGroup: pointer.Int64Ptr(0),
		FSGroup:    pointer.Int64Ptr(0),
	}
	job.Spec.Template.Spec.ServiceAccountName = task.Spec.ServiceAccountName
	job.Spec.Template.Spec.RestartPolicy = corev1.RestartPolicyNever
	job.Spec.Template.Spec.Containers = []corev1.Container{{
		Name:            "ffmpeg",
		Image:           DefaultFfmpegContainerImage,
		ImagePullPolicy: corev1.PullIfNotPresent,
		Args:            opts.args,
	}}
	if opts.mountSource {
		utils.EnsurePVCVolume(job, internaltypes.SourceSubMountPath, filepath.Join("/clustercode", internaltypes.SourceSubMountPath), task.Spec.Storage.SourcePvc)
	}
	if opts.mountIntermediate {
		utils.EnsurePVCVolume(job, internaltypes.IntermediateSubMountPath, filepath.Join("/clustercode", internaltypes.IntermediateSubMountPath), task.Spec.Storage.IntermediatePvc)
	}
	if opts.mountTarget {
		utils.EnsurePVCVolume(job, internaltypes.TargetSubMountPath, filepath.Join("/clustercode", internaltypes.TargetSubMountPath), task.Spec.Storage.TargetPvc)
	}
	if opts.mountConfig {
		addConfigMapVolume(job, internaltypes.ConfigSubMountPath, filepath.Join("/clustercode", internaltypes.ConfigSubMountPath), task.Spec.FileListConfigMapRef)
	}
	return job
}

func addConfigMapVolume(job *batchv1.Job, name, podMountRoot, configMapName string) {
	found := false
	for _, container := range job.Spec.Template.Spec.Containers {
		if utils.HasVolumeMount(name, container) {
			found = true
			break
		}
	}
	if found {
		return
	}
	job.Spec.Template.Spec.Containers[0].VolumeMounts = append(job.Spec.Template.Spec.Containers[0].VolumeMounts,
		corev1.VolumeMount{
			Name:      name,
			MountPath: podMountRoot,
		})
	job.Spec.Template.Spec.Volumes = append(job.Spec.Template.Spec.Volumes, corev1.Volume{
		Name: name,
		VolumeSource: corev1.VolumeSource{
			ConfigMap: &corev1.ConfigMapVolumeSource{
				LocalObjectReference: corev1.LocalObjectReference{Name: configMapName},
			},
		},
	})
}
