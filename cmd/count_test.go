package cmd

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func Test_matchesTaskSegment(t *testing.T) {
	tests := map[string]struct {
		path     string
		prefix   string
		expected bool
	}{
		"GivenValidSourcePath_WhenMatching_ThenReturnTrue": {
			path: "/clustercode/intermediate/task_0.mp4",
			prefix: "task_",
			expected: true,
		},
		"GivenInValidSourcePath_WhenMatching_ThenReturnFalse": {
			path: "/clustercode/intermediate/task_0_done.mp4",
			prefix: "task_",
			expected: false,
		},
	}
	for name, tt := range tests {
		t.Run(name, func(t *testing.T) {
			assert.Equal(t, tt.expected, matchesTaskSegment(tt.path, tt.prefix))
		})
	}
}
