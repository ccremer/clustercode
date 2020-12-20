package main

import (
	"os"
	"strings"

	"github.com/knadh/koanf"
	"github.com/knadh/koanf/providers/env"
	batchv1 "k8s.io/api/batch/v1"
	"k8s.io/apimachinery/pkg/runtime"
	utilruntime "k8s.io/apimachinery/pkg/util/runtime"
	clientgoscheme "k8s.io/client-go/kubernetes/scheme"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/log/zap"

	"github.com/ccremer/clustercode/cfg"
	"github.com/ccremer/clustercode/controllers"
)

var (
	// These will be populated by Goreleaser
	version string
	commit  string
	date    string

	scheme   = runtime.NewScheme()
	setupLog = ctrl.Log.WithName("setup")
	// Global koanfInstance instance. Use . as the key path delimiter.
	koanfInstance = koanf.New(".")
)

func init() {
	utilruntime.Must(clientgoscheme.AddToScheme(scheme))

	utilruntime.Must(batchv1.AddToScheme(scheme))
	// +kubebuilder:scaffold:scheme
}

func main() {

	ctrl.SetLogger(zap.New(zap.UseDevMode(true)))

	loadEnvironmentVariables()

	mgr, err := ctrl.NewManager(ctrl.GetConfigOrDie(), ctrl.Options{
		Scheme:             scheme,
		MetricsBindAddress: cfg.Config.MetricsBindAddress,
		Port:               9443,
		LeaderElection:     cfg.Config.EnableLeaderElection,
		LeaderElectionID:   "clustercode.github.io",
	})
	if err != nil {
		setupLog.Error(err, "unable to start operator")
		os.Exit(1)
	}

	if err = (&controllers.ClustercodePlanReconciler{
		Client: mgr.GetClient(),
		Log:    ctrl.Log.WithName("controllers").WithName("clustercodeplan"),
		Scheme: mgr.GetScheme(),
	}).SetupWithManager(mgr); err != nil {
		setupLog.Error(err, "unable to create controller", "controller", "clustercodeplan")
		os.Exit(1)
	}
	// +kubebuilder:scaffold:builder

	setupLog.WithValues("version", version, "date", date, "commit", commit).Info("Starting operator")
	if err := mgr.Start(ctrl.SetupSignalHandler()); err != nil {
		setupLog.Error(err, "problem running operator")
		os.Exit(1)
	}
}

func loadEnvironmentVariables() {
	prefix := "CC_"
	// Load environment variables
	err := koanfInstance.Load(env.Provider(prefix, ".", func(s string) string {
		s = strings.TrimLeft(s, prefix)
		s = strings.Replace(strings.ToLower(s), "_", "-", -1)
		return s
	}), nil)
	if err != nil {
		setupLog.Error(err, "could not load environment variables")
	}

	if err := koanfInstance.UnmarshalWithConf("", &cfg.Config, koanf.UnmarshalConf{Tag: "koanf", FlatPaths: true}); err != nil {
		setupLog.Error(err, "could not merge defaults with settings from environment variables")
	}
	if err := cfg.Config.ValidateSyntax(); err != nil {
		setupLog.Error(err, "settings invalid")
		os.Exit(2)
	}
}
