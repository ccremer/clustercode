package controllers

import (
	"strings"

	v1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/utils/pointer"
)

type (
	ClusterCodeJobType string
)

var (
	ClusterCodeLabels = labels.Set{
		"app.kubernetes.io/managed-by": "clustercode",
	}
	ClusterCodeScanLabels = labels.Set{
		ClustercodeTypeLabelKey: string(ClustercodeTypeScan),
	}
	ClusterCodeSplitLabels = labels.Set{
		ClustercodeTypeLabelKey: string(ClustercodeTypeSplit),
	}
	ClusterCodeCountLabels = labels.Set{
		ClustercodeTypeLabelKey: string(ClustercodeTypeCount),
	}
)

const (
	SourceSubMountPath       = "source"
	TargetSubMountPath       = "target"
	IntermediateSubMountPath = "intermediate"

	ClustercodeTypeLabelKey                      = "clustercode.github.io/type"
	ClustercodeTaskIdLabelKey                    = "clustercode.github.io/task-id"
	ClustercodeTypeScan       ClusterCodeJobType = "scan"
	ClustercodeTypeSplit      ClusterCodeJobType = "split"
	ClustercodeTypeCount      ClusterCodeJobType = "count"
)

var (
	ClustercodeTypes = []ClusterCodeJobType{ClustercodeTypeScan, ClustercodeTypeSplit, ClustercodeTypeCount}
)

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

func getOwner(obj v1.Object) types.NamespacedName {
	for _, owner := range obj.GetOwnerReferences() {
		if pointer.BoolPtrDerefOr(owner.Controller, false) {
			return types.NamespacedName{Namespace: obj.GetNamespace(), Name: owner.Name}
		}
	}
	return types.NamespacedName{}
}
