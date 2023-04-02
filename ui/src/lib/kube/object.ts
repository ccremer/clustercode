export interface KubeObject {
	apiVersion: string
	kind: string
	metadata: KubeMeta
}

export interface KubeMeta {
	name: string
	namespace?: string
	creationTimestamp?: string
	annotations?: { [key: string]: string }
	labels?: { [key: string]: string }
	finalizers?: string[]
	ownerReferences?: OwnerReference[]
	resourceVersion?: string
	uid?: string
	generateName?: string
	generation?: number
	deletionTimestamp?: string
}

export interface OwnerReference {
	apiVersion: string
	kind: string
	name: string
	uid: string
	controller?: boolean
	blockOwnerDeletion?: boolean
}

export interface KubeList<T extends KubeObject> {
	apiVersion: string
	kind: string
	metadata: {
		resourceVersion?: string
		continue?: string
		name: string
	}
	items: T[]
}

/**
 * Condition contains details for one aspect of the current state of a Kubernetes API Resource.
 */
export interface Condition {
	/**
	 * type of condition in CamelCase.
	 */
	type: string
	/**
	 * status of the condition, one of True, False, Unknown.
	 */
	status: string
	/**
	 * observedGeneration represents the .metadata.generation that the condition was set based upon.
	 * For instance, if .metadata.generation is currently 12, but the .status.conditions[x].observedGeneration is 9, the condition is out of date with respect to the current state of the instance.
	 */
	observedGeneration?: number
	/**
	 * lastTransitionTime is the last time the condition transitioned from one status to another.
	 * This should be when the underlying condition changed.
	 * If that is not known, then using the time when the API field changed is acceptable.
	 */
	lastTransitionTime?: string
	/**
	 * reason contains a programmatic identifier indicating the reason for the condition's last transition.
	 * Producers of specific condition types may define expected values and meanings for this field, and whether the values are considered a guaranteed API.
	 * The value should be a CamelCase string.
	 */
	reason: string
	/**
	 * message is a human-readable message indicating details about the transition.
	 */
	message?: string
}
