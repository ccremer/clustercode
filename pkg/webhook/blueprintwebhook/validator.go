package blueprintwebhook

import (
	"context"
	"fmt"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	"github.com/go-logr/logr"
	"k8s.io/apimachinery/pkg/runtime"
)

type Validator struct {
	Log logr.Logger
}

func (v *Validator) ValidateCreate(_ context.Context, obj runtime.Object) error {
	bp := obj.(*v1alpha1.Blueprint)
	v.Log.V(1).Info("Validate Create", "name", bp.Name, "namespace", bp.Namespace)
	return v.validateSpec(bp)
}

func (v *Validator) ValidateUpdate(_ context.Context, _, newObj runtime.Object) error {
	bp := newObj.(*v1alpha1.Blueprint)
	v.Log.V(1).Info("Validate Update", "name", bp.Name, "namespace", bp.Namespace)
	return v.validateSpec(bp)
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
