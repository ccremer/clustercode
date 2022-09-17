package main

import (
	"github.com/ccremer/clustercode/pkg/webui"
	"github.com/urfave/cli/v2"
)

func newWebuiCommand() *cli.Command {
	command := &webui.Command{}
	return &cli.Command{
		Name:   "webui",
		Usage:  "Start clustercode frontend web server",
		Before: LogMetadata,
		Action: func(ctx *cli.Context) error {
			command.Log = AppLogger(ctx).WithName(ctx.Command.Name)
			return command.Execute(ctx.Context)
		},
		Flags: []cli.Flag{},
	}
}
