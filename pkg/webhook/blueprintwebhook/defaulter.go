package blueprintwebhook

import (
	"context"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"github.com/go-logr/logr"
	"k8s.io/apimachinery/pkg/runtime"
)

type Defaulter struct {
	Log logr.Logger
}

func (d *Defaulter) Default(_ context.Context, obj runtime.Object) error {
	bp := obj.(*v1alpha1.Blueprint)
	d.Log.V(1).Info("Applying defaults", "name", bp.Name, "namespace", bp.Namespace)

	if bp.Spec.MaxParallelTasks == 0 {
		bp.Spec.MaxParallelTasks = 1
	}

	if bp.Spec.Encode.SliceSize == 0 {
		bp.Spec.Encode.SliceSize = 120
	}

	if len(bp.Spec.Scan.MediaFileExtensions) == 0 {
		bp.Spec.Scan.MediaFileExtensions = []string{"mkv", "mp4", "avi"}
	}

	if len(bp.Spec.Encode.SplitCommandArgs) == 0 {
		bp.Spec.Encode.SplitCommandArgs = []string{
			"-y", "-hide_banner", "-nostats", "-i", "${INPUT}", "-c", "copy", "-map", "0", "-segment_time", "${SLICE_SIZE}", "-f", "segment", "${OUTPUT}",
		}
	}

	if len(bp.Spec.Encode.TranscodeCommandArgs) == 0 {
		bp.Spec.Encode.TranscodeCommandArgs = []string{
			"-y", "-hide_banner", "-nostats", "-i", "${INPUT}", "-c:v", "copy", "-c:a", "copy", "${OUTPUT}",
		}
	}

	if len(bp.Spec.Encode.MergeCommandArgs) == 0 {
		bp.Spec.Encode.MergeCommandArgs = []string{
			"-y", "-hide_banner", "-nostats", "-f", "concat", "-i", "concat.txt", "-c", "copy", "media_out.mkv",
		}
	}
	return nil
}
