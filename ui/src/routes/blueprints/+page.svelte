<script lang="ts">
	import {
		Accordion,
		AccordionItem,
		CodeSnippet,
		Column,
		Grid,
		InlineNotification,
		Row,
		Tag,
		Tile,
	} from "carbon-components-svelte"
	import type { Blueprint } from "../../lib/kube/types/blueprint"
	import { newBlueprint } from "../../lib/kube/types/blueprint"
	import { ClientStore } from "../../lib/stores/ClientStore"
	import { user } from "../../lib/stores/user"
	import dayjs from "dayjs"
	import cronstrue from "cronstrue"
	import yaml from "yaml"

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
		<div class="w-100">
			<Tile>

				<Grid fullWidth>
					<Row condensed>
						<Column>
							<h2>{bp.metadata.namespace}/{bp.metadata.name}</h2>
						</Column>
					</Row>
					<Row condensed>
						<Column>
							Created
						</Column>
						<Column>
							{@const creationDate = dayjs(bp.metadata.creationTimestamp)}
							{creationDate.format("YYYY-MM-DD HH-mm-ss Z")} ({ creationDate.fromNow()})
						</Column>
					</Row>
					<Row condensed>
						<Column>
							Scan Schedule
						</Column>
						<Column>
							<CodeSnippet type="inline" light hideCopyButton>{bp.spec?.scan?.schedule}</CodeSnippet> ({cronstrue.toString(bp.spec?.scan?.schedule ?? '')})
						</Column>
					</Row>
					<Row condensed>
						<Column>
							Scan File extensions
						</Column>
						<Column>
							{#each bp.spec?.scan?.mediaFileExtensions ?? [] as extension}
								<Tag>{extension}</Tag>
							{/each}
						</Column>
					</Row>
					<Row condensed>
						<Accordion align="start">
							<AccordionItem title="Encoding options">
								asdf
							</AccordionItem>
							{@const cleanupSpec = bp.spec?.cleanup}
							{#if cleanupSpec}
							<AccordionItem title="Cleanup options">
								{#if cleanupSpec.podTemplate}
								<span>Pod Template</span>
								<CodeSnippet code={yaml.stringify(cleanupSpec.podTemplate)} type="multi" expanded hideCopyButton light />
								{/if}
							</AccordionItem>
							{/if}
						</Accordion>
					</Row>
				</Grid>
			</Tile>
		</div>

	</Row>
{/each}

{#if blueprints.length === 0}
	<Row>
		<Column>
			<InlineNotification title="No blueprints found" kind="info" lowContrast />
		</Column>
	</Row>
{/if}

<style>
    .w-100 {
        width: 100%
    }
</style>
