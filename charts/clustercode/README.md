# clustercode

![Version: 0.3.0](https://img.shields.io/badge/Version-0.3.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square)

Movie and Series conversion Operator with Ffmpeg

**Homepage:** <https://ccremer.github.io/clustercode>
<!---
The README.md file is automatically generated with helm-docs!

Edit the README.gotmpl.md template instead.
-->

## Installation

Install the CRDs:
```bash
kubectl apply -f https://github.com/ccremer/clustercode/releases/download/clustercode-0.3.0/crds.yaml
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
helm install clustercode clustercode/clustercode \
  --create-namespace \
  --namespace clustercode-system \
  --values webhook-values.yaml
```
(Note that the name and namespace must match the certificate you created in the step before.)

## Handling CRDs

* Always upgrade the CRDs before upgrading the Helm release.
* Watch out for breaking changes in the Clustercode release notes.

## Source Code

* <https://github.com/ccremer/clustercode>

<!---
The values below are generated with helm-docs!

Document your changes in values.yaml and let `make docs:helm` generate this section.
-->
## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| affinity | object | `{}` | The operator's pod affinity |
| clustercode.env | list | `[]` | Set additional environment variables to the Operator |
| clustercode.ffmpegImage.registry | string | `"ghcr.io"` |  |
| clustercode.ffmpegImage.repository | string | `"jrottenberg/ffmpeg"` |  |
| clustercode.ffmpegImage.tag | string | `"5.0-alpine"` |  |
| fullnameOverride | string | `""` |  |
| image.pullPolicy | string | `"IfNotPresent"` |  |
| image.registry | string | `"ghcr.io"` |  |
| image.repository | string | `"ccremer/clustercode"` |  |
| image.tag | string | `"latest"` |  |
| imagePullSecrets | list | `[]` |  |
| nameOverride | string | `""` |  |
| nodeSelector | object | `{}` | The operator's pod node selector |
| operator.enabled | bool | `true` | Whether the Operator deployment is enabled |
| operator.rbac.create | bool | `true` | Specifies whether RBAC roles and rolebindings should be enabled for users |
| operator.replicaCount | int | `1` | The operator's pod replica count |
| operator.resources.limits.memory | string | `"128Mi"` |  |
| operator.resources.requests.cpu | string | `"10m"` |  |
| operator.resources.requests.memory | string | `"32Mi"` |  |
| podAnnotations | object | `{}` | The operator's pod annotations |
| podSecurityContext | object | `{}` | The operator's pod security context |
| securityContext | object | `{}` | The operator's container security context |
| serviceAccount.annotations | object | `{}` | Annotations to add to the service account |
| serviceAccount.create | bool | `true` | Specifies whether a service account should be created |
| serviceAccount.name | string | `""` | The name of the service account to use. If not set and create is true, a name is generated using the fullname template |
| tolerations | list | `[]` | The operator's pod tolerations |
| webhook.annotations | object | `{}` | Annotations to add to the webhook configuration resources. |
| webhook.caBundle | string | `""` | Certificate in PEM format for the ValidatingWebhookConfiguration. |
| webhook.certificate | string | `""` | Certificate in PEM format for the TLS secret. |
| webhook.enabled | bool | `true` | Enable admission webhooks |
| webhook.externalSecretName | string | `""` | Name of an existing or external Secret with TLS to mount in the operator. The secret is expected to have `tls.crt` and `tls.key` keys. Note: You will still need to set `.caBundle` if the certificate is not verifiable (self-signed) by Kubernetes. |
| webhook.privateKey | string | `""` | Private key in PEM format for the TLS secret. |
| webhook.replicaCount | int | `1` | The webhook's pod replica count |
| webhook.service.annotations | object | `{}` | Annotations to add to the webhook service. |
| webui.api.externalName | object | `{"service":"kubernetes.default.svc.cluster.local"}` | The internal service name where the API service is served. |
| webui.api.externalName.service | string | `"kubernetes.default.svc.cluster.local"` | The Kubernetes service which the Ingress exposes additionally. This is likely only working with TLS. |
| webui.api.mode | string | `"proxy"` | The mode under which the Kubernetes API is served. Supported values: ["externalName", "proxy"] In case of CORS issues, you may use the integrated proxy. |
| webui.api.proxy.skipTlsVerify | bool | `false` | Whether TLS certificate verifications should be skipped. |
| webui.api.proxy.url | string | `"https://kubernetes.default.svc.cluster.local:443"` | The Kubernetes full base URL for the API. Ideally this is an internal URL. |
| webui.enabled | bool | `true` | Whether the WebUI deployment is enabled |
| webui.ingress.annotations | object | `{}` | Annotations to add to the Ingress |
| webui.ingress.className | string | `""` |  |
| webui.ingress.enabled | bool | `true` | Whether the WebUI ingress is enabled |
| webui.ingress.hosts[0].host | string | `"127.0.0.1.nip.io"` |  |
| webui.ingress.pathType | string | `"Prefix"` |  |
| webui.ingress.rootPath | string | `"/"` | The root path of the Ingress |
| webui.ingress.tls | list | `[]` |  |
| webui.replicaCount | int | `1` | The webserver's pod replica count |
| webui.resources.limits.memory | string | `"128Mi"` |  |
| webui.resources.requests.cpu | string | `"10m"` |  |
| webui.resources.requests.memory | string | `"32Mi"` |  |
| webui.service.annotations | object | `{}` | Annotations to add to the webhook service. |
| webui.service.port | int | `80` | Service port |

## Source Code

* <https://github.com/ccremer/clustercode>

