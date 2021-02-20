package pipeline

import (
	"context"
	"fmt"

	"github.com/go-logr/logr"
	apierrors "k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"

	"github.com/ccremer/clustercode/controllers"
)

type (
	Pipeline struct {
		Log          logr.Logger
		steps        []Step
		abortHandler Handler
	}
	Result struct {
		Abort   bool
		Err     error
		Requeue bool
	}
	Step struct {
		Name string
		F    ActionFunc
	}
	ActionFunc func() Result
	Handler    func(result Result)
)

func NewPipeline(log logr.Logger) *Pipeline {
	return &Pipeline{Log: log}
}

func (p *Pipeline) AddStep(step Step) *Pipeline {
	p.steps = append(p.steps, step)
	return p
}

func (p *Pipeline) WithSteps(steps ...Step) *Pipeline {
	p.steps = steps
	return p
}

func (p *Pipeline) WithAbortHandler(handler Handler) *Pipeline {
	p.abortHandler = handler
	return p
}

func (p *Pipeline) Run() Result {
	for i, step := range p.steps {
		index := i + 1
		p.Log.V(1).Info("executing step", "name", step.Name, "index", index)

		if r := step.F(); r.Abort || r.Err != nil {
			if p.abortHandler != nil {
				p.abortHandler(r)
			}
			if r.Err == nil {
				p.Log.V(1).Info("aborting pipeline", "step", step.Name, "index", index)
				return Result{Requeue: r.Requeue}
			}

			return Result{Err: fmt.Errorf("step '%s' (%d) failed: %w", step.Name, index, r.Err)}
		}
	}
	p.Log.V(1).Info("executed pipeline", "steps_completed", len(p.steps))
	return Result{}
}

func NewStep(name string, action ActionFunc) Step {
	return Step{
		Name: name,
		F:    action,
	}
}

func AbortIfDeleted(obj client.Object) ActionFunc {
	return func() Result {
		if obj.GetDeletionTimestamp() != nil {
			return Result{Abort: true}
		}
		return Result{}
	}
}

type UpdateStatusHandler struct {
	Log     logr.Logger
	Object  client.Object
	Context context.Context
	Client  client.Client
}

func (a UpdateStatusHandler) UpdateStatus(result Result) {
	if err := a.Client.Status().Update(a.Context, a.Object); err != nil {
		a.Log.Error(err, "could not update status")
	}
	a.Log.V(1).Info("updated status")
}

type ResourceAction struct {
	Log     logr.Logger
	Context context.Context
	Client  client.Client
	Scheme  *runtime.Scheme
}

func (r ResourceAction) GetOrAbort(obj client.Object) ActionFunc {
	return func() Result {
		name := controllers.MapToNamespacedName(obj)
		err := r.Client.Get(r.Context, name, obj)
		if err != nil {
			if apierrors.IsNotFound(err) {
				r.Log.Info("object not found", "object", name.String())
				return Result{Abort: true}
			}
			return Result{Err: err, Requeue: true}
		}
		r.Log.Info("Reconciling object")
		return Result{}
	}
}

func (r ResourceAction) CreateIfNotExisting(obj client.Object) ActionFunc {
	return func() Result {
		name := controllers.MapToNamespacedName(obj)
		if err := r.Client.Create(r.Context, obj); err != nil {
			if apierrors.IsAlreadyExists(err) {
				r.Log.V(1).Info("object already exists", "object", name.String())
				return Result{}
			}
			return Result{Err: err}
		}
		r.Log.Info("created object", "object", name.String(), "kind", obj.GetObjectKind().GroupVersionKind().Kind)
		return Result{}
	}
}
