# clustercode

![Version: 0.2.0](https://img.shields.io/badge/Version-0.2.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square)

Movie and Series conversion Operator with Ffmpeg

**Homepage:** <https://ccremer.github.io/clustercode>
<!---
The README.md file is automatically generated with helm-docs!

Edit the README.gotmpl.md template instead.
-->

## Installation

```bash
kubectl apply -f https://github.com/ccremer/clustercode/releases/download/clustercode-0.2.0/crds.yaml
```

```bash
helm repo add clustercode https://ccremer.github.io/clustercode
helm install clustercode clustercode/clustercode
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
| clustercode.ffmpegImage.tag | string | `"5.0-alpine"` |  |
| fullnameOverride | string | `""` |  |
| image.pullPolicy | string | `"IfNotPresent"` |  |
| image.registry | string | `"ghcr.io"` |  |
| image.repository | string | `"ccremer/clustercode"` |  |
| image.tag | string | `"latest"` |  |
| imagePullSecrets | list | `[]` |  |
| metrics.enabled | bool | `false` | Specifies whether metrics should be enabled |
| metrics.service.port | int | `9090` |  |
| metrics.service.type | string | `"ClusterIP"` |  |
| nameOverride | string | `""` |  |
| nodeSelector | object | `{}` | The operator's pod node selector |
| podAnnotations | object | `{}` | The operator's pod annotations |
| podSecurityContext | object | `{}` | The operator's pod security context |
| rbac.create | bool | `true` | Specifies whether RBAC roles and rolebindings should be enabled for users |
| replicaCount | int | `1` | The operator's pod replica count |
| resources.limits.memory | string | `"128Mi"` |  |
| resources.requests.cpu | string | `"10m"` |  |
| resources.requests.memory | string | `"32Mi"` |  |
| securityContext | object | `{}` | The operator's container security context |
| serviceAccount.annotations | object | `{}` | Annotations to add to the service account |
| serviceAccount.create | bool | `true` | Specifies whether a service account should be created |
| serviceAccount.name | string | `""` | The name of the service account to use. If not set and create is true, a name is generated using the fullname template |
| tolerations | list | `[]` | The operator's pod tolerations |

## Source Code

* <https://github.com/ccremer/clustercode>

