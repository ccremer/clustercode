package main

import (
	"github.com/ccremer/clustercode/pkg/scancmd"
	"github.com/urfave/cli/v2"
)

func newScanCommand() *cli.Command {
	command := &scancmd.Command{}
	return &cli.Command{
		Name:  "scan",
		Usage: "Scan source storage for new files and queue task",
		Action: func(c *cli.Context) error {
			ctx := SetLogger(c)
			return command.Execute(ctx)
		},
		Flags: []cli.Flag{
			newBlueprintNameFlag(&command.BlueprintName),
			newNamespaceFlag(&command.Namespace),
			newSourceRootDirFlag(&command.SourceRootDir),
		},
	}
}
