package main

import (
	"log"
	"os"
	"runtime"
	"strings"
	"sync/atomic"

	"github.com/go-logr/logr"
	"github.com/go-logr/zapr"
	"github.com/urfave/cli/v2"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
)

type loggerContextKey struct{}

// AppLogger retrieves the application-wide logger instance from the cli.Context.
func AppLogger(c *cli.Context) logr.Logger {
	return c.Context.Value(loggerContextKey{}).(*atomic.Value).Load().(logr.Logger)
}

// LogMetadata prints various metadata to the root logger.
// It prints version, architecture and current user ID and returns nil.
func LogMetadata(c *cli.Context) error {
	logger := AppLogger(c)
	if !usesProductionLoggingConfig(c) {
		logger = logger.WithValues("version", version)
	}
	logger.WithValues(
		"date", date,
		"commit", commit,
		"go_os", runtime.GOOS,
		"go_arch", runtime.GOARCH,
		"go_version", runtime.Version(),
		"uid", os.Getuid(),
		"gid", os.Getgid(),
	).Info("Starting up " + appName)
	return nil
}

func setupLogging(c *cli.Context) error {
	logger := newZapLogger(appName, c.Bool("debug"), usesProductionLoggingConfig(c))
	c.Context.Value(loggerContextKey{}).(*atomic.Value).Store(logger)
	return nil
}

func usesProductionLoggingConfig(c *cli.Context) bool {
	return strings.EqualFold("JSON", c.String("log-format"))
}

func newZapLogger(name string, debug bool, useProductionConfig bool) logr.Logger {
	cfg := zap.NewDevelopmentConfig()
	cfg.EncoderConfig.ConsoleSeparator = " | "
	if useProductionConfig {
		cfg = zap.NewProductionConfig()
	}
	if debug {
		// Zap's levels get more verbose as the number gets smaller,
		// bug logr's level increases with greater numbers.
		cfg.Level = zap.NewAtomicLevelAt(zapcore.Level(-2)) // max logger.V(2)
	} else {
		cfg.Level = zap.NewAtomicLevelAt(zapcore.InfoLevel)
	}
	z, err := cfg.Build()
	zap.ReplaceGlobals(z)
	if err != nil {
		log.Fatalf("error configuring the logging stack")
	}
	logger := zapr.NewLogger(z).WithName(name)
	if useProductionConfig {
		// Append the version to each log so that logging stacks like EFK/Loki can correlate errors with specific versions.
		return logger.WithValues("version", version)
	}
	return logger
}
