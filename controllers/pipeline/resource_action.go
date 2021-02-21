package pipeline

import (
	"context"

	"github.com/go-logr/logr"
	apierrors "k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"

	"github.com/ccremer/clustercode/controllers"
)

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
