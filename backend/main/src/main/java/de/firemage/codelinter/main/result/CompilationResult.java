package de.firemage.codelinter.main.result;

import de.firemage.codelinter.linter.compiler.CompilationDiagnostic;
import de.firemage.codelinter.main.result.transfer.TransferCompilationDiagnostic;
import java.util.List;
import java.util.stream.Collectors;

public record CompilationResult(List<TransferCompilationDiagnostic> diagnostics) {
    public static CompilationResult fromDiagnostics(List<CompilationDiagnostic> diagnostics) {
        return new CompilationResult(diagnostics.stream()
                .map(TransferCompilationDiagnostic::new)
                .collect(Collectors.toList()));
    }
}
