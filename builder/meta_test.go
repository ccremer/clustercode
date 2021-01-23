package builder

import (
	"testing"

	"github.com/stretchr/testify/assert"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
)

func Test_MetaBuilder_Constructor(t *testing.T) {
	name := "cm"
	ns := "ns"
	lbls := map[string]string{
		"overwritten": "label",
	}
	b := NewMetaBuilderWith(&corev1.ConfigMap{
		ObjectMeta: metav1.ObjectMeta{
			Name: name,
			Labels: labels.Set{
				"a": "label",
			},
		},
	})
	result := b.Build(WithNamespace(ns), WithLabels(lbls)).ObjectMeta
	assert.Equal(t, name, result.GetName())
	assert.Equal(t, ns, result.GetNamespace())
	assert.Equal(t, lbls, result.GetLabels())
}

func Test_MetaBuilder_Properties(t *testing.T) {
	genericKey := "genericKey"
	genericValue := "genericValue"
	tests := map[string]struct {
		given    metav1.Object
		property MetaProperty
		expected metav1.Object
	}{
		"WithName": {
			property: WithName(genericKey),
			expected: &metav1.ObjectMeta{Name: genericKey},
		},
		"WithNamespace": {
			property: WithNamespace(genericKey),
			expected: &metav1.ObjectMeta{Namespace: genericKey},
		},
		"WithNamespacedName": {
			property: WithNamespacedName{Name: genericKey, Namespace: genericValue},
			expected: &metav1.ObjectMeta{Name: genericKey, Namespace: genericValue},
		},
		"AddLabel": {
			property: AddLabel{genericKey, genericValue},
			expected: &metav1.ObjectMeta{Labels: labels.Set{
				genericKey: genericValue,
			}},
		},
		"WithLabels": {
			property: WithLabels{genericKey: genericValue},
			expected: &metav1.ObjectMeta{Labels: labels.Set{
				genericKey: genericValue,
			}},
		},
	}

	for name, tt := range tests {
		t.Run(name, func(t *testing.T) {
			result := NewMetaBuilder().Build(tt.property).ObjectMeta
			assert.Equal(t, tt.expected, result)
		})
	}
}
