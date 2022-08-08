package builder

import (
	corev1 "k8s.io/api/core/v1"
	"k8s.io/utils/pointer"
)

type (
	PodSpecProperty interface {
		Apply(b *PodSpecBuilder)
	}

	PodSpecBuilder struct {
		podSpec           *corev1.PodSpec
		ContainerBuilders []*ContainerBuilder
	}
)

func NewPodSpecBuilder(ctBuilders ...*ContainerBuilder) *PodSpecBuilder {
	return NewPodSpecBuilderWith(&corev1.PodSpec{}, ctBuilders...)
}

func NewPodSpecBuilderWith(spec *corev1.PodSpec, ctBuilders ...*ContainerBuilder) *PodSpecBuilder {
	return &PodSpecBuilder{podSpec: spec, ContainerBuilders: ctBuilders}
}

func (b *PodSpecBuilder) Build(props ...PodSpecProperty) *corev1.PodSpec {
	for _, opt := range props {
		opt.Apply(b)
	}
	for _, cb := range b.ContainerBuilders {
		if index := indexOf(cb.Container.Name, b.podSpec.Containers); index >= 0 {
			b.podSpec.Containers[index] = *cb.Container
		} else {
			b.podSpec.Containers = append(b.podSpec.Containers, *cb.Container)
		}
	}
	return b.podSpec
}

func indexOf(name string, containers []corev1.Container) int {
	for i, ct := range containers {
		if ct.Name == name {
			return i
		}
	}
	return -1
}

func (b *PodSpecBuilder) AddVolume(volume corev1.Volume) *PodSpecBuilder {
	b.podSpec.Volumes = append(b.podSpec.Volumes, volume)
	return b
}

func (b *PodSpecBuilder) AddConfigMapMount(cb *ContainerBuilder, configMapName, volumeName, mountPath string) *PodSpecBuilder {
	if cb == nil {
		for _, cb := range b.ContainerBuilders {
			cb.AddMountPath(volumeName, mountPath, "")
		}
	} else {
		cb.AddMountPath(volumeName, mountPath, "")
	}
	b.AddVolume(corev1.Volume{
		Name: volumeName,
		VolumeSource: corev1.VolumeSource{
			ConfigMap: &corev1.ConfigMapVolumeSource{
				LocalObjectReference: corev1.LocalObjectReference{
					Name: configMapName,
				},
			},
		},
	})
	return b
}

func (b *PodSpecBuilder) AddPvcMount(cb *ContainerBuilder, claimName, volumeName, mountPath, subPath string) *PodSpecBuilder {
	if cb == nil {
		for _, cb := range b.ContainerBuilders {
			cb.AddMountPath(volumeName, mountPath, subPath)
		}
	} else {
		cb.AddMountPath(volumeName, mountPath, subPath)
	}
	b.AddVolume(corev1.Volume{
		Name: volumeName,
		VolumeSource: corev1.VolumeSource{
			PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
				ClaimName: claimName,
			},
		},
	})
	return b
}

func (b *PodSpecBuilder) WithServiceAccount(sa string) *PodSpecBuilder {
	b.podSpec.ServiceAccountName = sa
	return b
}

func (b *PodSpecBuilder) RunAsUser(uid int64) *PodSpecBuilder {
	if b.podSpec.SecurityContext == nil {
		b.podSpec.SecurityContext = &corev1.PodSecurityContext{}
	}
	b.podSpec.SecurityContext.RunAsUser = pointer.Int64Ptr(uid)
	return b
}

func (b *PodSpecBuilder) RunAsGroup(gid int64) *PodSpecBuilder {
	if b.podSpec.SecurityContext == nil {
		b.podSpec.SecurityContext = &corev1.PodSecurityContext{}
	}
	b.podSpec.SecurityContext.RunAsGroup = pointer.Int64Ptr(gid)
	return b
}

func (b *PodSpecBuilder) WithFSGroup(id int64) *PodSpecBuilder {
	if b.podSpec.SecurityContext == nil {
		b.podSpec.SecurityContext = &corev1.PodSecurityContext{}
	}
	b.podSpec.SecurityContext.FSGroup = pointer.Int64Ptr(id)
	return b
}

func (b *PodSpecBuilder) WithRestartPolicy(policy corev1.RestartPolicy) *PodSpecBuilder {
	b.podSpec.RestartPolicy = policy
	return b
}
