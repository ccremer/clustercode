package builder

import (
	corev1 "k8s.io/api/core/v1"
)

type (
	PodSpecProperty interface {
		Apply(b *PodSpecBuilder)
	}

	PodSpecBuilder struct {
		PodSpec           *corev1.PodSpec
		ContainerBuilders []ContainerBuilder
	}

	AddConfigMapMount struct {
		ContainerBuilder *ContainerBuilder
		ConfigMapName    string
		Name             string
		MountPath        string
		DefaultMode      *int32
	}
	AddPvcMount struct {
		ContainerBuilder *ContainerBuilder
		ClaimName        string
		VolumeName       string
		MountPath        string
		SubPath          string
	}
	AddVolume corev1.Volume
)

func NewPodSpecBuilder(ctBuilders ...ContainerBuilder) PodSpecBuilder {
	return NewPodSpecBuilderWith(&corev1.PodSpec{}, ctBuilders...)
}

func NewPodSpecBuilderWith(spec *corev1.PodSpec, ctBuilders ...ContainerBuilder) PodSpecBuilder {
	return PodSpecBuilder{PodSpec: spec, ContainerBuilders: ctBuilders}
}

func (b PodSpecBuilder) Build(props ...PodSpecProperty) PodSpecBuilder {
	for _, opt := range props {
		opt.Apply(&b)
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

func (p AddVolume) Apply(b *PodSpecBuilder) {
	b.PodSpec.Volumes = append(b.PodSpec.Volumes, corev1.Volume(p))
}

func (p AddConfigMapMount) Apply(b *PodSpecBuilder) {
	if p.ContainerBuilder == nil {
		for _, cb := range b.ContainerBuilders {
			cb.AddMountPath(p.Name, p.MountPath, "")
		}
	} else {
		p.ContainerBuilder.AddMountPath(p.Name, p.MountPath, "")
	}
	AddVolume{
		Name: p.Name,
		VolumeSource: corev1.VolumeSource{
			ConfigMap: &corev1.ConfigMapVolumeSource{
				LocalObjectReference: corev1.LocalObjectReference{Name: p.ConfigMapName},
				DefaultMode:          p.DefaultMode,
			},
		},
	}.Apply(b)
}

func (p AddPvcMount) Apply(b *PodSpecBuilder) {
	if p.ContainerBuilder == nil {
		for _, cb := range b.ContainerBuilders {
			cb.AddMountPath(p.VolumeName, p.MountPath, p.SubPath)
		}
	} else {
		p.ContainerBuilder.AddMountPath(p.VolumeName, p.MountPath, p.SubPath)
	}
	AddVolume{
		Name: p.VolumeName,
		VolumeSource: corev1.VolumeSource{
			PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{ClaimName: p.ClaimName},
		},
	}.Apply(b)
}

func (b *PodSpecBuilder) AddConfigMapMount(volumeName, configMapName, podMountPath string) {
	AddConfigMapMount{
		ConfigMapName: configMapName,
		Name:          volumeName,
		MountPath:     podMountPath,
	}.Apply(b)
}

func (b *PodSpecBuilder) AddPvcMount(volumeName, claimName, podMountPath, subPath string) {
	AddPvcMount{
		ClaimName:  claimName,
		VolumeName: volumeName,
		MountPath:  podMountPath,
		SubPath:    subPath,
	}.Apply(b)
}
