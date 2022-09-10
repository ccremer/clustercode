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

func createClustercodeJobDefinition(job *batchv1.Job, task *v1alpha1.Task, opts TaskOpts) {
	job.Labels = labels.Merge(job.Labels, labels.Merge(internaltypes.ClusterCodeLabels, labels.Merge(opts.jobType.AsLabels(), task.Spec.TaskId.AsLabels())))
	job.Spec.BackoffLimit = pointer.Int32(0)

	podSpec := job.Spec.Template.Spec
	templateSpec := opts.template

	// merged from template
	podSpec.SecurityContext = templateSpec.PodSecurityContext
	podSpec.Volumes = templateSpec.Volumes
	if templateSpec.Metadata != nil && templateSpec.Metadata.Labels != nil {
		job.Spec.Template.Labels = templateSpec.Metadata.Labels
	}
	if templateSpec.Metadata != nil && templateSpec.Metadata.Annotations != nil {
		job.Spec.Template.Annotations = templateSpec.Metadata.Annotations
	}

	podSpec.Containers = convertContainerSpec(templateSpec.Containers)
	podSpec.InitContainers = convertContainerSpec(templateSpec.InitContainers)

	// overrides
	podSpec.ServiceAccountName = task.Spec.ServiceAccountName
	podSpec.RestartPolicy = corev1.RestartPolicyNever

	podSpec.Containers = createOrUpdateContainer("clustercode", podSpec.Containers, func(c *corev1.Container) {
		c.Image = opts.image
		c.Args = opts.args
		utils.EnsureVolumeMountIf(opts.mountSource, c, internaltypes.SourceSubMountPath,
			filepath.Join("/clustercode", internaltypes.SourceSubMountPath), task.Spec.Storage.SourcePvc.SubPath)
		utils.EnsureVolumeMountIf(opts.mountIntermediate, c, internaltypes.IntermediateSubMountPath,
			filepath.Join("/clustercode", internaltypes.IntermediateSubMountPath), task.Spec.Storage.IntermediatePvc.SubPath)
		utils.EnsureVolumeMountIf(opts.mountTarget, c, internaltypes.TargetSubMountPath,
			filepath.Join("/clustercode", internaltypes.TargetSubMountPath), task.Spec.Storage.TargetPvc.SubPath)
		utils.EnsureVolumeMountIf(opts.mountConfig, c, internaltypes.ConfigSubMountPath,
			filepath.Join("/clustercode", internaltypes.ConfigSubMountPath), "")
	})
	job.Spec.Template.Spec = podSpec

	if opts.mountSource {
		utils.EnsurePVCVolume(job, internaltypes.SourceSubMountPath, task.Spec.Storage.SourcePvc)
	}
	if opts.mountIntermediate {
		utils.EnsurePVCVolume(job, internaltypes.IntermediateSubMountPath, task.Spec.Storage.IntermediatePvc)
	}
	if opts.mountTarget {
		utils.EnsurePVCVolume(job, internaltypes.TargetSubMountPath, task.Spec.Storage.TargetPvc)
	}
	if opts.mountConfig {
		EnsureConfigMapVolume(job, internaltypes.ConfigSubMountPath, task.Spec.FileListConfigMapRef)
	}
}

func convertContainerSpec(templates []v1alpha1.ContainerTemplate) []corev1.Container {
	containers := make([]corev1.Container, len(templates))
	for i, ct := range templates {
		c := ct.ToContainer()
		containers[i] = c
	}
	return containers
}

func createOrUpdateContainer(containerName string, podContainers []corev1.Container, mutateFn func(*corev1.Container)) []corev1.Container {
	for i, c := range podContainers {
		if c.Name == containerName {
			mutateFn(&c)
			podContainers[i] = c
			return podContainers
		}
	}
	container := &corev1.Container{Name: containerName}
	mutateFn(container)
	return append(podContainers, *container)
}

func EnsureConfigMapVolume(job *batchv1.Job, name, configMapName string) {
	for _, volume := range job.Spec.Template.Spec.Volumes {
		if volume.Name == name {
			return
		}
	}
	job.Spec.Template.Spec.Volumes = append(job.Spec.Template.Spec.Volumes, corev1.Volume{
		Name: name,
		VolumeSource: corev1.VolumeSource{
			ConfigMap: &corev1.ConfigMapVolumeSource{
				LocalObjectReference: corev1.LocalObjectReference{Name: configMapName},
			},
		},
	})
}
