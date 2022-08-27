package main

import (
	"context"
	"fmt"
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

// SetLogger copies the application-wide logger instance from cli.Context to new context using logr.NewContext.
func SetLogger(ctx *cli.Context) context.Context {
	return logr.NewContext(ctx.Context, AppLogger(ctx))
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
	logger, err := newZapLogger(appName, c.Int("log-level"), usesProductionLoggingConfig(c))
	c.Context.Value(loggerContextKey{}).(*atomic.Value).Store(logger)
	return err
}

func usesProductionLoggingConfig(c *cli.Context) bool {
	return strings.EqualFold("JSON", c.String(newLogFormatFlag().Name))
}

func newZapLogger(name string, verbosityLevel int, useProductionConfig bool) (logr.Logger, error) {
	cfg := zap.NewDevelopmentConfig()
	cfg.EncoderConfig.ConsoleSeparator = " | "
	if useProductionConfig {
		cfg = zap.NewProductionConfig()
	}
	if verbosityLevel > 0 {
		// Zap's levels get more verbose as the number gets smaller,
		// bug logr's level increases with greater numbers.
		cfg.Level = zap.NewAtomicLevelAt(zapcore.Level(verbosityLevel * -1))
	} else {
		cfg.Level = zap.NewAtomicLevelAt(zapcore.InfoLevel)
	}
	z, err := cfg.Build()
	if err != nil {
		return logr.Discard(), fmt.Errorf("error configuring the logging stack: %w", err)
	}
	zap.ReplaceGlobals(z)
	logger := zapr.NewLogger(z).WithName(name)
	if useProductionConfig {
		// Append the version to each log so that logging stacks like EFK/Loki can correlate errors with specific versions.
		return logger.WithValues("version", version), nil
	}
	return logger, nil
}
