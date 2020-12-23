package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
	ctrl "sigs.k8s.io/controller-runtime"

	"github.com/ccremer/clustercode/cfg"
	"github.com/ccremer/clustercode/controllers"
)

// operateCmd represents the operate command
var (
	operateCmd = &cobra.Command{
		Use:   "operate",
		Short: "Starts Clustercode in Operator mode",
		RunE:  startOperator,
	}
)

func init() {
	rootCmd.AddCommand(operateCmd)
	// +kubebuilder:scaffold:scheme

	operateCmd.PersistentFlags().String("operator.metrics-bind-address", cfg.Config.Operator.MetricsBindAddress, "Prometheus metrics bind address")
	operateCmd.PersistentFlags().String("operator.watch-namespace", cfg.Config.Operator.WatchNamespace,
		"Restrict watching objects to the specified namespace. Watches all namespaces if left empty")
}

func startOperator(cmd *cobra.Command, args []string) error {

	registerScheme()
	mgr, err := ctrl.NewManager(ctrl.GetConfigOrDie(), ctrl.Options{
		Scheme:             scheme,
		MetricsBindAddress: cfg.Config.Operator.MetricsBindAddress,
		Port:               9443,
		LeaderElection:     cfg.Config.Operator.EnableLeaderElection,
		LeaderElectionID:   "clustercode.github.io",
		Namespace:          cfg.Config.Operator.WatchNamespace,
	})
	if err != nil {
		return fmt.Errorf("unable to start operator: %w", err)
	}

	if err = (&controllers.ClustercodePlanReconciler{
		Client: mgr.GetClient(),
		Log:    ctrl.Log.WithName("controllers").WithName("clustercodeplan"),
		Scheme: mgr.GetScheme(),
	}).SetupWithManager(mgr); err != nil {
		return fmt.Errorf("unable to create controller '%s': %w", "clustercodeplan", err)
	}
	// +kubebuilder:scaffold:builder

	if err := mgr.Start(ctrl.SetupSignalHandler()); err != nil {
		setupLog.Error(err, "problem running operator")
		return err
	}
	return nil
}
