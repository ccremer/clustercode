import { writable } from "svelte/store"
import type { KubernetesClient } from "../kube/kubernetes-client"
import type { Blueprint } from "../kube/types/blueprint"

/**
 * ClientStore contains the Kubernetes client
 */
export const ClientStore = writable<KubernetesClient>()

/**
 * BlueprintStore contains the blueprints as retrieved from the Kubernetes API
 */
export const BlueprintStore = writable<Array<Blueprint>>()
