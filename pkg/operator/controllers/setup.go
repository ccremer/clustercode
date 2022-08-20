package controllers

import (
	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	batchv1 "k8s.io/api/batch/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/builder"
	"sigs.k8s.io/controller-runtime/pkg/predicate"
)

// +kubebuilder:rbac:groups=coordination.k8s.io,resources=leases,verbs=get;list;create;update

// SetupBlueprintController adds a controller that reconciles managed resources.
func SetupBlueprintController(mgr ctrl.Manager) error {
	name := "blueprint.clustercode.github.io"

	return ctrl.NewControllerManagedBy(mgr).
		Named(name).
		For(&v1alpha1.Blueprint{}).
		WithEventFilter(predicate.GenerationChangedPredicate{}).
		Complete(&BlueprintReconciler{
			Client: mgr.GetClient(),
			Log:    mgr.GetLogger(),
		})
}

// SetupJobController adds a controller that reconciles managed resources.
func SetupJobController(mgr ctrl.Manager) error {
	name := "job.clustercode.github.io"
	pred, err := predicate.LabelSelectorPredicate(metav1.LabelSelector{MatchLabels: ClusterCodeLabels})
	if err != nil {
		return err
	}
	return ctrl.NewControllerManagedBy(mgr).
		Named(name).
		For(&batchv1.Job{}, builder.WithPredicates(pred)).
		Complete(&JobReconciler{
			Client: mgr.GetClient(),
			Log:    mgr.GetLogger(),
		})
}

// SetupTaskController adds a controller that reconciles managed resources.
func SetupTaskController(mgr ctrl.Manager) error {
	name := "task.clustercode.github.io"

	pred, err := predicate.LabelSelectorPredicate(metav1.LabelSelector{MatchLabels: ClusterCodeLabels})
	if err != nil {
		return err
	}
	return ctrl.NewControllerManagedBy(mgr).
		Named(name).
		For(&v1alpha1.Task{}, builder.WithPredicates(pred)).
		//Owns(&batchv1.Job{}).
		//WithEventFilter(predicate.GenerationChangedPredicate{}).
		Complete(&TaskReconciler{
			Client: mgr.GetClient(),
			Log:    mgr.GetLogger(),
		})
}
