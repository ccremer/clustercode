package builder

import (
	corev1 "k8s.io/api/core/v1"
)

type (
	ContainerProperty interface {
		Apply(b *ContainerBuilder)
	}

	ContainerBuilder struct {
		Container *corev1.Container
	}

	WithContainerName        string
	WithContainerImage       string
	WithImagePullPolicy      corev1.PullPolicy
	AddEnvVarValue           KeyValueTuple
	AddEnvValueFromConfigMap struct {
		ConfigMapName string
		Key           string
	}
	AddEnvFromConfigMap struct {
		ConfigMapName string
		Prefix        string
	}
	WithArgs       []string
	AddArg         string
	AddArgs        []string
	AddVolumeMount corev1.VolumeMount
)

func NewContainerBuilder(containerName string) ContainerBuilder {
	return NewContainerBuilderWith(&corev1.Container{Name: containerName})
}

func NewContainerBuilderWith(container *corev1.Container) ContainerBuilder {
	return ContainerBuilder{Container: container}
}

func (b ContainerBuilder) Build(props ...ContainerProperty) ContainerBuilder {
	for _, prop := range props {
		prop.Apply(&b)
	}
	return b
}

func (p WithContainerName) Apply(b *ContainerBuilder) {
	b.Container.Name = string(p)
}

func (p WithContainerImage) Apply(b *ContainerBuilder) {
	b.Container.Image = string(p)
}

func (p WithImagePullPolicy) Apply(b *ContainerBuilder) {
	b.Container.ImagePullPolicy = corev1.PullPolicy(p)
}

func (p AddEnvVarValue) Apply(b *ContainerBuilder) {
	b.Container.Env = append(b.Container.Env, corev1.EnvVar{Name: p.Key, Value: p.Value})
}

func (p WithArgs) Apply(b *ContainerBuilder) {
	b.Container.Args = p
}

func (p AddArg) Apply(b *ContainerBuilder) {
	b.Container.Args = append(b.Container.Args, string(p))
}

func (p AddArgs) Apply(b *ContainerBuilder) {
	b.Container.Args = append(b.Container.Args, p...)
}

func (p AddVolumeMount) Apply(b *ContainerBuilder) {
	b.Container.VolumeMounts = append(b.Container.VolumeMounts, corev1.VolumeMount(p))
}

func (b *ContainerBuilder) AddMountPath(volumeName, mountPath, subPath string) {
	AddVolumeMount{
		Name:      volumeName,
		MountPath: mountPath,
		SubPath:   subPath,
	}.Apply(b)
}

func (p AddEnvFromConfigMap) Apply(b *ContainerBuilder) {
	b.Container.EnvFrom = append(b.Container.EnvFrom, corev1.EnvFromSource{
		Prefix: p.Prefix,
		ConfigMapRef: &corev1.ConfigMapEnvSource{
			LocalObjectReference: corev1.LocalObjectReference{Name: p.ConfigMapName},
		},
	})
}

func (p AddEnvValueFromConfigMap) Apply(b *ContainerBuilder) {
	b.Container.Env = append(b.Container.Env, corev1.EnvVar{
		Name: p.Key,
		ValueFrom: &corev1.EnvVarSource{
			ConfigMapKeyRef: &corev1.ConfigMapKeySelector{
				LocalObjectReference: corev1.LocalObjectReference{Name: p.ConfigMapName},
				Key:                  p.Key,
			},
		},
	})
}
