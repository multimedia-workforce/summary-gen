<script lang="ts">
    import { onMount } from "svelte";
    import type { PageData } from "./$types";

    const { data }: { data: PageData } = $props();
    let file: File | null = $state(null);
    let transcript = $state("");
    let isServerOnline = $state(false);
    let serverStatusColor = $derived(
        isServerOnline ? "text-green-400" : "text-red-400",
    );
    let selectFileColors = $derived(
        isServerOnline
            ? "file:bg-indigo-900 hover:file:bg-indigo-800 file:text-gray-200"
            : "file:bg-indigo-900 hover:file:bg-indigo-900 file:text-gray-500",
    );
    let transcribeButtonColors = $derived(
        isServerOnline && file !== null
            ? "bg-indigo-900 hover:bg-indigo-800 text-gray-200"
            : "bg-indigo-900 hover:bg-indigo-900 text-gray-500",
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
    class="min-h-screen flex flex-col items-center justify-center bg-indigo-950 p-4 font-mono text-md"
>
    <div
        class="flex flex-col bg-indigo-950 rounded-lg p-6 max-w-4xl w-full text-center"
    >
        <pre class="text-gray-200 leading-none p-0 m-0">
███╗   ███╗███████╗███████╗████████╗██╗███╗   ██╗ ██████╗       
████╗ ████║██╔════╝██╔════╝╚══██╔══╝██║████╗  ██║██╔════╝       
██╔████╔██║█████╗  █████╗     ██║   ██║██╔██╗ ██║██║  ███╗      
██║╚██╔╝██║██╔══╝  ██╔══╝     ██║   ██║██║╚██╗██║██║   ██║      
██║ ╚═╝ ██║███████╗███████╗   ██║   ██║██║ ╚████║╚██████╔╝      
╚═╝     ╚═╝╚══════╝╚══════╝   ╚═╝   ╚═╝╚═╝  ╚═══╝ ╚═════╝       
                                                            
███████╗██╗   ██╗███╗   ███╗███╗   ███╗ █████╗ ██████╗ ██╗   ██╗
██╔════╝██║   ██║████╗ ████║████╗ ████║██╔══██╗██╔══██╗╚██╗ ██╔╝
███████╗██║   ██║██╔████╔██║██╔████╔██║███████║██████╔╝ ╚████╔╝ 
╚════██║██║   ██║██║╚██╔╝██║██║╚██╔╝██║██╔══██║██╔══██╗  ╚██╔╝  
███████║╚██████╔╝██║ ╚═╝ ██║██║ ╚═╝ ██║██║  ██║██║  ██║   ██║   
╚══════╝ ╚═════╝ ╚═╝     ╚═╝╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝   ╚═╝   
            </pre>
        <p class="mb-2 text-gray-400">
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
                    class="block w-full text-gray-500
                file:mr-4 file:py-2 file:px-4 file:rounded-lg
                file:font-semibold {selectFileColors}"
                />
            </div>
            <p class="text-gray-400">
                Heartbeat:
                <span class={serverStatusColor}>
                    {isServerOnline ? "Healthy ❤️" : "Unhealthy 💔"}
                </span>
            </p>
        </div>

        <button
            onclick={uploadMedia}
            class="py-2 px-4 rounded-lg font-semibold {transcribeButtonColors}"
        >
            Transcribe
        </button>

        {#if transcript.length > 0}
            <div class="mt-6 text-left bg-indigo-950 shadow-sm p-4 rounded-lg">
                <h2 class="font-semibold mb-2 text-gray-200">Transcription:</h2>
                <p class="text-gray-200 whitespace-pre-line">
                    {transcript || "..."}
                </p>
            </div>
        {/if}
    </div>
</div>
