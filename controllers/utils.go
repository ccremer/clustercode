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
	"github.com/ccremer/clustercode/builder"
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

func createFfmpegJobDefinition(task *v1alpha1.Task, opts *TaskOpts) *batchv1.Job {
	cb := builder.NewContainerBuilder("ffmpeg").Build(
		builder.WithContainerImage(cfg.Config.Operator.FfmpegContainerImage),
		builder.WithImagePullPolicy(corev1.PullIfNotPresent),
		builder.WithArgs(opts.args),
	)
	pb := builder.NewPodSpecBuilder(cb).Build()
	pb.PodSpec.ServiceAccountName = task.Spec.ServiceAccountName
	pb.PodSpec.SecurityContext = &corev1.PodSecurityContext{
		RunAsUser:  pointer.Int64Ptr(1000),
		RunAsGroup: pointer.Int64Ptr(0),
		FSGroup:    pointer.Int64Ptr(0),
	}
	pb.PodSpec.RestartPolicy = corev1.RestartPolicyNever

	if opts.mountSource {
		pvc := task.Spec.Storage.SourcePvc
		pb.AddPvcMount(SourceSubMountPath, pvc.ClaimName, filepath.Join("/clustercode", SourceSubMountPath), pvc.SubPath)
	}
	if opts.mountIntermediate {
		pvc := task.Spec.Storage.IntermediatePvc
		pb.AddPvcMount(IntermediateSubMountPath, pvc.ClaimName, filepath.Join("/clustercode", IntermediateSubMountPath), pvc.SubPath)
	}
	if opts.mountTarget {
		pvc := task.Spec.Storage.TargetPvc
		pb.AddPvcMount(TargetSubMountPath, pvc.ClaimName, filepath.Join("/clustercode", TargetSubMountPath), pvc.SubPath)
	}
	if opts.mountConfig {
		pb.AddConfigMapMount(ConfigSubMountPath, task.Spec.FileListConfigMapRef, filepath.Join("/clustercode", ConfigSubMountPath))
	}

	job := &batchv1.Job{
		Spec: batchv1.JobSpec{
			BackoffLimit: pointer.Int32Ptr(0),
			Template: corev1.PodTemplateSpec{
				Spec: *pb.Build().PodSpec,
			},
		},
	}
	builder.NewMetaBuilderWith(job).Build(
		builder.WithNamespacedName{Namespace: task.Namespace, Name: fmt.Sprintf("%s-%s", task.Spec.TaskId, opts.jobType)},
		builder.WithLabels(ClusterCodeLabels), builder.AddLabels(opts.jobType.AsLabels()), builder.AddLabels(task.Spec.TaskId.AsLabels()),
	)
	return job
}
