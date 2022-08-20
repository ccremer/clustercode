package controllers

import (
	"context"
	"fmt"
	"path/filepath"
	"strconv"
	"time"

	"github.com/ccremer/clustercode/api/v1alpha1"
	"github.com/go-logr/logr"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	apierrors "k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/utils/pointer"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type (
	// JobReconciler reconciles Job objects
	JobReconciler struct {
		Client client.Client
		Log    logr.Logger
	}
	// JobContext holds the parameters of a single reconciliation
	JobContext struct {
		ctx     context.Context
		job     *batchv1.Job
		jobType ClusterCodeJobType
		task    *v1alpha1.Task
		log     logr.Logger
	}
)

// +kubebuilder:rbac:groups=batch,resources=jobs,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=core,resources=configmaps,verbs=get;list;watch;create;update;patch;delete

func (r *JobReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	rc := &JobContext{
		job: &batchv1.Job{},
		ctx: ctx,
	}
	err := r.Client.Get(ctx, req.NamespacedName, rc.job)
	if err != nil {
		if apierrors.IsNotFound(err) {
			r.Log.V(1).Info("object not found, ignoring reconcile", "object", req.NamespacedName)
			return ctrl.Result{}, nil
		}
		r.Log.Error(err, "could not retrieve object", "object", req.NamespacedName)
		return ctrl.Result{Requeue: true, RequeueAfter: time.Minute}, err
	}
	rc.log = r.Log.WithValues("job", req.NamespacedName)
	if rc.job.GetDeletionTimestamp() != nil {
		rc.log.V(1).Info("job is being deleted, ignoring reconcile")
		return ctrl.Result{}, nil
	}
	jobType, err := rc.getJobType()
	if err != nil {
		rc.log.V(1).Info("cannot determine job type, ignoring reconcile", "error", err.Error())
		return ctrl.Result{}, nil
	}
	conditions := castConditions(rc.job.Status.Conditions)
	if !meta.IsStatusConditionPresentAndEqual(conditions, string(batchv1.JobComplete), metav1.ConditionTrue) {
		rc.log.V(1).Info("job is not completed yet, ignoring reconcile")
		return ctrl.Result{}, nil
	}
	rc.jobType = jobType
	switch jobType {
	case ClustercodeTypeSplit:
		return ctrl.Result{}, r.handleSplitJob(rc)
	case ClustercodeTypeCount:
		rc.log.Info("reconciled count job")
	case ClustercodeTypeSlice:
		rc.log.Info("reconciling slice job")
		return ctrl.Result{}, r.handleSliceJob(rc)
	case ClustercodeTypeMerge:
		rc.log.Info("reconciling merge job")
		return ctrl.Result{}, r.handleMergeJob(rc)
	}
	return ctrl.Result{}, nil
}

func (r *JobReconciler) handleSplitJob(rc *JobContext) error {
	rc.task = &v1alpha1.Task{}
	if err := r.Client.Get(rc.ctx, getOwner(rc.job), rc.task); err != nil {
		return err
	}

	return r.createCountJob(rc)
}

func (r *JobReconciler) handleSliceJob(rc *JobContext) error {
	indexStr, found := rc.job.Labels[ClustercodeSliceIndexLabelKey]
	if !found {
		return fmt.Errorf("cannot determine slice index, missing label '%s'", ClustercodeSliceIndexLabelKey)
	}
	index, err := strconv.Atoi(indexStr)
	if err != nil {
		return fmt.Errorf("cannot determine slice index from label '%s': %w", ClustercodeSliceIndexLabelKey, err)
	}

	rc.task = &v1alpha1.Task{}
	if err := r.Client.Get(rc.ctx, getOwner(rc.job), rc.task); err != nil {
		return err
	}
	finished := rc.task.Status.SlicesFinished
	finished = append(finished, v1alpha1.ClustercodeSliceRef{
		SliceIndex: index,
		JobName:    rc.job.Name,
	})
	rc.task.Status.SlicesFinished = finished
	rc.task.Status.SlicesFinishedCount = len(finished)

	var scheduled []v1alpha1.ClustercodeSliceRef
	for _, ref := range rc.task.Status.SlicesScheduled {
		if ref.SliceIndex != index {
			scheduled = append(scheduled, ref)
		}
	}
	rc.task.Status.SlicesScheduled = scheduled
	return r.Client.Status().Update(rc.ctx, rc.task)
}

func (r *JobReconciler) createCountJob(rc *JobContext) error {

	taskId := rc.task.Spec.TaskId
	intermediateMountRoot := filepath.Join("/clustercode", IntermediateSubMountPath)
	job := &batchv1.Job{
		ObjectMeta: metav1.ObjectMeta{
			Name:      fmt.Sprintf("%.*s-%s", 62-len(ClustercodeTypeCount), taskId, ClustercodeTypeCount),
			Namespace: rc.job.Namespace,
			Labels:    labels.Merge(ClusterCodeLabels, labels.Merge(ClustercodeTypeCount.AsLabels(), taskId.AsLabels())),
		},
		Spec: batchv1.JobSpec{
			BackoffLimit: pointer.Int32Ptr(0),
			Template: corev1.PodTemplateSpec{
				Spec: corev1.PodSpec{
					ServiceAccountName: rc.job.Spec.Template.Spec.ServiceAccountName,
					RestartPolicy:      corev1.RestartPolicyNever,
					Containers: []corev1.Container{
						{
							Name:            "clustercode",
							Image:           DefaultClusterCodeContainerImage,
							ImagePullPolicy: corev1.PullIfNotPresent,
							Args: []string{
								"-d",
								"count",
								"--task-name=" + rc.task.Name,
								"--namespace=" + rc.job.Namespace,
							},
							VolumeMounts: []corev1.VolumeMount{
								{Name: IntermediateSubMountPath, MountPath: intermediateMountRoot, SubPath: rc.task.Spec.Storage.IntermediatePvc.SubPath},
							},
						},
					},
					Volumes: []corev1.Volume{
						{
							Name: IntermediateSubMountPath,
							VolumeSource: corev1.VolumeSource{
								PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
									ClaimName: rc.task.Spec.Storage.IntermediatePvc.ClaimName,
								},
							},
						},
					},
				},
			},
		},
	}
	if err := controllerutil.SetControllerReference(rc.task, job.GetObjectMeta(), r.Client.Scheme()); err != nil {
		rc.log.Info("could not set controller reference, deleting the task won't delete the job", "err", err.Error())
	}
	if err := r.Client.Create(rc.ctx, job); err != nil {
		if apierrors.IsAlreadyExists(err) {
			rc.log.Info("skip creating job, it already exists", "job", job.Name)
		} else {
			rc.log.Error(err, "could not create job", "job", job.Name)
			return err
		}
	} else {
		rc.log.Info("job created", "job", job.Name)
	}
	return nil
}

func (r *JobReconciler) handleMergeJob(rc *JobContext) error {
	rc.task = &v1alpha1.Task{}
	if err := r.Client.Get(rc.ctx, getOwner(rc.job), rc.task); err != nil {
		return err
	}

	return r.createCleanupJob(rc)
}

func (r *JobReconciler) createCleanupJob(rc *JobContext) error {

	taskId := rc.task.Spec.TaskId
	job := &batchv1.Job{
		ObjectMeta: metav1.ObjectMeta{
			Name:      fmt.Sprintf("%.*s-%s", 62-len(ClustercodeTypeCleanup), taskId, ClustercodeTypeCleanup),
			Namespace: rc.job.Namespace,
			Labels:    labels.Merge(ClusterCodeLabels, labels.Merge(ClustercodeTypeCleanup.AsLabels(), taskId.AsLabels())),
		},
		Spec: batchv1.JobSpec{
			BackoffLimit: pointer.Int32Ptr(0),
			Template: corev1.PodTemplateSpec{
				Spec: corev1.PodSpec{
					SecurityContext: &corev1.PodSecurityContext{
						RunAsUser:  pointer.Int64Ptr(1000),
						RunAsGroup: pointer.Int64Ptr(0),
						FSGroup:    pointer.Int64Ptr(0),
					},
					ServiceAccountName: rc.job.Spec.Template.Spec.ServiceAccountName,
					RestartPolicy:      corev1.RestartPolicyNever,
					Containers: []corev1.Container{
						{
							Name:            "clustercode",
							Image:           DefaultClusterCodeContainerImage,
							ImagePullPolicy: corev1.PullIfNotPresent,
							Args: []string{
								"-d",
								"--namespace=" + rc.job.Namespace,
								"cleanup",
								"--task-name=" + rc.task.Name,
							},
						},
					},
				},
			},
		},
	}
	addPvcVolume(job, SourceSubMountPath, filepath.Join("/clustercode", SourceSubMountPath), rc.task.Spec.Storage.SourcePvc)
	addPvcVolume(job, IntermediateSubMountPath, filepath.Join("/clustercode", IntermediateSubMountPath), rc.task.Spec.Storage.IntermediatePvc)
	if err := controllerutil.SetControllerReference(rc.task, job.GetObjectMeta(), r.Client.Scheme()); err != nil {
		rc.log.Info("could not set controller reference, deleting the task won't delete the job", "err", err.Error())
	}
	if err := r.Client.Create(rc.ctx, job); err != nil {
		if apierrors.IsAlreadyExists(err) {
			rc.log.Info("skip creating job, it already exists", "job", job.Name)
		} else {
			rc.log.Error(err, "could not create job", "job", job.Name)
			return err
		}
	} else {
		rc.log.Info("job created", "job", job.Name)
	}
	return nil
}

func (c JobContext) getJobType() (ClusterCodeJobType, error) {
	set := labels.Set(c.job.Labels)
	if !set.Has(ClustercodeTypeLabelKey) {
		return "", fmt.Errorf("missing label key '%s", ClustercodeTypeLabelKey)
	}
	label := set.Get(ClustercodeTypeLabelKey)
	for _, jobType := range ClustercodeTypes {
		if label == string(jobType) {
			return jobType, nil
		}
	}
	return "", fmt.Errorf("value of label '%s' unrecognized: %s", ClustercodeTypeLabelKey, label)
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
