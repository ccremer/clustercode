package cmd

import (
	"context"
	"fmt"

	"github.com/spf13/cobra"
	"k8s.io/apimachinery/pkg/types"
	ctrl "sigs.k8s.io/controller-runtime"
	controllerclient "sigs.k8s.io/controller-runtime/pkg/client"

	"github.com/ccremer/clustercode/api/v1alpha1"
	"github.com/ccremer/clustercode/cfg"
)

// scanCmd represents the scan command
var (
	scanCmd = &cobra.Command{
		Use:     "scan",
		Short:   "A brief description of your command",
		PreRunE: validateScanCmd,
		RunE:    scanMedia,
	}
	scanLog = ctrl.Log.WithName("scan")
)

func validateScanCmd(cmd *cobra.Command, args []string) error {
	if cfg.Config.Scan.ClustercodePlanName == "" {
		return fmt.Errorf("'%s' cannot be empty", "scan.clustercode-plan-name")
	}
	if cfg.Config.Scan.Namespace == "" {
		return fmt.Errorf("'%s' cannot be empty", "scan.namespace")
	}
	return nil
}

func init() {
	rootCmd.AddCommand(scanCmd)

	scanCmd.PersistentFlags().String("scan.clustercode-plan-name", cfg.Config.Scan.ClustercodePlanName, "Clustercode Plan name (namespace/name)")
	scanCmd.PersistentFlags().StringP("scan.namespace", "n", cfg.Config.Scan.Namespace, "Namespace")
}

func scanMedia(cmd *cobra.Command, args []string) error {

	registerScheme()
	client, err := controllerclient.New(ctrl.GetConfigOrDie(), controllerclient.Options{Scheme: scheme})
	if err != nil {
		return err
	}
	ctx := context.Background()
	plan := v1alpha1.ClustercodePlan{}
	name := types.NamespacedName{
		Name: cfg.Config.Scan.ClustercodePlanName,
		Namespace: cfg.Config.Scan.Namespace,
	}
	err = client.Get(ctx, name, &plan)
	if err != nil {
		return err
	}
	scanLog.Info("found plan", "plan", plan)
	return nil
}
