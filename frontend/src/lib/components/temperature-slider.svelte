<script lang="ts">
    import { Label } from "@/components/ui/label";
    import { Slider } from "@/components/ui/slider";

    type Props = {
        temperature: number;
    };

    let { temperature = $bindable() }: Props = $props();
</script>

<div class="flex flex-row gap-4 w-full max-w-xs items-center justify-between">
    <Label for="temperature">Temperature</Label>
    <Slider bind:value={temperature} max={2} min={0} step={0.1} type="single" />

    <span
        class="text-muted-foreground hover:border-border rounded-md border border-transparent px-2 py-0.5 text-right text-sm"
    >
        {temperature}
    </span>
</div>
