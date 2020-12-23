package v1alpha1

import (
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

type (
	// +kubebuilder:object:root=true
	// +kubebuilder:subresource:status

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
		SourceVolume corev1.Volume `json:"sourceVolume,omitempty"`
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
		DefaultCommandArgs []string `json:"defaultFfmpegArgs"`
		// +kubebuilder:default=-i;"\"${INPUT}\"";-c;copy;-map;0;-segment_time;"\"${SLICE_SIZE}\"";-f;segment;"\"${OUTPUT}\""
		SplitCommandArgs []string `json:"splitFfmpegArgs"`
		// +kubebuilder:default=-i;"\"${INPUT}\"";"-c:v";copy;"-c:a";copy;"\"${OUTPUT}\""
		TranscodeCommandArgs []string `json:"transcodeArgs"`
		// +kubebuilder:default=-f;concat;-i;concat.txt;-c;copy;media_out.mkv
		MergeCommandArgs []string `json:"mergeFfmpegArgs"`

		SliceSize int `json:"sliceSize,omitempty"`
	}

	ClustercodePlanStatus struct {
		Conditions []metav1.Condition `json:"conditions,omitempty"`
	}
)

func init() {
	SchemeBuilder.Register(&ClustercodePlan{}, &ClustercodePlanList{})
}
