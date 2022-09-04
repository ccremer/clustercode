package pipe

import (
	"context"
	"fmt"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	pipeline "github.com/ccremer/go-command-pipeline"
	batchv1 "k8s.io/api/batch/v1"
	"k8s.io/apimachinery/pkg/runtime"
	kubernetesscheme "k8s.io/client-go/kubernetes/scheme"
	"k8s.io/client-go/rest"
	controllerruntime "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type kubeContext struct {
	context.Context

	kubeconfig *rest.Config
	kube       client.Client
	scheme     *runtime.Scheme
}

// NewKubeClient creates a new client.Client using in-cluster config.
func NewKubeClient(ctx context.Context) (client.Client, error) {
	pctx := &kubeContext{Context: ctx}
	p := pipeline.NewPipeline[*kubeContext]().WithBeforeHooks(DebugLogger[*kubeContext](controllerruntime.LoggerFrom(ctx)))
	p.WithSteps(
		p.NewStep("register schemes", registerSchemesFn),
		p.NewStep("load kube config", loadKubeConfigFn),
		p.NewStep("create client", createClientFn),
	)
	err := p.RunWithContext(pctx)
	if err != nil {
		return nil, fmt.Errorf("cannot instantiate new kubernetes client: %w", err)
	}
	return pctx.kube, nil
}

var createClientFn = func(ctx *kubeContext) error {
	kube, err := client.New(ctx.kubeconfig, client.Options{Scheme: ctx.scheme})
	ctx.kube = kube
	return err
}

var registerSchemesFn = func(ctx *kubeContext) error {
	ctx.scheme = runtime.NewScheme()
	b := &runtime.SchemeBuilder{}
	b.Register(
		kubernetesscheme.AddToScheme,
		batchv1.AddToScheme,
		v1alpha1.AddToScheme,
	)
	return b.AddToScheme(ctx.scheme)
}

var loadKubeConfigFn = func(ctx *kubeContext) error {
	clientConfig, err := controllerruntime.GetConfig()
	ctx.kubeconfig = clientConfig
	return err
}
