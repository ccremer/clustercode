//go:build ui

package ui

import (
	"embed"
)

//go:embed all:build
var publicFs embed.FS

func init() {
	PublicFs = publicFs
}
