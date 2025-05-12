<script lang="ts">
    import * as Table from "@/components/ui/table";
    import {Button} from "@/components/ui/button";
    import {Trash2} from "@lucide/svelte";
    import {onMount} from "svelte";1
    import {type SmartSession, fetchSmartSessions, deleteSmartSession} from "@/http/persistence";
    import {format} from "date-fns";
    import {goto} from "$app/navigation";

    let sessions: Array<SmartSession> = $state([]);

    /**
     * Whenever the page is mounted, we fetch and sort the smart sessions
     * from the backend.
     */
    onMount(() => {
        fetchAndSortSmartSession();
    });

    /**
     * Fetches and sorts the smart sessions from the persistence backend
     */
    function fetchAndSortSmartSession() {
        fetchSmartSessions().then((value) => {
            sessions = value.sort((a, b) => b.createdAt - a.createdAt); // Sort by most recent
        });
    }

    /**
     * Truncates the given text after the specified amount of words
     * @param text The text that shall be truncated
     * @param maxWords The number of words after to truncate
     * @return The truncated text
     */
    function truncate(text: string, maxWords = 8) {
        if (!text) return "";
        const words = text.split(/\s+/);
        return words.slice(0, maxWords).join(" ") + (words.length > maxWords ? "…" : "");
    }

    /**
     * Handler that is executed whenever a smart session should be deleted
     * @param id The ID of the smart session that shall be deleted
     */
    function handleDelete(id: string) {
        deleteSmartSession(id).then(() => {
            fetchAndSortSmartSession();
        });
    }

    /**
     * Handler that is executed whenever the user wants to visit the details of a smart session
     * @param id The ID of the smart session that shall be visited
     */
    function handleNavigateToDetails(id: string) {
        goto(`/dashboard/history/${id}`);
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
                <Table.Head class="w-[20%]">Date</Table.Head>
                <Table.Head class="w-[15%]">Action</Table.Head>
            </Table.Row>
        </Table.Header>
        <Table.Body>
            {#each sessions as session, i}
                <Table.Row>
                    <Table.Cell class="font-mono text-sm text-muted-foreground">{i + 1}</Table.Cell>
                    <Table.Cell>{truncate(session.transcription?.text)}</Table.Cell>
                    <Table.Cell>{truncate(session.summary?.text)}</Table.Cell>
                    <Table.Cell>
                        {format(new Date(session.createdAt), 'yyyy-MM-dd HH:mm')}
                    </Table.Cell>
                    <Table.Cell class="text-right flex flex-row gap-2">
                        <Button variant="outline" onclick={() => handleNavigateToDetails(session.id)}>Details</Button>
                        <Button variant="destructive" onclick={() => handleDelete(session.id)}><Trash2 /></Button>
                    </Table.Cell>
                </Table.Row>
            {/each}
        </Table.Body>
    </Table.Root>
</div>
