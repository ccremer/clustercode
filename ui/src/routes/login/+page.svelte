<script lang="ts">
	import {
		Button,
		Column,
		Form,
		FormGroup,
		InlineLoading,
		InlineNotification,
		NotificationActionButton,
		PasswordInput,
		Row,
	} from "carbon-components-svelte"
	import { decodeToken, KubernetesClient } from "../../lib/kube/kubernetes-client"
	import { ClientStore } from "../../lib/stores/ClientStore"
	import { goto } from "$app/navigation"
	import { user } from "../../lib/stores/user"
	import { base } from "$app/paths"
	import { _logoutRouteId } from "../logout/route.js"
	import type { SelfSubjectRulesReview } from "../../lib/kube/types/self-subject-rules-review"
	import { newSelfSubjectRulesReview } from "../../lib/kube/types/self-subject-rules-review"
	import { page } from "$app/stores"
	import { alertStore } from "../../lib/components/alerts"

	let token = ""
	let loginPromise: Promise<void>
	let isLoading = false

	async function login() {
		const decoded = decodeToken(token)
		const client = new KubernetesClient($page.data.fetch, token)
		isLoading = true
		loginPromise = client.create<SelfSubjectRulesReview>(newSelfSubjectRulesReview(decoded.namespace)).then((ssar) => {
			console.log("successfully logged in", ssar)
			ClientStore.set(client)
			user.logIn(token)
			alertStore.add({ message: `Welcome, ${$user.fullName}`, severity: "success", title: "Login success" })
			goto(base)
		})
		await loginPromise
	}
</script>

<Row>
	<Column>
		<h1>Login</h1>
	</Column>
</Row>

{#if isLoading}
	<Row>
		<Column>
			{#await loginPromise}
				<InlineLoading description="logging in..." />
			{:catch err}
				<InlineNotification lowContrast kind="error" title="Error:" subtitle={err.message} />
			{/await}
		</Column>
	</Row>
{/if}

<Row>
	<Column>
		{#if !$user.loggedIn}
			<Form>
				<FormGroup>
					<PasswordInput labelText="Kubernetes Token" placeholder="Paste Token..." bind:value={token} />
				</FormGroup>
				<Button on:click={login} disabled={!token} id="btn-submit">Login</Button>
			</Form>
		{:else if !loginPromise}
			<InlineNotification lowContrast kind="info" subtitle="You are already logged in" hideCloseButton>
				<svelte:fragment slot="actions">
					<NotificationActionButton on:click={() => goto(_logoutRouteId)}>Log Out</NotificationActionButton>
					<NotificationActionButton on:click={() => goto(base)}>Home</NotificationActionButton>
				</svelte:fragment>
			</InlineNotification>
		{/if}
	</Column>
</Row>
