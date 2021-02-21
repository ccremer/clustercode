{{- if gt (len .File.StructMethods) 0}}
== Functions

{{range .File.StructMethods}}
{{- if notreceiver $ .}}{{if or .Exported $.Config.Private }}{{render $ .}}{{end}}{{end}}
{{end}}
{{- end}}
