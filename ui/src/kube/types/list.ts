export interface List<T> {
  kind: 'List'
  apiVersion: 'v1'
  metadata: object
  items: T[]
}
