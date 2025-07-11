<script lang="ts">
    import { Button } from "$lib/components/ui/button";
    import {
        ACCEPT_VIDEO,
        displaySize,
        FileDropZone,
        type FileDropZoneProps,
    } from "$lib/components/ui/file-drop-zone";
    import { Progress } from "$lib/components/ui/progress";
    import { sleep } from "$lib/utils";
    import { X } from "lucide-svelte";
    import { onDestroy } from "svelte";
    import { toast } from "svelte-sonner";
    import { SvelteDate } from "svelte/reactivity";

    type Props = {
        files: Array<UploadedFile>;
        maxFiles: number;
    };

    let { files = $bindable(), maxFiles }: Props = $props();

    const onUpload: FileDropZoneProps["onUpload"] = async (files) => {
        await Promise.allSettled(files.map((file) => uploadFile(file)));
    };

    const onFileRejected: FileDropZoneProps["onFileRejected"] = async ({
        reason,
        file,
    }) => {
        toast.error(`${file.name} failed to upload!`, { description: reason });
    };

    const uploadFile = async (file: File) => {
        // don't upload duplicate files
        if (files.find((f) => f.name === file.name)) return;

        const urlPromise = new Promise<string>((resolve) => {
            // add some fake loading time
            sleep(1000).then(() => resolve(URL.createObjectURL(file)));
        });

        files.push({
            name: file.name,
            type: file.type,
            size: file.size,
            uploadedAt: Date.now(),
            file: file,
            url: urlPromise,
        });

        // we await since we don't want the onUpload to be complete until the files are actually uploaded
        await urlPromise;
    };

    export type UploadedFile = {
        name: string;
        type: string;
        size: number;
        uploadedAt: number;
        file: File;
        url: Promise<string>;
    };

    let date = new SvelteDate();

    onDestroy(async () => {
        for (const file of files) {
            URL.revokeObjectURL(await file.url);
        }
    });

    $effect(() => {
        const interval = setInterval(() => {
            date.setTime(Date.now());
        }, 10);

        return () => {
            clearInterval(interval);
        };
    });
</script>

<div class="flex w-full flex-col gap-2">
    <FileDropZone
        {onUpload}
        {onFileRejected}
        maxFileSize={undefined}
        accept={ACCEPT_VIDEO}
        {maxFiles}
        fileCount={files.length}
    />
    {#if files.length > 0}
        <div class="flex flex-col gap-2">
            {#each files as file, i (file.name)}
                <div class="flex place-items-center justify-between gap-2">
                    <div class="flex place-items-center gap-2">
                        <div class="flex flex-col">
                            <span>{file.name}</span>
                            <span class="text-xs text-muted-foreground"
                                >{displaySize(file.size)}</span
                            >
                        </div>
                    </div>
                    {#await file.url}
                        <Progress
                            class="h-2 w-full flex-grow"
                            value={((date.getTime() - file.uploadedAt) / 1000) *
                                100}
                            max={100}
                        />
                    {:then url}
                        <Button
                            variant="outline"
                            size="icon"
                            onclick={() => {
                                URL.revokeObjectURL(url);
                                files = [
                                    ...files.slice(0, i),
                                    ...files.slice(i + 1),
                                ];
                            }}
                        >
                            <X />
                        </Button>
                    {/await}
                </div>
            {/each}
        </div>
    {/if}
</div>
