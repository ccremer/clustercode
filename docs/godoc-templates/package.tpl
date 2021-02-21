= {{if .File.FqPackage}}Package {{.File.FqPackage}}{{else}}{{.File.Decl}}{{end}}

{{if (index .Docs "package-overview")}}include::{{index .Docs "package-overview"}}[leveloffset=+1]{{"\n"}}{{else}}{{ .File.Doc }}{{end}}
