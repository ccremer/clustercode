package blueprintcontroller

import (
	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/predicate"
)

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
