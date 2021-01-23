package builder

import (
	"testing"

	"github.com/stretchr/testify/assert"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/utils/pointer"
)

func Test_PodSpecBuilder_MultipleBuilds(t *testing.T) {
	name := "test"
	image := "image"
	cb := NewContainerBuilder(name)
	pb := NewPodSpecBuilder(cb).Build()

	cb.Build(WithContainerImage(image))

	result := pb.Build().Build(AddPvcMount{}).PodSpec
	assert.Equal(t, name, result.Containers[0].Name)
	assert.Len(t, result.Containers[0].VolumeMounts, 1)
	assert.Len(t, result.Volumes, 1)
	assert.Equal(t, image, result.Containers[0].Image)
}

func Test_PodSpecBuilder_Properties(t *testing.T) {
	genericKey := "genericKey"
	genericValue := "genericValue"
	genericMountPath := "/generic"
	genericVolumeName := "volume"
	tests := map[string]struct {
		property PodSpecProperty
		expected *corev1.PodSpec
	}{
		"AddConfigMapMount": {
			property: AddConfigMapMount{
				ConfigMapName: genericKey,
				Name:          genericVolumeName,
				MountPath:     genericMountPath,
				DefaultMode:   pointer.Int32Ptr(0660),
			},
			expected: &corev1.PodSpec{
				Volumes: []corev1.Volume{
					{
						Name: genericVolumeName,
						VolumeSource: corev1.VolumeSource{ConfigMap: &corev1.ConfigMapVolumeSource{
							LocalObjectReference: corev1.LocalObjectReference{Name: genericKey},
							DefaultMode:          pointer.Int32Ptr(0660),
						}},
					},
				},
				Containers: []corev1.Container{
					{
						Name: genericKey,
						VolumeMounts: []corev1.VolumeMount{
							{
								Name:      genericVolumeName,
								MountPath: genericMountPath,
							},
						},
					},
				},
			},
		},
		"AddPvcMount": {
			property: AddPvcMount{
				VolumeName: genericVolumeName,
				MountPath:  genericMountPath,
				SubPath:    genericValue,
				ClaimName:  genericValue,
			},
			expected: &corev1.PodSpec{
				Volumes: []corev1.Volume{
					{
						Name: genericVolumeName,
						VolumeSource: corev1.VolumeSource{PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
							ClaimName: genericValue,
						}},
					},
				},
				Containers: []corev1.Container{
					{
						Name: genericKey,
						VolumeMounts: []corev1.VolumeMount{
							{
								Name:      genericVolumeName,
								MountPath: genericMountPath,
								SubPath:   genericValue,
							},
						},
					},
				},
			},
		},
	}
	for name, tt := range tests {
		t.Run(name, func(t *testing.T) {
			result := NewPodSpecBuilder(NewContainerBuilder(genericKey)).Build(tt.property).PodSpec
			assert.Equal(t, tt.expected, result)
		})
	}
}
