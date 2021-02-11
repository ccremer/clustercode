package blueprint

import (
	"path/filepath"

	batchv1 "k8s.io/api/batch/v1"
	"k8s.io/api/batch/v1beta1"
	corev1 "k8s.io/api/core/v1"
	rbacv1 "k8s.io/api/rbac/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/utils/pointer"
	"k8s.io/utils/strings"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"

	"github.com/ccremer/clustercode/cfg"
	"github.com/ccremer/clustercode/controllers"
	"github.com/ccremer/clustercode/controllers/pipeline"
)

type RbacAction struct {
	*BlueprintContext
}

func (a RbacAction) CreateServiceAccount() pipeline.Result {
	sa := a.newServiceAccount()

	if err := controllers.UpsertResource(a.ctx, &sa, a.Client, a.Log); err != nil {
		return pipeline.Result{Err: err}
	}
	a.Log.Info("service account created", "sa", sa.Name)
	a.serviceAccount = &sa
	return pipeline.Result{}
}

func (a RbacAction) CreateRoleBinding() pipeline.Result {
	binding := a.newRoleBinding()

	if err := controllers.UpsertResource(a.ctx, &binding, a.Client, a.Log); err != nil {
		return pipeline.Result{Err: err}
	}
	a.Log.Info("role binding created", "roleBinding", binding.Name)
	return pipeline.Result{}
}

func (a *RbacAction) newServiceAccount() corev1.ServiceAccount {
	saName := a.blueprint.GetServiceAccountName()
	account := corev1.ServiceAccount{
		ObjectMeta: metav1.ObjectMeta{
			Name:      saName,
			Namespace: a.blueprint.Namespace,
			Labels:    controllers.ClusterCodeLabels,
		},
	}
	if err := controllerutil.SetControllerReference(a.blueprint, account.GetObjectMeta(), a.Scheme); err != nil {
		a.Log.Error(err, "could not set controller reference on service account", "sa", account.Name)
	}
	return account
}

func (a *RbacAction) newRoleBinding() rbacv1.RoleBinding {
	saName := a.blueprint.GetServiceAccountName()
	roleBinding := rbacv1.RoleBinding{
		ObjectMeta: metav1.ObjectMeta{
			Name:      strings.ShortenString(saName, 51) + "-rolebinding",
			Namespace: a.blueprint.Namespace,
			Labels:    controllers.ClusterCodeLabels,
		},
		Subjects: []rbacv1.Subject{
			{
				Kind:      "ServiceAccount",
				Namespace: a.blueprint.Namespace,
				Name:      saName,
			},
		},
		RoleRef: rbacv1.RoleRef{
			Kind:     cfg.Config.Scan.RoleKind,
			Name:     cfg.Config.Scan.RoleName,
			APIGroup: rbacv1.GroupName,
		},
	}
	if err := controllerutil.SetControllerReference(a.blueprint, roleBinding.GetObjectMeta(), a.Scheme); err != nil {
		a.Log.Error(err, "could not set controller reference on role", "roleBinding", roleBinding.Name)
	}
	return roleBinding
}

type CreateCronJobAction struct {
	*BlueprintContext
}

func (a CreateCronJobAction) Execute() pipeline.Result {

	cronJob := &v1beta1.CronJob{
		ObjectMeta: metav1.ObjectMeta{
			Name:      a.blueprint.Name + "-scan-job",
			Namespace: a.blueprint.Namespace,
			Labels:    labels.Merge(controllers.ClusterCodeLabels, controllers.ClustercodeTypeScan.AsLabels()),
		},
		Spec: v1beta1.CronJobSpec{
			Schedule:          a.blueprint.Spec.ScanSchedule,
			ConcurrencyPolicy: v1beta1.ForbidConcurrent,
			Suspend:           &a.blueprint.Spec.Suspend,

			JobTemplate: v1beta1.JobTemplateSpec{
				Spec: batchv1.JobSpec{
					BackoffLimit: pointer.Int32Ptr(0),
					Template: corev1.PodTemplateSpec{
						Spec: corev1.PodSpec{
							ServiceAccountName: a.serviceAccount.Name,
							RestartPolicy:      corev1.RestartPolicyNever,
							Containers: []corev1.Container{
								{
									Name: "scanner",
									Env: []corev1.EnvVar{
										{
											Name:  "CC_LOG__DEBUG",
											Value: "true",
										},
									},
									Args: []string{
										"scan",
										"--namespace=" + a.blueprint.Namespace,
										"--scan.blueprint-name=" + a.blueprint.Name,
									},
									Image: cfg.Config.Operator.ClustercodeContainerImage,
									VolumeMounts: []corev1.VolumeMount{
										{
											Name:      controllers.SourceSubMountPath,
											MountPath: filepath.Join("/clustercode", controllers.SourceSubMountPath),
											SubPath:   a.blueprint.Spec.Storage.SourcePvc.SubPath,
										},
										{
											Name:      controllers.IntermediateSubMountPath,
											MountPath: filepath.Join("/clustercode", controllers.IntermediateSubMountPath),
											SubPath:   a.blueprint.Spec.Storage.SourcePvc.SubPath,
										},
									},
								},
							},
							Volumes: []corev1.Volume{
								{
									Name: controllers.SourceSubMountPath,
									VolumeSource: corev1.VolumeSource{
										PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
											ClaimName: a.blueprint.Spec.Storage.SourcePvc.ClaimName,
										},
									},
								},
								{
									Name: controllers.IntermediateSubMountPath,
									VolumeSource: corev1.VolumeSource{
										PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
											ClaimName: a.blueprint.Spec.Storage.IntermediatePvc.ClaimName,
										},
									},
								},
							},
						},
					},
				},
			},
			SuccessfulJobsHistoryLimit: pointer.Int32Ptr(1),
			FailedJobsHistoryLimit:     pointer.Int32Ptr(1),
		},
	}
	if err := controllerutil.SetControllerReference(a.blueprint, cronJob.GetObjectMeta(), a.Scheme); err != nil {
		a.Log.Error(err, "could not set controller reference, deleting the blueprint will not delete the cronjob", "cronjob", cronJob.Name)
	}

	if err := controllers.UpsertResource(a.ctx, cronJob, a.Client, a.Log); err != nil {
		return pipeline.Result{Err: err, Requeue: true}
	}
	return pipeline.Result{}
}
