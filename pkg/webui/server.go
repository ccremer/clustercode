package webui

import (
	"context"
	"crypto/tls"
	"fmt"
	"github.com/ccremer/clustercode/pkg/internal/pipe"
	"github.com/ccremer/clustercode/ui"
	pipeline "github.com/ccremer/go-command-pipeline"
	"github.com/go-logr/logr"
	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	"io/fs"
	"net/http"
	"net/url"
	"os"
)

type Command struct {
	Log logr.Logger
	// ApiURL is the Kubernetes API URL to proxy the API requests.
	// The frontend is making API calls directly to Kubernetes.
	ApiURL string
	// ApiTLSSkipVerify controls whether the certificate of the Kubernetes API is verified.
	ApiTLSSkipVerify bool
	UIAssetDir       string
}

type commandContext struct {
	context.Context
	dependencyResolver pipeline.DependencyResolver[*commandContext]

	echo     *echo.Echo
	settings Settings
}

// Execute runs the command and returns an error, if any.
func (c *Command) Execute(ctx context.Context) error {

	pctx := &commandContext{
		dependencyResolver: pipeline.NewDependencyRecorder[*commandContext](),
		Context:            ctx,
	}

	p := pipeline.NewPipeline[*commandContext]().WithBeforeHooks(pipe.DebugLogger[*commandContext](c.Log), pctx.dependencyResolver.Record)
	p.WithSteps(
		p.NewStep("create server", c.createServer),
		p.NewStep("setup routes", c.setupRoutes),
		p.When(c.isProxyEnabled, "proxy API server", c.setupProxy),
		p.NewStep("run server", c.startServer),
	)
	return p.RunWithContext(pctx)
}

func (c *Command) isProxyEnabled(_ *commandContext) bool {
	return c.ApiURL != ""
}

func (c *Command) createServer(ctx *commandContext) error {
	ctx.echo = echo.New()
	ctx.echo.Pre(middleware.RemoveTrailingSlash())
	ctx.echo.Use(middleware.LoggerWithConfig(middleware.LoggerConfig{
		Skipper:          skipAccessLogs,
		Format:           middleware.DefaultLoggerConfig.Format,
		CustomTimeFormat: middleware.DefaultLoggerConfig.CustomTimeFormat,
	}))
	return nil
}

func (c *Command) setupRoutes(ctx *commandContext) error {
	ctx.dependencyResolver.MustRequireDependencyByFuncName(c.createServer)
	ctx.echo.GET("/settings", c.settings(ctx))
	ctx.echo.GET("/healthz", healthz)
	ctx.echo.GET("/", func(c echo.Context) error {
		return c.Redirect(http.StatusPermanentRedirect, "/ui")
	})

	staticConfig := middleware.StaticConfig{HTML5: true}

	if fileExists(c.UIAssetDir + "/index.html") {
		staticConfig.Root = c.UIAssetDir
		c.Log.Info("Serving assets from local filesystem", "dir", c.UIAssetDir)
	} else if ui.IsEmbedded() {
		c.Log.Info("Using embedded UI assets")
		fsys, err := fs.Sub(ui.PublicFs, "build")
		if err != nil {
			c.Log.Info("This release was built without embedding UI assets. Build or download the assets separately.")
			return fmt.Errorf("cannot use embedded UI assets nor assets from dir: %w", err)
		}
		staticConfig.Root = "/"
		staticConfig.Filesystem = http.FS(fsys)
	}
	ctx.echo.Group("/ui").Use(middleware.StaticWithConfig(staticConfig))
	return nil
}

func (c *Command) setupProxy(ctx *commandContext) error {
	c.Log.Info("Setting up proxy", "url", c.ApiURL, "skip_tls_verify", c.ApiTLSSkipVerify)
	u, err := url.Parse(c.ApiURL)
	if err != nil {
		return err
	}
	d := http.DefaultTransport.(*http.Transport)
	config := middleware.ProxyConfig{
		Balancer: middleware.NewRoundRobinBalancer([]*middleware.ProxyTarget{
			{URL: u},
		}),
		// copy default settings but allow skip TLS verification
		Transport: &http.Transport{
			Proxy:                 d.Proxy,
			DialContext:           d.DialContext,
			TLSClientConfig:       &tls.Config{InsecureSkipVerify: c.ApiTLSSkipVerify},
			TLSHandshakeTimeout:   d.TLSHandshakeTimeout,
			MaxIdleConns:          d.MaxIdleConns,
			IdleConnTimeout:       d.IdleConnTimeout,
			ExpectContinueTimeout: d.ExpectContinueTimeout,
			ForceAttemptHTTP2:     d.ForceAttemptHTTP2,
		},
	}
	ctx.echo.Group("/apis").Use(middleware.ProxyWithConfig(config))
	return nil
}

func (c *Command) startServer(ctx *commandContext) error {
	ctx.dependencyResolver.MustRequireDependencyByFuncName(c.createServer)
	return ctx.echo.Start(":8080")
}

func (c *Command) settings(ctx *commandContext) func(echo.Context) error {
	return func(e echo.Context) error {
		return e.JSON(http.StatusOK, ctx.settings)
	}
}

var staticAssetFiles = map[string]bool{
	"/favicon.ico": true,
	"/ui/*":        true,
	"/healthz":     true,
}

func skipAccessLogs(ctx echo.Context) bool {
	// given an exact known key, lookups in maps are faster than iterating over slices.
	_, exists := staticAssetFiles[ctx.Path()]
	return exists
}

func healthz(c echo.Context) error {
	return c.String(http.StatusNoContent, "")
}

func fileExists(name string) bool {
	info, err := os.Stat(name)
	if err != nil {
		return false
	}
	return !info.IsDir()
}
