package taskcontroller

import (
	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"github.com/ccremer/clustercode/pkg/internal/types"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/builder"
	"sigs.k8s.io/controller-runtime/pkg/predicate"
)

// SetupTaskController adds a controller that reconciles managed resources.
func SetupTaskController(mgr ctrl.Manager) error {
	name := "task.clustercode.github.io"

	pred, err := predicate.LabelSelectorPredicate(metav1.LabelSelector{MatchLabels: types.ClusterCodeLabels})
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
