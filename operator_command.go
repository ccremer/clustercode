package main

import (
	"context"
	"time"

	"github.com/ccremer/clustercode/pkg/api"
	"github.com/ccremer/clustercode/pkg/operator"
	"github.com/ccremer/clustercode/pkg/operator/blueprintcontroller"
	"github.com/ccremer/clustercode/pkg/operator/taskcontroller"
	pipeline "github.com/ccremer/go-command-pipeline"
	"github.com/urfave/cli/v2"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/leaderelection/resourcelock"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

type operatorCommand struct {
	manager    manager.Manager
	kubeconfig *rest.Config

	LeaderElectionEnabled bool
	WebhookCertDir        string
	FfmpegImage           string
}

var operatorCommandName = "operator"

func newOperatorCommand() *cli.Command {
	command := &operatorCommand{}
	return &cli.Command{
		Name:   operatorCommandName,
		Usage:  "Start provider in operator mode",
		Before: LogMetadata,
		Action: command.execute,
		Flags: []cli.Flag{
			&cli.BoolFlag{Name: "leader-election-enabled", Value: false, EnvVars: envVars("LEADER_ELECTION_ENABLED"),
				Usage:       "Use leader election for the controller manager.",
				Destination: &command.LeaderElectionEnabled,
				Category:    "Operator",
			},
			&cli.StringFlag{Name: "webhook-tls-cert-dir", EnvVars: envVars("WEBHOOK_TLS_CERT_DIR"),
				Usage:       "Directory containing the certificates for the webhook server. If empty, the webhook server is not started.",
				Destination: &command.WebhookCertDir,
				Category:    "Operator",
			},
			&cli.StringFlag{Name: "clustercode-image", EnvVars: envVars("CLUSTERCODE_IMAGE"),
				Usage:       "Container image to be used when launching Clustercode jobs.",
				Destination: &blueprintcontroller.DefaultClusterCodeContainerImage,
				Category:    "Encoding", Required: true,
			},
			&cli.StringFlag{Name: "ffmpeg-image", EnvVars: envVars("FFMPEG_IMAGE"),
				Usage:       "Container image to be used when launching Ffmpeg jobs.",
				Destination: &taskcontroller.DefaultFfmpegContainerImage,
				Category:    "Encoding", Required: true,
			},
			newScanRoleKindFlag(),
			&cli.StringFlag{Name: "scan-role-name", EnvVars: envVars("SCAN_ROLE_NAME"),
				Usage:       "TODO",
				Value:       "clustercode-editor-role",
				Destination: &blueprintcontroller.ScanRoleName,
				Category:    "Encoding",
			},
		},
	}
}

func (c *operatorCommand) execute(ctx *cli.Context) error {
	blueprintcontroller.ScanRoleKind = ctx.String(newScanRoleKindFlag().Name)
	log := AppLogger(ctx).WithName(operatorCommandName)
	log.Info("Setting up controllers", "config", c)
	ctrl.SetLogger(log)

	p := pipeline.NewPipeline[context.Context]()
	p.WithSteps(
		p.NewStep("get config", func(ctx context.Context) error {
			cfg, err := ctrl.GetConfig()
			c.kubeconfig = cfg
			return err
		}),
		p.NewStep("create manager", func(ctx context.Context) error {
			// configure client-side throttling
			c.kubeconfig.QPS = 100
			c.kubeconfig.Burst = 150 // more Openshift friendly

			mgr, err := ctrl.NewManager(c.kubeconfig, ctrl.Options{
				// controller-runtime uses both ConfigMaps and Leases for leader election by default.
				// Leases expire after 15 seconds, with a 10-second renewal deadline.
				// We've observed leader loss due to renewal deadlines being exceeded when under high load - i.e.
				//  hundreds of reconciles per second and ~200rps to the API server.
				// Switching to Leases only and longer leases appears to alleviate this.
				LeaderElection:             c.LeaderElectionEnabled,
				LeaderElectionID:           "leader-election-" + appName,
				LeaderElectionResourceLock: resourcelock.LeasesResourceLock,
				LeaseDuration:              func() *time.Duration { d := 60 * time.Second; return &d }(),
				RenewDeadline:              func() *time.Duration { d := 50 * time.Second; return &d }(),
			})
			c.manager = mgr
			return err
		}),
		p.NewStep("register schemes", func(ctx context.Context) error {
			return api.AddToScheme(c.manager.GetScheme())
		}),
		p.NewStep("setup controllers", func(ctx context.Context) error {
			return operator.SetupControllers(c.manager)
		}),
		p.When(pipeline.Bool[context.Context](c.WebhookCertDir != ""), "setup webhook server",
			func(ctx context.Context) error {
				ws := c.manager.GetWebhookServer()
				ws.CertDir = c.WebhookCertDir
				ws.TLSMinVersion = "1.3"
				return operator.SetupWebhooks(c.manager)
			}),
		p.NewStep("run manager", func(ctx context.Context) error {
			log.Info("Starting manager")
			return c.manager.Start(ctx)
		}),
	)

	return p.RunWithContext(ctx.Context)
}
