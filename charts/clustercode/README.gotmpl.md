<!---
The README.md file is automatically generated with helm-docs!

Edit the README.gotmpl.md template instead.
-->

## Installation

Install the CRDs:
```bash
kubectl apply -f https://github.com/ccremer/clustercode/releases/download/{{ template "chart.name" . }}-{{ template "chart.version" . }}/crds.yaml
```

To prepare the webhook server, you need `yq`, `openssl`, `base64` tools and run this:
```bash
webhook_service_name=clustercode-webhook.clustercode-system.svc # Change this!

openssl req -x509 -newkey rsa:4096 -nodes -keyout tls.key --noout -days 3650 -subj "/CN=${webhook_service_name}" -addext "subjectAltName = DNS:${webhook_service_name}"
openssl req -x509 -key tls.key -nodes -out tls.crt -days 3650 -subj "/CN=${webhook_service_name}" -addext "subjectAltName = DNS:${webhook_service_name}"

yq -n '.webhook.caBundle="$(base64 -w0 tls.crt)" | .webhook.certificate="$(base64 -w0 tls.crd)" | .webhook.privateKey="$(base64 -w0 tls.key)"' > webhook-values.yaml
```

Install the chart:
```bash
helm repo add clustercode https://ccremer.github.io/clustercode
helm install {{ template "chart.name" . }} clustercode/{{ template "chart.name" . }} \
  --create-namespace \
  --namespace clustercode-system \
  --values webhook-values.yaml
```
(Note that the name and namespace must match the certificate you created in the step before.)

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
