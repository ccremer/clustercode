package blueprintwebhook

import (
	"context"

	"k8s.io/apimachinery/pkg/runtime"
)

type Defaulter struct {
}

func (d *Defaulter) Default(ctx context.Context, obj runtime.Object) error {
	// TODO implement me
	panic("implement me")
}
