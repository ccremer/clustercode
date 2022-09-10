package blueprintwebhook

import (
	"context"

	"k8s.io/apimachinery/pkg/runtime"
)

type Validator struct {
}

func (v *Validator) ValidateCreate(ctx context.Context, obj runtime.Object) error {
	// TODO implement me
	panic("implement me")
}

func (v *Validator) ValidateUpdate(ctx context.Context, oldObj, newObj runtime.Object) error {
	// TODO implement me
	panic("implement me")
}

func (v *Validator) ValidateDelete(ctx context.Context, obj runtime.Object) error {
	// TODO implement me
	panic("implement me")
}
