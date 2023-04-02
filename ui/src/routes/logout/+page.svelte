<script lang="ts">
	import { user } from "../../lib/stores/user"
	import { Column, InlineNotification, NotificationActionButton, Row } from "carbon-components-svelte"
	import { goto } from "$app/navigation"
	import { onMount } from "svelte"
	import { _loginRouteId } from "../login/route"

	onMount(() => {
		localStorage.removeItem("kubetoken")
		user.logOut()
	})
</script>

<Row>
	<Column>
		<InlineNotification lowContrast kind="success" title="Success" subtitle="You are logged out" hideCloseButton>
			<svelte:fragment slot="actions">
				<NotificationActionButton on:click={() => goto(_loginRouteId)}>Log In</NotificationActionButton>
			</svelte:fragment>
		</InlineNotification>
	</Column>
</Row>
