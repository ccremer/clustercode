package blueprintwebhook

import (
	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	ctrl "sigs.k8s.io/controller-runtime"
)

// +kubebuilder:webhook:verbs=create;update,path=/validate-clustercode-github-io-v1alpha1-blueprint,mutating=true,failurePolicy=fail,groups=clustercode.github.io,resources=blueprints,versions=v1alpha1,name=blueprints.clustercode.github.io,sideEffects=None,admissionReviewVersions=v1
// +kubebuilder:webhook:verbs=create;update,path=/mutate-clustercode-github-io-v1alpha1-blueprint,mutating=false,failurePolicy=fail,groups=clustercode.github.io,resources=blueprints,versions=v1alpha1,name=blueprints.clustercode.github.io,sideEffects=None,admissionReviewVersions=v1

// SetupWebhook adds a webhook for v1alpha1.Blueprint managed resources.
func SetupWebhook(mgr ctrl.Manager) error {
	return ctrl.NewWebhookManagedBy(mgr).
		For(&v1alpha1.Blueprint{}).
		WithValidator(&Validator{
			Log: mgr.GetLogger().WithName("blueprint.validator"),
		}).
		WithDefaulter(&Defaulter{
			Log: mgr.GetLogger().WithName("blueprint.defaulter"),
		}).
		Complete()
}
