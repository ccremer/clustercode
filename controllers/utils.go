package controllers

import (
	"fmt"
	"path/filepath"
	"strings"

	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/utils/pointer"

	"github.com/ccremer/clustercode/api/v1alpha1"
	"github.com/ccremer/clustercode/cfg"
)

type (
	ClusterCodeJobType string
)

var (
	ClusterCodeLabels = labels.Set{
		"app.kubernetes.io/managed-by": "clustercode",
	}
)

const (
	SourceSubMountPath       = "source"
	TargetSubMountPath       = "target"
	IntermediateSubMountPath = "intermediate"
	ConfigSubMountPath       = "config"

	ClustercodeTypeLabelKey                          = "clustercode.github.io/type"
	ClustercodeSliceIndexLabelKey                    = "clustercode.github.io/slice-index"
	ClustercodeTypeScan           ClusterCodeJobType = "scan"
	ClustercodeTypeSplit          ClusterCodeJobType = "split"
	ClustercodeTypeSlice          ClusterCodeJobType = "slice"
	ClustercodeTypeCount          ClusterCodeJobType = "count"
	ClustercodeTypeMerge          ClusterCodeJobType = "merge"
	ClustercodeTypeCleanup        ClusterCodeJobType = "cleanup"
)

var (
	ClustercodeTypes = []ClusterCodeJobType{
		ClustercodeTypeScan, ClustercodeTypeSplit, ClustercodeTypeCount, ClustercodeTypeSlice,
		ClustercodeTypeMerge, ClustercodeTypeCleanup}
)

func (t ClusterCodeJobType) AsLabels() labels.Set {
	return labels.Set{
		ClustercodeTypeLabelKey: string(t),
	}
}

func (t ClusterCodeJobType) String() string {
	return string(t)
}

func mergeArgsAndReplaceVariables(variables map[string]string, argsList ...[]string) (merged []string) {
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

func getOwner(obj metav1.Object) types.NamespacedName {
	for _, owner := range obj.GetOwnerReferences() {
		if pointer.BoolPtrDerefOr(owner.Controller, false) {
			return types.NamespacedName{Namespace: obj.GetNamespace(), Name: owner.Name}
		}
	}
	return types.NamespacedName{}
}

func createFfmpegJobDefinition(task *v1alpha1.ClustercodeTask, opts *TaskOpts) *batchv1.Job {
	job := &batchv1.Job{
		ObjectMeta: metav1.ObjectMeta{
			Name:      fmt.Sprintf("%s-%s", task.Spec.TaskId, opts.jobType),
			Namespace: task.Namespace,
			Labels:    labels.Merge(ClusterCodeLabels, labels.Merge(opts.jobType.AsLabels(), task.Spec.TaskId.AsLabels())),
		},
		Spec: batchv1.JobSpec{
			BackoffLimit: pointer.Int32Ptr(0),
			Template: corev1.PodTemplateSpec{
				Spec: corev1.PodSpec{
					SecurityContext: &corev1.PodSecurityContext{
						RunAsUser:  pointer.Int64Ptr(1000),
						RunAsGroup: pointer.Int64Ptr(0),
						FSGroup:    pointer.Int64Ptr(0),
					},
					ServiceAccountName: task.Spec.ServiceAccountName,
					RestartPolicy:      corev1.RestartPolicyNever,
					Containers: []corev1.Container{
						{
							Name:            "ffmpeg",
							Image:           cfg.Config.Operator.FfmpegContainerImage,
							ImagePullPolicy: corev1.PullIfNotPresent,
							Args:            opts.args,
						},
					},
				},
			},
		},
	}
	if opts.mountSource {
		addPvcVolume(job, SourceSubMountPath, filepath.Join("/clustercode", SourceSubMountPath), task.Spec.Storage.SourcePvc)
	}
	if opts.mountIntermediate {
		addPvcVolume(job, IntermediateSubMountPath, filepath.Join("/clustercode", IntermediateSubMountPath), task.Spec.Storage.IntermediatePvc)
	}
	if opts.mountTarget {
		addPvcVolume(job, TargetSubMountPath, filepath.Join("/clustercode", TargetSubMountPath), task.Spec.Storage.TargetPvc)
	}
	if opts.mountConfig {
		addConfigMapVolume(job, ConfigSubMountPath, filepath.Join("/clustercode", ConfigSubMountPath), task.Spec.FileListConfigMapRef)
	}
	return job
}

func addPvcVolume(job *batchv1.Job, name, podMountRoot string, volume v1alpha1.ClusterCodeVolumeRef) {
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

func addConfigMapVolume(job *batchv1.Job, name, podMountRoot, configMapName string) {
	job.Spec.Template.Spec.Containers[0].VolumeMounts = append(job.Spec.Template.Spec.Containers[0].VolumeMounts,
		corev1.VolumeMount{
			Name:      name,
			MountPath: podMountRoot,
		})
	job.Spec.Template.Spec.Volumes = append(job.Spec.Template.Spec.Volumes, corev1.Volume{
		Name: name,
		VolumeSource: corev1.VolumeSource{
			ConfigMap: &corev1.ConfigMapVolumeSource{
				LocalObjectReference: corev1.LocalObjectReference{Name: configMapName},
			},
		},
	})
}
