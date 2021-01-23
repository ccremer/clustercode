package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
	batchv1 "k8s.io/api/batch/v1"
	ctrl "sigs.k8s.io/controller-runtime"
	controllerclient "sigs.k8s.io/controller-runtime/pkg/client"

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

	operateCmd.PersistentFlags().String("operator.metrics-bind-address", cfg.Config.Operator.MetricsBindAddress,
		"Prometheus metrics bind address.")
	operateCmd.PersistentFlags().String("operator.watch-namespace", cfg.Config.Operator.WatchNamespace,
		"Restrict watching objects to the specified namespace. Watches all namespaces if left empty.")
	operateCmd.PersistentFlags().String("operator.clustercode-image", cfg.Config.Operator.ClustercodeContainerImage,
		"Container image to be used when launching Clustercode jobs.")
	operateCmd.PersistentFlags().String("operator.ffmpeg-image", cfg.Config.Operator.FfmpegContainerImage,
		"Container image to be used when launching Ffmpeg jobs.")
	operateCmd.PersistentFlags().String("scan.role-name", cfg.Config.Scan.RoleName, "role name to be used for creating dynamic ServiceAccounts")
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

	uncached, err := controllerclient.NewDelegatingClient(controllerclient.NewDelegatingClientInput{
		CacheReader: mgr.GetClient(),
		Client:      mgr.GetClient(),
		UncachedObjects: []controllerclient.Object{
			&batchv1.Job{},
		},
	})
	if err != nil {
		return err
	}
	if err = (&controllers.BlueprintReconciler{
		Client: mgr.GetClient(),
		Log:    ctrl.Log.WithName("controllers").WithName("blueprint"),
		Scheme: mgr.GetScheme(),
	}).SetupWithManager(mgr); err != nil {
		return fmt.Errorf("unable to create controller '%s': %w", "blueprint", err)
	}
	if err = (&controllers.ClustercodeTaskReconciler{
		Client: mgr.GetClient(),
		Log:    ctrl.Log.WithName("controllers").WithName("clustercodetask"),
		Scheme: mgr.GetScheme(),
	}).SetupWithManager(mgr); err != nil {
		return fmt.Errorf("unable to create controller '%s': %w", "clustercodetask", err)
	}
	if err = (&controllers.JobReconciler{
		Client: uncached,
		Log:    ctrl.Log.WithName("controllers").WithName("job"),
		Scheme: mgr.GetScheme(),
	}).SetupWithManager(mgr); err != nil {
		return fmt.Errorf("unable to create controller '%s': %w", "job", err)
	}
	// +kubebuilder:scaffold:builder

	if err := mgr.Start(ctrl.SetupSignalHandler()); err != nil {
		setupLog.Error(err, "problem running operator")
		return err
	}
	return nil
}
