<script lang="ts">
    import {onMount} from 'svelte';
    import {page} from '$app/state';
    import {fetchSmartSession, type SmartSession} from '@/http/persistence';
    import {format} from 'date-fns';

    let session: SmartSession | null = $state(null);
    let error: string | null = $state(null);

    onMount(async () => {
        try {
            session = await fetchSmartSession(page.params.id);
        } catch (e) {
            error = 'Failed to load session.';
            console.error(e);
        }
    });
</script>

{#if error}
    <p class="text-red-500">{error}</p>
{:else if !session}
    <p>Loading…</p>
{:else}
    <div class="space-y-6 px-10 pb-10">
        <h2 class="text-2xl font-bold tracking-tight">Smart Session</h2>
        <p class="text-muted-foreground text-sm">
            This shows the smart session from {format(new Date(session.createdAt), 'yyyy-MM-dd HH:mm')}.
        </p>

        {#if session.transcription}
            <h3 class="text-lg font-semibold">Transcription</h3>
            <div class="bg-muted rounded-md border p-4">
				<span class="font-mono text-sm text-muted-foreground">
					{session.transcription.text}
				</span>
            </div>
        {/if}

        {#if session.summary}
            <h3 class="text-lg font-semibold">Summary</h3>
            <div class="bg-muted rounded-md border p-4">
				<span class="font-mono text-sm text-muted-foreground">
					{session.summary.text}
				</span>
            </div>
        {/if}
    </div>
{/if}
