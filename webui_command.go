package main

import (
	"github.com/ccremer/clustercode/pkg/webui"
	"github.com/urfave/cli/v2"
)

func newWebuiCommand() *cli.Command {
	command := &webui.Command{}
	return &cli.Command{
		Name:   "webui",
		Usage:  "Start clustercode frontend web server",
		Before: LogMetadata,
		Action: func(ctx *cli.Context) error {
			command.Log = AppLogger(ctx).WithName(ctx.Command.Name)
			return command.Execute(ctx.Context)
		},
		Flags: []cli.Flag{
			&cli.StringFlag{Name: "api-url", EnvVars: envVars("API_URL"),
				Usage:       "Full base URL of the Kubernetes API server that is being proxied. If empty, the proxy is disabled.",
				Destination: &command.ApiURL,
			},
			&cli.BoolFlag{Name: "api-tls-skip-verify", EnvVars: envVars("API_TLS_SKIP_VERIFY"),
				Usage:       "Whether the certificate verification of the Kubernetes API server should be verified",
				Destination: &command.ApiTLSSkipVerify,
			},
		},
	}
}
