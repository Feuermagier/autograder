<script lang="ts">
    import type { CompilationErrorResult } from "./types";
    import DiagnosticsView from "./DiagnosticsView.svelte";
    import CenterCard from "./CenterCard.svelte";

    export let result: CompilationErrorResult;
</script>

<CenterCard hasCloseButton={true} on:close>
    <p slot="header">Compilation errors</p>
    <div slot="content" class="flex relative">
        <div class="overflow-auto w-3/4-screen space-y-5 relative m-2">
            <div class="w-full flex bg-error-red p-2">
                <p>There were compilation errors! Fix them and try again.</p>
            </div>
            <DiagnosticsView diagnostics={result.diagnostics} />
        </div>
    </div>
</CenterCard>
