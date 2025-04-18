<script lang="ts">
    import {onMount} from 'svelte';
    import {ScrollArea} from '@/components/ui/scroll-area';
    import {Separator} from "@/components/ui/separator";
    import {
        Chart as ChartJS,
        CategoryScale,
        LinearScale,
        BarElement,
        Title,
        Tooltip,
        Legend,
        BarController
    } from 'chart.js';
    import type {HuffmanCode} from '@/http/analytics';

    ChartJS.register(BarController, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

    type Props = {
        huffman: HuffmanCode;
    };

    const {huffman}: Props = $props();
    let canvas: HTMLCanvasElement;
    let chart: ChartJS | undefined;

    onMount(() => {
        if (!canvas) return;

        const sorted = Object.entries(huffman ?? {}).sort((a, b) => a[1].length - b[1].length);
        const labels = sorted.map(([char]) => char);
        const dataLengths = sorted.map(([, code]) => code.length);

        chart = new ChartJS(canvas, {
            type: "bar",
            data: {
                labels,
                datasets: [
                    {
                        label: 'Code Length',
                        data: dataLengths,
                        backgroundColor: '#4f46e5'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1
                        }
                    }
                }
            }
        });

        return () => chart?.destroy();
    });
</script>
<div class="flex flex-col gap-2">
    <div>
        <canvas bind:this={canvas}></canvas>
    </div>
    <h4 class="mb-4 text-sm font-medium leading-none">Code</h4>
    <ScrollArea class="h-64">
        {#each Object.entries(huffman).sort((a, b) => a[1].length - b[1].length) as [char, code]}
            <div class="text-sm font-mono">
                {char}: {code}
            </div>
            <Separator class="my-2"/>
        {/each}
    </ScrollArea>
</div>
