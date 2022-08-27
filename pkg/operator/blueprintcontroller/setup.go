package blueprintcontroller

import (
	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"github.com/ccremer/clustercode/pkg/operator/reconciler"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/predicate"
)

// +kubebuilder:rbac:groups=clustercode.github.io,resources=blueprints,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=clustercode.github.io,resources=blueprints/status;blueprints/finalizers,verbs=get;update;patch
// +kubebuilder:rbac:groups=batch,resources=jobs,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=batch,resources=cronjobs,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=batch,resources=cronjobs/status,verbs=get;update;patch
// +kubebuilder:rbac:groups=core,resources=serviceaccounts,verbs=get;list;watch;create;delete
// +kubebuilder:rbac:groups=rbac.authorization.k8s.io,resources=roles;rolebindings,verbs=get;list;watch;create;delete

// SetupBlueprintController adds a controller that reconciles managed resources.
func SetupBlueprintController(mgr ctrl.Manager) error {
	name := "blueprint.clustercode.github.io"

	controller := reconciler.NewReconciler[*v1alpha1.Blueprint](mgr.GetClient(), &BlueprintProvisioner{
		Log:    mgr.GetLogger(),
		client: mgr.GetClient(),
	})

	return ctrl.NewControllerManagedBy(mgr).
		Named(name).
		For(&v1alpha1.Blueprint{}).
		WithEventFilter(predicate.GenerationChangedPredicate{}).
		Complete(controller)
}
