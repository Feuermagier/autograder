<script lang="ts">
    import Fa from "svelte-fa";
    import {
        faCaretRight,
        faCaretDown,
        faCheck,
        faCheckCircle,
        faExclamationCircle,
    } from "@fortawesome/free-solid-svg-icons";

    export let open = false;
    export let expandable = true;
</script>

<div class="bg-white border-2 border-gray-200 shadow-md m-0.5">
    {#if expandable}
        <button
            on:click={() => (open = !open)}
            class="w-full flex flex-row justify-between px-5 items-center"
        >
            <div class="flex flex-row justify-start items-center gap-2">
                <Fa
                    icon={faExclamationCircle}
                    primaryColor="red"
                    size="1x"
                    class="inline"
                />
                <slot name="header">
                    <p>Default header</p>
                </slot>
            </div>

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
    {:else}
        <div class="flex flex-row justify-start items-center gap-2 px-5 py-1">
            <Fa
                icon={faCheckCircle}
                primaryColor="green"
                size="1x"
                class="inline"
            />
            <slot name="header">
                <p>Default header</p>
            </slot>
        </div>
    {/if}
</div>
