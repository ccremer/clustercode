<script lang="ts">
	import { ClientStore } from "./stores/ClientStore"
	import { KubernetesClient, RequestError } from "./kube/kubernetesClient"
	import { BlueprintList } from "./kube/types/blueprint"
	import type { Blueprint } from "./kube/types/blueprint"
	import { onMount } from "svelte"
	import { Alert, Table } from "sveltestrap"

	let client: KubernetesClient
	let alertVisible = false
	let displayError = ""
	let blueprints: Array<Blueprint> = []

	ClientStore.subscribe((value) => {
		client = value
	})

	onMount(() => {
		console.debug("listing all blueprints")
		let blueprintList: BlueprintList = new BlueprintList()
		client
			.list(blueprintList)
			.then((bps) => {
				blueprints = bps.items
			})
			.catch((err) => {
				if (err instanceof RequestError) {
					showError(`Kubernetes error: ${err.message}`)
				}
			})
	})

	function showError(message: string) {
		displayError = message
		alertVisible = true
	}

	function dismissError() {
		alertVisible = false
		displayError = ""
	}
</script>

{#if alertVisible}
	<Alert color="danger" fade={false} isOpen={alertVisible} dismissible={true} toggle={dismissError}
		>{displayError}</Alert
	>
{/if}

<h1>Blueprints</h1>
<div>
	{#if blueprints.length === 0}
		<p>No Blueprints found</p>
	{:else}
		<Table hover>
			<thead>
				<tr>
					<th> Name</th>
					<th>Namespace</th>
					<th>Schedule</th>
				</tr>
			</thead>
			<tbody>
				{#each blueprints as bp, i}
					<tr>
						<td>{blueprints[i].metadata?.name}</td>
						<td>{blueprints[i].metadata?.namespace}</td>
						<td>{blueprints[i].spec?.scan?.schedule}</td>
					</tr>
				{/each}
			</tbody>
		</Table>
	{/if}
</div>
