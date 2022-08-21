package pipe

import (
	"context"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	pipeline "github.com/ccremer/go-command-pipeline"
	"github.com/pkg/errors"
	batchv1 "k8s.io/api/batch/v1"
	"k8s.io/apimachinery/pkg/runtime"
	kubernetesscheme "k8s.io/client-go/kubernetes/scheme"
	"k8s.io/client-go/rest"
	controllerruntime "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type kubeContext struct {
	context.Context

	Kubeconfig *rest.Config
	Kube       client.Client
	Scheme     *runtime.Scheme
}

// NewKubeClient creates a new client.Client using in-cluster config.
func NewKubeClient() (client.Client, error) {
	pctx := &kubeContext{}
	p := pipeline.NewPipeline[*kubeContext]().WithBeforeHooks(DebugLogger[*kubeContext](pctx))
	p.WithSteps(
		p.NewStep("register schemes", registerSchemesFn),
		p.NewStep("load kube config", loadKubeConfigFn),
		p.NewStep("create client", createClientFn),
	)
	err := p.RunWithContext(pctx)
	return pctx.Kube, errors.Wrap(err, "cannot instantiate new kubernetes client")
}

var createClientFn = func(ctx *kubeContext) error {
	kube, err := client.New(ctx.Kubeconfig, client.Options{Scheme: ctx.Scheme})
	ctx.Kube = kube
	return err
}

var registerSchemesFn = func(ctx *kubeContext) error {
	ctx.Scheme = runtime.NewScheme()
	b := &runtime.SchemeBuilder{}
	b.Register(
		kubernetesscheme.AddToScheme,
		batchv1.AddToScheme,
		v1alpha1.AddToScheme,
	)
	return b.AddToScheme(ctx.Scheme)
}

var loadKubeConfigFn = func(ctx *kubeContext) error {
	clientConfig, err := controllerruntime.GetConfig()
	ctx.Kubeconfig = clientConfig
	return err
}
