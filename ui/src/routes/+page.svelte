<script lang="ts">
	import { user } from "$lib/stores/user.js"
	import { Column, InlineLoading, Row } from "carbon-components-svelte"
	import { goto } from "$app/navigation"
	import { _loginRouteId } from "./login/route"

	if (!user.restore()) {
		goto(_loginRouteId)
	}
</script>

<Row>
	<Column>
		{#if $user.loggedIn}
			<h1>Logged In!</h1>
		{:else}
			<InlineLoading description="Not logged in, redirecting..." />
		{/if}
	</Column>
</Row>
