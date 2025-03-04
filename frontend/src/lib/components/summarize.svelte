<script lang="ts">
    import { onDestroy, onMount } from "svelte";
    import type {
        SummarizeResponse,
        SummarizeStatus,
    } from "../../routes/summarize/+server";
    import { Jumper } from "svelte-loading-spinners";
    import Clipboard from "./clipboard.svelte";

    type Props = {
        transcript: string;
        summary: string;
        heartbeatInterval: number;
    };

    // Input properties
    let {
        transcript,
        summary = $bindable(),
        heartbeatInterval,
    }: Props = $props();

    // Controller used for aborting the summarize call
    let abortController: AbortController | null = null;

    // Whether the summarize gRPC service can be reached
    let heartbeat = $state(false);

    // The latest status of the current summarize call
    let summarizeStatus: SummarizeStatus | null = $state(null);

    // The available models, populated inside the onMount method
    let models: string[] = $state([]);

    // The model that is currently selected
    let selectedModel = $state("");

    async function handleSummarizeRequest() {
        if (transcript.length <= 0 || !heartbeat) {
            return;
        }

        summary = "";
        abortController?.abort(); // Cancel previous request if running
        abortController = new AbortController();

        try {
            const response = await fetch("/summarize", {
                method: "POST",
                body: JSON.stringify({
                    transcript: transcript,
                    prompt: "Summarize the following transcript",
                    model: selectedModel,
                }),
                signal: abortController.signal, // Support for cancellation
                headers: { "Content-Type": "application/json" },
            });

            if (!response.ok) {
                console.error(
                    "HTTP Error:",
                    response.status,
                    response.statusText,
                );
                summary = `Error: Server responded with ${response.status}`;
                return;
            }

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
                        const data: SummarizeResponse = JSON.parse(atob(chunk));
                        summarizeStatus = data.status;

                        switch (data.status) {
                            case "processing":
                                console.log("Processing...");
                                break;
                            case "completed":
                                console.log(
                                    "Summary finished",
                                    data.result as string,
                                );
                                summary = data.result as string; // Update summary with final result
                                return; // Stop reading after final result
                            case "error":
                                console.error("Error:", data.result);
                                summary = `Error: ${data.result}`;
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
            summarizeStatus = null;
        }
    }

    function checkSummarizeHeartbeat() {
        fetch("/summarize/heartbeat")
            .then((response) => response.json())
            .then((data) => (heartbeat = data.status === "OK"))
            .catch(() => (heartbeat = false));
    }

    function fetchModels() {
        fetch("/summarize/models")
            .then((response) => response.json())
            .then((data) => {
                models = data.models;
                selectedModel = models.length > 0 ? models[0] : "";
            })
            .catch(() => (models = []));
    }

    onMount(() => {
        checkSummarizeHeartbeat();
        fetchModels();
        setInterval(checkSummarizeHeartbeat, heartbeatInterval);
    });

    onDestroy(() => {
        abortController?.abort();
    });
</script>

<div
    class="flex flex-col gap-1 align-center items-start card border-[1px] border-surface-100-900 p-4"
>
    <div class="flex flex-row w-full align-center items-center justify-between">
        <div class="flex flex-row gap-2 align-center items-center">
            <button
                type="button"
                disabled={transcript.length <= 0 || summarizeStatus !== null}
                onclick={handleSummarizeRequest}
                class="btn preset-filled"
            >
                {#if summarizeStatus === null}
                    Summarize
                {:else}
                    <div class="flex flex-row gap-1 items-center align-middle">
                        <span>Summarizing</span>
                        <Jumper unit="em" size="1" color="#CCC" duration="1s" />
                    </div>
                {/if}
            </button>

            {#if heartbeat}
                <select
                    disabled={transcript.length == 0}
                    bind:value={selectedModel}
                    class="select variant-filled"
                >
                    {#each models as model}
                        <option value={model}>{model}</option>
                    {/each}
                </select>
            {/if}

            {heartbeat ? "💚" : "💔"}
        </div>

        <Clipboard size={20} disabled={summary.length <= 0} content={summary} />
    </div>

    {#if summary.length > 0}
        <div class="mt-6 text-left">
            <p>
                {summary || ""}
            </p>
        </div>
    {/if}
</div>
