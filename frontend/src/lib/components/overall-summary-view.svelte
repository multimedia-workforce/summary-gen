<script lang="ts">
    import OverallSummarize from "./overall-summarize.svelte";
    import ModelSelect from "./model-select.svelte";
    import TemperatureSlider from "./temperature-slider.svelte";
    import {Textarea} from "@/components/ui/textarea";
    import {ScrollArea} from "@/components/ui/scroll-area";

    let model: string | undefined = $state(undefined);
    let temperature: number = $state(0.1);
    let summary: string = $state("");
    let prompt: string = $state("");

    type Props = {
        smartSessionIds: string[]
    };

    const {smartSessionIds}: Props = $props();
</script>

<div class="flex flex-col gap-2">
    <Textarea bind:value={prompt} placeholder="Enter additional prompt information here..."></Textarea>
    <div class="flex flex-row gap-2">
        <OverallSummarize bind:summary {model} {prompt} {smartSessionIds} {temperature}/>
        <ModelSelect bind:model/>
        <TemperatureSlider bind:temperature/>
    </div>
    {#if summary.length > 0}
        <div class="flex-1 bg-muted rounded-md border p-4">
            <ScrollArea class="h-[22vh]">

                    <span class="font-mono text-sm text-muted-foreground">
                        {summary}
                    </span>
            </ScrollArea>

        </div>
    {/if}
</div>
