package de.firemage.codelinter.linter.spoon;

import de.firemage.codelinter.linter.LinterException;

public class CompilationException extends LinterException {
    public CompilationException() {
    }

    public CompilationException(String message) {
        super(message);
    }

    public CompilationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompilationException(Throwable cause) {
        super(cause);
    }
}
