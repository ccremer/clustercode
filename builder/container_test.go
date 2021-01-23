package builder

import (
	"testing"

	"github.com/stretchr/testify/assert"
	corev1 "k8s.io/api/core/v1"
)

func Test_ContainerBuilder_Constructor(t *testing.T) {
	name := "test"
	b := NewContainerBuilder(name)

	result := b.Container
	assert.Equal(t, name, result.Name)
}

func Test_ContainerBuilder_MultipleBuilds(t *testing.T) {
	name := "test"
	b := NewContainerBuilder(name)

	result := b.Build(WithContainerImage(name)).Build(AddArg(name)).Container
	assert.Equal(t, name, result.Name)
	assert.Equal(t, name, result.Image)
	assert.Equal(t, name, result.Args[0])
}

func Test_ContainerBuilder_Properties(t *testing.T) {
	genericKey := "genericKey"
	genericValue := "genericValue"
	tests := map[string]struct {
		given    *corev1.Container
		property ContainerProperty
		expected *corev1.Container
	}{
		"WithContainerImage": {
			property: WithContainerImage(genericValue),
			expected: &corev1.Container{Image: genericValue},
		},
		"WithContainerName": {
			property: WithContainerName(genericKey),
			expected: &corev1.Container{Name: genericKey},
		},
		"AddVolumeMount": {
			property: AddVolumeMount{Name: genericKey, MountPath: genericValue},
			expected: &corev1.Container{VolumeMounts: []corev1.VolumeMount{
				{
					Name:      genericKey,
					MountPath: genericValue,
				},
			}},
		},
		"AddArg": {
			property: AddArg(genericValue),
			expected: &corev1.Container{Args: []string{genericValue}},
		},
		"AddArgs": {
			given: &corev1.Container{
				Args: []string{genericKey},
			},
			property: AddArgs{genericValue},
			expected: &corev1.Container{Args: []string{genericKey, genericValue}},
		},
		"WithArgs": {
			given: &corev1.Container{
				Args: []string{genericKey},
			},
			property: WithArgs{genericValue},
			expected: &corev1.Container{Args: []string{genericValue}},
		},
		"AddEnvVarValue": {
			property: AddEnvVarValue{genericKey, genericValue},
			expected: &corev1.Container{Env: []corev1.EnvVar{{Name: genericKey, Value: genericValue}}},
		},
		"AddEnvValueFromConfigMap": {
			property: AddEnvValueFromConfigMap{"cm", genericValue},
			expected: &corev1.Container{Env: []corev1.EnvVar{{Name: genericValue, ValueFrom: &corev1.EnvVarSource{
				ConfigMapKeyRef: &corev1.ConfigMapKeySelector{
					LocalObjectReference: corev1.LocalObjectReference{Name: "cm"},
					Key:                  genericValue,
				}}}},
			},
		},
		"AddEnvFromConfigMap": {
			property: AddEnvFromConfigMap{genericKey, genericValue},
			expected: &corev1.Container{EnvFrom: []corev1.EnvFromSource{
				{
					Prefix: genericValue, ConfigMapRef: &corev1.ConfigMapEnvSource{
						LocalObjectReference: corev1.LocalObjectReference{Name: genericKey},
					}}},
			},
		},
	}
	for name, tt := range tests {
		t.Run(name, func(t *testing.T) {
			if tt.given == nil {
				tt.given = &corev1.Container{}
			}
			result := NewContainerBuilderWith(tt.given).Build(tt.property).Container
			assert.Equal(t, tt.expected, result)
		})
	}
}
