import type { KubeObject } from "../object"

export class SelfSubjectAccessReview implements KubeObject {
  readonly kind = "SelfSubjectAccessReview"
  readonly apiVersion = "authorization.k8s.io/v1"
  readonly spec = {
    resourceAttributes: {
      namespace: "",
      verb: "",
      resource: "",
      group: ""
    }
  }
  status?: SelfSubjectAccessReviewStatus

  constructor(verb: string, resource: string, group: string, namespace: string) {
    this.spec.resourceAttributes.verb = verb
    this.spec.resourceAttributes.resource = resource
    this.spec.resourceAttributes.group = group
    this.spec.resourceAttributes.namespace = namespace
  }
}

export interface SelfSubjectAccessReviewStatus {
  allowed: boolean
  reason: string
}
