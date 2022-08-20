package main

import (
	"os"
	"path/filepath"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"github.com/ccremer/clustercode/pkg/operator/controllers"
	"github.com/urfave/cli/v2"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/client-go/rest"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type cleanupCommand struct {
	kubeconfig *rest.Config
	kube       client.Client

	TaskName      string
	TaskNamespace string
	SourceRootDir string
}

var cleanupCommandName = "cleanup"
var cleanupLog = ctrl.Log.WithName("cleanup")

func newCleanupCommand() *cli.Command {
	command := &cleanupCommand{}
	return &cli.Command{
		Name:   cleanupCommandName,
		Usage:  "Remove intermediary files and finish the task",
		Action: command.execute,
		Flags: []cli.Flag{
			newTaskNameFlag(&command.TaskName),
			newNamespaceFlag(&command.TaskNamespace),
			newSourceRootDirFlag(&command.SourceRootDir),
		},
	}
}

func (c *cleanupCommand) execute(ctx *cli.Context) error {

	registerScheme()
	if err := createClientFn(&commandContext{}); err != nil {
		return err
	}

	nsName := types.NamespacedName{Namespace: c.TaskNamespace, Name: c.TaskName}
	task := &v1alpha1.Task{}
	cleanupLog.Info("get task", "name", nsName.String())
	if err := c.kube.Get(ctx.Context, nsName, task); err != nil {
		return err
	}

	intermediaryFiles, err := filepath.Glob(filepath.Join(c.SourceRootDir, controllers.IntermediateSubMountPath, task.Spec.TaskId.String()+"*"))
	if err != nil {
		return err
	}
	cleanupLog.Info("deleting intermediary files", "files", intermediaryFiles)
	deleteFiles(intermediaryFiles)

	sourceFile := filepath.Join(c.SourceRootDir, controllers.SourceSubMountPath, task.Spec.SourceUrl.GetPath())
	cleanupLog.Info("deleting source file", "file", sourceFile)
	if err := os.Remove(sourceFile); err != nil {
		return err
	}
	if err := c.kube.Delete(ctx.Context, task); err != nil {
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
