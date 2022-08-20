package jobcontroller

import (
	"github.com/ccremer/clustercode/pkg/internal/types"
	batchv1 "k8s.io/api/batch/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/builder"
	"sigs.k8s.io/controller-runtime/pkg/predicate"
)

// SetupJobController adds a controller that reconciles managed resources.
func SetupJobController(mgr ctrl.Manager) error {
	name := "job.clustercode.github.io"
	pred, err := predicate.LabelSelectorPredicate(metav1.LabelSelector{MatchLabels: types.ClusterCodeLabels})
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
