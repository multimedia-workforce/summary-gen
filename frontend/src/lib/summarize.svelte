<script lang="ts">
    import { onDestroy, onMount } from "svelte";
    import type { SummarizeResponse } from "../routes/summarize/+server";

    type Props = {
        transcript: string;
        summary: string;
        heartbeatInterval: number;
    };

    let {
        transcript,
        summary = $bindable(),
        heartbeatInterval,
    }: Props = $props();

    let abortController: AbortController | null = null;
    let heartbeat = $state(false);
    let summarizeButtonColors = $derived(
        heartbeat && transcript.length > 0
            ? "bg-indigo-900 hover:bg-indigo-800 text-gray-200"
            : "bg-indigo-900 hover:bg-indigo-900 text-gray-500",
    );

    let models: string[] = $state([]);
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
                        const data: SummarizeResponse = JSON.parse(line);

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

<div class="flex flex-col gap-1 align-center items-start">
    <div>
        <button
            onclick={handleSummarizeRequest}
            class="py-2 px-4 rounded-lg font-semibold {summarizeButtonColors}"
        >
            Summarize {heartbeat ? "💚" : "💔"}
        </button>
        <select
            disabled={transcript.length == 0}
            bind:value={selectedModel}
            class="py-2 px-4 rounded-lg h-full {summarizeButtonColors}"
        >
            {#each models as model}
                <option value={model} class="text-white">{model}</option>
            {/each}
        </select>
    </div>

    {#if summary.length > 0}
        <div class="mt-6 text-left bg-indigo-950 shadow-sm p-4 rounded-lg">
            <h2 class="font-semibold mb-2 text-gray-200">Summary:</h2>
            <p class="text-gray-200 whitespace-pre-line">
                {summary || ""}
            </p>
        </div>
    {/if}
</div>
