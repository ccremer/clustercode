<script lang="ts">
	import { ToastNotification } from "carbon-components-svelte"
	import { alertStore } from "./alerts"
	import { flip } from "svelte/animate"
	import { fly } from "svelte/transition"
</script>

<div class="alert-container">
	{#each $alertStore as alert (alert.id)}
		<div animate:flip transition:fly={{ y: 30 }}>
			<ToastNotification
				title={alert.title}
				subtitle={alert.message}
				hideCloseButton={!alert.sticky}
				kind={alert.severity}
				on:close={() => alertStore.remove(alert)}
			/>
		</div>
	{/each}
</div>
