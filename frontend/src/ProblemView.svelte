<script lang="ts">
    import CenterCard from "./CenterCard.svelte";
    import Accordion from "./Accordion.svelte";
    import InCodeProblemView from "./InCodeProblemView.svelte";
    import type {
        CompilationDiagnostic,
        InCodeProblem,
        Problem,
        SuccesfulResult,
    } from "./types";
    import DiagnosticsView from "./DiagnosticsView.svelte";

    export let result: SuccesfulResult;

    let disclaimerVisible = true;

    function castProblems(problems: Problem[]): InCodeProblem[] {
        return problems as InCodeProblem[];
    }
</script>

<CenterCard hasCloseButton={true} on:close>
    <p slot="header">Result</p>
    <div slot="content" class="flex relative">
        {#if disclaimerVisible}
            <div
                class="absolute z-20 p-5 bg-gray-400 bg-opacity-90 w-full h-full flex flex-col justify-around items-center"
            >
                <div class="flex justify-between flex-col bg-white rounded p-2">
                    <div class="flex flex-col place-items-center">
                        <p class="font-bold text-lg">Disclaimer</p>
                        <p class="max-w-prose">
                            This is not an official product. The results may
                            miss (esp. complex) problems and may contain
                            false-positives. Please use common sense!
                        </p>
                    </div>
                    <div class="flex flex-row justify-end mt-4">
                        <button
                            on:click={() => (disclaimerVisible = false)}
                            class="p-2 rounded-lg bg-blue-300"
                        >
                            I understand
                        </button>
                    </div>
                </div>
            </div>
        {/if}

        <div
            class="overflow-auto w-3/4-screen flex flex-col space-y-5 relative m-2"
        >
            {#if result.compilation}
                <Accordion open={true}>
                    <p slot="header" class="font-medium">Compilation</p>
                    <div slot="content">
                        {#if result.compilation.diagnostics.length > 0}
                        <div class="bg-error-red p-2">
                            <p>You should fix those compilation warnings! Reproduce them using 'javac -Xlint:all -Xlint:-serial -Xlint:-process'</p>
                        </div>
                        <DiagnosticsView
                            diagnostics={result.compilation.diagnostics}
                        />
                    {:else}
                        <div class="bg-ok-green p-2">
                            <p>There were no warnings.</p>
                        </div>
                    {/if}
                    </div>
                </Accordion>
            {/if}
            {#if result.spoon}
                <Accordion open={true}>
                    <p slot="header" class="font-medium">Problems</p>
                    <div slot="content">
                        {#if result.spoon.problems.length > 0}
                            <InCodeProblemView
                                problems={castProblems(result.spoon.problems)}
                            />
                        {:else}
                            <div class="bg-ok-green p-2">
                                <p>No problems found - good job!</p>
                            </div>
                        {/if}
                    </div>
                </Accordion>
            {/if}
            {#if result.pmd}
                <Accordion open={false}>
                    <p slot="header" class="font-medium">PMD</p>
                    <div slot="content">
                        {#if result.pmd.problems.length > 0}
                            <InCodeProblemView
                                problems={castProblems(result.pmd.problems)}
                            />
                        {:else}
                            <div class="bg-ok-green p-2">
                                No problems found - good job!
                            </div>
                        {/if}
                    </div>
                </Accordion>
            {/if}
        </div>
    </div>
</CenterCard>
