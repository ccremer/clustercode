import type { PageLoad, PageLoadEvent } from "./$types"

export const load = ((e: PageLoadEvent) => {
	console.debug("Page loaded", e.route.id)
	return {
		fetch: e.fetch,
	}
}) satisfies PageLoad
