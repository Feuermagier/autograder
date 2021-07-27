<script lang="ts">
	import { linter } from "./linter";
	import ProblemView from "./ProblemView.svelte";
	import { SuccesfulResult, SUCCESSFUL_RESULT } from "./types";

	import Upload from "./Upload.svelte";

	let result;
	$: if ($linter.result != null && $linter.result.type == SUCCESSFUL_RESULT) {
		result = $linter.result as SuccesfulResult;
	} else {
		result = null;
	}
</script>

<div class="w-screen h-screen max-w-screen max-h-screen flex flex-col">
	<header>
		<div class="bg-primary text-gray-100 shadow-xl z-10">
			<p class="px-4 py-4 font-medium text-xl">Simple Code Linter</p>
		</div>
	</header>
	<main class="flex-grow min-h-0">
		<div class="bg-gray-100 w-full h-full flex">
			{#if result != null}
				<ProblemView {result} />
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
