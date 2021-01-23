package cmd

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"sort"
	"strings"

	"github.com/spf13/cobra"
	v1 "k8s.io/api/core/v1"
	apierrors "k8s.io/apimachinery/pkg/api/errors"
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

	countCmd.PersistentFlags().String("count.task-name", cfg.Config.Count.TaskName, "Task Name")
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
	task, err := getTask()
	if err != nil {
		return err
	}
	countLog = countLog.WithValues("task", task.Name)
	countLog.Info("found task", "task", task)

	files, err := scanSegmentFiles(task.Spec.TaskId.String() + "_")
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

func updateTask(task *v1alpha1.Task, count int) error {
	task.Spec.SlicesPlannedCount = count
	err := client.Update(context.Background(), task)
	if err != nil {
		return err
	}
	return nil
}

func createFileList(files []string, task *v1alpha1.Task) error {
	var fileList []string
	for _, file := range files {
		fileList = append(fileList, fmt.Sprintf("file '%s'", file))
	}
	data := strings.Join(fileList, "\n") + "\n"
	cm := &v1.ConfigMap{
		ObjectMeta: metav1.ObjectMeta{
			Name:      task.Spec.FileListConfigMapRef,
			Namespace: task.Namespace,
			Labels:    labels.Merge(controllers.ClusterCodeLabels, task.Spec.TaskId.AsLabels()),
		},
		Data: map[string]string{
			v1alpha1.ConfigMapFileName: data,
		},
	}
	if err := controllerutil.SetControllerReference(task, cm.GetObjectMeta(), scheme); err != nil {
		return fmt.Errorf("could not set controller reference: %w", err)
	}
	if err := client.Create(context.Background(), cm); err != nil {
		if apierrors.IsAlreadyExists(err) {
			if err := client.Update(context.Background(), cm); err != nil {
				return fmt.Errorf("could not update config map: %w", err)
			}
			countLog.Info("updated config map", "configmap", cm.Name)
		}
		return fmt.Errorf("could not create config map: %w", err)
	} else {
		countLog.Info("created config map", "configmap", cm.Name, "data", cm.Data)
	}
	return nil
}

func scanSegmentFiles(prefix string) ([]string, error) {
	var files []string
	root := filepath.Join(cfg.Config.Scan.SourceRoot, controllers.IntermediateSubMountPath)
	err := filepath.Walk(root, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			// could not access file, let's prevent a panic
			return err
		}
		if info.IsDir() {
			return nil
		}
		if !matchesTaskSegment(path, prefix) {
			return nil
		}
		files = append(files, path)
		return nil
	})
	if len(files) <= 0 {
		return files, fmt.Errorf("could not find any segments in '%s", root)
	}
	sort.Strings(files)
	return files, err
}

func matchesTaskSegment(path string, prefix string) bool {
	base := filepath.Base(path)
	return strings.HasPrefix(base, prefix) && !strings.Contains(base, v1alpha1.MediaFileDoneSuffix)
}

func getTask() (*v1alpha1.Task, error) {
	ctx := context.Background()
	task := &v1alpha1.Task{}
	name := types.NamespacedName{
		Name:      cfg.Config.Count.TaskName,
		Namespace: cfg.Config.Namespace,
	}
	err := client.Get(ctx, name, task)
	if err != nil {
		return &v1alpha1.Task{}, err
	}
	return task, nil
}
