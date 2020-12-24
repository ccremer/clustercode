package cmd

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"strings"

	"github.com/spf13/cobra"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/apimachinery/pkg/util/uuid"
	ctrl "sigs.k8s.io/controller-runtime"
	controllerclient "sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"

	"github.com/ccremer/clustercode/api/v1alpha1"
	"github.com/ccremer/clustercode/cfg"
	"github.com/ccremer/clustercode/controllers"
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
	// client is the K8s client for scan command
	client controllerclient.Client
)

func validateScanCmd(cmd *cobra.Command, args []string) error {
	if cfg.Config.Scan.ClustercodePlanName == "" {
		return fmt.Errorf("'%s' cannot be empty", "scan.clustercode-plan-name")
	}
	if cfg.Config.Scan.Namespace == "" {
		return fmt.Errorf("'%s' cannot be empty", "scan.namespace")
	}
	if !(cfg.Config.Scan.RoleKind == cfg.ClusterRole || cfg.Config.Scan.RoleKind == cfg.Role) {
		return fmt.Errorf("scan.role-kind (%s) is not in %s", cfg.Config.Scan.RoleKind, []string{cfg.ClusterRole, cfg.Role})
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
	err := createClient()
	if err != nil {
		return err
	}
	plan, err := getClustercodePlan()
	if err != nil {
		return err
	}
	scanLog.Info("found plan", "plan", plan)

	if plan.IsMaxParallelTaskLimitReached() {
		scanLog.Info("max parallel task count is reached, ignoring scan")
		return nil
	}

	files, err := scanSourceForMedia(plan)
	if err != nil {
		return err
	}

	if len(files) <= 0 {
		scanLog.Info("no media files found")
		return nil
	}

	task := &v1alpha1.ClustercodeTask{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: cfg.Config.Scan.Namespace,
			Name: string(uuid.NewUUID()),
			Labels: controllers.ClusterCodeLabels,
		},
		Spec:       v1alpha1.ClustercodeTaskSpec{
			SourceUrl:  files[0],
			TargetUrl:  files[0],
			Suspend:    false,
			EncodeSpec: plan.Spec.EncodeSpec,
		},
	}
	if err := controllerutil.SetControllerReference(plan, task.GetObjectMeta(), scheme); err != nil {
		scanLog.Error(err, "could not set controller reference. Deleting the plan might not delete this task")
	}
	if err := client.Create(context.Background(), task); err != nil {
		return fmt.Errorf("could not create task: %w", err)
	} else {
		scanLog.Info("created task", "task", task.Name, "source", task.Spec.SourceUrl)
	}
	return nil
}

func scanSourceForMedia(plan *v1alpha1.ClustercodePlan) (files []string, funcErr error) {
	root := cfg.Config.Scan.SourceRoot
	err := filepath.Walk(root, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			// could not access file, let's prevent a panic
			return err
		}
		if info.IsDir() {
			return nil
		}
		if !containsExtension(filepath.Ext(path), plan.Spec.ScanSpec.MediaFileExtensions) {
			scanLog.V(1).Info("file extension not accepted", "path", path)
			return nil
		}

		files = append(files, path)
		return nil
	})

	return files, err
}

func getClustercodePlan() (*v1alpha1.ClustercodePlan, error) {
	ctx := context.Background()
	plan := &v1alpha1.ClustercodePlan{}
	name := types.NamespacedName{
		Name:      cfg.Config.Scan.ClustercodePlanName,
		Namespace: cfg.Config.Scan.Namespace,
	}
	err := client.Get(ctx, name, plan)
	if err != nil {
		return &v1alpha1.ClustercodePlan{}, err
	}
	return plan, nil
}

func createClient() error {
	clientConfig, err := ctrl.GetConfig()
	if err != nil {
		return err
	}
	client, err = controllerclient.New(clientConfig, controllerclient.Options{Scheme: scheme})
	if err != nil {
		return err
	}
	return nil
}

// containsExtension returns true if the given extension is in the given acceptableFileExtensions. For each entry in the list,
// the leading "." prefix is optional. The leading "." is mandatory for `extension` and it returns false if extension is empty
func containsExtension(extension string, acceptableFileExtensions []string) bool {
	if extension == "" {
		return false
	}
	for _, ext := range acceptableFileExtensions {
		if strings.HasPrefix(ext, ".") {
			if extension == ext {
				return true
			}
			continue
		}
		if extension == "."+ext {
			return true
		}
	}
	return false
}
