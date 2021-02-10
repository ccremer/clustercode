{{- define "gvList" -}}
{{- $groupVersions := . -}}

// Generated documentation. Please do not edit.
:anchor_prefix: k8s-api

[id="api-reference"]
= API Reference

This is a https://github.com/elastic/crd-ref-docs[generated] API documentation.

.Packages
{{- range $groupVersions }}
- {{ asciidocRenderGVLink . }}
{{- end }}

{{ range $groupVersions }}
{{ template "gvDetails" . }}
{{ end }}

{{- end -}}
