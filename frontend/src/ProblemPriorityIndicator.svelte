<script lang="ts">
    import { ProblemPriority } from "./types";   
    import Fa from "svelte-fa";
    import { faExclamationCircle, faExclamationTriangle, faSkullCrossbones } from "@fortawesome/free-solid-svg-icons";

    export let priority: string;
</script>

{#if priority == ProblemPriority.SEVERE || priority == ProblemPriority.POSSIBLE_SEVERE}
    <Fa icon={faSkullCrossbones} class="inline" primaryColor="orangered" size="lg" />
{:else if priority == ProblemPriority.FIX_RECOMMENDED}
    <Fa icon={faExclamationTriangle} class="inline" primaryColor="orange" size="lg" />
{:else if priority == ProblemPriority.INFO}
    <Fa icon={faExclamationCircle} class="inline" primaryColor="blue" size="lg" />
{/if}
