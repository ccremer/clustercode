import * as jose from "jose"
import type { KubeList, KubeObject } from "./object"

export class KubernetesClient {
	constructor(
		private fetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
		private token: string,
		private apiBase = ""
	) {}

	async create<T extends KubeObject>(obj: T): Promise<T> {
		return this.makeRequest(this.getEndpoint(obj.apiVersion, obj.kind), "POST", JSON.stringify(obj))
	}

	async get<T extends KubeObject>(obj: T): Promise<T> {
		const endpoint: string[] = []
		if (obj.apiVersion == "v1") {
			endpoint.push(`${this.apiBase}/api`)
		} else {
			endpoint.push(`${this.apiBase}/apis`)
		}
		endpoint.push(obj.apiVersion)
		if (obj.metadata.namespace) {
			endpoint.push(obj.metadata.namespace)
		}
		endpoint.push(obj.kind.toLowerCase() + "s")
		endpoint.push(obj.metadata.name)
		return this.makeRequest(endpoint.join("/"), "GET")
	}

	async list<T extends KubeObject>(apiVersion: string, kind: string, inNamespace?: string): Promise<T[]> {
		return this.makeRequest<KubeList<T>>(this.getEndpoint(apiVersion, kind, inNamespace, ""), "GET").then((list) => {
			return list.items
		})
	}

	/*
		async watch<T extends KubeObject>(apiVersion: string, kind: string, inNamespace: string | undefined, callback: (v?: T, err?: Error) => void) {
			this.makeRequest<{ apiVersion: "v1", kind: "List", metadata: { name: "", resourceVersion: string }; items: T[] }>(
				this.getEndpoint(apiVersion, kind, inNamespace, ""),
				"GET",
			).then(list => {
				list.items.forEach(item => {
					callback(item)
				})
				fetch(this.getEndpoint(apiVersion, kind, inNamespace, "", new URLSearchParams({
					watch: "true",
					resourceVersion: list.metadata.resourceVersion,
				}))).then(response => {
					if (!response.ok) {
						callback(undefined, new Error("not ok"))
						return
					}
					const reader = response.body?.pipeThrough(new TextDecoderStream()).getReader()
					if (reader) {
						reader.read().then(value => {
							console.log("value", value)
						}).catch(err => {
							console.error("failed reader", err)
						})
					}
				})
			}).catch(err => {
				callback(undefined, err)
			})
		}
	*/

	private getEndpoint(
		apiVersion: string,
		kind: string,
		inNamespace?: string,
		name?: string,
		queryParams?: URLSearchParams
	): string {
		const endpoint: string[] = []
		if (apiVersion == "v1") {
			endpoint.push(`${this.apiBase}/api`)
		} else {
			endpoint.push(`${this.apiBase}/apis`)
		}

		endpoint.push(apiVersion)
		if (inNamespace && inNamespace !== "") {
			endpoint.push("namespaces")
			endpoint.push(inNamespace)
		}
		endpoint.push(kind.toLowerCase().concat("s"))
		if (name && name !== "") {
			endpoint.push(name)
		}
		if (queryParams) {
			return endpoint.join("/") + queryParams.toString()
		}
		return endpoint.join("/")
	}

	private async makeRequest<T extends KubeObject>(endpoint: string, method: string, body?: string): Promise<T> {
		return await this.fetch(endpoint, {
			headers: {
				"Content-Type": "application/json",
				Authorization: `Bearer ${this.token}`,
			},
			body: body,
			method: method,
			credentials: "omit", // No cookies needed, we have the auth header.
		})
			.then((response) => response.json())
			.then((json) => {
				if (Object.prototype.hasOwnProperty.call(json, "kind")) {
					const err: kubeerror = json as kubeerror
					if (err.kind === "Status") {
						throw new RequestError(err.message, err.reason, err.status, err.code)
					}
				}
				return json as Promise<T>
			})
	}
}

/**
 * Decodes a Kubernetes JWT token and returns some metadata.
 * @param token to decode as JWT
 * @returns The parsed {@link ServiceAccountToken }
 * @throws any {@link jose.JWTInvalid} error
 */
export function decodeToken(token: string): ServiceAccountToken {
	const payload = jose.decodeJwt(token)
	const parts = payload.sub?.split(":") ?? ["", "", "", ""]
	const namespace = parts[2]
	const name = parts[3]
	return {
		sub: payload.sub ?? "",
		namespace: namespace,
		name: name,
		expires: payload.exp ? new Date(payload.exp * 1000) : new Date(),
	}
}

export interface ServiceAccountToken {
	readonly sub: string
	readonly namespace: string
	readonly name: string
	readonly expires: Date
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
