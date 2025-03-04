<script lang="ts">
    import { FileUpload } from "@skeletonlabs/skeleton-svelte";
    import IconDropzone from "lucide-svelte/icons/file-plus";
    import IconFile from "lucide-svelte/icons/paperclip";
    import IconRemove from "lucide-svelte/icons/circle-x";

    import { type FileChangeDetails } from "@zag-js/file-upload";

    type Props = {
        file?: File;
    };

    let { file = $bindable() }: Props = $props();

    function handleFileSelect(details: FileChangeDetails) {
        if (!details.acceptedFiles) return;
        file = details.acceptedFiles[0];
    }
</script>

<FileUpload
    label="Click here to upload your file or drag."
    subtext="Supports any audio/video file"
    name="media-file"
    accept="video/*"
    maxFiles={1}
    onFileChange={handleFileSelect}
    classes="w-full"
>
    {#snippet iconInterface()}<IconDropzone class="size-8" />{/snippet}
    {#snippet iconFile()}<IconFile class="size-4" />{/snippet}
    {#snippet iconFileRemove()}<IconRemove class="size-4" />{/snippet}
</FileUpload>
