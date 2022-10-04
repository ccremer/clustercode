import * as jose from 'jose'
import type { KubeObject } from './object'
import { SelfSubjectAccessReview } from './types/selfSubjectAccessReview'

export class Client {
  token = ''

  async create<T extends KubeObject>(obj: T): Promise<T> {
    return this.makeRequest(obj, 'POST')
  }

  async get<T extends KubeObject>(obj: T): Promise<T> {
    return this.makeRequest(obj, 'GET')
  }

  private async makeRequest<T extends KubeObject>(obj: T, method: string): Promise<T> {
    const endpoint = `/apis/${obj.apiVersion}/${obj.kind.toLowerCase()}s`
    return await fetch(endpoint, {
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${this.token}`
      },
      body: JSON.stringify(obj),
      method: method
    })
      .then(response => response.json())
      .then(json => {
        if (Object.prototype.hasOwnProperty.call(json, 'kind')) {
          const err: kubeerror = json as kubeerror
          if (err.kind === 'Status') {
            throw new RequestError(err.message, err.reason, err.status, err.code)
          }
        }
        return json as Promise<T>
      })
  }

  /**
   * Parses the given Kubernetes token, performs a SelfSubjectAccessReview request and stores the token in the client.
   * @param token the JWT of a Kubernetes Service Account.
   * @throws JWTInvalid if token is not a JWT token, {@link RequestError} from Kubernetes or other Error if request fails.
   */
  async login(token: string): Promise<SelfSubjectAccessReview> {
    const saToken: ServiceAccountToken = decodeToken(token)
    const obj = new SelfSubjectAccessReview(
      'get',
      'blueprints',
      'clustercode.github.io',
      saToken.namespace
    )
    this.token = token
    return this.create<SelfSubjectAccessReview>(obj)
  }
}

/**
 * Decodes a Kubernetes JWT token and returns some metadata.
 * @param token to decode as JWT
 * @returns The parsed {@link ServiceAccountToken }
 * @throws any {@link jose.JWTInavlid} error
 */
export function decodeToken(token: string): ServiceAccountToken {
  const payload = jose.decodeJwt(token)
  const parts = payload.sub.split(':')
  const namespace = parts[2]
  const name = parts[3]
  return {
    sub: payload.sub,
    namespace: namespace,
    name: name
  }
}

export interface ServiceAccountToken {
  readonly sub: string
  readonly namespace: string
  readonly name: string
}

interface kubeerror {
  message: string
  reason: string
  code: number
  status: string
  kind?: string
}

export class RequestError extends Error {
  readonly reason?: string
  readonly status?: string
  readonly code?: number

  constructor(msg: string, reason: string, status: string, code: number) {
    super(msg)
    this.reason = reason
    this.status = status
    this.code = code
  }
}
