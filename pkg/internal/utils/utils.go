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

func EnsurePVCVolume(job *v1.Job, name, podMountRoot string, volume v1alpha1.ClusterCodeVolumeRef) {
	found := false
	for _, container := range job.Spec.Template.Spec.Containers {
		if HasVolumeMount(name, container) {
			found = true
			break
		}
	}
	if found {
		return
	}
	job.Spec.Template.Spec.Containers[0].VolumeMounts = append(job.Spec.Template.Spec.Containers[0].VolumeMounts,
		corev1.VolumeMount{Name: name, MountPath: podMountRoot, SubPath: volume.SubPath})
	job.Spec.Template.Spec.Volumes = append(job.Spec.Template.Spec.Volumes, corev1.Volume{
		Name: name,
		VolumeSource: corev1.VolumeSource{
			PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
				ClaimName: volume.ClaimName,
			},
		}})
}

func HasVolumeMount(name string, container corev1.Container) bool {
	found := false
	for _, mount := range container.VolumeMounts {
		if mount.Name == name {
			found = true
			break
		}
	}
	return found
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
