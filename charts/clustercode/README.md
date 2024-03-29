# clustercode

![Version: 0.3.1](https://img.shields.io/badge/Version-0.3.1-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square)

Movie and Series conversion Operator with Ffmpeg

**Homepage:** <https://ccremer.github.io/clustercode>
<!---
The README.md file is automatically generated with helm-docs!

Edit the README.gotmpl.md template instead.
-->

## Installation

Install the CRDs:
```bash
kubectl apply -f https://github.com/ccremer/clustercode/releases/download/clustercode-0.3.1/crds.yaml
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
| clustercode.ffmpegImage.tag | string | `"5.1-alpine"` |  |
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
| webui.api.proxy.skipTlsVerify | bool | `true` | Whether TLS certificate verifications should be skipped. |
| webui.api.proxy.url | string | `"auto"` | The Kubernetes full base URL for the API. Ideally this is an internal URL. If set to `auto`, the URL is detected based on the Service Account token. |
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
| webui.users | list | see below | List of Service Accounts that can access the web-ui. |
| webui.users[0].name | string | `"clustercode-webadmin"` | name of the Service Account |
| webui.users[0].namespace | string | `""` | namespace of the Service Account, defaults to release namespace |
| webui.users[0].skipSecret | bool | `false` | If true, no Secret will be created for the Service Account |

## Source Code

* <https://github.com/ccremer/clustercode>

