package controllers

import (
	"context"

	"github.com/go-logr/logr"
	"k8s.io/apimachinery/pkg/runtime"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/predicate"

	"github.com/ccremer/clustercode/api/v1alpha1"
)

type (
	// ClustercodePlanReconciler reconciles ClustercodePlan objects
	ClustercodePlanReconciler struct {
		Client client.Client
		Log    logr.Logger
		Scheme *runtime.Scheme
	}
	// ReconciliationContext holds the parameters of a single reconciliation
	ReconciliationContext struct {
		ctx  context.Context
		plan *v1alpha1.ClustercodePlan
	}
)

func (r *ClustercodePlanReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&v1alpha1.ClustercodePlan{}).
		WithEventFilter(predicate.GenerationChangedPredicate{}).
		Complete(r)
}

// +kubebuilder:rbac:groups=clustercode.github.io,resources=encodingplans,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=clustercode.github.io,resources=encodingplans/status,verbs=get;update;patch
func (r *ClustercodePlanReconciler) Reconcile(ctx context.Context, req ctrl.Request) (result ctrl.Result, returnErr error) {
	rc := ReconciliationContext{
		ctx: ctx,
	}

	return ctrl.Result{}, nil
}
