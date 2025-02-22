<script lang="ts">
    import { onMount } from "svelte";

    let ws: WebSocket | null = $state(null);
    let uploading = $state(false);
    let transcript = $state("");
    let serverStatus = $state("Checking...");

    async function uploadVideo(
        event: Event & { currentTarget: HTMLInputElement },
    ) {
        if (!event.currentTarget.files) {
            return;
        }

        const file = event.currentTarget.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append("video", file);

        const response = await fetch("/transcribe", {
            method: "POST",
            body: file,
        });

        if (!response.body) {
            return;
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;
            transcript += decoder.decode(value);
        }
    }

    function checkHeartbeat() {
        fetch("/heartbeat")
            .then((response) => response.json())
            .then((data) => {
                serverStatus =
                    data.status === "OK"
                        ? "Server is Online"
                        : "Server is Offline";
            })
            .catch(() => (serverStatus = "Request failed"));
    }

    onMount(() => {
        checkHeartbeat();
        setInterval(checkHeartbeat, 5000);
    });
</script>

<div
    class="min-h-screen flex flex-col items-center justify-center bg-gray-100 p-4"
>
    <div class="bg-white shadow-lg rounded-lg p-6 max-w-lg w-full text-center">
        <h1 class="text-2xl font-bold mb-4">Video Transcriber</h1>
        <p class="mb-2 text-sm text-gray-600">
            Upload an MP4 file and get its transcribed text.
        </p>

        <div class="mb-4">
            <input
                type="file"
                accept="video/mp4"
                onchange={uploadVideo}
                class="block w-full text-sm text-gray-500
                file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0
                file:text-sm file:font-semibold file:bg-blue-500 file:text-white
                hover:file:bg-blue-600"
            />
        </div>

        <div class="p-4 bg-gray-200 rounded-lg text-gray-700 mt-4">
            <h2 class="font-semibold">Server Status:</h2>
            <p class="text-lg font-bold">{serverStatus}</p>
        </div>

        <div
            class="mt-6 text-left bg-gray-50 p-4 rounded-lg border border-gray-300"
        >
            <h2 class="font-semibold mb-2">Transcription Result:</h2>
            <p class="text-gray-800 whitespace-pre-line">
                {transcript || "No transcription yet..."}
            </p>
        </div>
    </div>
</div>
