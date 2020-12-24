package v1alpha1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func init() {
	SchemeBuilder.Register(&ClustercodePlan{}, &ClustercodePlanList{})
}

type (
	// +kubebuilder:object:root=true
	// +kubebuilder:subresource:status
	// +kubebuilder:printcolumn:name="Schedule",type="string",JSONPath=".spec.scanSchedule",description="Cron schedule of media scans"
	// +kubebuilder:printcolumn:name="Suspended",type="boolean",JSONPath=".spec.suspend",description="Whether media scanning is suspended"
	// +kubebuilder:printcolumn:name="Current Tasks",type="integer",JSONPath=".status.currentTasks",description="Currently active tasks"
	// +kubebuilder:printcolumn:name="Age",type="date",JSONPath=".metadata.creationTimestamp"

	// ClustercodePlan is the Schema for the ClusterCodePlan API
	ClustercodePlan struct {
		metav1.TypeMeta   `json:",inline"`
		metav1.ObjectMeta `json:"metadata,omitempty"`

		Spec   ClustercodePlanSpec   `json:"spec,omitempty"`
		Status ClustercodePlanStatus `json:"status,omitempty"`
	}

	// +kubebuilder:object:root=true

	// ClustercodePlanList contains a list of ClustercodePlans.
	ClustercodePlanList struct {
		metav1.TypeMeta `json:",inline"`
		metav1.ListMeta `json:"metadata,omitempty"`
		Items           []ClustercodePlan `json:"items"`
	}

	// ClustercodePlanSpec specifies a Clustercode
	ClustercodePlanSpec struct {
		ScanSchedule string `json:"scanSchedule"`
		// +kubebuilder:validation:Required
		SourcePvcRef string `json:"sourcePvcRef,omitempty"`
		// +kubebuilder:validation:Required
		SourceVolumeSubdir string `json:"sourceVolumeSubdir,omitempty"`

		// +kubebuilder:default=1
		MaxParallelTasks int `json:"maxParallelTasks,omitempty"`

		Suspend bool `json:"suspend,omitempty"`

		ScanSpec   ScanSpec   `json:"scanSpec,omitempty"`
		EncodeSpec EncodeSpec `json:"encodeSpec,omitempty"`
	}

	ScanSpec struct {
		// +kubebuilder:default=mkv;mp4;avi
		MediaFileExtensions []string `json:"mediaFileExtensions,omitempty"`
	}

	EncodeSpec struct {
		// +kubebuilder:default=-y;-hide_banner;-nostats
		DefaultCommandArgs []string `json:"defaultCommandArgs"`
		// +kubebuilder:default=-i;"\"${INPUT}\"";-c;copy;-map;0;-segment_time;"\"${SLICE_SIZE}\"";-f;segment;"\"${OUTPUT}\""
		SplitCommandArgs []string `json:"splitCommandArgs"`
		// +kubebuilder:default=-i;"\"${INPUT}\"";"-c:v";copy;"-c:a";copy;"\"${OUTPUT}\""
		TranscodeCommandArgs []string `json:"transcodeCommandArgs"`
		// +kubebuilder:default=-f;concat;-i;concat.txt;-c;copy;media_out.mkv
		MergeCommandArgs []string `json:"mergeCommandArgs"`

		SliceSize int `json:"sliceSize,omitempty"`
	}

	ClustercodePlanStatus struct {
		Conditions   []metav1.Condition   `json:"conditions,omitempty"`
		CurrentTasks []ClusterCodeTaskRef `json:"currentTasks,omitempty"`
	}

	ClusterCodeTaskRef struct {
		TaskName string `json:"taskName,omitempty"`
	}
)

// IsMaxParallelTaskLimitReached will return true if the count of current task has reached MaxParallelTasks.
func (plan *ClustercodePlan) IsMaxParallelTaskLimitReached() bool {
	return len(plan.Status.CurrentTasks) >= plan.Spec.MaxParallelTasks
}
