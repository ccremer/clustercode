<script lang="ts">
	import { page } from "$app/stores"
	import { Column, Grid, InlineNotification, NotificationActionButton, Row } from "carbon-components-svelte"
	import { goto } from "$app/navigation"
	import { base } from "$app/paths"
</script>

<Grid>
	<Row>
		<Column>
			<InlineNotification lowContrast title="Error:" subtitle={$page.error?.message} hideCloseButton>
				<svelte:fragment slot="actions">
					<NotificationActionButton on:click={() => goto(base)}>Home</NotificationActionButton>
				</svelte:fragment>
			</InlineNotification>
		</Column>
	</Row>
</Grid>
