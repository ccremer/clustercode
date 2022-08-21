package main

import (
	"github.com/ccremer/clustercode/pkg/scancmd"
	"github.com/urfave/cli/v2"
)

func newScanCommand() *cli.Command {
	command := &scancmd.Command{}
	return &cli.Command{
		Name:   "scan",
		Usage:  "Scan source storage for new files and queue task",
		Before: LogMetadata,
		Action: func(ctx *cli.Context) error {
			command.Log = AppLogger(ctx).WithName(ctx.Command.Name)
			return command.Execute(ctx.Context)
		},
		Flags: []cli.Flag{
			newBlueprintNameFlag(&command.BlueprintName),
			newNamespaceFlag(&command.Namespace),
			newSourceRootDirFlag(&command.SourceRootDir),
		},
	}
}
