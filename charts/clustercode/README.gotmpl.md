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

### WebUI

By default, the WebUI is also installed.
To log into the frontend, you must provide Kubernetes tokens in the login form as the frontend talks directly to the Kubernetes API.

To get a token, you can create Service Accounts with the `webui.users` parameter.
Once deployed, get the token by the following command:

```bash
kubectl -n clustercode-system get secret clustercode-webadmin -o jsonpath='{.data.token}' | base64 -d
```

Alternatively, set `.skipSecret` in `webui.users[*]` to skip creating a Secret for the Service Account.
To get a time-limited token without permanent Secret, you can generate one with kubectl:

```bash
kubectl -n clustercode-system create token clustercode-webadmin
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
