package webhook

import (
	"github.com/ccremer/clustercode/pkg/webhook/blueprintwebhook"
	ctrl "sigs.k8s.io/controller-runtime"
)

// SetupWebhooks creates all webhooks and adds them to the supplied manager.
func SetupWebhooks(mgr ctrl.Manager) error {
	/*
		Totally undocumented and hard-to-find feature is that the builder automatically registers the URL path for the webhook.
		What's more, not even the tests in upstream controller-runtime reveal what this path is _actually_ going to look like.
		So here's how the path is built (dots replaced with dash, lower-cased, single-form):
		 /validate-<group>-<version>-<kind>
		 /mutate-<group>-<version>-<kind>
		Example:
		 /validate-clustercode-github-io-v1alpha1-blueprint
		This path has to be given in the `//+kubebuilder:webhook:...` magic comment, see example:
		 +kubebuilder:webhook:verbs=create;update;delete,path=/validate-clustercode-github-io-v1alpha1-blueprint,mutating=false,failurePolicy=fail,groups=clustercode.github.io,resources=blueprints,versions=v1alpha1,name=blueprints.clustercode.github.io,sideEffects=None,admissionReviewVersions=v1
		Pay special attention to the plural forms and correct versions!
	*/
	for _, setup := range []func(ctrl.Manager) error{
		blueprintwebhook.SetupWebhook,
	} {
		if err := setup(mgr); err != nil {
			return err
		}
	}
	return nil
}
