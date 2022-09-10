package scancmd

import (
	"context"
	"errors"
	"os"
	"path/filepath"
	"strings"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"github.com/ccremer/clustercode/pkg/internal/pipe"
	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	pipeline "github.com/ccremer/go-command-pipeline"
	"github.com/go-logr/logr"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/apimachinery/pkg/util/uuid"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type Command struct {
	Log logr.Logger

	SourceRootDir string
	Namespace     string
	BlueprintName string
}

type commandContext struct {
	context.Context
	dependencyResolver pipeline.DependencyResolver[*commandContext]

	kube            client.Client
	blueprint       *v1alpha1.Blueprint
	segmentFiles    []string
	currentTasks    []v1alpha1.Task
	selectedRelPath string
}

var noMatchFoundErr = errors.New("no matching source file found")

// Execute runs the command and returns an error, if any.
func (c *Command) Execute(ctx context.Context) error {

	pctx := &commandContext{
		dependencyResolver: pipeline.NewDependencyRecorder[*commandContext](),
		Context:            ctx,
	}

	p := pipeline.NewPipeline[*commandContext]().WithBeforeHooks(pipe.DebugLogger[*commandContext](c.Log), pctx.dependencyResolver.Record)
	p.WithSteps(
		p.NewStep("create client", c.createClient),
		p.NewStep("fetch blueprint", c.fetchBlueprint),
		p.WithNestedSteps("schedule new task", c.hasFreeTaskSlots,
			p.NewStep("fetch current tasks", c.fetchCurrentTasks),
			p.NewStep("select new file", c.selectNewFile),
			p.NewStep("create task", c.createTask),
		).WithErrorHandler(c.abortIfNoMatchFound),
	)

	return p.RunWithContext(pctx)
}

func (c *Command) createClient(ctx *commandContext) error {
	kube, err := pipe.NewKubeClient(ctx)
	ctx.kube = kube
	return err
}

func (c *Command) fetchBlueprint(ctx *commandContext) error {
	ctx.dependencyResolver.MustRequireDependencyByFuncName(c.createClient)
	log := c.getLogger()

	blueprint := &v1alpha1.Blueprint{}
	if err := ctx.kube.Get(ctx, types.NamespacedName{Namespace: c.Namespace, Name: c.BlueprintName}, blueprint); err != nil {
		return err
	}
	ctx.blueprint = blueprint
	log.Info("fetched blueprint")
	return nil
}

func (c *Command) hasFreeTaskSlots(ctx *commandContext) bool {
	return !ctx.blueprint.IsMaxParallelTaskLimitReached()
}

func (c *Command) fetchCurrentTasks(ctx *commandContext) error {
	ctx.dependencyResolver.MustRequireDependencyByFuncName(c.createClient, c.fetchBlueprint)
	log := c.getLogger()

	taskList := v1alpha1.TaskList{}
	err := ctx.kube.List(ctx, &taskList,
		client.MatchingLabels(internaltypes.ClusterCodeLabels),
		client.InNamespace(ctx.blueprint.Namespace),
	)
	if err != nil {
		return err
	}
	filteredTasks := make([]v1alpha1.Task, 0)
	for _, task := range taskList.Items {
		if !task.GetDeletionTimestamp().IsZero() {
			// ignore tasks about to be deleted
			continue
		}
		for _, owner := range task.GetOwnerReferences() {
			if owner.Name == ctx.blueprint.Name {
				filteredTasks = append(filteredTasks, task)
			}
		}
	}
	log.Info("fetched current tasks", "count", len(filteredTasks))
	ctx.currentTasks = filteredTasks
	return nil
}

func (c *Command) selectNewFile(ctx *commandContext) error {
	ctx.dependencyResolver.MustRequireDependencyByFuncName(c.fetchBlueprint, c.fetchCurrentTasks)
	log := c.getLogger()

	alreadyQueuedFiles := make([]string, len(ctx.currentTasks))
	for i, task := range ctx.currentTasks {
		alreadyQueuedFiles[i] = c.getAbsolutePath(task.Spec.SourceUrl)
	}

	var foundFileErr = errors.New("found")

	root := filepath.Join(c.SourceRootDir, internaltypes.SourceSubMountPath)
	err := filepath.Walk(root, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			// could not access file, let's prevent a panic
			return err
		}
		if info.IsDir() {
			return nil
		}
		if !containsExtension(filepath.Ext(path), ctx.blueprint.Spec.Scan.MediaFileExtensions) {
			log.V(1).Info("file extension not accepted", "path", path)
			return nil
		}
		for _, queuedFile := range alreadyQueuedFiles {
			if queuedFile == path {
				log.V(1).Info("skipping already queued file", "path", path)
				return nil
			}
		}
		ctx.selectedRelPath = path
		return foundFileErr // abort early if found a match
	})
	if errors.Is(err, foundFileErr) {
		return nil
	}
	if err != nil {
		return err
	}
	// we didn't find anything, let the pipeline know.
	return noMatchFoundErr
}

func (c *Command) getAbsolutePath(uri v1alpha1.ClusterCodeUrl) string {
	return filepath.Join(c.SourceRootDir, uri.GetRoot(), uri.GetPath())
}

func (c *Command) createTask(ctx *commandContext) error {
	ctx.dependencyResolver.MustRequireDependencyByFuncName(c.selectNewFile)
	log := c.getLogger()

	bp := ctx.blueprint
	selectedFile, err := filepath.Rel(filepath.Join(c.SourceRootDir, internaltypes.SourceSubMountPath), ctx.selectedRelPath)
	if err != nil {
		return err
	}

	taskId := string(uuid.NewUUID())
	task := &v1alpha1.Task{ObjectMeta: metav1.ObjectMeta{
		Namespace: c.Namespace,
		Name:      taskId,
	}}
	op, err := controllerutil.CreateOrUpdate(ctx, ctx.kube, task, func() error {
		task.Labels = labels.Merge(task.Labels, internaltypes.ClusterCodeLabels)
		task.Spec = v1alpha1.TaskSpec{
			TaskId:               v1alpha1.ClustercodeTaskId(taskId),
			SourceUrl:            v1alpha1.ToUrl(internaltypes.SourceSubMountPath, selectedFile),
			TargetUrl:            v1alpha1.ToUrl(internaltypes.TargetSubMountPath, selectedFile),
			Encode:               bp.Spec.Encode,
			Cleanup:              bp.Spec.Cleanup,
			Storage:              bp.Spec.Storage,
			ServiceAccountName:   bp.GetServiceAccountName(),
			FileListConfigMapRef: taskId + "-slice-list",
			ConcurrencyStrategy:  bp.Spec.TaskConcurrencyStrategy,
		}
		return controllerutil.SetOwnerReference(ctx.blueprint, task, ctx.kube.Scheme())
	})
	if op == controllerutil.OperationResultCreated || op == controllerutil.OperationResultUpdated {
		log.Info("Updated task", "name", task.Name)
	}
	return err
}

func (c *Command) abortIfNoMatchFound(_ *commandContext, err error) error {
	log := c.getLogger()

	if errors.Is(err, noMatchFoundErr) {
		log.Info("no media files found", "path", c.SourceRootDir)
		return nil
	}
	return err
}

func (c *Command) getLogger() logr.Logger {
	return c.Log.WithValues("blueprint", c.BlueprintName, "namespace", c.Namespace)
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
