package main

import (
	"github.com/ccremer/clustercode/pkg/webhook"
	"github.com/urfave/cli/v2"
)

func newWebhookCommand() *cli.Command {
	command := &webhook.Command{}
	return &cli.Command{
		Name:   "webhook",
		Usage:  "Start clustercode in admission controller mode",
		Before: LogMetadata,
		Action: func(ctx *cli.Context) error {
			command.Log = AppLogger(ctx).WithName(ctx.Command.Name)
			return command.Execute(ctx.Context)
		},
		Flags: []cli.Flag{
			&cli.StringFlag{Name: "webhook-tls-cert-dir", EnvVars: envVars("WEBHOOK_TLS_CERT_DIR"), Required: true,
				Usage:       "Directory containing the certificates for the webhook server. It's expected to contain the 'tls.crt' and 'tls.key' files.",
				Destination: &command.WebhookCertDir,
			},
		},
	}
}
