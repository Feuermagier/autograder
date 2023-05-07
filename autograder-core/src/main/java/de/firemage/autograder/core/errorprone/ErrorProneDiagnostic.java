package de.firemage.autograder.core.errorprone;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.PathUtil;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Locale;

public record ErrorProneDiagnostic(String path, int line, int column, String message, ErrorProneLint lint) implements Serializable {
    public static ErrorProneDiagnostic from(Diagnostic<? extends JavaFileObject> diagnostic, Path root) {
        if (!diagnostic.getCode().equals("compiler.warn.error.prone")) {
            throw new IllegalArgumentException(
                "diagnostic is not emitted by error-prone, code '%s'".formatted(diagnostic.getCode())
            );
        }
        String message = diagnostic.getMessage(Locale.ENGLISH);
        // the message looks like this "[lint name] actual message"
        String[] parts = message.split("]", 2);

        if (parts.length != 2) {
            throw new IllegalArgumentException("diagnostic message is not in the expected format");
        }

        message = parts[1].trim();
        ErrorProneLint lint = ErrorProneLint.fromString(parts[0].substring(1));

        return new ErrorProneDiagnostic(
            PathUtil.getSanitizedPath(diagnostic.getSource().toUri(), root),
            (int) diagnostic.getLineNumber(),
            (int) diagnostic.getColumnNumber(),
            message,
            lint
        );
    }

    public CodePosition position() {
        return new CodePosition(Path.of(this.path), this.line, this.line, this.column, this.column);
    }
}
