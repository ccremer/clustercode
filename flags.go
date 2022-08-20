package main

import (
	"fmt"
	"strings"

	"github.com/urfave/cli/v2"
)

type EnumValue struct {
	Enum     []string
	Default  string
	selected string
}

func (e *EnumValue) Set(value string) error {
	for _, enum := range e.Enum {
		if enum == value {
			e.selected = value
			return nil
		}
	}

	return fmt.Errorf("allowed values are [%s]", strings.Join(e.Enum, ", "))
}

func (e *EnumValue) String() string {
	if e.selected == "" {
		return e.Default
	}
	return e.selected
}

func newTaskNameFlag(dest *string) *cli.StringFlag {
	return &cli.StringFlag{Name: "task-name", EnvVars: envVars("TASK_NAME"), Required: true,
		Usage:       "Task Name",
		Destination: dest,
	}
}

func newNamespaceFlag(dest *string) *cli.StringFlag {
	return &cli.StringFlag{Name: "namespace", EnvVars: envVars("NAMESPACE"), Required: true,
		Usage:       "Namespace in which to find the resource.",
		Destination: dest,
	}
}

func newBlueprintNameFlag(dest *string) *cli.StringFlag {
	return &cli.StringFlag{Name: "blueprint-name", EnvVars: envVars("BLUEPRINT_NAME"), Required: true,
		Usage:       "Blueprint Name",
		Destination: dest,
	}
}

func newScanRoleKindFlag(dest *cli.Generic) *cli.GenericFlag {
	enum := &EnumValue{Enum: []string{ClusterRole, Role}, Default: ClusterRole}
	return &cli.GenericFlag{Name: "scan-role-kind", EnvVars: envVars("SCAN_ROLE"),
		Usage:       "TODO",
		Destination: dest,
		Category:    "Encoding",
		DefaultText: fmt.Sprintf("%q [%s]", enum.Default, strings.Join(enum.Enum, ", ")),
		Value:       enum,
	}
}

func newSourceRootDirFlag(dest *string) *cli.StringFlag {
	return &cli.StringFlag{Name: "source-root-dir", EnvVars: envVars("SOURCE_ROOT_DIR"),
		Usage:       "Directory path where to find the source files",
		Destination: dest, Value: "/clustercode",
	}
}
