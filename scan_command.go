package main

import (
	"github.com/ccremer/clustercode/pkg/scancmd"
	"github.com/urfave/cli/v2"
	controllerruntime "sigs.k8s.io/controller-runtime"
)

func newScanCommand() *cli.Command {
	command := &scancmd.Command{}
	return &cli.Command{
		Name:   "scan",
		Usage:  "Scan source storage for new files and queue task",
		Before: LogMetadata,
		Action: func(ctx *cli.Context) error {
			command.Log = AppLogger(ctx).WithName(ctx.Command.Name)
			controllerruntime.SetLogger(command.Log)
			return command.Execute(controllerruntime.LoggerInto(ctx.Context, command.Log))
		},
		Flags: []cli.Flag{
			newBlueprintNameFlag(&command.BlueprintName),
			newNamespaceFlag(&command.Namespace),
			newSourceRootDirFlag(&command.SourceRootDir),
		},
	}
}
