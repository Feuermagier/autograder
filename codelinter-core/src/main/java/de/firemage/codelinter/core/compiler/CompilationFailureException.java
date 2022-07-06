package de.firemage.codelinter.core.compiler;

import de.firemage.codelinter.core.LinterException;
import lombok.Getter;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class CompilationFailureException extends LinterException {
    @Getter
    private final List<CompilationDiagnostic> diagnostics;

    public CompilationFailureException(List<CompilationDiagnostic> diagnostics, Path root) {
        super(CompilationDiagnostic.formatMultiple(diagnostics));
        this.diagnostics = diagnostics;
    }
}
