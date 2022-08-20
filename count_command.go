package main

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"sort"
	"strings"

	"github.com/ccremer/clustercode/api/v1alpha1"
	"github.com/ccremer/clustercode/pkg/operator/controllers"
	"github.com/urfave/cli/v2"
	v1 "k8s.io/api/core/v1"
	apierrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/client-go/rest"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type countCommand struct {
	kubeconfig *rest.Config
	kube       client.Client

	TaskName      string
	TaskNamespace string
	SourceRootDir string
}

var countCommandName = "count"
var countLog = ctrl.Log.WithName("count")

func newCountCommand() *cli.Command {
	command := &countCommand{}
	return &cli.Command{
		Name:   countCommandName,
		Usage:  "Counts the number of generated intermediary media files",
		Action: command.execute,
		Flags: []cli.Flag{
			newTaskNameFlag(&command.TaskName),
			newNamespaceFlag(&command.TaskNamespace),
			newSourceRootDirFlag(&command.SourceRootDir),
		},
	}
}

func (c *countCommand) execute(ctx *cli.Context) error {

	registerScheme()
	err := createClientFn(&commandContext{})
	if err != nil {
		return err
	}
	task, err := c.getTask()
	if err != nil {
		return err
	}
	countLog = countLog.WithValues("task", task.Name)
	countLog.Info("found task", "task", task)

	files, err := c.scanSegmentFiles(task.Spec.TaskId.String() + "_")
	if err != nil {
		return err
	}
	countLog.Info("found segments", "count", len(files))

	err = c.createFileList(files, task)
	if err != nil {
		return err
	}

	err = c.updateTask(task, len(files))
	if err != nil {
		return err
	}
	countLog.Info("updated task")

	return nil
}

func (c *countCommand) updateTask(task *v1alpha1.Task, count int) error {
	task.Spec.SlicesPlannedCount = count
	err := c.kube.Update(context.Background(), task)
	if err != nil {
		return err
	}
	return nil
}

func (c *countCommand) createFileList(files []string, task *v1alpha1.Task) error {
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
	if err := c.kube.Create(context.Background(), cm); err != nil {
		if apierrors.IsAlreadyExists(err) {
			if err := c.kube.Update(context.Background(), cm); err != nil {
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

func (c *countCommand) scanSegmentFiles(prefix string) ([]string, error) {
	var files []string
	root := filepath.Join(c.SourceRootDir, controllers.IntermediateSubMountPath)
	err := filepath.Walk(root, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			// could not access file, let's prevent a panic
			return err
		}
		if info.IsDir() {
			return nil
		}
		if matchesTaskSegment(path, prefix) {
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

func (c *countCommand) getTask() (*v1alpha1.Task, error) {
	ctx := context.Background()
	task := &v1alpha1.Task{}
	name := types.NamespacedName{
		Name:      c.TaskName,
		Namespace: c.TaskNamespace,
	}
	err := c.kube.Get(ctx, name, task)
	if err != nil {
		return &v1alpha1.Task{}, err
	}
	return task, nil
}
