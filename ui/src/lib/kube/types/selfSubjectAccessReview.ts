import type { KubeObject } from "../object"

export interface SelfSubjectAccessReview extends KubeObject {
	kind: "SelfSubjectAccessReview"
	apiVersion: "authorization.k8s.io/v1"
	spec: {
		resourceAttributes: {
			namespace: string
			verb: string
			resource: string
			group: string
		}
	}
	status?: {
		allowed: boolean
		reason?: string
	}
}

export function newSelfSubjectAccessReview(
	verb: string,
	resource: string,
	group: string,
	namespace: string
): SelfSubjectAccessReview {
	return {
		kind: "SelfSubjectAccessReview",
		apiVersion: "authorization.k8s.io/v1",
		metadata: {
			name: "",
		},
		spec: {
			resourceAttributes: {
				namespace: namespace,
				resource: resource,
				group: group,
				verb: verb,
			},
		},
	}
}
