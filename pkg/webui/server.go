package webui

import (
	"context"
	"crypto/tls"
	"io/fs"
	"net/http"
	"net/url"
	"os"
	"strings"

	"github.com/ccremer/clustercode/pkg/internal/pipe"
	"github.com/ccremer/clustercode/ui"
	pipeline "github.com/ccremer/go-command-pipeline"
	"github.com/go-logr/logr"
	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
)

type Command struct {
	Log logr.Logger
	// ApiURL is the Kubernetes API URL to proxy the API requests.
	// The frontend is making API calls directly to Kubernetes.
	ApiURL           string
	ApiTLSSkipVerify bool
}

type commandContext struct {
	context.Context
	dependencyResolver pipeline.DependencyResolver[*commandContext]

	echo *echo.Echo
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
	assetHandler := http.FileServer(c.getFileSystem())
	ctx.echo.GET("/", echo.WrapHandler(assetHandler))
	ctx.echo.GET("/vite.svg", echo.WrapHandler(assetHandler))
	ctx.echo.GET("/assets/*", echo.WrapHandler(assetHandler))
	ctx.echo.GET("/robots.txt", echo.WrapHandler(assetHandler))
	ctx.echo.GET("/healthz", healthz)
	return nil
}

func (c *Command) setupProxy(ctx *commandContext) error {
	u, err := url.Parse(c.ApiURL)
	if err != nil {
		return err
	}
	d := http.DefaultTransport.(*http.Transport)
	config := middleware.ProxyConfig{
		Balancer: middleware.NewRoundRobinBalancer([]*middleware.ProxyTarget{
			{URL: u},
		}),
		Skipper: func(c echo.Context) bool {
			return !strings.HasPrefix(c.Path(), "/apis")
		},
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
	ctx.echo.Use(middleware.ProxyWithConfig(config))
	return nil
}

func (c *Command) getFileSystem() http.FileSystem {
	dir := "ui/dist"
	if ui.IsEmbedded() {
		c.Log.Info("Using embedded assets")
		fsys, err := fs.Sub(ui.PublicFs, "dist")
		if err == nil {
			return http.FS(fsys)
		}
		c.Log.Info("Cannot use embedded assets, resort to live assets", "error", err.Error())
	} else {
		c.Log.Info("This release was built without embedding UI assets. Build or download the assets separately.")
	}
	c.Log.Info("Serving assets from local filesystem", "dir", dir)
	return http.FS(os.DirFS(dir))
}

func (c *Command) startServer(ctx *commandContext) error {
	ctx.dependencyResolver.MustRequireDependencyByFuncName(c.createServer)
	return ctx.echo.Start(":8080")
}

var publicRoutes = map[string]bool{
	"/favicon.ico": true,
	"/robots.txt":  true,
	"/vite.svg":    true,
	"/assets/*":    true,
	"/healthz":     true,
}

func skipAccessLogs(ctx echo.Context) bool {
	// given an exact known key, lookups in maps are faster than iterating over slices.
	_, exists := publicRoutes[ctx.Path()]
	return exists
}

func healthz(c echo.Context) error {
	return c.String(http.StatusNoContent, "")
}
