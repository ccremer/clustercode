package countcmd

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"sort"
	"strings"

	"github.com/ccremer/clustercode/pkg/api/v1alpha1"
	"github.com/ccremer/clustercode/pkg/internal/pipe"
	internaltypes "github.com/ccremer/clustercode/pkg/internal/types"
	pipeline "github.com/ccremer/go-command-pipeline"
	"github.com/go-logr/logr"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
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

	kube         client.Client
	task         *v1alpha1.Task
	segmentFiles []string
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
		p.NewStep("scan segment files", c.scanSegmentFiles),
		p.NewStep("create file list", c.ensureConfigMap),
		p.NewStep("update task", c.updateTask),
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

func (c *Command) scanSegmentFiles(ctx *commandContext) error {
	ctx.dependencyResolver.MustRequireDependencyByFuncName(c.fetchTask)
	log := c.getLogger()

	prefix := ctx.task.Spec.TaskId.String() + "_"
	files := make([]string, 0)
	root := filepath.Join(c.SourceRootDir, internaltypes.IntermediateSubMountPath)
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
		return fmt.Errorf("could not find any segments in '%s", root)
	}
	sort.Strings(files)
	ctx.segmentFiles = files
	log.Info("found segments", "count", len(files))
	return err
}

func matchesTaskSegment(path string, prefix string) bool {
	base := filepath.Base(path)
	return strings.HasPrefix(base, prefix) && !strings.Contains(base, v1alpha1.MediaFileDoneSuffix)
}

func (c *Command) ensureConfigMap(ctx *commandContext) error {
	ctx.dependencyResolver.MustRequireDependencyByFuncName(c.createClient, c.fetchTask, c.scanSegmentFiles)
	log := c.getLogger()

	task := ctx.task
	cm := &corev1.ConfigMap{ObjectMeta: metav1.ObjectMeta{
		Name:      task.Spec.FileListConfigMapRef,
		Namespace: task.Namespace}}

	op, err := controllerutil.CreateOrUpdate(ctx, ctx.kube, cm, func() error {
		cm.Labels = labels.Merge(cm.Labels, labels.Merge(internaltypes.ClusterCodeLabels, task.Spec.TaskId.AsLabels()))

		fileList := make([]string, len(ctx.segmentFiles))
		for i, file := range ctx.segmentFiles {
			fileList[i] = fmt.Sprintf("file '%s'", file)
		}
		data := strings.Join(fileList, "\n") + "\n"
		cm.Data = map[string]string{
			v1alpha1.ConfigMapFileName: data,
		}
		return controllerutil.SetOwnerReference(task, cm, ctx.kube.Scheme())
	})
	if op == controllerutil.OperationResultCreated || op == controllerutil.OperationResultUpdated {
		log.Info("Updated config map", "configmap", cm.Name)
	}
	return err
}

func (c *Command) updateTask(ctx *commandContext) error {
	ctx.dependencyResolver.MustRequireDependencyByFuncName(c.createClient)
	log := c.getLogger()

	task := ctx.task
	op, err := controllerutil.CreateOrPatch(ctx, ctx.kube, task, func() error {
		task.Spec.SlicesPlannedCount = len(ctx.segmentFiles)
		return nil
	})
	if op == controllerutil.OperationResultCreated || op == controllerutil.OperationResultUpdated {
		log.Info("Updated task")
	}
	return err
}

func (c *Command) getLogger() logr.Logger {
	return c.Log.WithValues("task_name", c.TaskName, "namespace", c.Namespace)
}
