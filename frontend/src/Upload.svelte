<script lang="ts">
    import { faSpinner } from "@fortawesome/free-solid-svg-icons";
    import Fa from "svelte-fa";
    import CenterCard from "./CenterCard.svelte";
    import { linter } from "./linter";
    let files;

    let uploadEnabled;
    $: uploadEnabled = files != null && files.length > 0 && !$linter.loading;

    function confirmFile() {
        linter.setFile(files[0]);
    }
</script>

<CenterCard>
    <p slot="header">Upload</p>
    <div slot="content" class="min-w-full">
        <p>
            Compress your whole <span class="font-mono">src</span>-folder into a
            ZIP-File and upload it here.
        </p>
        <div class="flex justify-center">
            {#if !$linter.loading}
                <input
                    type="file"
                    accept=".zip"
                    bind:files
                    on:change={confirmFile}
                    disabled={$linter.loading}
                    class="p-4"
                />
            {:else}
                <div>
                    <Fa icon={faSpinner} class="animate-spin" size="2x" />
                </div>
            {/if}
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
