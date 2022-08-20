package taskcontroller

import (
	"fmt"
	"path/filepath"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	"github.com/ccremer/clustercode/pkg/internal/utils"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/utils/pointer"
)

var DefaultFfmpegContainerImage string

func createFfmpegJobDefinition(task *v1alpha1.Task, opts *TaskOpts) *batchv1.Job {
	job := &batchv1.Job{
		ObjectMeta: metav1.ObjectMeta{
			Name:      fmt.Sprintf("%s-%s", task.Spec.TaskId, opts.jobType),
			Namespace: task.Namespace,
			Labels:    labels.Merge(internaltypes.ClusterCodeLabels, labels.Merge(opts.jobType.AsLabels(), task.Spec.TaskId.AsLabels())),
		},
		Spec: batchv1.JobSpec{
			BackoffLimit: pointer.Int32Ptr(0),
			Template: corev1.PodTemplateSpec{
				Spec: corev1.PodSpec{
					SecurityContext: &corev1.PodSecurityContext{
						RunAsUser:  pointer.Int64Ptr(1000),
						RunAsGroup: pointer.Int64Ptr(0),
						FSGroup:    pointer.Int64Ptr(0),
					},
					ServiceAccountName: task.Spec.ServiceAccountName,
					RestartPolicy:      corev1.RestartPolicyNever,
					Containers: []corev1.Container{
						{
							Name:            "ffmpeg",
							Image:           DefaultFfmpegContainerImage,
							ImagePullPolicy: corev1.PullIfNotPresent,
							Args:            opts.args,
						},
					},
				},
			},
		},
	}
	if opts.mountSource {
		utils.AddPvcVolume(job, internaltypes.SourceSubMountPath, filepath.Join("/clustercode", internaltypes.SourceSubMountPath), task.Spec.Storage.SourcePvc)
	}
	if opts.mountIntermediate {
		utils.AddPvcVolume(job, internaltypes.IntermediateSubMountPath, filepath.Join("/clustercode", internaltypes.IntermediateSubMountPath), task.Spec.Storage.IntermediatePvc)
	}
	if opts.mountTarget {
		utils.AddPvcVolume(job, internaltypes.TargetSubMountPath, filepath.Join("/clustercode", internaltypes.TargetSubMountPath), task.Spec.Storage.TargetPvc)
	}
	if opts.mountConfig {
		addConfigMapVolume(job, internaltypes.ConfigSubMountPath, filepath.Join("/clustercode", internaltypes.ConfigSubMountPath), task.Spec.FileListConfigMapRef)
	}
	return job
}

func addConfigMapVolume(job *batchv1.Job, name, podMountRoot, configMapName string) {
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
