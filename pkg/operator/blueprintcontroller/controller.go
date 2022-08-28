package blueprintcontroller

import (
	"context"
	"path/filepath"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"github.com/ccremer/clustercode/pkg/internal/pipe"
	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	pipeline "github.com/ccremer/go-command-pipeline"
	"github.com/go-logr/logr"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	rbacv1 "k8s.io/api/rbac/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/utils/pointer"
	"k8s.io/utils/strings"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
)

var ScanRoleName = "clustercode-edit"
var ScanRoleKind = "ClusterRole"
var DefaultClusterCodeContainerImage string

// BlueprintProvisioner reconciles Blueprint objects
type BlueprintProvisioner struct {
	client client.Client
	Log    logr.Logger
}

// BlueprintContext holds the parameters of a single reconciliation
type BlueprintContext struct {
	context.Context
	blueprint *v1alpha1.Blueprint
	log       logr.Logger
}

func (r *BlueprintProvisioner) NewObject() *v1alpha1.Blueprint {
	return &v1alpha1.Blueprint{}
}

func (r *BlueprintProvisioner) Provision(ctx context.Context, obj *v1alpha1.Blueprint) (reconcile.Result, error) {

	pctx := &BlueprintContext{
		blueprint: obj,
		Context:   ctx,
	}

	p := pipeline.NewPipeline[*BlueprintContext]().WithBeforeHooks(pipe.DebugLogger(pctx))
	p.WithSteps(
		p.NewStep("ensure service account", r.ensureServiceAccount),
		p.NewStep("ensure role binding", r.ensureRoleBinding),
		p.NewStep("ensure cron job", r.ensureCronJob),
	)
	return reconcile.Result{}, p.RunWithContext(pctx)
}

func (r *BlueprintProvisioner) Deprovision(_ context.Context, _ *v1alpha1.Blueprint) (reconcile.Result, error) {
	return reconcile.Result{}, nil
}

func (r *BlueprintProvisioner) ensureServiceAccount(ctx *BlueprintContext) error {
	sa := &corev1.ServiceAccount{ObjectMeta: metav1.ObjectMeta{
		Name:      ctx.blueprint.GetServiceAccountName(),
		Namespace: ctx.blueprint.Namespace,
	}}

	_, err := controllerutil.CreateOrUpdate(ctx, r.client, sa, func() error {
		sa.Labels = labels.Merge(sa.Labels, internaltypes.ClusterCodeLabels)
		return controllerutil.SetOwnerReference(ctx.blueprint, sa, r.client.Scheme())
	})
	return err
}

func (r *BlueprintProvisioner) ensureRoleBinding(ctx *BlueprintContext) error {
	saName := ctx.blueprint.GetServiceAccountName()
	roleBinding := &rbacv1.RoleBinding{ObjectMeta: metav1.ObjectMeta{
		Name:      strings.ShortenString(saName, 51) + "-rolebinding",
		Namespace: ctx.blueprint.Namespace,
	}}

	_, err := controllerutil.CreateOrUpdate(ctx, r.client, roleBinding, func() error {
		roleBinding.Labels = labels.Merge(roleBinding.Labels, internaltypes.ClusterCodeLabels)
		roleBinding.Subjects = []rbacv1.Subject{{
			Kind:      "ServiceAccount",
			Namespace: ctx.blueprint.Namespace,
			Name:      saName,
		}}
		// Don't change existing kind or role name if already existing
		kind := roleBinding.RoleRef.Kind
		if kind == "" {
			kind = ScanRoleKind
		}
		roleName := roleBinding.RoleRef.Name
		if roleName == "" {
			roleName = ScanRoleName
		}
		roleBinding.RoleRef = rbacv1.RoleRef{Kind: kind, Name: roleName, APIGroup: rbacv1.GroupName}
		return controllerutil.SetOwnerReference(ctx.blueprint, roleBinding, r.client.Scheme())
	})
	return err
}

func (r *BlueprintProvisioner) ensureCronJob(ctx *BlueprintContext) error {
	cronJob := &batchv1.CronJob{
		ObjectMeta: metav1.ObjectMeta{
			Name:      ctx.blueprint.Name + "-scan-job",
			Namespace: ctx.blueprint.Namespace,
		},
	}

	_, err := controllerutil.CreateOrUpdate(ctx, r.client, cronJob, func() error {
		cronJob.Labels = labels.Merge(cronJob.Labels, internaltypes.ClusterCodeLabels)
		cronJob.Spec.Suspend = pointer.Bool(ctx.blueprint.Spec.Suspend)
		cronJob.Spec.Schedule = ctx.blueprint.Spec.ScanSchedule
		cronJob.Spec.ConcurrencyPolicy = batchv1.ForbidConcurrent
		cronJob.Spec.SuccessfulJobsHistoryLimit = pointer.Int32Ptr(1)
		cronJob.Spec.FailedJobsHistoryLimit = pointer.Int32Ptr(1)

		cronJob.Spec.JobTemplate.Spec.BackoffLimit = pointer.Int32Ptr(0)
		cronJob.Spec.JobTemplate.Spec.Template.Labels = internaltypes.JobTypeScan.AsLabels()
		cronJob.Spec.JobTemplate.Spec.Template.Spec.ServiceAccountName = ctx.blueprint.GetServiceAccountName()
		cronJob.Spec.JobTemplate.Spec.Template.Spec.RestartPolicy = corev1.RestartPolicyNever
		if len(cronJob.Spec.JobTemplate.Spec.Template.Spec.Containers) == 0 {
			cronJob.Spec.JobTemplate.Spec.Template.Spec.Containers = []corev1.Container{{
				Name: "scanner",
				Env: []corev1.EnvVar{
					{
						Name:  "CC_LOG_DEBUG",
						Value: "true",
					},
				},
				Args: []string{
					"scan",
					"--namespace=" + ctx.blueprint.Namespace,
					"--blueprint-name=" + ctx.blueprint.Name,
				},
				Image:           DefaultClusterCodeContainerImage,
				ImagePullPolicy: corev1.PullIfNotPresent,
				VolumeMounts: []corev1.VolumeMount{
					{
						Name:      internaltypes.SourceSubMountPath,
						MountPath: filepath.Join("/clustercode", internaltypes.SourceSubMountPath),
						SubPath:   ctx.blueprint.Spec.Storage.SourcePvc.SubPath},
					{
						Name:      internaltypes.IntermediateSubMountPath,
						MountPath: filepath.Join("/clustercode", internaltypes.IntermediateSubMountPath),
						SubPath:   ctx.blueprint.Spec.Storage.SourcePvc.SubPath,
					},
				},
			}}
			cronJob.Spec.JobTemplate.Spec.Template.Spec.Volumes = []corev1.Volume{
				{
					Name: internaltypes.SourceSubMountPath,
					VolumeSource: corev1.VolumeSource{
						PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
							ClaimName: ctx.blueprint.Spec.Storage.SourcePvc.ClaimName}},
				},
				{
					Name: internaltypes.IntermediateSubMountPath,
					VolumeSource: corev1.VolumeSource{
						PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
							ClaimName: ctx.blueprint.Spec.Storage.IntermediatePvc.ClaimName}},
				},
			}
		}
		return controllerutil.SetOwnerReference(ctx.blueprint, cronJob, r.client.Scheme())
	})
	return err
}
