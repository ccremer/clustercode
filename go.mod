module github.com/ccremer/clustercode

go 1.15

require (
	github.com/elastic/crd-ref-docs v0.0.7
	github.com/go-logr/logr v1.2.0
	github.com/knadh/koanf v1.4.2
	github.com/spf13/cobra v1.4.0
	github.com/spf13/pflag v1.0.5
	github.com/stretchr/testify v1.7.0
	go.uber.org/zap v1.19.0
	k8s.io/api v0.24.0
	k8s.io/apimachinery v0.24.0
	k8s.io/client-go v0.24.0
	k8s.io/utils v0.0.0-20220210201930-3a6ce19ff2f9
	sigs.k8s.io/controller-runtime v0.9.2
	sigs.k8s.io/controller-tools v0.9.2
	sigs.k8s.io/kustomize/kustomize/v3 v3.8.8
)
