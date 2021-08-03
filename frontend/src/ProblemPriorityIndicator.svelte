<script lang="ts">
    import { ProblemPriority } from "./types";
    import Fa from "svelte-fa";
    import {
        faExclamationCircle,
        faExclamationTriangle,
        faSkullCrossbones,
    } from "@fortawesome/free-solid-svg-icons";

    export let priority: string;
</script>

{#if priority == ProblemPriority.SEVERE || priority == ProblemPriority.POSSIBLE_SEVERE}
    <span class="has-tooltip">
        <Fa
            icon={faSkullCrossbones}
            class="inline"
            primaryColor="orangered"
            size="lg"
        />
        <span
            class="tooltip mt-5 border-2 border-gray-500 rounded bg-white shadow-md p-4 max-w-prose"
        >
            <span class="font-bold">Fix this!</span> This problem will most likely impact your grade heavily.
        </span>
    </span>
{:else if priority == ProblemPriority.FIX_RECOMMENDED}
    <span class="has-tooltip">
        <Fa
            icon={faExclamationTriangle}
            class="inline"
            primaryColor="orange"
            size="lg"
        />
        <span
            class="tooltip mt-5 border-2 border-gray-500 rounded bg-white shadow-md p-4 max-w-prose"
        >
            You should fix this problem if there is enough time.
        </span>
    </span>
{:else if priority == ProblemPriority.INFO}
    <span class="has-tooltip">
        <Fa
            icon={faExclamationCircle}
            class="inline"
            primaryColor="blue"
            size="lg"
        />
        <span
            class="tooltip mt-5 border-2 border-gray-500 rounded bg-white shadow-md p-4 max-w-prose"
        >
            This will most likely not directly affect your grade. However, if
            you fix this problem, your code will leave a better general
            impression.
        </span>
    </span>
{/if}
