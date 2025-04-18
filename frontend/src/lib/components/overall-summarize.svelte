<script lang="ts">
    import {onDestroy, onMount} from "svelte";
    import {Jumper} from "svelte-loading-spinners";
    import {Button} from "@/components/ui/button";
    import {streamPrompt} from "@/http/analytics";
    import type {PromptStatus} from "../../routes/api/analytics/prompt/+server";

    type Props = {
        model?: string;
        prompt: string;
        temperature: number;
        smartSessionIds: string[];
        summary: string;
    };

    // Input properties
    let {
        model,
        prompt,
        temperature,
        smartSessionIds,
        summary = $bindable()
    }: Props = $props();

    // Controller used for aborting the summarize call
    let abortController: AbortController | null = null;

    // The latest status of the current summarize call
    let promptStatus: PromptStatus | null = $state(null);

    async function handleSummarizeRequest() {
        if (!model) return;
        
        summary = "";
        abortController?.abort();
        abortController = new AbortController();

        try {
            for await (const data of streamPrompt(
                model,
                prompt,
                temperature,
                smartSessionIds,
                abortController.signal,
            )) {
                promptStatus = data.status;
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
            console.error("Summary failed:", err);
        } finally {
            promptStatus = null;
        }
    }

    onDestroy(() => {
        abortController?.abort();
    });
</script>

<Button
        disabled={promptStatus !== null}
        onclick={handleSummarizeRequest}
        type="button"
>
    {#if promptStatus === null}
        Generate
    {:else}
        <div class="flex flex-row gap-1 items-center align-middle">
            <span>Summarizing</span>
            <Jumper unit="em" size="1" color="#CCC" duration="1s"/>
        </div>
    {/if}
</Button>
