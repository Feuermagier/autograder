package de.firemage.autograder.core.compiler;

import de.firemage.autograder.core.LinterException;
import lombok.Getter;

import java.nio.file.Path;
import java.util.List;

public class CompilationFailureException extends LinterException {
    @Getter
    private final List<CompilationDiagnostic> diagnostics;

    public CompilationFailureException(List<CompilationDiagnostic> diagnostics, Path root) {
        super(CompilationDiagnostic.formatMultiple(diagnostics));
        this.diagnostics = diagnostics;
    }
}
