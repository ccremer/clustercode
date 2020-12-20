package cmd

import (
	"fmt"
	"os"
	"strings"

	"github.com/knadh/koanf"
	"github.com/knadh/koanf/providers/env"
	"github.com/knadh/koanf/providers/posflag"
	"github.com/spf13/cobra"
	"github.com/spf13/pflag"
	"go.uber.org/zap/zapcore"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/log/zap"

	"github.com/ccremer/clustercode/cfg"
)

// rootCmd represents the base command when called without any subcommands
var (
	rootCmd = &cobra.Command{
		Use:               "clustercode",
		Short:             "clustercode brings media encoding into Kubernetes",
		Long:              `clustercode scans media volumes for media files, splits them up into multiple segment and encodes them in parallel.`,
		PersistentPreRunE: parseConfig,
		// Uncomment the following line if your bare application
		// has an action associated with it:
		//	Run: func(cmd *cobra.Command, args []string) { },
	}

	setupLog = ctrl.Log.WithName("setup")
	// Global koanfInstance instance. Use . as the key path delimiter.
	koanfInstance = koanf.New(".")
	version       = "undefined"
)

// Execute adds all child commands to the root command and sets flags appropriately.
// This is called by main.main(). It only needs to happen once to the rootCmd.
func Execute() {
	if err := rootCmd.Execute(); err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
}

func initRootConfig() {
	if err := bindFlags(rootCmd.Flags()); err !=nil {
		fmt.Println(err)
		os.Exit(1)
	}
}

func init() {
	rootCmd.PersistentFlags().BoolP("log.debug", "v", cfg.Config.Log.Debug, "Enable debug log")
	cobra.OnInitialize(initRootConfig)
}

// parseConfig reads the flags and ENV vars
func parseConfig(cmd *cobra.Command, args []string) error {
	if err := loadEnvironmentVariables(); err != nil {
		return err
	}
	if err := bindFlags(cmd.PersistentFlags()); err != nil {
		return err
	}
	if err := koanfInstance.Unmarshal("", &cfg.Config); err != nil {
		return fmt.Errorf("could not read config: %w", err)
	}

	level := zapcore.InfoLevel
	if cfg.Config.Log.Debug {
		level = zapcore.DebugLevel
	}
	ctrl.SetLogger(zap.New(zap.UseDevMode(true), zap.Level(level)))
	setupLog.Info("Starting Clustercode", "version", version, "command", cmd.Name())
	setupLog.V(1).Info("using config", "config", cfg.Config)
	return cfg.Config.ValidateSyntax()
}

func loadEnvironmentVariables() error {
	prefix := "CC_"
	return koanfInstance.Load(env.Provider(prefix, ".", func(s string) string {
		/*
			Configuration can contain hierarchies (YAML, etc.) and CLI flags dashes. To read environment variables with
			hierarchies and dashes we replace the hierarchy delimiter with double underscore and dashes with single underscore,
			so that parent.child-with-dash becomes PARENT__CHILD_WITH_DASH
		*/
		s = strings.TrimPrefix(s, prefix)
		s = strings.Replace(strings.ToLower(s), "__", ".", -1)
		s = strings.Replace(strings.ToLower(s), "_", "-", -1)
		return s
	}), nil)
}

func bindFlags(flagSet *pflag.FlagSet) error {
	return koanfInstance.Load(posflag.Provider(flagSet, ".", koanfInstance), nil)
}

// SetVersion sets the version string in the help messages
func SetVersion(v string) {
	// We need to set both properties in order to break an initialization loop
	rootCmd.Version = v
	version = v
}
