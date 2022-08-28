package main

import (
	"context"
	"fmt"
	"os"
	"os/signal"
	"sync/atomic"
	"syscall"
	"time"

	"github.com/go-logr/logr"
	"github.com/urfave/cli/v2"
)

var (
	// These will be populated by Goreleaser
	version = "unknown"
	commit  = "-dirty-"
	date    = time.Now().Format("2006-01-02")

	appName     = "clustercode"
	appLongName = "Distribute your video encoding tasks across a cluster of nodes!"

	// envPrefix is the global prefix to use for the keys in environment variables
	envPrefix = "CC"
)

func init() {
	// Remove `-v` short option from --version flag
	cli.VersionFlag.(*cli.BoolFlag).Aliases = nil
}

func main() {
	ctx, stop, app := newApp()
	defer stop()
	err := app.RunContext(ctx, os.Args)
	// If required flags aren't set, it will return with error before we could set up logging
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "%v\n", err)
		stop()
		os.Exit(1)
	}

}

func newApp() (context.Context, context.CancelFunc, *cli.App) {
	logInstance := &atomic.Value{}
	logInstance.Store(logr.Discard())
	app := &cli.App{
		Name:                 appName,
		Usage:                appLongName,
		Version:              fmt.Sprintf("%s, revision=%s, date=%s", version, commit, date),
		EnableBashCompletion: true,

		Before: setupLogging,
		Flags: []cli.Flag{
			&cli.IntFlag{
				Name: "log-level", Aliases: []string{"v"}, EnvVars: envVars("LOG_LEVEL"),
				Usage: "number of the log level verbosity",
				Value: 0,
			},
			newLogFormatFlag(),
		},
		Commands: []*cli.Command{
			newOperatorCommand(),
			newScanCommand(),
			newCountCommand(),
			newCleanupCommand(),
		},
		ExitErrHandler: func(ctx *cli.Context, err error) {
			if err != nil {
				AppLogger(ctx).Error(err, "fatal error")
				cli.HandleExitCoder(cli.Exit("", 1))
			}
		},
	}
	hasSubcommands := len(app.Commands) > 0
	app.Action = rootAction(hasSubcommands)
	// There is logr.NewContext(...) which returns a context that carries the logger instance.
	// However, since we are configuring and replacing this logger after starting up and parsing the flags,
	// we'll store a thread-safe atomic reference.
	parentCtx := context.WithValue(context.Background(), loggerContextKey{}, logInstance)
	ctx, stop := signal.NotifyContext(parentCtx, syscall.SIGINT, syscall.SIGTERM)
	return ctx, stop, app
}

func rootAction(hasSubcommands bool) func(context *cli.Context) error {
	return func(ctx *cli.Context) error {
		if hasSubcommands {
			return cli.ShowAppHelp(ctx)
		}
		return LogMetadata(ctx)
	}
}

// env combines envPrefix with given suffix delimited by underscore.
func env(suffix string) string {
	return envPrefix + "_" + suffix
}

// envVars combines envPrefix with each given suffix delimited by underscore.
func envVars(suffixes ...string) []string {
	arr := make([]string, len(suffixes))
	for i := range suffixes {
		arr[i] = env(suffixes[i])
	}
	return arr
}
