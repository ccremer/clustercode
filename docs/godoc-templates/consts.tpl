=== Constants
{{range .File.ConstAssignments}}{{if or .Exported $.Config.Private }}
{{render $ .}}{{end}}
{{end}}
