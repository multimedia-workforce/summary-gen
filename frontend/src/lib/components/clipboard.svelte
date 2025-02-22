<script lang="ts">
    import { Copy } from "lucide-svelte";
    import { Popover } from "@skeletonlabs/skeleton-svelte";

    type Props = {
        size: number;
        content: string;
        disabled: boolean;
    };

    let open = $state(false);
    let { size, content, disabled }: Props = $props();

    function handleCopy() {
        navigator.clipboard.writeText(content);
        setInterval(() => {
            open = false;
        }, 3000);
    }
</script>

{#if !disabled}
    <Popover
        {open}
        onOpenChange={(e) => (open = e.open)}
        onclick={handleCopy}
        positioning={{ placement: "top" }}
        triggerBase="btn"
        contentBase="card bg-surface-100-900 p-2"
    >
        {#snippet trigger()}
            <Copy {size} />
        {/snippet}
        {#snippet content()}
            Copied!
        {/snippet}
    </Popover>
{/if}
