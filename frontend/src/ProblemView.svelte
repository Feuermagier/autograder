<script lang="ts">
    import { InCodeProblem, IN_CODE_PROBLEM, SuccesfulResult } from "./types";
    import CenterCard from "./CenterCard.svelte";
    import Fa from "svelte-fa";
    import {
        faQuestionCircle,
    } from "@fortawesome/free-solid-svg-icons";
import Accordion from "./Accordion.svelte";

    export let result: SuccesfulResult;

    function formatInCodeProblem(problem): string {
        let inCodeProblem = problem as InCodeProblem;
        return inCodeProblem.displayPath;
    }
</script>

<CenterCard>
    <p slot="header">Result</p>
    <div slot="content" class="min-h-0 min-w-full w-3/4-screen flex flex-col gap-5 overflow-auto">
        {#if result.spoon}
            <Accordion open={true}>
                <p slot="header" class="font-medium">Problems</p>
                <div slot="content">
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
                                    Position
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
                                            {formatInCodeProblem(problem)}
                                        </td>
                                    {/if}
                                </tr>
                            {/each}
                        </tbody>
                    </table>
                {:else}
                    <div class="bg-ok-green p-2">No problems found - good job!</div>
                {/if}
                </div>
            </Accordion>
        {/if}
        {#if result.pmd}
            <Accordion open={false}>
                <p slot="header" class="font-medium">PMD</p>
                <div slot="content">
                    {#if result.pmd.problems.length > 0}
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
                                    Position
                                </th>
                            </tr>
                        </thead>
                        <tbody class="bg-white divide-y divide-gray-200">
                            {#each result.pmd.problems as problem}
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
                                            {formatInCodeProblem(problem)}
                                        </td>
                                    {/if}
                                </tr>
                            {/each}
                        </tbody>
                    </table>
                {:else}
                    <div class="bg-ok-green p-2">No problems found - good job!</div>
                {/if}
                </div>
            </Accordion>
        {/if}
    </div>
</CenterCard>
