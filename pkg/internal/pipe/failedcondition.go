package pipe

import (
	"context"

	"github.com/ccremer/clustercode/pkg/api/conditions"
	"k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

func UpdateFailedCondition(ctx context.Context, kube client.Client, conds *[]metav1.Condition, obj client.Object, err error) error {
	if err == nil {
		meta.RemoveStatusCondition(conds, conditions.TypeFailed)
	} else {
		meta.SetStatusCondition(conds, conditions.Failed(err))
	}
	return kube.Status().Update(ctx, obj)
}
