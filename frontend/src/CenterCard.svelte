<div class="grid flex-grow place-items-center">
    <div class="flex flex-col gap-1 rounded-md shadow-lg bg-white max-w-nearly-full max-h-nearly-full min-h-0">
        <div
            class="flex bg-primary text-white p-2 rounded-t-md justify-center font-bold"
        >
            <slot name="header"><p>Default header</p></slot>
        </div>
        <div class="p-2 flex min-h-0">
            <slot name="content"><p>Default content</p></slot>
        </div>
    </div>
</div>
