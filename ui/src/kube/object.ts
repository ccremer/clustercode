export interface KubeObject {
  apiVersion: string
  kind: string
  metadata?: KubeMeta
}

export interface KubeMeta {
  name: string
  namespace: string
}
