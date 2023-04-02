import type { LayoutLoad } from "./$types"
import type { LoadEvent } from "@sveltejs/kit"
import { user } from "../lib/stores/user"
import { ClientStore } from "../lib/stores/ClientStore"
import { KubernetesClient } from "../lib/kube/kubernetes-client"
import { get } from "svelte/store"

// make SPA
export const ssr = false

// can't use prerendering, since the built-in Echo webserver won't find files without file extension, and resorts to index.html anyway.
export const prerender = false

export const load = ((e: LoadEvent) => {
	console.debug("Layout loaded")
	if (user.restore()) {
		ClientStore.set(new KubernetesClient(e.fetch, get(user).rawToken ?? ""))
	}
}) satisfies LayoutLoad
