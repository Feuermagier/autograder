package de.firemage.autograder.core.dynamic;

import de.firemage.autograder.core.LinterException;

public class RunnerException extends LinterException {
    public RunnerException() {
    }

    public RunnerException(String message) {
        super(message);
    }

    public RunnerException(String message, Throwable cause) {
        super(message, cause);
    }

    public RunnerException(Throwable cause) {
        super(cause);
    }
}
