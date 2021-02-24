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
		PodSpec           *corev1.PodSpec
		ContainerBuilders []*ContainerBuilder
	}
)

func NewPodSpecBuilder(ctBuilders ...*ContainerBuilder) *PodSpecBuilder {
	return NewPodSpecBuilderWith(&corev1.PodSpec{}, ctBuilders...)
}

func NewPodSpecBuilderWith(spec *corev1.PodSpec, ctBuilders ...*ContainerBuilder) *PodSpecBuilder {
	return &PodSpecBuilder{PodSpec: spec, ContainerBuilders: ctBuilders}
}

func (b *PodSpecBuilder) Build(props ...PodSpecProperty) *PodSpecBuilder {
	for _, opt := range props {
		opt.Apply(b)
	}
	for _, cb := range b.ContainerBuilders {
		if index := indexOf(cb.Container.Name, b.PodSpec.Containers); index >= 0 {
			b.PodSpec.Containers[index] = *cb.Container
		} else {
			b.PodSpec.Containers = append(b.PodSpec.Containers, *cb.Container)
		}
	}
	return b
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
	b.PodSpec.Volumes = append(b.PodSpec.Volumes, volume)
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
	b.PodSpec.ServiceAccountName = sa
	return b
}

func (b *PodSpecBuilder) RunAsUser(uid int64) *PodSpecBuilder {
	if b.PodSpec.SecurityContext == nil {
		b.PodSpec.SecurityContext = &corev1.PodSecurityContext{}
	}
	b.PodSpec.SecurityContext.RunAsUser = pointer.Int64Ptr(uid)
	return b
}
