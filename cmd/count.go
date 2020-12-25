package cmd

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"strings"

	"github.com/spf13/cobra"
	v1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/types"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"

	"github.com/ccremer/clustercode/api/v1alpha1"
	"github.com/ccremer/clustercode/cfg"
	"github.com/ccremer/clustercode/controllers"
)

// countCmd represents the count command
var (
	countCmd = &cobra.Command{
		Use:     "count",
		Short:   "counts the number of generated intermediary media files",
		PreRunE: validateCountCmd,
		RunE:    runCountCmd,
	}
	countLog = ctrl.Log.WithName("count")
)

func init() {
	rootCmd.AddCommand(countCmd)

	countCmd.PersistentFlags().String("count.task-name", cfg.Config.Count.TaskName, "ClustercodeTask Name")
}

func validateCountCmd(cmd *cobra.Command, args []string) error {
	if cfg.Config.Count.TaskName == "" {
		return fmt.Errorf("'%s' cannot be empty", "count.task-name")
	}
	if cfg.Config.Namespace == "" {
		return fmt.Errorf("'%s' cannot be empty", "namespace")
	}
	return nil
}

func runCountCmd(cmd *cobra.Command, args []string) error {

	registerScheme()
	err := createClient()
	if err != nil {
		return err
	}
	task, err := getClustercodeTask()
	if err != nil {
		return err
	}
	countLog.Info("found task", "task", task)

	files, err := scanSegmentFiles(task.Spec.TaskId + "_")
	if err != nil {
		return err
	}
	countLog.Info("found segments", "count", len(files))

	err = createFileList(files, task)
	if err != nil {
		return err
	}

	err = updateTask(task, len(files))
	if err != nil {
		return err
	}
	countLog.Info("updated task")

	return nil
}

func updateTask(task *v1alpha1.ClustercodeTask, count int) error {
	task.Status.SlicesPlanned = count
	return client.Status().Update(context.Background(), task)
}

func createFileList(files []string, task *v1alpha1.ClustercodeTask) error {
	fileList := strings.Join(files, "\n")
	cm := &v1.ConfigMap{
		ObjectMeta: metav1.ObjectMeta{
			Name:      task.Spec.FileListConfigMapRef,
			Namespace: task.Namespace,
			Labels:    labels.Merge(controllers.ClusterCodeLabels, controllers.JobIdLabel(task.Spec.TaskId)),
		},
		Data: map[string]string{
			"file-list.txt": fileList,
		},
	}
	if err := controllerutil.SetControllerReference(task, cm.GetObjectMeta(), scheme); err != nil {
		countLog.Error(err, "could not set controller reference. Deleting the task might not delete this config map")
	}
	if err := client.Create(context.Background(), cm); err != nil {
		return fmt.Errorf("could not create config map: %w", err)
	} else {
		countLog.Info("created config map", "configmap", cm.Name)
	}
	return nil
}

func scanSegmentFiles(prefix string) (files []string, funcErr error) {
	root := filepath.Join(cfg.Config.Scan.SourceRoot, controllers.IntermediateSubMountPath)
	err := filepath.Walk(root, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			// could not access file, let's prevent a panic
			return err
		}
		if info.IsDir() {
			return nil
		}
		if !strings.HasPrefix(filepath.Base(path), prefix) {
			return nil
		}
		files = append(files, path)
		return nil
	})
	return files, err
}

func getClustercodeTask() (*v1alpha1.ClustercodeTask, error) {
	ctx := context.Background()
	task := &v1alpha1.ClustercodeTask{}
	name := types.NamespacedName{
		Name:      cfg.Config.Count.TaskName,
		Namespace: cfg.Config.Namespace,
	}
	err := client.Get(ctx, name, task)
	if err != nil {
		return &v1alpha1.ClustercodeTask{}, err
	}
	return task, nil
}
