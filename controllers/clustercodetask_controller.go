package controllers

import (
	"context"
	"time"

	"github.com/go-logr/logr"
	apierrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/builder"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/predicate"

	"github.com/ccremer/clustercode/api/v1alpha1"
)

type (
	// ClustercodeTaskReconciler reconciles ClustercodeTask objects
	ClustercodeTaskReconciler struct {
		Client client.Client
		Log    logr.Logger
		Scheme *runtime.Scheme
	}
	// ClustercodeTaskContext holds the parameters of a single reconciliation
	ClustercodeTaskContext struct {
		ctx  context.Context
		task *v1alpha1.ClustercodeTask
		log  logr.Logger
	}
)

func (r *ClustercodeTaskReconciler) SetupWithManager(mgr ctrl.Manager) error {
	pred, err := predicate.LabelSelectorPredicate(metav1.LabelSelector{MatchLabels: ClusterCodeLabels})
	if err != nil {
		return err
	}
	return ctrl.NewControllerManagedBy(mgr).
		For(&v1alpha1.ClustercodeTask{}, builder.WithPredicates(pred)).
		WithEventFilter(predicate.GenerationChangedPredicate{}).
		Complete(r)
}

// +kubebuilder:rbac:groups=clustercode.github.io,resources=clustercodetasks,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=clustercode.github.io,resources=clustercodetasks/status,verbs=get;update;patch

func (r *ClustercodeTaskReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	rc := &ClustercodeTaskContext{
		ctx:  ctx,
		task: &v1alpha1.ClustercodeTask{},
	}
	err := r.Client.Get(ctx, req.NamespacedName, rc.task)
	if err != nil {
		if apierrors.IsNotFound(err) {
			r.Log.Info("object not found, ignoring reconcile", "object", req.NamespacedName)
			return ctrl.Result{}, nil
		}
		r.Log.Error(err, "could not retrieve object", "object", req.NamespacedName)
		return ctrl.Result{Requeue: true, RequeueAfter: time.Minute}, err
	}
	rc.log = r.Log.WithValues("task", req.NamespacedName)
	//r.handleTask(rc)
	rc.log.Info("reconciled task")
	return ctrl.Result{}, nil
}
