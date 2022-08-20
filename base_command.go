package main

import (
	"context"

	"github.com/ccremer/clustercode/api/v1alpha1"
	batchv1 "k8s.io/api/batch/v1"
	"k8s.io/apimachinery/pkg/runtime"
	clientgoscheme "k8s.io/client-go/kubernetes/scheme"
	"k8s.io/client-go/rest"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type commandContext struct {
	context.Context

	kubeconfig *rest.Config
	kube       client.Client
	scheme     *runtime.Scheme
}

var createClientFn = func(ctx *commandContext) error {
	kube, err := client.New(ctx.kubeconfig, client.Options{Scheme: ctx.scheme})
	ctx.kube = kube
	return err
}

var registerSchemesFn = func(ctx *commandContext) error {
	ctx.scheme = runtime.NewScheme()
	b := &runtime.SchemeBuilder{}
	b.Register(
		clientgoscheme.AddToScheme,
		batchv1.AddToScheme,
		v1alpha1.AddToScheme,
	)
	return b.AddToScheme(ctx.scheme)
}

var loadKubeConfigFn = func(ctx *commandContext) error {
	clientConfig, err := ctrl.GetConfig()
	ctx.kubeconfig = clientConfig
	return err
}
