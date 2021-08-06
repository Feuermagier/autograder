<script lang="ts">
    import { faWindowClose } from "@fortawesome/free-solid-svg-icons";
    import { createEventDispatcher } from "svelte";
    import Fa from "svelte-fa";

    export let hasCloseButton = false;

    const dispatch = createEventDispatcher();

    function onClosePressed() {
        dispatch("close");
    }
</script>

<div class="grid flex-grow place-items-center">
    <div
        class="flex flex-col gap-1 rounded-md shadow-lg bg-gray-100 max-w-nearly-full max-h-nearly-full min-h-0"
    >
        <div class="flex bg-primary text-white p-2 rounded-t-md font-bold">
            <div class="flex flex-1 justify-center">
                <slot name="header"><p>Default header</p></slot>
            </div>
            {#if hasCloseButton}
                <button class="flex-none" on:click={onClosePressed}>
                    <Fa icon={faWindowClose} size="lg" />
                </button>
            {/if}
        </div>
        <div class="flex min-h-0">
            <slot name="content"><p>Default content</p></slot>
        </div>
    </div>
</div>
