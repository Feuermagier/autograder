<script lang="ts">
    import CenterCard from "./CenterCard.svelte";
    import { linter } from "./linter";
    let files;

    let uploadEnabled;
    $: uploadEnabled = files != null && files.length > 0;

    function confirmFile() {
        linter.setFile(files[0]);
    }
</script>

<CenterCard>
    <p slot="header">Upload</p>
    <div slot="content" class="min-w-full w-">
        <p>
            Compress your whole <span class="font-mono">src</span>-folder into a
            ZIP-File and upload it here.
        </p>
        <div class="flex justify-center">
            <input type="file" accept=".zip" bind:files class="p-4" />
        </div>
        <div class="flex justify-end">
            <button
                type="button"
                on:click={confirmFile}
                disabled={!uploadEnabled}
                class="p-2 rounded-lg bg-blue-300 disabled:opacity-50 disabled:cursor-not-allowed"
                >Go!</button
            >
        </div>
    </div>
</CenterCard>
