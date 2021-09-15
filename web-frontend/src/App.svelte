<script lang="ts">
	import CompilationErrorView from "./CompilationErrorView.svelte";

	import { linter } from "./linter";
	import ProblemView from "./ProblemView.svelte";
	import {
		COMPILATION_ERROR_RESULT,
		SUCCESSFUL_RESULT,
	} from "./types";

	import Upload from "./Upload.svelte";

	let result;
	$: if ($linter.result != null) {
		result = $linter.result;
	} else {
		result = null;
	}
</script>

<div
	class="w-screen h-screen max-w-screen max-h-screen flex flex-col bg-gray-100"
>
	<header>
		<div class="bg-primary text-gray-100 shadow-lg mb-1">
			<p class="px-4 py-4 font-medium text-xl">Simple Code Linter</p>
		</div>
	</header>
	<main class="flex-grow min-h-0">
		<div class="w-full h-full flex">
			{#if result != null}
				{#if result.type == COMPILATION_ERROR_RESULT}
					<CompilationErrorView {result} on:close={linter.clear} />
				{:else if result.type == SUCCESSFUL_RESULT}
					<ProblemView {result} on:close={linter.clear} />
				{/if}
			{:else}
				<Upload />
			{/if}
		</div>
	</main>
</div>

<style global>
	@tailwind base;
	@tailwind components;
	@tailwind utilities;

	/* https://github.com/Cosbgn/tailwindcss-tooltips */
	.tooltip {
		@apply invisible absolute;
	}

	.has-tooltip:hover .tooltip {
		@apply visible z-50;
	}
</style>
