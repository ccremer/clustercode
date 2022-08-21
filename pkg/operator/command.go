package operator

import (
	"context"
	"time"

	"github.com/ccremer/clustercode/pkg/api"
	pipeline "github.com/ccremer/go-command-pipeline"
	"github.com/go-logr/logr"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/leaderelection/resourcelock"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

type Command struct {
	Log logr.Logger

	LeaderElectionEnabled bool
	FfmpegImage           string
}

type commandContext struct {
	context.Context
	manager    manager.Manager
	kubeconfig *rest.Config
}

func (c *Command) Execute(ctx context.Context) error {
	log := c.Log
	log.Info("Setting up controllers", "config", c)
	ctrl.SetLogger(log)

	pctx := &commandContext{Context: ctx}
	p := pipeline.NewPipeline[*commandContext]()
	p.WithSteps(
		p.NewStep("get config", func(ctx *commandContext) error {
			cfg, err := ctrl.GetConfig()
			ctx.kubeconfig = cfg
			return err
		}),
		p.NewStep("create manager", func(ctx *commandContext) error {
			// configure client-side throttling
			ctx.kubeconfig.QPS = 100
			ctx.kubeconfig.Burst = 150 // more Openshift friendly

			mgr, err := ctrl.NewManager(ctx.kubeconfig, ctrl.Options{
				// controller-runtime uses both ConfigMaps and Leases for leader election by default.
				// Leases expire after 15 seconds, with a 10-second renewal deadline.
				// We've observed leader loss due to renewal deadlines being exceeded when under high load - i.e.
				//  hundreds of reconciles per second and ~200rps to the API server.
				// Switching to Leases only and longer leases appears to alleviate this.
				LeaderElection:             c.LeaderElectionEnabled,
				LeaderElectionID:           "leader-election-clustercode",
				LeaderElectionResourceLock: resourcelock.LeasesResourceLock,
				LeaseDuration:              func() *time.Duration { d := 60 * time.Second; return &d }(),
				RenewDeadline:              func() *time.Duration { d := 50 * time.Second; return &d }(),
			})
			ctx.manager = mgr
			return err
		}),
		p.NewStep("register schemes", func(ctx *commandContext) error {
			return api.AddToScheme(ctx.manager.GetScheme())
		}),
		p.NewStep("setup controllers", func(ctx *commandContext) error {
			return SetupControllers(ctx.manager)
		}),
		p.NewStep("run manager", func(ctx *commandContext) error {
			log.Info("Starting manager")
			return ctx.manager.Start(ctx)
		}),
	)

	return p.RunWithContext(pctx)
}
