package builder

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/types"
)

type (
	MetaProperty interface {
		Apply(b *MetaBuilder)
	}

	MetaBuilder struct {
		ObjectMeta metav1.Object
	}

	WithName           string
	WithNamespace      string
	WithNamespacedName types.NamespacedName
	WithLabels         labels.Set
	AddLabel           KeyValueTuple
)

func NewMetaBuilder() MetaBuilder {
	return NewMetaBuilderWith(&metav1.ObjectMeta{})
}

func NewMetaBuilderWith(obj metav1.Object) MetaBuilder {
	return MetaBuilder{ObjectMeta: obj}
}

func (b MetaBuilder) Build(props ...MetaProperty) MetaBuilder {
	for _, opt := range props {
		opt.Apply(&b)
	}
	return b
}

func (w WithName) Apply(b *MetaBuilder) {
	b.ObjectMeta.SetName(string(w))
}

func (w WithNamespace) Apply(b *MetaBuilder) {
	b.ObjectMeta.SetNamespace(string(w))
}

func (w WithNamespacedName) Apply(b *MetaBuilder) {
	b.ObjectMeta.SetNamespace(w.Namespace)
	b.ObjectMeta.SetName(w.Name)
}

func (w AddLabel) Apply(b *MetaBuilder) {
	l := b.ObjectMeta.GetLabels()
	if l == nil {
		l = labels.Set{}
	}
	l[w.Key] = w.Value
	b.ObjectMeta.SetLabels(l)
}
func (w WithLabels) Apply(b *MetaBuilder) {
	b.ObjectMeta.SetLabels(w)
}
