<script lang="ts">
    import type { CompilationDiagnostic } from "./types";

    export let diagnostics: CompilationDiagnostic[];
</script>

<table class="min-w-full divide-y">
    <thead class="bg-gray-50">
        <tr>
            <th
                scope="col"
                class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
            >
                Position
            </th>
            <th
                scope="col"
                class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
            >
                Message
            </th>
        </tr>
    </thead>
    <tbody class="bg-white divide-y divide-gray-200">
        {#each diagnostics as diagnostic}
            <tr>
                <td class="px-6 py-4">
                    {diagnostic.className +
                        (diagnostic.line != -1 ? ":" + diagnostic.line : "")}
                </td>
                <td class="px-6 py-4">
                    {diagnostic.message}
                </td>
            </tr>
        {/each}
    </tbody>
</table>
