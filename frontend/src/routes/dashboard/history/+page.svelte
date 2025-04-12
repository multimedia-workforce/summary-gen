<script lang="ts">
    import * as Table from "@/components/ui/table";
    import {onMount} from "svelte";
    import {type SmartSession, fetchSmartSessions} from "@/http/persistence";

    let sessions: Array<SmartSession> = $state([]);
    onMount(() => {
        fetchSmartSessions().then((value) => {
            sessions = value;
        });
    });
</script>

<div class="hidden space-y-6 px-10 pb-10 md:block">
    <div class="space-y-0.5">
        <h2 class="text-2xl font-bold tracking-tight">Job History</h2>
        <p class="text-muted-foreground">
            Look at your previous transcriptions/summaries.
        </p>
    </div>
    <div class="flex flex-col space-y-8 lg:flex-row lg:space-x-12 lg:space-y-0">
        {sessions}
    </div>
</div>
