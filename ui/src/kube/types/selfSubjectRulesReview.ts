import type { KubeMeta, KubeObject } from '../object'

export class SelfSubjectRulesReview implements KubeObject {
  readonly apiVersion: string = 'authorization.k8s.io/v1'
  readonly kind: string = 'SelfSubjectRulesReview'
  metadata?: KubeMeta
  spec: SubjectRulesReviewSpec
  status?: SubjectRulesReviewStatus

  constructor(namespace: string) {
    this.spec = {
      namespace: namespace
    }
  }
}

export interface SubjectRulesReviewSpec {
  namespace: string
}

export interface SubjectRulesReviewStatus {
  resourceRules: ResourceRule[]
}

export class ResourceRule {
  verbs: string[]
  apiGroups: string[]
  resourceNames: string[]
  resources: string[]
}
