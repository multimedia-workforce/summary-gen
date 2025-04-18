<script lang="ts">
    import {type SmartSessionMetrics} from "@/http/analytics";
    import {
        Chart as ChartJS,
        CategoryScale,
        LinearScale,
        BarElement,
        Title,
        Tooltip,
        Legend
    } from 'chart.js';
    import {onMount} from 'svelte';

    ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

    type Props = {
        metrics: SmartSessionMetrics;
    };

    const {metrics}: Props = $props();

    let heatmapCanvas: HTMLCanvasElement;
    let heatmapChart: ChartJS;

    function formatFloat(value: number): string {
        return value.toFixed(2);
    }

    onMount(() => {
        if (metrics.dailyHeatmap?.length > 0 && heatmapCanvas) {
            const labels = metrics.dailyHeatmap.map(cell => cell.date);
            const counts = metrics.dailyHeatmap.map(cell => cell.count);

            heatmapChart = new ChartJS(heatmapCanvas, {
                type: 'bar',
                data: {
                    labels,
                    datasets: [{
                        label: 'Sessions per Day',
                        data: counts,
                        backgroundColor: '#10b981'
                    }]
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
        }

        return () => heatmapChart?.destroy();
    });
</script>

<div class="h-full w-full space-y-4 overflow-auto text-sm">
    {#if metrics.transcriptionMetrics}
        <div>
            <h4 class="font-semibold mb-1">Transcription Stats</h4>
            <ul class="grid grid-cols-2 gap-x-4">
                <li>Total: {metrics.transcriptionMetrics.totalTranscriptions}</li>
                <li>Avg Length: {formatFloat(metrics.transcriptionMetrics.textStats.averageLength)}</li>
                <li>Min Length: {metrics.transcriptionMetrics.textStats.minLength}</li>
                <li>Max Length: {metrics.transcriptionMetrics.textStats.maxLength}</li>
                <li>Avg Generation Time: {formatFloat(metrics.transcriptionMetrics.createTimeStats.average)} ms</li>
                <li>Most Active Weekday: {metrics.transcriptionMetrics.activityStats.mostActiveWeekday}</li>
            </ul>
        </div>
    {/if}

    {#if metrics.summaryMetrics}
        <div>
            <h3 class="font-semibold mb-1">Summary Stats</h3>
            <ul class="grid grid-cols-2 gap-x-4">
                <li>Total: {metrics.summaryMetrics.totalSummaries}</li>
                <li>Avg Length: {formatFloat(metrics.summaryMetrics.textStats.averageLength)}</li>
                <li>Min Length: {metrics.summaryMetrics.textStats.minLength}</li>
                <li>Max Length: {metrics.summaryMetrics.textStats.maxLength}</li>
                <li>Avg Generation Time: {formatFloat(metrics.summaryMetrics.createTimeStats.average)} ms</li>
                <li>Most Active Weekday: {metrics.summaryMetrics.activityStats.mostActiveWeekday}</li>
            </ul>
        </div>
    {/if}
    
    {#if metrics.dailyHeatmap?.length !== undefined && metrics.dailyHeatmap?.length > 0}
        <div>
            <h3 class="font-semibold mb-1">Heatmap</h3>
            <div class="h-48">
                <canvas bind:this={heatmapCanvas}></canvas>
            </div>
        </div>
    {/if}
</div>
