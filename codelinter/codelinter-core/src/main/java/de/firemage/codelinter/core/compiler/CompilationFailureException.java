package de.firemage.codelinter.core.compiler;

import de.firemage.codelinter.core.LinterException;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class CompilationFailureException extends LinterException {
    public CompilationFailureException(List<Diagnostic<? extends JavaFileObject>> diagnostics, File root) {
        super(CompilationDiagnostic.formatMultiple(diagnostics.stream()
                .map(d -> new CompilationDiagnostic(d, root))
                .collect(Collectors.toList())));
    }
}
