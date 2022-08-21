package main

import (
	"github.com/ccremer/clustercode/pkg/cleanupcmd"
	"github.com/urfave/cli/v2"
)

func newCleanupCommand() *cli.Command {
	command := cleanupcmd.Command{}
	return &cli.Command{
		Name:   "cleanup",
		Usage:  "Remove intermediary files and finish the task",
		Before: LogMetadata,
		Action: func(context *cli.Context) error {
			ctx := SetLogger(context)
			return command.Execute(ctx)
		},
		Flags: []cli.Flag{
			newTaskNameFlag(&command.TaskName),
			newNamespaceFlag(&command.Namespace),
			newSourceRootDirFlag(&command.SourceRootDir),
		},
	}
}
