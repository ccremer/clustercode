package reconciler

import (
	"context"

	apierrors "k8s.io/apimachinery/pkg/api/errors"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
)

// Reconciler is a generic controller.
type Reconciler[T client.Object] interface {
	// NewObject returns a new instance of T.
	// Users should just return an empty object without any fields set.
	NewObject() T
	// Provision is called when reconciling objects.
	// This is only called when the object exists and was fetched successfully.
	Provision(ctx context.Context, obj T) (reconcile.Result, error)
	// Deprovision is called when the object has a deletion timestamp set.
	Deprovision(ctx context.Context, obj T) (reconcile.Result, error)
}

type controller[T client.Object] struct {
	kube       client.Client
	reconciler Reconciler[T]
}

// NewReconciler returns a new instance of Reconciler.
func NewReconciler[T client.Object](kube client.Client, reconciler Reconciler[T]) reconcile.Reconciler {
	return &controller[T]{
		kube:       kube,
		reconciler: reconciler,
	}
}

// Reconcile implements reconcile.Reconciler
func (ctrl *controller[T]) Reconcile(ctx context.Context, request ctrl.Request) (ctrl.Result, error) {
	obj := ctrl.reconciler.NewObject()
	err := ctrl.kube.Get(ctx, request.NamespacedName, obj)
	if err != nil && apierrors.IsNotFound(err) {
		// doesn't exist anymore, ignore.
		return reconcile.Result{}, nil
	}
	if err != nil {
		// some other error
		return reconcile.Result{}, err
	}
	if !obj.GetDeletionTimestamp().IsZero() {
		return ctrl.reconciler.Deprovision(ctx, obj)
	}
	return ctrl.reconciler.Provision(ctx, obj)
}
