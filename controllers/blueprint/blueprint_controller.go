package blueprint

import (
	"context"
	"time"

	"github.com/go-logr/logr"
	corev1 "k8s.io/api/core/v1"
	apierrors "k8s.io/apimachinery/pkg/api/errors"
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
	// BlueprintContext holds the parameters of a single reconciliation
	BlueprintContext struct {
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
	rc := BlueprintContext{
		ctx:       ctx,
		blueprint: &v1alpha1.Blueprint{},
		Log:       r.Log.WithValues("blueprint", req.NamespacedName),
		Client:    r.Client,
		Scheme:    r.Scheme,
	}

	err := r.Client.Get(ctx, req.NamespacedName, rc.blueprint)
	if err != nil {
		if apierrors.IsNotFound(err) {
			r.Log.Info("object not found, ignoring reconcile", "object", req.NamespacedName)
			return ctrl.Result{}, nil
		}
		r.Log.Error(err, "could not retrieve object", "object", req.NamespacedName)
		return ctrl.Result{Requeue: true, RequeueAfter: time.Minute}, err
	}

	result := pipeline.NewPipeline(rc.Log).
		WithAbortHandler(pipeline.UpdateStatusHandler{
			Log:     rc.Log,
			Object:  rc.blueprint,
			Context: rc.ctx,
			Client:  rc.Client,
		}.UpdateStatus).
		AddStep(pipeline.NewStep("abort when deleted", pipeline.AbortWhenDeletedAction{Object: rc.blueprint}.Execute)).
		AddStep(pipeline.NewStep("create service account", RbacAction{&rc}.CreateServiceAccount)).
		AddStep(pipeline.NewStep("create role binding", RbacAction{&rc}.CreateRoleBinding)).
		AddStep(pipeline.NewStep("create cronjob", CreateCronJobAction{&rc}.Execute)).
		Run()
	if result.Requeue {
		return ctrl.Result{RequeueAfter: 1 * time.Minute}, result.Err
	}
	return ctrl.Result{}, nil
}
