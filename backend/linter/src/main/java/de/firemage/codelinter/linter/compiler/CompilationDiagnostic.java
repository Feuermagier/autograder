package de.firemage.codelinter.linter.compiler;

import de.firemage.codelinter.linter.PathUtil;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public record CompilationDiagnostic(String path, int line, int column, String message) {
    protected CompilationDiagnostic(Diagnostic<? extends JavaFileObject> diagnostic, File root) {
        this(PathUtil.getSanitizedPath(diagnostic.getSource().toUri(), root),
                (int) diagnostic.getLineNumber(),
                (int) diagnostic.getColumnNumber(),
                diagnostic.getMessage(Compiler.COMPILER_LOCALE));
    }

    public static String formatMultiple(List<CompilationDiagnostic> diagnostics) {
        return diagnostics.stream()
                .map(CompilationDiagnostic::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    @Override
    public String toString() {
        String message = this.path;
        if (this.line != Diagnostic.NOPOS) {
            message += ":" + this.line;
        }
        return message + " " + this.message;
    }
}
