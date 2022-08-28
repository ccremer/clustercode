package jobcontroller

import (
	"context"
	"fmt"

	"github.com/ccremer/clustercode/pkg/api/conditions"
	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"github.com/ccremer/clustercode/pkg/internal/pipe"
	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	"github.com/ccremer/clustercode/pkg/internal/utils"
	pipeline "github.com/ccremer/go-command-pipeline"
	"github.com/go-logr/logr"
	batchv1 "k8s.io/api/batch/v1"
	"k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
)

type (
	// JobProvisioner reconciles Job objects
	JobProvisioner struct {
		Client client.Client
		Log    logr.Logger
	}
	// JobContext holds the parameters of a single reconciliation
	JobContext struct {
		context.Context
		resolver pipeline.DependencyResolver[*JobContext]

		job        *batchv1.Job
		jobType    internaltypes.ClusterCodeJobType
		task       *v1alpha1.Task
		log        logr.Logger
		sliceIndex int
	}
)

func (r *JobProvisioner) NewObject() *batchv1.Job {
	return &batchv1.Job{}
}

func (r *JobProvisioner) Provision(ctx context.Context, obj *batchv1.Job) (reconcile.Result, error) {
	pctx := &JobContext{job: obj, Context: ctx, resolver: pipeline.NewDependencyRecorder[*JobContext]()}

	if !r.isJobComplete(obj) {
		r.Log.V(1).Info("job is not completed yet, ignoring reconcile", "conditions", obj.Status.Conditions, "job", fmt.Sprintf("%s/%s", obj.Namespace, obj.Name))
		return reconcile.Result{}, nil
	}

	p := pipeline.NewPipeline[*JobContext]().WithBeforeHooks(pipe.DebugLogger[*JobContext](pctx), pctx.resolver.Record)
	p.WithSteps(
		p.NewStep("determine job type", r.getJobType),
		p.NewStep("fetch owning task", r.fetchTask),
		p.When(r.isJobType(internaltypes.JobTypeSplit), "update task status", r.updateStatusWithCondition(conditions.SplitComplete())),
		p.When(r.isJobType(internaltypes.JobTypeCount), "update task status", r.updateStatusWithCountComplete),
		p.When(r.isJobType(internaltypes.JobTypeMerge), "update task status", r.updateStatusWithCondition(conditions.MergeComplete())),
		p.When(r.isJobType(internaltypes.JobTypeCleanup), "update task status", r.updateStatusWithCondition(conditions.CleanupComplete())),
		p.WithNestedSteps("reconcile slice job", r.isJobType(internaltypes.JobTypeSlice),
			p.NewStep("determine slice index", r.determineSliceIndex),
			p.NewStep("update task status", r.updateStatusWithSlicesFinished),
		),
	)
	err := p.RunWithContext(pctx)
	return reconcile.Result{}, err
}

func (r *JobProvisioner) Deprovision(_ context.Context, _ *batchv1.Job) (reconcile.Result, error) {
	r.Log.V(1).Info("job is being deleted, ignoring reconcile")
	return reconcile.Result{}, nil
}

func (r *JobProvisioner) isJobComplete(job *batchv1.Job) bool {
	conditions := castConditions(job.Status.Conditions)
	return meta.IsStatusConditionPresentAndEqual(conditions, string(batchv1.JobComplete), metav1.ConditionTrue)
}

func (r *JobProvisioner) isJobType(jobType internaltypes.ClusterCodeJobType) func(ctx *JobContext) bool {
	return func(ctx *JobContext) bool {
		return ctx.jobType == jobType
	}
}

func (r *JobProvisioner) getJobType(ctx *JobContext) error {
	jobType, err := getJobType(ctx.job)
	ctx.jobType = jobType
	return err
}

func (r *JobProvisioner) fetchTask(ctx *JobContext) error {
	ctx.task = &v1alpha1.Task{}
	err := r.Client.Get(ctx, utils.GetOwner(ctx.job), ctx.task)
	return err
}

func getJobType(job *batchv1.Job) (internaltypes.ClusterCodeJobType, error) {
	set := labels.Set(job.Labels)
	if !set.Has(internaltypes.ClustercodeTypeLabelKey) {
		return "", fmt.Errorf("missing label key '%s", internaltypes.ClustercodeTypeLabelKey)
	}
	label := set.Get(internaltypes.ClustercodeTypeLabelKey)
	for _, jobType := range internaltypes.JobTypes {
		if label == string(jobType) {
			return jobType, nil
		}
	}
	return "", fmt.Errorf("value of label '%s' unrecognized: %s", internaltypes.ClustercodeTypeLabelKey, label)
}

func castConditions(conditions []batchv1.JobCondition) (converted []metav1.Condition) {
	for _, c := range conditions {
		converted = append(converted, metav1.Condition{
			Type:               string(c.Type),
			Status:             metav1.ConditionStatus(c.Status),
			LastTransitionTime: c.LastTransitionTime,
			Reason:             c.Reason,
			Message:            c.Message,
		})
	}
	return converted
}
