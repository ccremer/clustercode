package pipeline

import (
	"context"
	"testing"

	"github.com/go-logr/zapr"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"go.uber.org/zap/zaptest"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/client/fake"
)

func TestResourceAction_UpsertResource(t *testing.T) {
	tests := map[string]struct {
		initObjects       []client.Object
		givenObject       client.Object
		expectedOperation ResourceOperation
		assertFn          func(t *testing.T, obj client.Object)
	}{
		"GivenNonExistingObject_WhenUpsertResource_ThenCreateResource": {
			givenObject:       newConfigMap(),
			expectedOperation: ResourceCreated,
		},
		"GivenExistingObject_WhenUpsertResource_ThenUpdateResource": {
			initObjects: []client.Object{
				newConfigMap(),
			},
			givenObject: &corev1.ConfigMap{
				ObjectMeta: metav1.ObjectMeta{
					Namespace: "default",
					Name:      "test",
				},
				Data: map[string]string{
					"KEY": "VALUE",
				},
			},
			expectedOperation: ResourceUpdated,
			assertFn: func(t *testing.T, obj client.Object) {
				assert.Equal(t, "VALUE", obj.(*corev1.ConfigMap).Data["KEY"])
			},
		},
	}
	for name, tt := range tests {
		t.Run(name, func(t *testing.T) {
			r := &ResourceAction{
				Log:    zapr.NewLogger(zaptest.NewLogger(t)),
				Client: fake.NewClientBuilder().WithObjects(tt.initObjects...).Build(),
			}

			op, err := r.UpsertResource(context.TODO(), tt.givenObject)
			require.NoError(t, err)
			assert.Equal(t, tt.expectedOperation, op)
			assert.NotEmpty(t, tt.givenObject.GetResourceVersion())
			if tt.assertFn != nil {
				tt.assertFn(t, tt.givenObject)
			}
		})
	}
}

func newConfigMap() *corev1.ConfigMap {
	return &corev1.ConfigMap{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: "default",
			Name:      "test",
		},
	}
}
