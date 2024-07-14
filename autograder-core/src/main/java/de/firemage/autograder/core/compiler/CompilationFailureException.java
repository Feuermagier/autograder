package de.firemage.autograder.core.compiler;

import de.firemage.autograder.api.LinterException;

import java.util.List;

public class CompilationFailureException extends LinterException {
    private final List<CompilationDiagnostic> diagnostics;

    CompilationFailureException(List<CompilationDiagnostic> diagnostics) {
        super(CompilationDiagnostic.formatMultiple(diagnostics));
        this.diagnostics = diagnostics;
    }

    public List<CompilationDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}
