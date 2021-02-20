package blueprint

import (
	"path/filepath"

	batchv1 "k8s.io/api/batch/v1"
	"k8s.io/api/batch/v1beta1"
	corev1 "k8s.io/api/core/v1"
	rbacv1 "k8s.io/api/rbac/v1"
	"k8s.io/utils/pointer"
	"k8s.io/utils/strings"

	"github.com/ccremer/clustercode/builder"
	"github.com/ccremer/clustercode/cfg"
	"github.com/ccremer/clustercode/controllers"
	"github.com/ccremer/clustercode/controllers/pipeline"
)

type RbacAction struct {
	*pipeline.ResourceAction
}

func NewRbacAction(action *pipeline.ResourceAction) RbacAction {
	return RbacAction{
		ResourceAction: action,
	}
}

func (a RbacAction) CreateServiceAccount(rc *ReconciliationContext) pipeline.ActionFunc {
	rc.serviceAccount = a.newServiceAccount(rc)
	return a.CreateIfNotExisting(rc.serviceAccount)
}

func (a RbacAction) CreateRoleBinding(rc *ReconciliationContext) pipeline.ActionFunc {
	binding := a.newRoleBinding(rc)
	return a.CreateIfNotExisting(binding)
}

func (a *RbacAction) newServiceAccount(rc *ReconciliationContext) *corev1.ServiceAccount {
	saName := rc.blueprint.GetServiceAccountName()
	account := &corev1.ServiceAccount{}
	builder.NewMetaBuilderWith(account).
		WithName(saName).
		WithNamespace(rc.blueprint.Namespace).
		WithLabels(controllers.ClusterCodeLabels).
		WithControllerReference(rc.blueprint, a.ResourceAction.Scheme)
	return account
}

func (a *RbacAction) newRoleBinding(rc *ReconciliationContext) *rbacv1.RoleBinding {
	saName := rc.blueprint.GetServiceAccountName()
	roleBinding := &rbacv1.RoleBinding{
		Subjects: []rbacv1.Subject{
			{
				Kind:      "ServiceAccount",
				Namespace: rc.blueprint.Namespace,
				Name:      saName,
			},
		},
		RoleRef: rbacv1.RoleRef{
			Kind:     cfg.Config.Scan.RoleKind,
			Name:     cfg.Config.Scan.RoleName,
			APIGroup: rbacv1.GroupName,
		},
	}
	builder.NewMetaBuilderWith(roleBinding).
		WithName(strings.ShortenString(saName, 51)+"-rolebinding").
		WithNamespace(rc.blueprint.Namespace).
		WithLabels(controllers.ClusterCodeLabels).
		WithControllerReference(rc.blueprint, a.ResourceAction.Scheme)
	return roleBinding
}

func CreateCronJob(rc *ReconciliationContext) pipeline.ActionFunc {
	return func() pipeline.Result {
		ctb := builder.NewContainerBuilder("scanner").
			WithImage(cfg.Config.Operator.ClustercodeContainerImage).
			AddEnvVarValue("CC_LOG__DEBUG", "true").
			AddArg("scan").
			AddArg("--namespace=%s", rc.blueprint.Namespace).
			AddArg("--scan.blueprint-name=%s", rc.blueprint.Name)
		psb := builder.NewPodSpecBuilder(ctb).
			AddPvcMount(nil,
				rc.blueprint.Spec.Storage.SourcePvc.ClaimName,
				controllers.SourceSubMountPath,
				filepath.Join("/clustercode", controllers.SourceSubMountPath),
				rc.blueprint.Spec.Storage.SourcePvc.SubPath).
			AddPvcMount(nil,
				rc.blueprint.Spec.Storage.IntermediatePvc.ClaimName,
				controllers.IntermediateSubMountPath,
				filepath.Join("/clustercode", controllers.IntermediateSubMountPath),
				rc.blueprint.Spec.Storage.IntermediatePvc.SubPath).
			Build()

		psb.PodSpec.ServiceAccountName = rc.serviceAccount.Name
		psb.PodSpec.RestartPolicy = corev1.RestartPolicyNever

		cronJob := &v1beta1.CronJob{
			Spec: v1beta1.CronJobSpec{
				Schedule:          rc.blueprint.Spec.ScanSchedule,
				ConcurrencyPolicy: v1beta1.ForbidConcurrent,
				Suspend:           &rc.blueprint.Spec.Suspend,

				JobTemplate: v1beta1.JobTemplateSpec{
					Spec: batchv1.JobSpec{
						BackoffLimit: pointer.Int32Ptr(0),
						Template: corev1.PodTemplateSpec{
							Spec: *psb.PodSpec,
						},
					},
				},
				SuccessfulJobsHistoryLimit: pointer.Int32Ptr(1),
				FailedJobsHistoryLimit:     pointer.Int32Ptr(1),
			},
		}
		builder.NewMetaBuilderWith(cronJob).
			WithLabels(controllers.ClusterCodeLabels, controllers.ClustercodeTypeScan.AsLabels()).
			WithName(rc.blueprint.Name+"-scan-job").
			WithNamespace(rc.blueprint.Namespace).
			WithControllerReference(rc.blueprint, rc.Scheme).
			Build()

		if err := controllers.UpsertResource(rc.ctx, cronJob, rc.Client, rc.Log); err != nil {
			return pipeline.Result{Err: err, Requeue: true}
		}
		return pipeline.Result{}
	}
}
