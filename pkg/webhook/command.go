package webhook

import (
	"context"

	"github.com/ccremer/clustercode/pkg/api"
	pipeline "github.com/ccremer/go-command-pipeline"
	"github.com/go-logr/logr"
	"k8s.io/client-go/rest"
	controllerruntime "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

type Command struct {
	Log            logr.Logger
	WebhookCertDir string
}

type commandContext struct {
	context.Context
	manager    manager.Manager
	kubeconfig *rest.Config
}

func (c *Command) Execute(ctx context.Context) error {
	log := c.Log
	log.Info("Setting up webhook server", "config", c)
	controllerruntime.SetLogger(log)

	pctx := &commandContext{Context: ctx}
	p := pipeline.NewPipeline[*commandContext]()
	p.WithSteps(
		p.NewStep("get config", func(ctx *commandContext) error {
			cfg, err := controllerruntime.GetConfig()
			ctx.kubeconfig = cfg
			return err
		}),
		p.NewStep("create manager", func(ctx *commandContext) error {
			// configure client-side throttling
			ctx.kubeconfig.QPS = 100
			ctx.kubeconfig.Burst = 150 // more Openshift friendly

			mgr, err := controllerruntime.NewManager(ctx.kubeconfig, controllerruntime.Options{
				MetricsBindAddress: "0", // disable
			})
			ctx.manager = mgr
			return err
		}),
		p.NewStep("register schemes", func(ctx *commandContext) error {
			return api.AddToScheme(ctx.manager.GetScheme())
		}),
		p.NewStep("setup webhook server", func(ctx *commandContext) error {
			ws := ctx.manager.GetWebhookServer()
			ws.CertDir = c.WebhookCertDir
			ws.TLSMinVersion = "1.3"
			return SetupWebhooks(ctx.manager)
		}),
		p.NewStep("run manager", func(ctx *commandContext) error {
			log.Info("Starting manager")
			return ctx.manager.Start(ctx)
		}),
	)

	return p.RunWithContext(pctx)
}
