<script lang="ts">
    import { onMount } from "svelte";
    import type { PageData } from "./$types";

    const { data }: { data: PageData } = $props();
    let file: File | null = $state(null);
    let transcript = $state("");
    let isServerOnline = $state(false);
    let serverStatusColor = $derived(
        isServerOnline ? "text-green-500" : "text-red-400",
    );
    let selectFileColors = $derived(
        isServerOnline
            ? "file:bg-blue-500 hover:file:bg-blue-600"
            : "file:bg-blue-300 hover:file:bg-blue-300",
    );
    let transcribeButtonColors = $derived(
        isServerOnline && file !== null
            ? "bg-blue-500 hover:bg-blue-600"
            : "bg-blue-300 file:bg-blue-300",
    );

    function selectFile(event: Event & { currentTarget: HTMLInputElement }) {
        if (!event.currentTarget.files) return;
        file = event.currentTarget.files[0];
    }

    async function uploadMedia() {
        if (!file || !isServerOnline) return;

        const formData = new FormData();
        formData.append("file", file);

        transcript = "";
        const response = await fetch("/transcribe", {
            method: "POST",
            body: formData,
        });

        if (!response.body) return;

        const reader = response.body.getReader();
        const decoder = new TextDecoder();

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;
            transcript += decoder.decode(value);
        }
    }

    function checkHeartbeat() {
        console.log("heartbeat");
        fetch("/heartbeat")
            .then((response) => response.json())
            .then((data) => (isServerOnline = data.status === "OK"))
            .catch(() => (isServerOnline = false));
    }

    onMount(() => {
        checkHeartbeat();
        setInterval(checkHeartbeat, data.heartbeatInterval);
    });
</script>

<div
    class="min-h-screen flex flex-col items-center justify-center bg-gray-100 p-4"
>
    <div
        class="flex flex-col gap-1 bg-white shadow-lg rounded-lg p-6 max-w-4xl w-full text-center"
    >
        <h1 class="text-2xl font-bold mb-4">Meeting Summary</h1>
        <p class="mb-2 text-sm text-gray-600">
            Upload an audio/video file and get its transcribed summary.
        </p>

        <div class="flex items-center mb-4 justify-between">
            <div>
                <input
                    disabled={!isServerOnline}
                    readonly={!isServerOnline}
                    type="file"
                    accept="video/mp4"
                    onchange={selectFile}
                    class="block w-full text-sm text-gray-500
                file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0
                file:text-sm file:font-semibold file:text-white {selectFileColors}"
                />
            </div>
            <p class="text-lg font-bold">
                Server Status:
                <span class={serverStatusColor}>
                    {isServerOnline ? "Online" : "Offline"}
                </span>
            </p>
        </div>

        <button
            onclick={uploadMedia}
            class="py-2 px-4 rounded-lg border-0 text-sm font-semibold text-white {transcribeButtonColors}"
        >
            Transcribe
        </button>

        <div
            class="mt-6 text-left bg-gray-50 p-4 rounded-lg border border-gray-300"
        >
            <h2 class="font-semibold mb-2">Transcription:</h2>
            <p class="text-gray-800 whitespace-pre-line">
                {transcript || "..."}
            </p>
        </div>
    </div>
</div>
