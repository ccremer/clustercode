//go:build ui

package ui

import (
	"embed"
)

//go:embed dist/*
//go:embed dist/assets
var publicFs embed.FS

func init() {
	PublicFs = publicFs
}
