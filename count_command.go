package main

import (
	"github.com/ccremer/clustercode/pkg/countcmd"
	"github.com/urfave/cli/v2"
	controllerruntime "sigs.k8s.io/controller-runtime"
)

func newCountCommand() *cli.Command {
	command := &countcmd.Command{}
	return &cli.Command{
		Name:   "count",
		Usage:  "Counts the number of generated intermediary media files",
		Before: LogMetadata,
		Action: func(ctx *cli.Context) error {
			command.Log = AppLogger(ctx).WithName(ctx.Command.Name)
			controllerruntime.SetLogger(command.Log)
			return command.Execute(controllerruntime.LoggerInto(ctx.Context, command.Log))
		},
		Flags: []cli.Flag{
			newTaskNameFlag(&command.TaskName),
			newNamespaceFlag(&command.Namespace),
			newSourceRootDirFlag(&command.SourceRootDir),
		},
	}
}
