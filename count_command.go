package main

import (
	"github.com/ccremer/clustercode/pkg/countcmd"
	"github.com/urfave/cli/v2"
)

func newCountCommand() *cli.Command {
	command := &countcmd.Command{}
	return &cli.Command{
		Name:  "count",
		Usage: "Counts the number of generated intermediary media files",
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
