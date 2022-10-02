// https://kubernetes.io/docs/reference/kubernetes-api/authentication-resources/token-review-v1/

import type { KubeMeta, KubeObject } from '../object'

export class TokenReview implements KubeObject {
  readonly apiVersion: string = 'authentication.k8s.io/v1'
  readonly kind: string = 'TokenReview'
  metadata?: KubeMeta
  spec: TokenReviewSpec
  status: TokenReviewStatus

  constructor(token: string) {
    this.spec = {
      token: token
    }
  }
}

export interface TokenReviewSpec {
  token: string
  audiences?: string[]
}

export interface TokenReviewStatus {
  authenticated: boolean
  audiences: string[]
  error: string
  user: UserInfo
}

export interface UserInfo {
  username: string
  extra: Map<string, string[]>
  groups: string[]
}
