package main

import (
	"github.com/ccremer/clustercode/pkg/webui"
	"github.com/lestrrat-go/jwx/v2/jwt"
	"github.com/urfave/cli/v2"
	"k8s.io/client-go/tools/clientcmd"
	"os"
)

const apiUrlFlag = "api-url"

func newWebuiCommand() *cli.Command {
	command := &webui.Command{}
	return &cli.Command{
		Name:   "webui",
		Usage:  "Start clustercode frontend web server",
		Before: discoverKubernetesAPI,
		Action: func(ctx *cli.Context) error {
			command.Log = AppLogger(ctx).WithName(ctx.Command.Name)
			return command.Execute(ctx.Context)
		},
		Flags: []cli.Flag{
			&cli.StringFlag{Name: apiUrlFlag, EnvVars: envVars("API_URL"),
				Usage:       "Full base URL of the Kubernetes API server that is being proxied. If empty, the proxy is disabled. If set to 'auto', it will try to discover it using the service account token.",
				Value:       "auto",
				Destination: &command.ApiURL,
			},
			&cli.BoolFlag{Name: "api-tls-skip-verify", EnvVars: envVars("API_TLS_SKIP_VERIFY"),
				Usage:       "Whether the certificate verification of the Kubernetes API server should be verified",
				Destination: &command.ApiTLSSkipVerify,
			},
			&cli.PathFlag{Name: "sa-token-path", EnvVars: envVars("API_SA_TOKEN_PATH"),
				Usage: "Path to the Kubernetes Service Account token secret for auto-discovery",
				Value: "/var/run/secrets/kubernetes.io/serviceaccount/token",
			},
			&cli.PathFlag{Name: "ui-dir", EnvVars: envVars("UI_ASSET_DIR"),
				Usage:       "Relative Path to the web interface static assets directory. If not found, it will use embedded assets.",
				Value:       "ui",
				Destination: &command.UIAssetDir,
			},
		},
	}
}

func discoverKubernetesAPI(ctx *cli.Context) error {
	_ = LogMetadata(ctx)
	log := AppLogger(ctx).WithName(ctx.Command.Name)

	if ctx.String(apiUrlFlag) != "auto" {
		return nil
	}

	path := ctx.String("sa-token-path")
	raw, err := os.ReadFile(path)
	if err != nil {
		log.Info("Cannot read the token", "error", err.Error())
		return ctx.Set(apiUrlFlag, "")
	}

	clientConfig, err := clientcmd.NewClientConfigFromBytes(raw)
	if err == nil {
		cfg, _ := clientConfig.ClientConfig()
		log.Info("Discovered Kubernetes API URL from kubeconfig", "url", cfg.Host)
		return ctx.Set(apiUrlFlag, cfg.Host)
	}

	token, err := jwt.Parse(raw, jwt.WithVerify(false))
	if err != nil {
		log.Info("Cannot parse the token", "error", err.Error())
		return ctx.Set(apiUrlFlag, "")
	}
	aud := token.Audience()
	if len(aud) > 0 {
		log.Info("Discovered Kubernetes API URL", "url", aud[0])
		return ctx.Set(apiUrlFlag, aud[0])
	}
	return nil
}
