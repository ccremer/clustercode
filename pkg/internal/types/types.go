package types

import (
	"k8s.io/apimachinery/pkg/labels"
)

var (
	ClusterCodeLabels = labels.Set{
		"app.kubernetes.io/managed-by": "clustercode",
	}
)

type (
	ClusterCodeJobType string
)

const (
	SourceSubMountPath       = "source"
	TargetSubMountPath       = "target"
	IntermediateSubMountPath = "intermediate"
	ConfigSubMountPath       = "config"

	ClustercodeTypeLabelKey       = "clustercode.github.io/type"
	ClustercodeSliceIndexLabelKey = "clustercode.github.io/slice-index"

	JobTypeScan    ClusterCodeJobType = "scan"
	JobTypeSplit   ClusterCodeJobType = "split"
	JobTypeSlice   ClusterCodeJobType = "slice"
	JobTypeCount   ClusterCodeJobType = "count"
	JobTypeMerge   ClusterCodeJobType = "merge"
	JobTypeCleanup ClusterCodeJobType = "cleanup"
)

var (
	JobTypes = []ClusterCodeJobType{
		JobTypeScan, JobTypeSplit, JobTypeCount, JobTypeSlice,
		JobTypeMerge, JobTypeCleanup}
)

func (t ClusterCodeJobType) AsLabels() labels.Set {
	return labels.Set{
		ClustercodeTypeLabelKey: string(t),
	}
}

func (t ClusterCodeJobType) String() string {
	return string(t)
}
