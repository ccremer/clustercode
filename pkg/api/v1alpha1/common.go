package v1alpha1

import (
	"fmt"
	"net/url"
	"strings"

	"k8s.io/apimachinery/pkg/util/runtime"
)

type StorageSpec struct {
	// SourcePvc is a reference to the PVC which contains the source media files to encode.
	// If `sourcePvc.claimName` is empty, then you need to specify a pod template that configures a volume named "source".
	SourcePvc VolumeRef `json:"sourcePvc"`
	// SourcePvc is a reference to the PVC which contains the intermediate media files as part of the splitting and merging.
	// If `intermediatePvc.claimName` is empty, then you need to specify a pod template that configures a volume named "intermediate".
	IntermediatePvc VolumeRef `json:"intermediatePvc"`
	// SourcePvc is a reference to the PVC which contains the final result files.
	// If `targetPvc.claimName` is empty, then you need to specify a pod template that configures a volume named "target".
	TargetPvc VolumeRef `json:"targetPvc"`
}

type VolumeRef struct {
	// ClaimName is the name of the PVC.
	ClaimName string `json:"claimName"`
	// SubPath is an optional path within the referenced PVC.
	// This is useful if the same PVC is shared.
	SubPath string `json:"subPath,omitempty"`
}

type EncodeSpec struct {
	SplitCommandArgs     []string `json:"splitCommandArgs"`
	TranscodeCommandArgs []string `json:"transcodeCommandArgs"`
	MergeCommandArgs     []string `json:"mergeCommandArgs"`

	// PodTemplate contains a selection of fields to customize the spawned ffmpeg-based pods.
	// Some fields will be overwritten:
	//  * Volumes and volume mounts will be set based on StorageSpec, if the claim names are given.
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
