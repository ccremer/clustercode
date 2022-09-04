package pipe

import (
	"context"

	pipeline "github.com/ccremer/go-command-pipeline"
	"github.com/go-logr/logr"
)

// DebugLogger returns a list with a single hook that logs the step name.
// The logger is retrieved from the given context.
func DebugLogger[T context.Context](logger logr.Logger) pipeline.Listener[T] {
	hook := func(step pipeline.Step[T]) {
		logger.V(2).Info(`Entering step "` + step.Name + `"`)
	}
	return hook
}
