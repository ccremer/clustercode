package ui

import (
	"embed"
)

var PublicFs embed.FS

// IsEmbedded returns true if PublicFs contains embedded UI assets.
// The UI assets are embedded when built with `ui` tag.
func IsEmbedded() bool {
	_, err := PublicFs.ReadFile("dist/index.html")
	return err == nil
}
