package de.firemage.autograder.core.compiler;

import de.firemage.autograder.core.CodePositionImpl;
import de.firemage.autograder.core.file.SourceInfo;
import de.firemage.autograder.core.file.SourcePath;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.List;
import java.util.stream.Collectors;

public record CompilationDiagnostic(SourceInfo sourceInfo, SourcePath path, int line, int column, String message,
                                    String code) {
    CompilationDiagnostic(Diagnostic<? extends JavaFileObject> diagnostic, SourceInfo sourceInfo) {
        this(sourceInfo,
            sourceInfo.getCompilationUnit(diagnostic.getSource().toUri()).path(),
            (int) diagnostic.getLineNumber(),
            (int) diagnostic.getColumnNumber(),
            diagnostic.getMessage(Compiler.COMPILER_LOCALE),
            diagnostic.getCode());
    }

    public static String formatMultiple(List<CompilationDiagnostic> diagnostics) {
        return diagnostics.stream().map(CompilationDiagnostic::toString).collect(Collectors.joining(System.lineSeparator()));
    }

    public CodePositionImpl codePosition() {
        return new CodePositionImpl(this.sourceInfo, this.path, this.line, this.line, this.column, this.column);
    }

    @Override
    public String toString() {
        String message = this.path.toString();
        if (this.line != Diagnostic.NOPOS) {
            message += ":" + this.line;
        }
        return message + " " + this.message;
    }
}
