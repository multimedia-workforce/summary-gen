<script lang="ts">
    import * as Table from "@/components/ui/table";
    import {onMount} from "svelte";
    import {type SmartSession, fetchSmartSessions} from "@/http/persistence";
    import {format} from "date-fns";

    let sessions: Array<SmartSession> = $state([]);

    onMount(() => {
        fetchSmartSessions().then((value) => {
            sessions = value.sort((a, b) => b.createdAt - a.createdAt); // Sort by most recent
        });
    });

    function truncate(text: string, maxWords = 8) {
        if (!text) return "";
        const words = text.split(/\s+/);
        return words.slice(0, maxWords).join(" ") + (words.length > maxWords ? "…" : "");
    }
</script>

<div class="hidden space-y-6 px-10 pb-10 md:block">
    <div class="space-y-0.5">
        <h2 class="text-2xl font-bold tracking-tight">Job History</h2>
        <p class="text-muted-foreground">
            Look at your previous transcriptions/summaries.
        </p>
    </div>

    <Table.Root>
        <Table.Header>
            <Table.Row>
                <Table.Head class="w-[5%]">#</Table.Head>
                <Table.Head class="w-[30%]">Transcript</Table.Head>
                <Table.Head class="w-[30%]">Summary</Table.Head>
                <Table.Head class="w-[20%] text-right">Date</Table.Head>
                <Table.Head class="w-[15%] text-right">Action</Table.Head>
            </Table.Row>
        </Table.Header>
        <Table.Body>
            {#each sessions as session, i}
                <Table.Row>
                    <Table.Cell class="font-mono text-sm text-muted-foreground">{i + 1}</Table.Cell>
                    <Table.Cell>{truncate(session.transcription?.text)}</Table.Cell>
                    <Table.Cell>{truncate(session.summary?.text)}</Table.Cell>
                    <Table.Cell class="text-right">
                        {format(new Date(session.createdAt), 'yyyy-MM-dd HH:mm')}
                    </Table.Cell>
                    <Table.Cell class="text-right">
                        <a href={`/dashboard/history/${session.id}`} class="text-blue-600 hover:underline">Details</a>
                    </Table.Cell>
                </Table.Row>
            {/each}
        </Table.Body>
    </Table.Root>
</div>
