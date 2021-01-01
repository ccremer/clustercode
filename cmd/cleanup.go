package cmd

import (
	"context"
	"fmt"
	"os"
	"path/filepath"

	"github.com/spf13/cobra"
	"k8s.io/apimachinery/pkg/types"
	ctrl "sigs.k8s.io/controller-runtime"

	"github.com/ccremer/clustercode/api/v1alpha1"
	"github.com/ccremer/clustercode/cfg"
	"github.com/ccremer/clustercode/controllers"
)

// cleanupCmd represents the cleanup command
var (
	cleanupCmd = &cobra.Command{
		Use:     "cleanup",
		Short:   "removes intermediary files and finishes the task",
		PreRunE: validateCleanupCmd,
		RunE:    runCleanupCmd,
	}
	cleanupLog = ctrl.Log.WithName("cleanup")
)

func init() {
	rootCmd.AddCommand(cleanupCmd)

	cleanupCmd.PersistentFlags().String("cleanup.task-name", cfg.Config.Cleanup.TaskName, "ClustercodeTask Name")
}

func validateCleanupCmd(cmd *cobra.Command, args []string) error {
	if cfg.Config.Cleanup.TaskName == "" {
		return fmt.Errorf("'%s' cannot be empty", "cleanup.task-name")
	}
	if cfg.Config.Namespace == "" {
		return fmt.Errorf("'%s' cannot be empty", "namespace")
	}
	return nil
}

func runCleanupCmd(cmd *cobra.Command, args []string) error {
	registerScheme()
	if err := createClient(); err != nil {
		return err
	}

	nsName := types.NamespacedName{Namespace: cfg.Config.Namespace, Name: cfg.Config.Cleanup.TaskName}
	task := &v1alpha1.ClustercodeTask{}
	cleanupLog.Info("get clustercode task", "name", nsName.String())
	if err := client.Get(context.Background(), nsName, task); err != nil {
		return err
	}

	intermediaryFiles, err := filepath.Glob(filepath.Join(cfg.Config.Scan.SourceRoot, controllers.IntermediateSubMountPath, task.Spec.TaskId.String()+"*"))
	if err != nil {
		return err
	}
	cleanupLog.Info("deleting intermediary files", "files", intermediaryFiles)
	deleteFiles(intermediaryFiles)

	sourceFile := filepath.Join(cfg.Config.Scan.SourceRoot, controllers.SourceSubMountPath, task.Spec.SourceUrl.GetPath())
	cleanupLog.Info("deleting source file", "file", sourceFile)
	if err := os.Remove(sourceFile); err != nil {
		return err
	}
	if err := client.Delete(context.Background(), task); err != nil {
		return err
	}
	return nil
}

func deleteFiles(files []string) {
	for _, file := range files {
		if err := os.Remove(file); err != nil {
			cleanupLog.Info("could not delete file", "file", file, "error", err.Error())
		} else {
			cleanupLog.V(1).Info("deleted file", "file", file)
		}
	}
}
