<script lang="ts">
    import { onDestroy, onMount } from "svelte";
    import type { SummarizeStatus } from "../../routes/api/summarize/+server";
    import { Jumper } from "svelte-loading-spinners";
    import { Button } from "@/components/ui/button";
    import { hasHeartbeat, streamSummarization } from "@/http/summarize";

    type Props = {
        transcript: string;
        transcriptId: string;
        model?: string;
        summary: string;
        temperature: number;
        heartbeatInterval: number;
    };

    // Input properties
    let {
        transcript,
        transcriptId,
        model,
        summary = $bindable(),
        temperature,
        heartbeatInterval,
    }: Props = $props();

    // Controller used for aborting the summarize call
    let abortController: AbortController | null = null;

    // Whether the summarize gRPC service can be reached
    let heartbeat = $state(false);

    // The latest status of the current summarize call
    let summarizeStatus: SummarizeStatus | null = $state(null);

    async function handleSummarizeRequest() {
        if (transcript.length <= 0 || !heartbeat || !model) return;

        summary = "";
        abortController?.abort();
        abortController = new AbortController();

        try {
            for await (const data of streamSummarization(
                transcript,
                transcriptId,
                model,
                temperature,
                abortController.signal,
            )) {
                summarizeStatus = data.status;
                switch (data.status) {
                    case "processing":
                        break;
                    case "chunk":
                        summary += data.result as string;
                        break;
                    case "completed":
                        return;
                    case "error":
                        summary = `Error: ${data.result}`;
                        return;
                }
            }
        } catch (err) {
            console.error("Transcription failed:", err);
        } finally {
            summarizeStatus = null;
        }
    }

    function checkSummarizeHeartbeat() {
        hasHeartbeat().then((value) => (heartbeat = value));
    }

    onMount(() => {
        checkSummarizeHeartbeat();
        setInterval(checkSummarizeHeartbeat, heartbeatInterval);
    });

    onDestroy(() => {
        abortController?.abort();
    });
</script>

<Button
    type="button"
    disabled={transcript.length <= 0 || summarizeStatus !== null}
    onclick={handleSummarizeRequest}
>
    {#if summarizeStatus === null}
        Summarize {heartbeat ? "💚" : "💔"}
    {:else}
        <div class="flex flex-row gap-1 items-center align-middle">
            <span>Summarizing</span>
            <Jumper unit="em" size="1" color="#CCC" duration="1s" />
        </div>
    {/if}
</Button>
