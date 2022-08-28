package jobcontroller

import (
	"github.com/ccremer/clustercode/pkg/internal/types"
	"github.com/ccremer/clustercode/pkg/operator/reconciler"
	batchv1 "k8s.io/api/batch/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/builder"
	"sigs.k8s.io/controller-runtime/pkg/predicate"
)

// +kubebuilder:rbac:groups=batch,resources=jobs,verbs=get;list;watch;create;update;patch;delete;deletecollection
// +kubebuilder:rbac:groups=core,resources=configmaps,verbs=get;list;watch;create;update;patch;delete

// SetupJobController adds a controller that reconciles managed resources.
func SetupJobController(mgr ctrl.Manager) error {
	name := "job.clustercode.github.io"
	pred, err := predicate.LabelSelectorPredicate(metav1.LabelSelector{MatchLabels: types.ClusterCodeLabels})
	if err != nil {
		return err
	}
	controller := reconciler.NewReconciler[*batchv1.Job](mgr.GetClient(), &JobProvisioner{Client: mgr.GetClient(), Log: mgr.GetLogger().WithName("job")})
	return ctrl.NewControllerManagedBy(mgr).
		Named(name).
		For(&batchv1.Job{}, builder.WithPredicates(pred)).
		Complete(controller)
}
