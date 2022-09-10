package blueprintwebhook

import (
	"context"
	"fmt"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	"k8s.io/apimachinery/pkg/runtime"
)

type Validator struct{}

func (v *Validator) ValidateCreate(_ context.Context, obj runtime.Object) error {
	return v.validateSpec(obj.(*v1alpha1.Blueprint))
}

func (v *Validator) ValidateUpdate(_ context.Context, _, newObj runtime.Object) error {
	return v.validateSpec(newObj.(*v1alpha1.Blueprint))
}

func (v *Validator) ValidateDelete(_ context.Context, _ runtime.Object) error {
	return nil
}

func (v *Validator) validateSpec(bp *v1alpha1.Blueprint) error {
	if err := v.validateStorage(bp, internaltypes.SourceSubMountPath, bp.Spec.Storage.SourcePvc); err != nil {
		return err
	}
	if err := v.validateStorage(bp, internaltypes.IntermediateSubMountPath, bp.Spec.Storage.IntermediatePvc); err != nil {
		return err
	}
	if err := v.validateStorage(bp, internaltypes.TargetSubMountPath, bp.Spec.Storage.TargetPvc); err != nil {
		return err
	}
	return nil
}

func (v *Validator) validateStorage(bp *v1alpha1.Blueprint, volName string, volRef v1alpha1.VolumeRef) error {
	if volRef.ClaimName == "" {
		if !v.hasPodVolume(bp.Spec.Encode.PodTemplate, volName) || !v.hasPodVolume(bp.Spec.Cleanup.PodTemplate, volName) {
			return fmt.Errorf("missing required volume template for %s volume in the spec, since PVC references are empty", volName)
		}
	}
	if volRef.ClaimName != "" {
		if v.hasPodVolume(bp.Spec.Encode.PodTemplate, volName) || v.hasPodVolume(bp.Spec.Cleanup.PodTemplate, volName) {
			return fmt.Errorf("duplicate volume specification for %s volume in the template: spec already references a PVC named '%s'", volName, volRef.ClaimName)
		}
	}
	return nil
}

func (v *Validator) hasPodVolume(podTemplate v1alpha1.PodTemplate, volName string) bool {
	for _, volume := range podTemplate.Volumes {
		if volume.Name == volName {
			return true
		}
	}
	return false
}
