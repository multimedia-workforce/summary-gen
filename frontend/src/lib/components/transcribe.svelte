<script lang="ts">
    import { Jumper } from "svelte-loading-spinners";
    import { onDestroy, onMount } from "svelte";
    import type {
        TranscribeResponse,
        TranscribeStatus,
    } from "../../routes/transcribe/+server";

    type Props = {
        file: File | null;
        transcript: string;
        heartbeatInterval: number;
    };

    // Input properties
    let { file, transcript = $bindable(), heartbeatInterval }: Props = $props();

    // Controller used for aborting the transcribe call
    let abortController: AbortController | null = null;

    // Whether the transcribe gRPC service can be reached
    let heartbeat = $state(false);

    // The latest status of the current transcription
    let transcribeStatus: TranscribeStatus | null = $state(null);

    // Dynamic button colors of the transcribe button
    let transcribeButtonColors = $derived(
        heartbeat && file !== null && transcribeStatus === null
            ? "bg-indigo-900 hover:bg-indigo-800 text-gray-200"
            : "bg-indigo-900 hover:bg-indigo-900 text-gray-500",
    );

    /**
     * Performs a transcribe request to the gRPC service and streams the response to the frontend
     */
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

                const decoded = decoder.decode(value).trim();
                if (!decoded) continue;

                for (const chunk of decoded.split("\n")) {
                    try {
                        const data: TranscribeResponse = JSON.parse(
                            atob(chunk),
                        );
                        transcribeStatus = data.status;

                        switch (data.status) {
                            case "processing":
                                console.log("Processing...");
                                break;
                            case "chunk":
                                transcript += data.result as string;
                                break;
                            case "completed":
                                console.log("Transcribe finished");
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
        } finally {
            transcribeStatus = null;
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
        disabled={!file || transcribeStatus !== null}
        onclick={handleTranscribeRequest}
        class="py-2 px-4 rounded-lg font-semibold {transcribeButtonColors}"
    >
        {#if transcribeStatus === null}
            Transcribe {heartbeat ? "💚" : "💔"}
        {:else}
            <div class="flex flex-row gap-1 items-center align-middle">
                <span>Transcribing</span>
                <Jumper unit="em" size="1" color="#CCC" duration="1s" />
            </div>
        {/if}
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
