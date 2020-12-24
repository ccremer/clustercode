package controllers

import "strings"

var (
	ClusterCodeLabels = map[string]string{
		"app.kubernetes.io/managed-by": "clustercode",
	}
	ClusterCodeScanLabels = map[string]string {
		"clustercode.github.io/type": "scan",
	}
	ClusterCodeSplitLabels = map[string]string {
		"clustercode.github.io/type": "split",
	}
	ClusterCodeCountLabels = map[string]string {
		"clustercode.github.io/type": "count",
	}
)

const (
	SourceSubMountPath       = "source"
	TargetSubMountPath       = "target"
	IntermediateSubMountPath = "intermediate"
)

func mergeLabels(labels ...map[string]string) map[string]string {
	merged := make(map[string]string)
	for _, labelMap := range labels {
		for k, v := range labelMap {
			merged[k] = v
		}
	}
	return merged
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
