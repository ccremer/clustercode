package taskcontroller

import (
	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"github.com/ccremer/clustercode/pkg/internal/types"
	"github.com/ccremer/clustercode/pkg/operator/reconciler"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/builder"
	"sigs.k8s.io/controller-runtime/pkg/predicate"
)

// +kubebuilder:rbac:groups=clustercode.github.io,resources=tasks,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=clustercode.github.io,resources=tasks/status;tasks/finalizers,verbs=get;update;patch

// SetupTaskController adds a controller that reconciles managed resources.
func SetupTaskController(mgr ctrl.Manager) error {
	name := "task.clustercode.github.io"

	controller := reconciler.NewReconciler[*v1alpha1.Task](mgr.GetClient(), &TaskReconciler{
		Client: mgr.GetClient(),
		Log:    mgr.GetLogger(),
	})

	pred, err := predicate.LabelSelectorPredicate(metav1.LabelSelector{MatchLabels: types.ClusterCodeLabels})
	if err != nil {
		return err
	}
	return ctrl.NewControllerManagedBy(mgr).
		Named(name).
		For(&v1alpha1.Task{}, builder.WithPredicates(pred)).
		//Owns(&batchv1.Job{}).
		//WithEventFilter(predicate.GenerationChangedPredicate{}).
		Complete(controller)
}
