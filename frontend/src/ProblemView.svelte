<script lang="ts">
    import CenterCard from "./CenterCard.svelte";
    import Accordion from "./Accordion.svelte";
    import InCodeProblemView from "./InCodeProblemView.svelte";
    import type { InCodeProblem, Problem, SuccesfulResult } from "./types";

    export let result: SuccesfulResult;

    function castProblems(problems: Problem[]): InCodeProblem[] {
        return problems as InCodeProblem[];
    }
</script>

<CenterCard>
    <p slot="header">Result</p>
    <div
        slot="content"
        class="min-h-0 min-w-full w-3/4-screen flex flex-col gap-5 overflow-auto relative"
    >
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
                            No problems found - good job!
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
</CenterCard>
