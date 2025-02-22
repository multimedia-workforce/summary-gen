<script lang="ts">
    import type { PageData } from "./$types";
    import Transcribe from "$lib/components/transcribe.svelte";
    import Summarize from "$lib/components/summarize.svelte";
    import Banner from "$lib/components/banner.svelte";
    import Upload from "$lib/components/upload.svelte";

    const { data }: { data: PageData } = $props();
    let file: File | undefined = $state(undefined);
    let transcript: string = $state("");
    let summary: string = $state("");
</script>

<div class="max-h-full flex flex-col items-center justify-center p-4 text-md">
    <div
        class="flex flex-col gap-4 w-full max-w-5xl rounded-lg p-6 text-center"
    >
        <Banner />

        <Upload bind:file />

        <div class="grid grid-cols-2 gap-2">
            <Transcribe
                {file}
                bind:transcript
                heartbeatInterval={data.heartbeatInterval}
            />

            <Summarize
                {transcript}
                bind:summary
                heartbeatInterval={data.heartbeatInterval}
            />
        </div>
    </div>
</div>
