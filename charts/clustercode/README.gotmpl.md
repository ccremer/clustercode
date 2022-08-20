<!---
The README.md file is automatically generated with helm-docs!

Edit the README.gotmpl.md template instead.
-->

## Installation

```bash
kubectl apply -f https://github.com/ccremer/clustercode/releases/download/{{ template "chart.name" . }}-{{ template "chart.version" . }}/crds.yaml
```

```bash
helm repo add clustercode https://ccremer.github.io/clustercode
helm install {{ template "chart.name" . }} clustercode/{{ template "chart.name" . }}
```

## Handling CRDs

* Always upgrade the CRDs before upgrading the Helm release.
* Watch out for breaking changes in the {{ title .Name }} release notes.

{{ template "chart.sourcesSection" . }}

{{ template "chart.requirementsSection" . }}
<!---
The values below are generated with helm-docs!

Document your changes in values.yaml and let `make docs:helm` generate this section.
-->
{{ template "chart.valuesSection" . }}
