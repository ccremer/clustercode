package utils

import (
	"strings"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"k8s.io/api/batch/v1"
	v12 "k8s.io/api/core/v1"
	v13 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/utils/pointer"
)

func AddPvcVolume(job *v1.Job, name, podMountRoot string, volume v1alpha1.ClusterCodeVolumeRef) {
	job.Spec.Template.Spec.Containers[0].VolumeMounts = append(job.Spec.Template.Spec.Containers[0].VolumeMounts,
		v12.VolumeMount{Name: name, MountPath: podMountRoot, SubPath: volume.SubPath})
	job.Spec.Template.Spec.Volumes = append(job.Spec.Template.Spec.Volumes, v12.Volume{
		Name: name,
		VolumeSource: v12.VolumeSource{
			PersistentVolumeClaim: &v12.PersistentVolumeClaimVolumeSource{
				ClaimName: volume.ClaimName,
			},
		}})
}

func GetOwner(obj v13.Object) types.NamespacedName {
	for _, owner := range obj.GetOwnerReferences() {
		if pointer.BoolPtrDerefOr(owner.Controller, false) {
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
