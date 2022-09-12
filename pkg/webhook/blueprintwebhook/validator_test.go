package blueprintwebhook

import (
	"testing"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"github.com/go-logr/logr"
	"github.com/stretchr/testify/assert"
	corev1 "k8s.io/api/core/v1"
)

func TestValidator_ValidateCreate(t *testing.T) {
	tests := map[string]struct {
		givenSpec     v1alpha1.BlueprintSpec
		expectedError string
	}{
		"GivenEmptyClaim_WhenPodTemplateEmpty_ThenReturnError": {
			givenSpec:     v1alpha1.BlueprintSpec{},
			expectedError: "missing required volume template for source volume in the spec, since PVC references are empty",
		},
		"GivenEmptyClaim_WhenBothPodTemplateHaveVolume_ThenReturnNil": {
			givenSpec: v1alpha1.BlueprintSpec{
				Encode: v1alpha1.EncodeSpec{PodTemplate: v1alpha1.PodTemplate{
					Volumes: []corev1.Volume{
						{Name: "source"},
						{Name: "intermediate"},
						{Name: "target"},
					}},
				},
				Cleanup: v1alpha1.CleanupSpec{PodTemplate: v1alpha1.PodTemplate{
					Volumes: []corev1.Volume{
						{Name: "source"},
						{Name: "intermediate"},
						{Name: "target"},
					}},
				},
			},
			expectedError: "",
		},
		"GivenEmptyClaim_WhenPodTemplateMissingVolume_ThenReturnError": {
			givenSpec: v1alpha1.BlueprintSpec{
				Encode: v1alpha1.EncodeSpec{PodTemplate: v1alpha1.PodTemplate{
					Volumes: []corev1.Volume{
						{Name: "source"},
						{Name: "intermediate"},
						{Name: "target"},
					}},
				},
				Cleanup: v1alpha1.CleanupSpec{PodTemplate: v1alpha1.PodTemplate{
					Volumes: []corev1.Volume{
						{Name: "source"},
					}},
				},
			},
			expectedError: "missing required volume template for intermediate volume in the spec, since PVC references are empty",
		},
		"GivenClaimRef_WhenPodTemplateMissingVolume_ThenReturnNil": {
			givenSpec: v1alpha1.BlueprintSpec{
				Storage: v1alpha1.StorageSpec{
					SourcePvc:       v1alpha1.VolumeRef{ClaimName: "pvc-source"},
					IntermediatePvc: v1alpha1.VolumeRef{ClaimName: "pvc-intermediate"},
					TargetPvc:       v1alpha1.VolumeRef{ClaimName: "pvc-target"}},
			},
			expectedError: "",
		},
		"GivenClaimRef_WhenPodTemplateVolumeDuplicates_ThenReturnError": {
			givenSpec: v1alpha1.BlueprintSpec{
				Storage: v1alpha1.StorageSpec{
					SourcePvc:       v1alpha1.VolumeRef{ClaimName: "pvc-source"},
					IntermediatePvc: v1alpha1.VolumeRef{ClaimName: "pvc-intermediate"},
				},
				Encode: v1alpha1.EncodeSpec{PodTemplate: v1alpha1.PodTemplate{
					Volumes: []corev1.Volume{
						{Name: "source"},
					}},
				},
			},
			expectedError: "duplicate volume specification for source volume in the template: spec already references a PVC named 'pvc-source'",
		},
		"GivenClaimRef_WhenMixedWithPodTemplateVolume_ThenReturnNil": {
			givenSpec: v1alpha1.BlueprintSpec{
				Storage: v1alpha1.StorageSpec{
					SourcePvc:       v1alpha1.VolumeRef{ClaimName: "pvc-source"},
					IntermediatePvc: v1alpha1.VolumeRef{ClaimName: "pvc-intermediate"},
				},
				Encode: v1alpha1.EncodeSpec{PodTemplate: v1alpha1.PodTemplate{
					Volumes: []corev1.Volume{
						{Name: "target"},
					}},
				},
				Cleanup: v1alpha1.CleanupSpec{PodTemplate: v1alpha1.PodTemplate{
					Volumes: []corev1.Volume{
						{Name: "target"},
					}},
				},
			},
			expectedError: "",
		},
	}
	for name, tt := range tests {
		t.Run(name, func(t *testing.T) {
			v := &Validator{Log: logr.Discard()}
			err := v.validateSpec(&v1alpha1.Blueprint{Spec: tt.givenSpec})
			if tt.expectedError != "" {
				assert.EqualError(t, err, tt.expectedError)
			} else {
				assert.NoError(t, err)
			}
		})
	}
}
