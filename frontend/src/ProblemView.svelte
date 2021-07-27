<script lang="ts">
    import { InCodeProblem, IN_CODE_PROBLEM, SpoonResult, SuccesfulResult } from "./types";
    import CenterCard from "./CenterCard.svelte";
    import Fa from "svelte-fa";
    import {
        faQuestionCircle,
        faCaretRight,
        faCaretDown
    } from "@fortawesome/free-solid-svg-icons";

    export let result: SuccesfulResult;

    let pmdVisible = false;

    function formatInCodePosition(spoonProblem): string {
        let problem = spoonProblem as InCodeProblem;
        return problem.qualifiedClass + ":" + problem.line;
    }
</script>

<CenterCard>
    <p slot="header">Result</p>
    <div slot="content" class="min-h-0 min-w-full w-3/4-screen flex flex-col gap-5">
        {#if result.spoon}
            {#if result.spoon.problems.length > 0}
                <table class="min-w-full divide-y">
                    <thead class="bg-gray-50">
                        <tr>
                            <th
                                scope="col"
                                class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                            >
                                Type
                            </th>
                            <th
                                scope="col"
                                class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                            >
                                Problem
                            </th>
                            <th
                                scope="col"
                                class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                            >
                                Positon
                            </th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        {#each result.spoon.problems as problem}
                            <tr>
                                <td class="px-6 py-4">{problem.category}</td>
                                <td class="px-6 py-4">
                                    {problem.description}
                                    <span class="has-tooltip">
                                        <Fa
                                            icon={faQuestionCircle}
                                            class="inline"
                                        />
                                        <span
                                            class="tooltip mt-5 border-2 border-gray-500 rounded bg-white shadow-md p-4 max-w-prose"
                                        >
                                            {problem.explanation}
                                        </span>
                                    </span>
                                </td>
                                {#if problem.type == IN_CODE_PROBLEM}
                                    <td class="px-6 py-4">
                                        {formatInCodePosition(problem)}
                                    </td>
                                {/if}
                            </tr>
                        {/each}
                    </tbody>
                </table>
            {:else}
                <div class="bg-ok-green p-2">No problems found - good job!</div>
            {/if}
        {/if}
        {#if result.pmd}
            <div
                class="bg-gray-100 border-black flex flex-row justify-between px-5"
            >
                <p class="font-medium">PMD</p>
                <button on:click={() => (pmdVisible = !pmdVisible)}>
                    {#if pmdVisible}
                        <Fa icon={faCaretDown} size="2x" />
                    {:else}
                        <Fa icon={faCaretRight} size="2x" />
                    {/if}
                </button>
            </div>
            {#if pmdVisible}
                <div class="overflow-auto whitespace-pre-wrap">
                    <p>
                        {result.pmd.result}
                    </p>
                </div>
            {/if}
        {/if}
    </div>
</CenterCard>
