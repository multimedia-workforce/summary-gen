<script lang="ts">
    import type { PageData } from "./$types";
    import Transcribe from "$lib/transcribe.svelte";
    import Summarize from "$lib/summarize.svelte";
    import Banner from "$lib/banner.svelte";

    const { data }: { data: PageData } = $props();
    let file: File | null = $state(null);
    let transcript: string = $state("");
    let summary: string = $state("");

    function selectFile(event: Event & { currentTarget: HTMLInputElement }) {
        if (!event.currentTarget.files) return;
        file = event.currentTarget.files[0];
    }
</script>

<div
    class="min-h-screen flex flex-col items-center justify-center bg-indigo-950 p-4 font-mono text-md"
>
    <div
        class="flex flex-col gap-2 w-full max-w-5xl bg-indigo-950 rounded-lg p-6 text-center"
    >
        <Banner />

        <p class="mb-2 text-gray-400">
            Upload any audio/video file and get its transcribed summary.
        </p>

        <div class="flex items-center mb-4 justify-between">
            <div>
                <input
                    type="file"
                    accept="video/mp4"
                    onchange={selectFile}
                    class="block w-full text-gray-500
                file:mr-4 file:py-2 file:px-4 file:rounded-lg
                file:font-semibold file:bg-indigo-900 hover:file:bg-indigo-800 file:text-gray-200"
                />
            </div>
        </div>

        <div class="grid grid-cols-2">
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
