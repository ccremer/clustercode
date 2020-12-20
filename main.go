package main

import (
	"fmt"

	"github.com/ccremer/clustercode/cmd"
)

var (
	// These will be populated by Goreleaser
	version string
	commit  string
	date    string
)

func main() {

	cmd.SetVersion(fmt.Sprintf("%s, commit %s, date %s", version, commit, date))
	cmd.Execute()

}
