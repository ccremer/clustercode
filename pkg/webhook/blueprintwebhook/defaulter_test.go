package blueprintwebhook

import (
	"testing"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"github.com/go-logr/logr"
	"github.com/stretchr/testify/assert"
)

func TestDefaulter_Default(t *testing.T) {
	tests := map[string]struct {
		givenSpec    v1alpha1.BlueprintSpec
		expectedSpec v1alpha1.BlueprintSpec
	}{
		"GivenEmptySpec_ThenExpectDefaultsSet": {
			givenSpec: v1alpha1.BlueprintSpec{},
			expectedSpec: v1alpha1.BlueprintSpec{
				Scan:             v1alpha1.ScanSpec{MediaFileExtensions: []string{"mkv", "mp4", "avi"}},
				MaxParallelTasks: 1,
				Encode: v1alpha1.EncodeSpec{
					SliceSize:            120,
					SplitCommandArgs:     []string{"-y", "-hide_banner", "-nostats", "-i", "${INPUT}", "-c", "copy", "-map", "0", "-segment_time", "${SLICE_SIZE}", "-f", "segment", "${OUTPUT}"},
					TranscodeCommandArgs: []string{"-y", "-hide_banner", "-nostats", "-i", "${INPUT}", "-c:v", "copy", "-c:a", "copy", "${OUTPUT}"},
					MergeCommandArgs:     []string{"-y", "-hide_banner", "-nostats", "-f", "concat", "-i", "concat.txt", "-c", "copy", "media_out.mkv"},
				},
			},
		},
	}
	for name, tt := range tests {
		t.Run(name, func(t *testing.T) {
			d := &Defaulter{Log: logr.Discard()}
			bp := &v1alpha1.Blueprint{Spec: tt.givenSpec}
			err := d.Default(nil, bp)
			assert.NoError(t, err)
			assert.Equal(t, tt.expectedSpec, bp.Spec)
		})
	}
}
