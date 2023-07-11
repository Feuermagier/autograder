package de.firemage.autograder.core.compiler;

import de.firemage.autograder.core.LinterException;
import lombok.Getter;

import java.util.List;

public class CompilationFailureException extends LinterException {
    @Getter
    private final List<CompilationDiagnostic> diagnostics;

    CompilationFailureException(List<CompilationDiagnostic> diagnostics) {
        super(CompilationDiagnostic.formatMultiple(diagnostics));
        this.diagnostics = diagnostics;
    }
}
