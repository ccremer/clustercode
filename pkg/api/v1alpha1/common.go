package v1alpha1

import (
	"fmt"
	"net/url"
	"strings"

	"k8s.io/apimachinery/pkg/util/runtime"
)

type StorageSpec struct {
	SourcePvc       ClusterCodeVolumeRef `json:"sourcePvc"`
	IntermediatePvc ClusterCodeVolumeRef `json:"intermediatePvc"`
	TargetPvc       ClusterCodeVolumeRef `json:"targetPvc"`
}

type ClusterCodeVolumeRef struct {
	// +kubebuilder:validation:Required
	ClaimName string `json:"claimName"`
	SubPath   string `json:"subPath,omitempty"`
}

type EncodeSpec struct {
	SplitCommandArgs     []string `json:"splitCommandArgs"`
	TranscodeCommandArgs []string `json:"transcodeCommandArgs"`
	MergeCommandArgs     []string `json:"mergeCommandArgs"`

	// PodTemplate contains a selection of fields to customize the spawned ffmpeg-based pods.
	// Some fields will be overwritten:
	//  * Volumes and volume mounts will be set based on StorageSpec.
	//  * Container args of the `ffmpeg` container will be set based on SplitCommandArgs, TranscodeCommandArgs, MergeCommandArgs.
	PodTemplate PodTemplate `json:"podTemplate,omitempty"`

	// SliceSize is the time in seconds of the slice lengths.
	// Higher values yield lower parallelization but less overhead.
	// Lower values yield high parallelization but more overhead.
	// If SliceSize is higher than the total length of the media, there may be just 1 slice with effectively no parallelization.
	SliceSize int `json:"sliceSize,omitempty"`
}

const (
	MediaFileDoneSuffix = "_done"
	ConfigMapFileName   = "file-list.txt"
)

type ClusterCodeUrl string

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
