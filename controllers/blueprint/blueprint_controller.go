package blueprint

import (
	"context"
	"time"

	"github.com/go-logr/logr"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/predicate"

	"github.com/ccremer/clustercode/api/v1alpha1"
	"github.com/ccremer/clustercode/controllers/pipeline"
)

type (
	// BlueprintReconciler reconciles Blueprint objects
	BlueprintReconciler struct {
		Client client.Client
		Log    logr.Logger
		Scheme *runtime.Scheme
	}
	// ReconciliationContext holds the parameters of a single reconciliation
	ReconciliationContext struct {
		ctx            context.Context
		blueprint      *v1alpha1.Blueprint
		serviceAccount *corev1.ServiceAccount
		Log            logr.Logger
		Scheme         *runtime.Scheme
		Client         client.Client
	}
)

func (r *BlueprintReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&v1alpha1.Blueprint{}).
		WithEventFilter(predicate.GenerationChangedPredicate{}).
		Complete(r)
}

// +kubebuilder:rbac:groups=clustercode.github.io,resources=blueprints,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=clustercode.github.io,resources=blueprints/status,verbs=get;update;patch
// +kubebuilder:rbac:groups=batch,resources=jobs,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=batch,resources=cronjobs,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=batch,resources=cronjobs/status,verbs=get;update;patch
// +kubebuilder:rbac:groups=core,resources=serviceaccounts,verbs=get;list;create;delete
// +kubebuilder:rbac:groups=rbac.authorization.k8s.io,resources=roles;rolebindings,verbs=get;list;create;delete

func (r *BlueprintReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	rc := &ReconciliationContext{
		ctx: ctx,
		blueprint: &v1alpha1.Blueprint{
			ObjectMeta: metav1.ObjectMeta{
				Name:      req.Name,
				Namespace: req.Namespace,
			},
		},
		Log:    r.Log.WithValues("blueprint", req.NamespacedName),
		Client: r.Client,
		Scheme: r.Scheme,
	}

	resourceAction := &pipeline.ResourceAction{
		Log:     rc.Log,
		Context: ctx,
		Client:  r.Client,
		Scheme:  r.Scheme,
	}
	rbacAction := NewRbacAction(resourceAction)

	result := pipeline.NewPipeline(rc.Log).
		WithAbortHandler(pipeline.UpdateStatusHandler{
			Log:     rc.Log,
			Object:  rc.blueprint,
			Context: rc.ctx,
			Client:  rc.Client,
		}.UpdateStatus).
		WithSteps(
			pipeline.NewStep("get reconcile object", resourceAction.GetOrAbort(rc.blueprint)),
			pipeline.NewStep("abort if deleted", pipeline.AbortIfDeleted(rc.blueprint)),
			pipeline.NewStep("create service account", rbacAction.CreateServiceAccount(rc)),
			pipeline.NewStep("create role binding", rbacAction.CreateRoleBinding(rc)),
			pipeline.NewStep("create cronjob", CreateCronJob(rc)),
		).Run()
	if result.Requeue {
		return ctrl.Result{RequeueAfter: 1 * time.Minute}, result.Err
	}
	return ctrl.Result{}, nil
}
