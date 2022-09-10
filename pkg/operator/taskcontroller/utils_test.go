package taskcontroller

import (
	"testing"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"github.com/ccremer/clustercode/pkg/internal/types"
	"github.com/stretchr/testify/assert"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/resource"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/utils/pointer"
)

func Test_createClustercodeJobDefinition(t *testing.T) {
	tests := map[string]struct {
		givenJob    *batchv1.Job
		givenTask   *v1alpha1.Task
		expectedJob *batchv1.Job
	}{
		"GivenNoTemplate_ThenExpectDefaultJobSpec": {
			givenJob: &batchv1.Job{},
			givenTask: &v1alpha1.Task{
				Spec: v1alpha1.TaskSpec{
					Encode: v1alpha1.EncodeSpec{},
					Storage: v1alpha1.StorageSpec{
						SourcePvc: v1alpha1.ClusterCodeVolumeRef{
							ClaimName: "source-claim"}}}},
			expectedJob: &batchv1.Job{
				Spec: batchv1.JobSpec{
					BackoffLimit: pointer.Int32(0),
					Template: corev1.PodTemplateSpec{
						Spec: corev1.PodSpec{
							InitContainers: []corev1.Container{},
							RestartPolicy:  corev1.RestartPolicyNever,
							Containers: []corev1.Container{{
								Name:  "clustercode",
								Image: "image:tag",
								Args:  []string{"arg1"},
								VolumeMounts: []corev1.VolumeMount{{
									Name:      "source",
									MountPath: "/clustercode/source",
								}}}},
							Volumes: []corev1.Volume{{
								Name: "source",
								VolumeSource: corev1.VolumeSource{
									PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
										ClaimName: "source-claim"}}}},
						},
					},
				},
			},
		},
		"GivenPodTemplate_WhenTemplateIsAdditive_ThenExpectDefaultJobSpecWithAdditions": {
			givenJob: &batchv1.Job{},
			givenTask: &v1alpha1.Task{
				Spec: v1alpha1.TaskSpec{
					Encode: v1alpha1.EncodeSpec{
						PodTemplate: v1alpha1.PodTemplate{
							Volumes: []corev1.Volume{{
								Name: "extra",
								VolumeSource: corev1.VolumeSource{
									HostPath: &corev1.HostPathVolumeSource{Path: "/path"}}}},
							Containers: []v1alpha1.ContainerTemplate{{
								Name:    "sidecar",
								Image:   "registry.com/repo/image:tag",
								Command: []string{"command"},
								Args:    []string{"args"},
								EnvFrom: []corev1.EnvFromSource{{
									ConfigMapRef: &corev1.ConfigMapEnvSource{LocalObjectReference: corev1.LocalObjectReference{Name: "configmap"}}}},
								Env: []corev1.EnvVar{{
									Name: "ENV_VAR", Value: "value"}},
								ImagePullPolicy: "",
								VolumeMounts: []corev1.VolumeMount{{
									Name:      "extraVolume",
									MountPath: "/extra/mount"}},
								Resources: corev1.ResourceRequirements{
									Requests: map[corev1.ResourceName]resource.Quantity{
										"memory": resource.MustParse("1G")}},
								SecurityContext: &corev1.SecurityContext{
									RunAsUser: pointer.Int64(33)}}},
						},
					},
					Storage: v1alpha1.StorageSpec{
						SourcePvc: v1alpha1.ClusterCodeVolumeRef{
							ClaimName: "source-claim"}}}},
			expectedJob: &batchv1.Job{
				Spec: batchv1.JobSpec{
					BackoffLimit: pointer.Int32(0),
					Template: corev1.PodTemplateSpec{
						Spec: corev1.PodSpec{
							InitContainers: []corev1.Container{},
							RestartPolicy:  corev1.RestartPolicyNever,
							Containers: []corev1.Container{
								{
									Name:    "sidecar",
									Image:   "registry.com/repo/image:tag",
									Command: []string{"command"},
									Args:    []string{"args"},
									EnvFrom: []corev1.EnvFromSource{{
										ConfigMapRef: &corev1.ConfigMapEnvSource{LocalObjectReference: corev1.LocalObjectReference{Name: "configmap"}}}},
									Env: []corev1.EnvVar{{
										Name: "ENV_VAR", Value: "value"}},
									ImagePullPolicy: "",
									VolumeMounts: []corev1.VolumeMount{{
										Name:      "extraVolume",
										MountPath: "/extra/mount"}},
									Resources: corev1.ResourceRequirements{
										Requests: map[corev1.ResourceName]resource.Quantity{
											"memory": resource.MustParse("1G")}},
									SecurityContext: &corev1.SecurityContext{
										RunAsUser: pointer.Int64(33)},
								},
								{
									Name:  "clustercode",
									Args:  []string{"arg1"},
									Image: "image:tag",
									VolumeMounts: []corev1.VolumeMount{{
										Name:      "source",
										MountPath: "/clustercode/source"}},
								},
							},
							Volumes: []corev1.Volume{
								{
									Name: "extra",
									VolumeSource: corev1.VolumeSource{
										HostPath: &corev1.HostPathVolumeSource{Path: "/path"}},
								},
								{
									Name: "source",
									VolumeSource: corev1.VolumeSource{
										PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
											ClaimName: "source-claim"}},
								},
							},
						},
					},
				}},
		},
		"GivenPodTemplate_WhenTemplateIsOverwriting_ThenExpectDefaultJobSpecWithMergedProperties": {
			givenJob: &batchv1.Job{},
			givenTask: &v1alpha1.Task{
				Spec: v1alpha1.TaskSpec{
					Encode: v1alpha1.EncodeSpec{
						PodTemplate: v1alpha1.PodTemplate{
							Metadata: &v1alpha1.PodMetadata{
								Labels: map[string]string{"key": "value"}},
							Volumes: []corev1.Volume{{
								Name: "source",
								VolumeSource: corev1.VolumeSource{
									HostPath: &corev1.HostPathVolumeSource{Path: "/path"}}}},
							Containers: []v1alpha1.ContainerTemplate{{
								Name:            "clustercode",
								Image:           "registry.com/repo/image:tag", // custom image won't be set
								ImagePullPolicy: corev1.PullAlways,
								Env: []corev1.EnvVar{{ // let's add an env var
									Name: "ENV_VAR", Value: "value"}},
								SecurityContext: &corev1.SecurityContext{
									RunAsUser: pointer.Int64(33)},
							}}},
					}}},
			expectedJob: &batchv1.Job{
				Spec: batchv1.JobSpec{
					BackoffLimit: pointer.Int32(0),
					Template: corev1.PodTemplateSpec{
						ObjectMeta: metav1.ObjectMeta{
							Labels: map[string]string{"key": "value"}},
						Spec: corev1.PodSpec{
							InitContainers: []corev1.Container{},
							RestartPolicy:  corev1.RestartPolicyNever,
							Containers: []corev1.Container{{
								Name:            "clustercode",
								Image:           "image:tag",
								ImagePullPolicy: corev1.PullAlways,
								Args:            []string{"arg1"},
								Env: []corev1.EnvVar{{
									Name: "ENV_VAR", Value: "value"}},
								VolumeMounts: []corev1.VolumeMount{{
									Name:      "source",
									MountPath: "/clustercode/source"}},
								SecurityContext: &corev1.SecurityContext{
									RunAsUser: pointer.Int64(33)}}},
							Volumes: []corev1.Volume{{
								Name: "source",
								VolumeSource: corev1.VolumeSource{
									HostPath: &corev1.HostPathVolumeSource{Path: "/path"}}}},
						},
					},
				},
			},
		},
	}
	for name, tt := range tests {
		t.Run(name, func(t *testing.T) {
			createClustercodeJobDefinition(tt.givenJob, tt.givenTask, TaskOpts{
				template:    tt.givenTask.Spec.Encode.PodTemplate,
				image:       "image:tag",
				args:        []string{"arg1"},
				jobType:     types.JobTypeSplit,
				mountSource: true,
			})
			assert.Equal(t, tt.expectedJob.Spec, tt.givenJob.Spec)
		})
	}
}
