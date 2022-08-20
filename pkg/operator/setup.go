package operator

import (
	"github.com/ccremer/clustercode/pkg/operator/controllers"
	ctrl "sigs.k8s.io/controller-runtime"
)

// SetupControllers creates all controllers and adds them to the supplied manager.
func SetupControllers(mgr ctrl.Manager) error {
	for _, setup := range []func(ctrl.Manager) error{
		controllers.SetupBlueprintController,
		controllers.SetupJobController,
		controllers.SetupTaskController,
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
