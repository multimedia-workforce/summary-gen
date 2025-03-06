<script lang="ts">
    import ModelSelect from "@/components/model-select.svelte";
    import TemperatureSlider from "@/components/temperature-slider.svelte";
    import FileUpload, {
        type UploadedFile,
    } from "@/components/file-upload.svelte";
    import Transcribe from "@/components/transcribe.svelte";
    import Summarize from "@/components/summarize.svelte";
    import * as Collapsible from "$lib/components/ui/collapsible/index.js";

    const { data } = $props();
    let files: Array<UploadedFile> = $state([]);
    let model: string | undefined = $state(undefined);
    let temperature: number = $state(0.1);
    let transcript: string = $state("");
    let summary: string = $state("");

    const file = $derived(files.length > 0 ? files[0].file : undefined);
    const displayedTranscript = $derived(
        transcript.length > 0 ? transcript : "Transcript will appear here...",
    );
</script>

<div class="hidden space-y-6 px-10 pb-10 h-full flex-col md:flex">
    <div class="space-y-0.5">
        <h2 class="text-2xl font-bold tracking-tight">Summary Generation</h2>
        <p class="text-muted-foreground">
            Automatically transcribe and summarize media.
        </p>
    </div>
    <div class="col-span-3 flex flex-col gap-4">
        <FileUpload maxFiles={1} bind:files />
        <div class="flex flex-row gap-2">
            <Transcribe
                bind:transcript
                heartbeatInterval={data.heartbeatInterval}
                {file}
            />
            <Summarize
                bind:summary
                heartbeatInterval={data.heartbeatInterval}
                {transcript}
                {model}
                {temperature}
            />
            <ModelSelect bind:model />
            <TemperatureSlider bind:temperature />
        </div>
        <div class="flex flex-row gap-2">
            <div class="flex-1 bg-muted rounded-md border p-4">
                <span class="font-mono text-sm text-muted-foreground"
                    >{displayedTranscript}</span
                >
            </div>
            {#if summary.length > 0}
                <div class="flex-1 bg-muted rounded-md border p-4">
                    <span class="font-mono text-sm text-muted-foreground"
                        >{summary}</span
                    >
                </div>
            {/if}
        </div>
    </div>
</div>
