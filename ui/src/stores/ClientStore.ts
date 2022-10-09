import { writable } from "svelte/store"
import type { Client } from "../kube/client"

/**
 * ClientStore contains the Kubernetes client
 */
export const ClientStore = writable<Client>()
