package main

import (
	"github.com/ccremer/clustercode/pkg/operator"
	"github.com/ccremer/clustercode/pkg/operator/blueprintcontroller"
	"github.com/ccremer/clustercode/pkg/operator/taskcontroller"
	"github.com/urfave/cli/v2"
)

func newOperatorCommand() *cli.Command {
	command := &operator.Command{}
	return &cli.Command{
		Name:   "operator",
		Usage:  "Start provider in operator mode",
		Before: LogMetadata,
		Action: func(ctx *cli.Context) error {
			command.Log = AppLogger(ctx).WithName(ctx.Command.Name)
			blueprintcontroller.ScanRoleKind = ctx.String(newScanRoleKindFlag().Name)
			return command.Execute(ctx.Context)
		},
		Flags: []cli.Flag{
			&cli.BoolFlag{Name: "leader-election-enabled", Value: false, EnvVars: envVars("LEADER_ELECTION_ENABLED"),
				Usage:       "Use leader election for the controller manager.",
				Destination: &command.LeaderElectionEnabled,
				Category:    "Operator",
			},
			&cli.StringFlag{Name: "clustercode-image", EnvVars: envVars("CLUSTERCODE_IMAGE"),
				Usage:       "Container image to be used when launching Clustercode jobs.",
				Destination: &blueprintcontroller.DefaultClusterCodeContainerImage,
				Category:    "Encoding", Required: true,
			},
			&cli.StringFlag{Name: "ffmpeg-image", EnvVars: envVars("FFMPEG_IMAGE"),
				Usage:       "Container image to be used when launching Ffmpeg jobs.",
				Destination: &taskcontroller.DefaultFfmpegContainerImage,
				Category:    "Encoding", Required: true,
			},
			newScanRoleKindFlag(),
			&cli.StringFlag{Name: "scan-role-name", EnvVars: envVars("SCAN_ROLE_NAME"),
				Usage:       "TODO",
				Value:       "clustercode-editor-role",
				Destination: &blueprintcontroller.ScanRoleName,
				Category:    "Encoding",
			},
		},
	}
}
