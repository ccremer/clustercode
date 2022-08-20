package main

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"strings"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	"github.com/urfave/cli/v2"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/apimachinery/pkg/util/uuid"
	"k8s.io/client-go/rest"
	"k8s.io/utils/pointer"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	controllerclient "sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type scanCommand struct {
	kubeconfig *rest.Config
	kube       client.Client

	BlueprintName      string
	BlueprintNamespace string
	SourceRoot         string
	RoleKind           string
}

const (
	ClusterRole = "ClusterRole"
	Role        = "Role"
)

var scanCommandName = "scan"
var scanLog = ctrl.Log.WithName("scan")

func newScanCommand() *cli.Command {
	command := &scanCommand{}
	return &cli.Command{
		Name:   scanCommandName,
		Usage:  "Scan source storage for new files",
		Action: command.execute,
		Flags: []cli.Flag{
			newBlueprintNameFlag(&command.BlueprintName),
			newNamespaceFlag(&command.BlueprintNamespace),
			newSourceRootDirFlag(&command.SourceRoot),
		},
	}
}

func (c *scanCommand) execute(ctx *cli.Context) error {
	registerScheme()
	err := createClientFn(&commandContext{})
	if err != nil {
		return err
	}
	bp, err := c.getBlueprint(ctx.Context)
	if err != nil {
		return err
	}
	scanLog.Info("found bp", "bp", bp)

	if bp.IsMaxParallelTaskLimitReached() {
		scanLog.Info("max parallel task count is reached, ignoring scan")
		return nil
	}

	tasks, err := c.getCurrentTasks(ctx.Context, bp)
	if err != nil {
		return err
	}
	scanLog.Info("get list of current tasks", "tasks", tasks)
	existingFiles := c.mapAndFilterTasks(tasks, bp)
	files, err := c.scanSourceForMedia(bp, existingFiles)
	if err != nil {
		return err
	}

	if len(files) <= 0 {
		scanLog.Info("no media files found")
		return nil
	}

	selectedFile, err := filepath.Rel(filepath.Join(c.SourceRoot, internaltypes.SourceSubMountPath), files[0])

	taskId := string(uuid.NewUUID())
	task := &v1alpha1.Task{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: c.BlueprintNamespace,
			Name:      taskId,
			Labels:    internaltypes.ClusterCodeLabels,
		},
		Spec: v1alpha1.TaskSpec{
			TaskId:               v1alpha1.ClustercodeTaskId(taskId),
			SourceUrl:            v1alpha1.ToUrl(internaltypes.SourceSubMountPath, selectedFile),
			TargetUrl:            v1alpha1.ToUrl(internaltypes.TargetSubMountPath, selectedFile),
			EncodeSpec:           bp.Spec.EncodeSpec,
			Storage:              bp.Spec.Storage,
			ServiceAccountName:   bp.GetServiceAccountName(),
			FileListConfigMapRef: taskId + "-slice-list",
			ConcurrencyStrategy:  bp.Spec.TaskConcurrencyStrategy,
		},
	}
	if err := controllerutil.SetControllerReference(bp, task.GetObjectMeta(), scheme); err != nil {
		scanLog.Error(err, "could not set controller reference. Deleting the bp might not delete this task")
	}
	if err := c.kube.Create(ctx.Context, task); err != nil {
		return fmt.Errorf("could not create task: %w", err)
	} else {
		scanLog.Info("created task", "task", task.Name, "source", task.Spec.SourceUrl)
	}
	return nil
}

func (c *scanCommand) mapAndFilterTasks(tasks []v1alpha1.Task, bp *v1alpha1.Blueprint) []string {

	var sourceFiles []string
	for _, task := range tasks {
		if task.GetDeletionTimestamp() != nil {
			continue
		}
		sourceFiles = append(sourceFiles, c.getAbsolutePath(task.Spec.SourceUrl))
	}

	return sourceFiles
}

func (c *scanCommand) getAbsolutePath(uri v1alpha1.ClusterCodeUrl) string {
	return filepath.Join(c.SourceRoot, uri.GetRoot(), uri.GetPath())
}

func (c *scanCommand) getCurrentTasks(ctx context.Context, bp *v1alpha1.Blueprint) ([]v1alpha1.Task, error) {
	list := v1alpha1.TaskList{}
	err := c.kube.List(ctx, &list,
		controllerclient.MatchingLabels(internaltypes.ClusterCodeLabels),
		controllerclient.InNamespace(bp.Namespace))
	if err != nil {
		return list.Items, err
	}
	var tasks []v1alpha1.Task
	for _, task := range list.Items {
		for _, owner := range task.GetOwnerReferences() {
			if pointer.BoolPtrDerefOr(owner.Controller, false) && owner.Name == bp.Name {
				tasks = append(tasks, task)
			}
		}
	}
	return list.Items, err
}

func (c *scanCommand) scanSourceForMedia(bp *v1alpha1.Blueprint, skipFiles []string) (files []string, funcErr error) {
	root := filepath.Join(c.SourceRoot, internaltypes.SourceSubMountPath)
	err := filepath.Walk(root, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			// could not access file, let's prevent a panic
			return err
		}
		if info.IsDir() {
			return nil
		}
		if !containsExtension(filepath.Ext(path), bp.Spec.ScanSpec.MediaFileExtensions) {
			scanLog.V(1).Info("file extension not accepted", "path", path)
			return nil
		}
		for _, skipFile := range skipFiles {
			if skipFile == path {
				scanLog.V(1).Info("skipping already queued file", "path", path)
				return nil
			}
		}

		files = append(files, path)
		return nil
	})

	return files, err
}

func (c *scanCommand) getBlueprint(ctx context.Context) (*v1alpha1.Blueprint, error) {
	bp := &v1alpha1.Blueprint{}
	name := types.NamespacedName{
		Name:      c.BlueprintName,
		Namespace: c.BlueprintNamespace,
	}
	err := c.kube.Get(ctx, name, bp)
	if err != nil {
		return &v1alpha1.Blueprint{}, err
	}
	return bp, nil
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
