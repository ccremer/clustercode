package pipe

import (
	"context"

	pipeline "github.com/ccremer/go-command-pipeline"
	controllerruntime "sigs.k8s.io/controller-runtime"
)

// DebugLogger returns a list with a single hook that logs the step name.
// The logger is retrieved from the given context.
func DebugLogger[T context.Context](ctx T) pipeline.Listener[T] {
	log := controllerruntime.LoggerFrom(ctx)
	hook := func(step pipeline.Step[T]) {
		log.V(2).Info(`Entering step "` + step.Name + `"`)
	}
	return hook
}
