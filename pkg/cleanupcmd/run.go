package cleanupcmd

import (
	"context"
	"os"
	"path/filepath"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"github.com/ccremer/clustercode/pkg/internal/pipe"
	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	pipeline "github.com/ccremer/go-command-pipeline"
	"github.com/go-logr/logr"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type Command struct {
	Log logr.Logger

	SourceRootDir string
	Namespace     string
	TaskName      string
}

type commandContext struct {
	context.Context
	dependencyResolver pipeline.DependencyResolver[*commandContext]

	kube              client.Client
	task              *v1alpha1.Task
	intermediaryFiles []string
}

// Execute runs the command and returns an error, if any.
func (c *Command) Execute(ctx context.Context) error {

	pctx := &commandContext{
		dependencyResolver: pipeline.NewDependencyRecorder[*commandContext](),
		Context:            ctx,
	}

	p := pipeline.NewPipeline[*commandContext]().WithBeforeHooks(pipe.DebugLogger(pctx), pctx.dependencyResolver.Record)
	p.WithSteps(
		p.NewStep("create client", c.createClient),
		p.NewStep("fetch task", c.fetchTask),
		p.NewStep("list intermediary files", c.listIntermediaryFiles),
		p.NewStep("delete intermediary files", c.deleteFiles),
		p.NewStep("delete source file", c.deleteSourceFile),
		p.NewStep("delete task", c.deleteTask),
	)

	return p.RunWithContext(pctx)
}

func (c *Command) createClient(ctx *commandContext) error {
	kube, err := pipe.NewKubeClient(ctx)
	ctx.kube = kube
	return err
}

func (c *Command) fetchTask(ctx *commandContext) error {
	ctx.dependencyResolver.MustRequireDependencyByFuncName(c.createClient)
	log := c.getLogger()

	task := &v1alpha1.Task{}
	if err := ctx.kube.Get(ctx, types.NamespacedName{Namespace: c.Namespace, Name: c.TaskName}, task); err != nil {
		return err
	}
	ctx.task = task
	log.Info("fetched task")
	return nil
}

func (c *Command) listIntermediaryFiles(ctx *commandContext) error {
	ctx.dependencyResolver.MustRequireDependencyByFuncName(c.fetchTask)

	intermediaryFiles, err := filepath.Glob(filepath.Join(c.SourceRootDir, internaltypes.IntermediateSubMountPath, ctx.task.Spec.TaskId.String()+"*"))
	ctx.intermediaryFiles = intermediaryFiles
	return err
}

func (c *Command) deleteFiles(ctx *commandContext) error {
	ctx.dependencyResolver.MustRequireDependencyByFuncName(c.listIntermediaryFiles)
	log := c.getLogger()

	for _, file := range ctx.intermediaryFiles {
		log.Info("deleting file", "file", file)
		if err := os.Remove(file); err != nil {
			log.Info("could not delete file, ignoring", "file", file, "error", err.Error())
		}
	}
	return nil
}

func (c *Command) deleteSourceFile(ctx *commandContext) error {
	ctx.dependencyResolver.MustRequireDependencyByFuncName(c.fetchTask)
	log := c.getLogger()

	sourceFile := filepath.Join(c.SourceRootDir, internaltypes.SourceSubMountPath, ctx.task.Spec.SourceUrl.GetPath())
	log.Info("deleting file", "file", sourceFile)
	return os.Remove(sourceFile)
}

func (c *Command) deleteTask(ctx *commandContext) error {
	ctx.dependencyResolver.MustRequireDependencyByFuncName(c.createClient, c.fetchTask)
	return ctx.kube.Delete(ctx.Context, ctx.task)
}

func (c *Command) getLogger() logr.Logger {
	return c.Log.WithValues("task_name", c.TaskName, "namespace", c.Namespace)
}
