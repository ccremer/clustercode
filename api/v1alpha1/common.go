package v1alpha1

type (
	StorageSpec struct {
		SourcePvc       ClusterCodeVolumeRef `json:"sourcePvc"`
		IntermediatePvc ClusterCodeVolumeRef `json:"intermediatePvc"`
		TargetPvc       ClusterCodeVolumeRef `json:"targetPvc"`
	}
	ClusterCodeVolumeRef struct {
		// +kubebuilder:validation:Required
		ClaimName string `json:"claimName"`
		SubPath   string `json:"subPath,omitempty"`
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
)
