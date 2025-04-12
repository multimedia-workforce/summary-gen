<script lang="ts">
    import { Jumper } from "svelte-loading-spinners";
    import { onDestroy, onMount } from "svelte";
    import type { TranscribeStatus } from "../../routes/api/transcribe/+server";
    import { Button } from "@/components/ui/button";
    import { hasHeartbeat, streamTranscription } from "@/http/transcribe";

    type Props = {
        file?: File;
        transcript: string;
        transcriptId: string;
        heartbeatInterval: number;
    };

    // Input properties
    let { file, transcript = $bindable(), transcriptId = $bindable(), heartbeatInterval }: Props = $props();

    // Controller used for aborting the transcribe call
    let abortController: AbortController | null = null;

    // Whether the transcribe gRPC service can be reached
    let heartbeat = $state(false);

    // The latest status of the current transcription
    let transcribeStatus: TranscribeStatus | null = $state(null);

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
            for await (const data of streamTranscription(
                file,
                abortController.signal,
            )) {
                transcribeStatus = data.status;
                switch (data.status) {
                    case "processing":
                        break;
                    case "chunk":
                        transcript += data.result as string;
                        transcriptId = data.id as string;
                        break;
                    case "completed":
                        return;
                    case "error":
                        transcript = `Error: ${data.result}`;
                        return;
                }
            }
        } catch (err) {
            console.error("Transcription failed:", err);
        } finally {
            transcribeStatus = null;
        }
    }

    function checkTranscribeHeartbeat() {
        hasHeartbeat().then((value) => (heartbeat = value));
    }

    onMount(() => {
        checkTranscribeHeartbeat();
        setInterval(checkTranscribeHeartbeat, heartbeatInterval);
    });

    onDestroy(() => {
        abortController?.abort();
    });
</script>

<Button
    type="button"
    disabled={!file || transcribeStatus !== null}
    onclick={handleTranscribeRequest}
    class="btn preset-filled"
>
    {#if transcribeStatus === null}
        Transcribe {heartbeat ? "💚" : "💔"}
    {:else}
        <div class="flex flex-row gap-1 items-center align-middle">
            <span>Transcribing</span>
            <Jumper unit="em" size="1" color="#CCC" duration="1s" />
        </div>
    {/if}
</Button>
