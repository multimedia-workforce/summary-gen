<script lang="ts">
    import * as Table from "@/components/ui/table";
    import {onMount} from "svelte";
    import {type SmartSession, fetchSmartSessions} from "@/http/persistence";
    import {format} from "date-fns";
    import * as Card from "@/components/ui/card";
    import {ScrollArea} from "@/components/ui/scroll-area";
    import {
        fetchSelectedHuffmanCode,
        fetchSelectedMetrics,
        type HuffmanCode,
        type SmartSessionMetrics
    } from "@/http/analytics";

    import MetricsView from "@/components/metrics-view.svelte";
    import HuffmanView from "@/components/huffman-view.svelte";
    import OverallSummaryView from "@/components/overall-summary-view.svelte";

    let sessions: SmartSession[] = $state([]);
    let selectedSessionIds: string[] = $state([]);
    let metrics: SmartSessionMetrics | null = $state(null);
    let huffman: HuffmanCode | null = $state(null);

    onMount(() => {
        fetchSmartSessions().then((value) => {
            sessions = value.sort((a, b) => b.createdAt - a.createdAt);
            toggleSelectAll();
        });
    });

    function updateAnalytics() {
        if (selectedSessionIds.length === 0) {
            metrics = null;
            huffman = null;
            return;
        }

        fetchSelectedMetrics(selectedSessionIds).then((value) => {
            metrics = value;
        })
        fetchSelectedHuffmanCode(selectedSessionIds).then((value) => {
            huffman = value;
        })
    }

    function truncate(text: string, maxWords = 8) {
        if (!text) return "";
        const words = text.split(/\s+/);
        return words.slice(0, maxWords).join(" ") + (words.length > maxWords ? "…" : "");
    }

    function toggleSelection(id: string) {
        if (selectedSessionIds.includes(id)) {
            selectedSessionIds = selectedSessionIds.filter(existingId => existingId !== id);
        } else {
            selectedSessionIds = [...selectedSessionIds, id];
        }
        updateAnalytics();
    }

    function isSelected(id: string): boolean {
        return selectedSessionIds.includes(id);
    }

    function toggleSelectAll() {
        if (isAllSelected()) {
            selectedSessionIds = [];
        } else {
            selectedSessionIds = sessions.map(session => session.id);
        }
        updateAnalytics();
    }

    function isAllSelected(): boolean {
        return selectedSessionIds.length === sessions.length && sessions.length > 0;
    }

    function isSomeSelected(): boolean {
        return selectedSessionIds.length > 0 && selectedSessionIds.length < sessions.length;
    }

    // Custom action to support setting the indeterminate state
    function setIndeterminate(node: HTMLInputElement, condition: boolean) {
        node.indeterminate = condition;

        return {
            update(condition: boolean) {
                node.indeterminate = condition;
            }
        };
    }
</script>

<div class="hidden space-y-6 px-10 pb-10 md:block">
    <div class="space-y-0.5">
        <h2 class="text-2xl font-bold tracking-tight">Analytics</h2>
        <p class="text-muted-foreground">Perform text analysis on your previous smart sessions.</p>
    </div>

    <div class="grid grid-cols-2 gap-4 h-[42vh]">
        <Card.Root>
            <Card.Header>
                <Card.Title>Available Smart Sessions</Card.Title>
                <Card.Description>Toggle selection to update analytics</Card.Description>
            </Card.Header>
            <Card.Content>
                <ScrollArea class="h-[33vh]">
                    <Table.Root>
                        <Table.Header>
                            <Table.Row>
                                <Table.Head class="w-[5%]">
                                    <input
                                            checked={isAllSelected()}
                                            onchange={toggleSelectAll}
                                            type="checkbox"
                                            use:setIndeterminate={isSomeSelected()}
                                    />
                                </Table.Head>
                                <Table.Head class="w-[5%]">#</Table.Head>
                                <Table.Head class="w-[30%]">Transcript</Table.Head>
                                <Table.Head class="w-[45%]">Summary</Table.Head>
                                <Table.Head class="w-[20%]">Date</Table.Head>
                            </Table.Row>
                        </Table.Header>
                        <Table.Body>
                            {#each sessions as session, i}
                                <Table.Row>
                                    <Table.Cell>
                                        <input
                                                type="checkbox"
                                                checked={isSelected(session.id)}
                                                onchange={() => toggleSelection(session.id)}
                                        />
                                    </Table.Cell>
                                    <Table.Cell class="font-mono text-sm text-muted-foreground">{i + 1}</Table.Cell>
                                    <Table.Cell>{truncate(session.transcription?.text)}</Table.Cell>
                                    <Table.Cell>{truncate(session.summary?.text)}</Table.Cell>
                                    <Table.Cell>
                                        {format(new Date(session.createdAt), 'yyyy-MM-dd HH:mm')}
                                    </Table.Cell>
                                </Table.Row>
                            {/each}
                        </Table.Body>
                    </Table.Root>
                </ScrollArea>
            </Card.Content>
        </Card.Root>
        <Card.Root>
            <Card.Header>
                <Card.Title>Overall Summary Prompt</Card.Title>
                <Card.Description>Generate a summary of multiple smart sessions using AI</Card.Description>
            </Card.Header>
            <Card.Content>
                <OverallSummaryView smartSessionIds={selectedSessionIds}/>
            </Card.Content>
        </Card.Root>
    </div>

    <div class="grid grid-cols-2 gap-4 h-[42vh]">
        <Card.Root>
            <Card.Header>
                <Card.Title>Session Metrics</Card.Title>
                <Card.Description>Individual metrics for transcription and summary</Card.Description>
            </Card.Header>
            <Card.Content>
                {#if metrics !== null}
                    <MetricsView {metrics}></MetricsView>
                {/if}
            </Card.Content>
        </Card.Root>
        <Card.Root>
            <Card.Header>
                <Card.Title>Huffman-Code</Card.Title>
                <Card.Description>The Huffman-Code of the transcription</Card.Description>
            </Card.Header>
            <Card.Content>
                {#if huffman !== null}
                    <HuffmanView {huffman}></HuffmanView>
                {/if}
            </Card.Content>
        </Card.Root>
    </div>
</div>