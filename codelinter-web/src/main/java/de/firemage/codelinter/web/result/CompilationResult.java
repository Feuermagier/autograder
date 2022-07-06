package de.firemage.codelinter.web.result;

import de.firemage.codelinter.core.compiler.CompilationDiagnostic;
import de.firemage.codelinter.web.result.transfer.TransferCompilationDiagnostic;
import java.util.List;
import java.util.stream.Collectors;

public record CompilationResult(List<TransferCompilationDiagnostic> diagnostics) {
    public static CompilationResult fromDiagnostics(List<CompilationDiagnostic> diagnostics) {
        return new CompilationResult(diagnostics.stream()
                .map(TransferCompilationDiagnostic::new)
                .collect(Collectors.toList()));
    }
}
