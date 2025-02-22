<script lang="ts">
	import "../app.css";
	let { children } = $props();

	import { AppBar } from "@skeletonlabs/skeleton-svelte";
	import Lightswitch from "$lib/components/lightswitch.svelte";
</script>

<div class="max-h-full">
	<AppBar>
		{#snippet lead()}{/snippet}
		{#snippet trail()}
			<Lightswitch size={20} />
		{/snippet}
	</AppBar>

	<main class="flex-1 h-full max-h-full">
		{@render children()}
	</main>
</div>
