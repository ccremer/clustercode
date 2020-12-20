package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
	batchv1 "k8s.io/api/batch/v1"
	"k8s.io/apimachinery/pkg/runtime"
	utilruntime "k8s.io/apimachinery/pkg/util/runtime"
	clientgoscheme "k8s.io/client-go/kubernetes/scheme"
	ctrl "sigs.k8s.io/controller-runtime"

	"github.com/ccremer/clustercode/cfg"
	"github.com/ccremer/clustercode/controllers"
)

// operateCmd represents the operate command
var (
	scheme     = runtime.NewScheme()
	operateCmd = &cobra.Command{
		Use:   "operate",
		Short: "Starts Clustercode in Operator mode",
		RunE:  startOperator,
	}
)

func init() {
	rootCmd.AddCommand(operateCmd)
	utilruntime.Must(clientgoscheme.AddToScheme(scheme))

	utilruntime.Must(batchv1.AddToScheme(scheme))
	// +kubebuilder:scaffold:scheme

	operateCmd.PersistentFlags().String("operator.metrics-bind-address", cfg.Config.Operator.MetricsBindAddress, "Prometheus metrics bind address")
}

func startOperator(cmd *cobra.Command, args []string) error {

	mgr, err := ctrl.NewManager(ctrl.GetConfigOrDie(), ctrl.Options{
		Scheme:             scheme,
		MetricsBindAddress: cfg.Config.Operator.MetricsBindAddress,
		Port:               9443,
		LeaderElection:     cfg.Config.Operator.EnableLeaderElection,
		LeaderElectionID:   "clustercode.github.io",
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
