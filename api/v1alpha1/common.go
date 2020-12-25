package v1alpha1

import (
	"fmt"
	"net/url"
	"strings"

	"k8s.io/apimachinery/pkg/util/runtime"
)

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
	ClusterCodeUrl string
)

const (
	MediaFileDoneSuffix = "_done"
	ConfigMapFileName = "file-list.txt"
)

func ToUrl(root, path string) ClusterCodeUrl {
	newUrl, err := url.Parse(fmt.Sprintf("cc://%s/%s", root, strings.Replace(path, root, "", 1)))
	runtime.Must(err)
	return ClusterCodeUrl(newUrl.String())
}

func (u ClusterCodeUrl) GetRoot() string {
	parsed, err := url.Parse(string(u))
	if err != nil {
		return ""
	}
	return parsed.Host
}

func (u ClusterCodeUrl) GetPath() string {
	parsed, err := url.Parse(string(u))
	if err != nil {
		return ""
	}
	return parsed.Path
}

func (u ClusterCodeUrl) StripSubPath(subpath string) string {
	path := u.GetPath()
	return strings.Replace(path, subpath, "", 1)
}

func (u ClusterCodeUrl) String() string {
	return string(u)
}
