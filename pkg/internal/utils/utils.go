package utils

import (
	"strings"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/utils/pointer"
)

func EnsureVolumeMountIf(enabled bool, container *corev1.Container, volumeName, podMountRoot, subPath string) {
	if !enabled || HasVolumeMount(*container, volumeName) {
		return
	}
	container.VolumeMounts = append(container.VolumeMounts,
		corev1.VolumeMount{Name: volumeName, MountPath: podMountRoot, SubPath: subPath})
}

func EnsurePVCVolume(job *v1.Job, name string, volume v1alpha1.VolumeRef) {
	for _, v := range job.Spec.Template.Spec.Volumes {
		if v.Name == name {
			return
		}
	}
	job.Spec.Template.Spec.Volumes = append(job.Spec.Template.Spec.Volumes, corev1.Volume{
		Name: name,
		VolumeSource: corev1.VolumeSource{
			PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
				ClaimName: volume.ClaimName,
			},
		}})
}

func HasVolumeMount(container corev1.Container, name string) bool {
	for _, mount := range container.VolumeMounts {
		if mount.Name == name {
			return true
		}
	}
	return false
}

func GetOwner(obj metav1.Object) types.NamespacedName {
	for _, owner := range obj.GetOwnerReferences() {
		if pointer.BoolDeref(owner.Controller, false) {
			return types.NamespacedName{Namespace: obj.GetNamespace(), Name: owner.Name}
		}
	}
	return types.NamespacedName{}
}

func MergeArgsAndReplaceVariables(variables map[string]string, argsList ...[]string) (merged []string) {
	for _, args := range argsList {
		for _, arg := range args {
			for k, v := range variables {
				arg = strings.ReplaceAll(arg, k, v)
			}
			merged = append(merged, arg)
		}
	}
	return merged
}
