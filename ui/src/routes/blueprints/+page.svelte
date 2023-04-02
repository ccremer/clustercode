<script lang="ts">
	import { Column, Row } from "carbon-components-svelte"
	import type { Blueprint } from "../../lib/kube/types/blueprint"
	import { newBlueprint } from "../../lib/kube/types/blueprint"
	import { ClientStore } from "../../lib/stores/ClientStore"
	import { user } from "../../lib/stores/user"

	let blueprints: Blueprint[] = []

	$: {
		const bp = newBlueprint()
		$ClientStore.list<Blueprint>(bp.apiVersion, bp.kind, $user.namespace).then((result) => {
			blueprints = result
		})
	}
</script>

<Row>
	<Column>
		<h1>Blueprints</h1>
	</Column>
</Row>

{#each blueprints as bp}
	<Row>
		<Column>
			{bp.metadata.name}
		</Column>
	</Row>
{/each}

{#if blueprints.length === 0}
	<Row>
		<Column>
			<p>No blueprints found</p>
		</Column>
	</Row>
{/if}
