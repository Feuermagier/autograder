<script lang="ts">
    import Fa from "svelte-fa";
    import {
        faCaretRight,
        faCaretDown,
    } from "@fortawesome/free-solid-svg-icons";

    export let open = false;
</script>

<div class="bg-white border-2 border-gray-200 shadow-md m-0.5">
    <button
        on:click={() => (open = !open)}
        class="w-full flex flex-row justify-between px-5 items-center"
    >
        <slot name="header"><p>Default header</p></slot>

        {#if open}
            <Fa icon={faCaretDown} size="2x" />
        {:else}
            <Fa icon={faCaretRight} size="2x" />
        {/if}
    </button>
    {#if open}
        <div class="p-2">
            <slot name="content"><p>Default content</p></slot>
        </div>
    {/if}
</div>
