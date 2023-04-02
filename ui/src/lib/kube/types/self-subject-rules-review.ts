import type { KubeObject } from "../object"

export interface SelfSubjectRulesReview extends KubeObject {
	apiVersion: "authorization.k8s.io/v1"
	kind: "SelfSubjectRulesReview"
	spec: {
		namespace: string
	}
	status?: SelfSubjectRulesReviewStatus
}

export interface NonResourceRule {
	verbs: string[]
	nonResourceURLs?: string[]
}

export interface ResourceRule {
	verbs: string[]
	apiGroups?: string[]
	resourceNames?: string[]
	resources: string[]
}

export interface SelfSubjectRulesReviewStatus {
	incomplete: boolean
	nonResourceRules: NonResourceRule[]
	resourceRules: ResourceRule[]
	evaluationError: string
}

export function newSelfSubjectRulesReview(namespace: string): SelfSubjectRulesReview {
	return {
		apiVersion: "authorization.k8s.io/v1",
		kind: "SelfSubjectRulesReview",
		metadata: {
			name: "",
		},
		spec: {
			namespace,
		},
	}
}
