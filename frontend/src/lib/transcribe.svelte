<script lang="ts">
    import { onDestroy, onMount } from "svelte";
    import type { TranscribeResponse } from "../routes/transcribe/+server";

    type Props = {
        file: File | null;
        transcript: string;
        heartbeatInterval: number;
    };

    let { file, transcript = $bindable(), heartbeatInterval }: Props = $props();

    let abortController: AbortController | null = null;
    let heartbeat = $state(false);
    let transcribeButtonColors = $derived(
        heartbeat && file !== null
            ? "bg-indigo-900 hover:bg-indigo-800 text-gray-200"
            : "bg-indigo-900 hover:bg-indigo-900 text-gray-500",
    );

    async function handleTranscribeRequest() {
        if (!file || !heartbeat) return;

        const formData = new FormData();
        formData.append("file", file);

        transcript = "";
        abortController?.abort(); // Cancel previous request if running
        abortController = new AbortController();

        try {
            const response = await fetch("/transcribe", {
                method: "POST",
                body: formData,
                signal: abortController.signal, // Support for cancellation
            });

            if (!response.body) return;

            const reader = response.body.getReader();
            const decoder = new TextDecoder();

            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                const chunk = decoder.decode(value).trim();
                if (!chunk) continue;

                for (const line of chunk.split("\n")) {
                    try {
                        const data: TranscribeResponse = JSON.parse(line);

                        switch (data.status) {
                            case "processing":
                                console.log("Processing...");
                                break;
                            case "chunk":
                                transcript += data.result as string;
                                break;
                            case "completed":
                                console.log(
                                    "Transcribe finished",
                                    data.result as string,
                                );
                                return; // Stop reading after final result
                            case "error":
                                console.error("Error:", data.result);
                                transcript = `Error: ${data.result}`;
                                return; // Stop reading on error
                        }
                    } catch (err) {
                        console.error("Failed to parse JSON:", err);
                    }
                }
            }
        } catch (error) {
            console.error("Request failed:", error);
        }
    }

    function checkTranscribeHeartbeat() {
        fetch("/transcribe/heartbeat")
            .then((response) => response.json())
            .then((data) => (heartbeat = data.status === "OK"))
            .catch(() => (heartbeat = false));
    }

    onMount(() => {
        checkTranscribeHeartbeat();
        setInterval(checkTranscribeHeartbeat, heartbeatInterval);
    });

    onDestroy(() => {
        abortController?.abort();
    });
</script>

<div class="flex flex-col gap-1 align-center items-start">
    <button
        onclick={handleTranscribeRequest}
        class="py-2 px-4 rounded-lg font-semibold {transcribeButtonColors}"
    >
        Transcribe {heartbeat ? "💚" : "💔"}
    </button>

    {#if transcript.length > 0}
        <div class="mt-6 text-left bg-indigo-950 shadow-sm p-4 rounded-lg">
            <h2 class="font-semibold mb-2 text-gray-200">Transcription:</h2>
            <p class="text-gray-200 whitespace-pre-line">
                {transcript || ""}
            </p>
        </div>
    {/if}
</div>
