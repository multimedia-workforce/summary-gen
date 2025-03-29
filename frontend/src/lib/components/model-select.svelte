<script lang="ts">
    import * as Select from "@/components/ui/select";
    import { getModels } from "@/http/summarize";
    import { onMount } from "svelte";

    type Props = {
        model?: string;
    };

    let { model = $bindable() }: Props = $props();
    let models: Array<string> = $state([]);

    function fetchModels() {
        getModels().then((value) => {
            models = value;
            if (models.length === 1) {
                model = models[0];
            }
        });
    }

    onMount(() => {
        fetchModels();
    });
</script>

<Select.Root type="single" bind:value={model}>
    <Select.Trigger>
        {model}
    </Select.Trigger>
    <Select.Content>
        <Select.Group>
            <Select.GroupHeading>Models</Select.GroupHeading>
            {#each models as m}
                <Select.Item value={m} label={m} />
            {/each}
        </Select.Group>
    </Select.Content>
</Select.Root>
