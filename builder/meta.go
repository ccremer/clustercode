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
		Object metav1.Object
	}

	WithName           string
	WithNamespace      string
	WithNamespacedName types.NamespacedName
	WithLabels         labels.Set
	AddLabel           KeyValueTuple
	AddLabels          labels.Set
)

func NewMetaBuilder() MetaBuilder {
	return NewMetaBuilderWith(&metav1.ObjectMeta{})
}

func NewMetaBuilderWith(obj metav1.Object) MetaBuilder {
	return MetaBuilder{Object: obj}
}

func (b MetaBuilder) Build(props ...MetaProperty) MetaBuilder {
	for _, opt := range props {
		opt.Apply(&b)
	}
	return b
}

func (w WithName) Apply(b *MetaBuilder) {
	b.Object.SetName(string(w))
}

func (w WithNamespace) Apply(b *MetaBuilder) {
	b.Object.SetNamespace(string(w))
}

func (w WithNamespacedName) Apply(b *MetaBuilder) {
	b.Object.SetNamespace(w.Namespace)
	b.Object.SetName(w.Name)
}

func (w AddLabel) Apply(b *MetaBuilder) {
	l := b.Object.GetLabels()
	if l == nil {
		l = labels.Set{}
	}
	l[w.Key] = w.Value
	b.Object.SetLabels(l)
}

func (w WithLabels) Apply(b *MetaBuilder) {
	b.Object.SetLabels(w)
}

func (w AddLabels) Apply(b *MetaBuilder) {
	l := b.Object.GetLabels()
	if l == nil {
		l = labels.Set{}
	}
	b.Object.SetLabels(labels.Merge(l, labels.Set(w)))
}
