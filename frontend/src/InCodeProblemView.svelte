<script lang="ts">
    import { InCodeProblem, IN_CODE_PROBLEM, ProblemPriority } from "./types";
    import Fa from "svelte-fa";
    import { faQuestionCircle } from "@fortawesome/free-solid-svg-icons";
    import ProblemPriorityIndicator from "./ProblemPriorityIndicator.svelte";

    export let problems: InCodeProblem[];

    problems.sort(problemSorter);

    function problemSorter(p, q): number {
        switch (p.priority) {
            case ProblemPriority.SEVERE:
            case ProblemPriority.POSSIBLE_SEVERE:
                switch (q.priority) {
                    case ProblemPriority.SEVERE:
                    case ProblemPriority.POSSIBLE_SEVERE:
                        return p.displayPath.localeCompare(q.displayPath);
                    default:
                        return -1;
                }
            case ProblemPriority.FIX_RECOMMENDED:
                switch (q.priority) {
                    case ProblemPriority.SEVERE:
                    case ProblemPriority.POSSIBLE_SEVERE:
                        return 1;
                    case ProblemPriority.FIX_RECOMMENDED:
                        return p.displayPath.localeCompare(q.displayPath);
                    default:
                        return -1;
                }
            case ProblemPriority.INFO:
                switch (q.priority) {
                    case ProblemPriority.INFO:
                        return p.displayPath.localeCompare(q.displayPath);
                    default:
                        return 1;
                }
        }
    }

    function formatPath(problem): string {
        let inCodeProblem = problem as InCodeProblem;
        return inCodeProblem.displayPath;
    }
</script>

<table class="min-w-full divide-y">
    <thead class="bg-gray-50">
        <tr>
            <th
                scope="col"
                class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
            >
                <!-- Priority -->
            </th>
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
        {#each problems as problem}
            <tr>
                <td class="px-6 py-4">
                    <ProblemPriorityIndicator priority={problem.priority} />
                </td>
                <td class="px-6 py-4">{problem.category}</td>
                <td class="px-6 py-4">
                    {problem.description}
                    <span class="has-tooltip">
                        <Fa icon={faQuestionCircle} class="inline" />
                        <span
                            class="tooltip mt-5 border-2 border-gray-500 rounded bg-white shadow-md p-4 max-w-prose"
                        >
                            {problem.explanation}
                        </span>
                    </span>
                </td>
                {#if problem.type == IN_CODE_PROBLEM}
                    <td class="px-6 py-4">
                        {formatPath(problem)}
                    </td>
                {/if}
            </tr>
        {/each}
    </tbody>
</table>
