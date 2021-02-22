package pipeline

import (
	"context"
	"fmt"

	"github.com/go-logr/logr"
	"sigs.k8s.io/controller-runtime/pkg/client"
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
		P    Predicate
	}
	ActionFunc func() Result
	Handler    func(result Result)
	Predicate  func(step Step) bool
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

func (p *Pipeline) AsNestedStep(name string, predicate Predicate) Step {
	return NewStepWithPredicate(name, func() Result {
		p.Log = p.Log.WithValues("nestedPipeline", name)
		return p.runPipeline()
	}, predicate)
}

func (r Result) IsSuccessful() bool {
	return r.Err == nil
}

func (p *Pipeline) Run() Result {
	result := p.runPipeline()
	p.Log.V(1).Info("executed pipeline", "stepsCompleted", len(p.steps))
	return result
}

func (p *Pipeline) runPipeline() Result {
	for _, step := range p.steps {

		if step.P != nil && !step.P(step) {
			p.Log.V(1).Info("ignoring step", "name", step.Name)
			continue
		}

		p.Log.V(1).Info("executing step", "name", step.Name)

		if r := step.F(); r.Abort || r.Err != nil {
			if p.abortHandler != nil {
				p.abortHandler(r)
			}
			if r.Err == nil {
				p.Log.V(1).Info("aborting pipeline", "step", step.Name)
				return Result{Requeue: r.Requeue, Abort: r.Abort}
			}

			return Result{Err: fmt.Errorf("step '%s' failed: %w", step.Name, r.Err), Abort: r.Abort}
		}
	}
	return Result{}
}

func NewStep(name string, action ActionFunc) Step {
	return Step{
		Name: name,
		F:    action,
	}
}

func NewStepWithPredicate(name string, action ActionFunc, predicate Predicate) Step {
	return Step{
		Name: name,
		F:    action,
		P:    predicate,
	}
}

func Abort() ActionFunc {
	return func() Result {
		return Result{Abort: true}
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
