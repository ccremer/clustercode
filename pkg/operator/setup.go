package operator

import (
	"github.com/ccremer/clustercode/pkg/operator/blueprintcontroller"
	"github.com/ccremer/clustercode/pkg/operator/jobcontroller"
	"github.com/ccremer/clustercode/pkg/operator/taskcontroller"
	ctrl "sigs.k8s.io/controller-runtime"
)

// +kubebuilder:rbac:groups=coordination.k8s.io,resources=leases,verbs=get;list;create;update

// SetupControllers creates all controllers and adds them to the supplied manager.
func SetupControllers(mgr ctrl.Manager) error {
	for _, setup := range []func(ctrl.Manager) error{
		blueprintcontroller.SetupBlueprintController,
		jobcontroller.SetupJobController,
		taskcontroller.SetupTaskController,
	} {
		if err := setup(mgr); err != nil {
			return err
		}
	}
	return nil
}

// SetupWebhooks creates all webhooks and adds them to the supplied manager.
func SetupWebhooks(mgr ctrl.Manager) error {
	for _, setup := range []func(ctrl.Manager) error{} {
		if err := setup(mgr); err != nil {
			return err
		}
	}
	return nil
}
