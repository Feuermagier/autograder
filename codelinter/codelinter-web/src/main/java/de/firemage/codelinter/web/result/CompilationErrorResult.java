package de.firemage.codelinter.web.result;

import de.firemage.codelinter.web.result.transfer.TransferCompilationDiagnostic;

import java.util.Collections;
import java.util.List;

public record CompilationErrorResult(String description,
                                     List<TransferCompilationDiagnostic> diagnostics) implements LintingResult {
    public CompilationErrorResult(String description) {
        this(description, Collections.emptyList());
    }
}
