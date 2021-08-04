package de.firemage.codelinter.linter.compiler;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public record CompilationDiagnostic(String className, int line, int column, String message) {
    protected CompilationDiagnostic(Diagnostic<? extends JavaFileObject> diagnostic, String cutoffSuffix) {
        this(sanitizeClassName(diagnostic.getSource().getName(), cutoffSuffix),
                (int) diagnostic.getLineNumber(),
                (int) diagnostic.getColumnNumber(),
                diagnostic.getMessage(Compiler.COMPILER_LOCALE));
    }

    public static String formatMultiple(List<CompilationDiagnostic> diagnostics) {
        return diagnostics.stream()
                .map(CompilationDiagnostic::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static String sanitizeClassName(String className, String cutoffSuffix) {
        if (cutoffSuffix == null) {
            return className;
        } else {
            return className.substring(className.indexOf(cutoffSuffix) + cutoffSuffix.length());
        }
    }

    @Override
    public String toString() {
        String message = this.className;
        if (this.line != Diagnostic.NOPOS) {
            message += ":" + this.line;
        }
        return message + " " + this.message;
    }
}
