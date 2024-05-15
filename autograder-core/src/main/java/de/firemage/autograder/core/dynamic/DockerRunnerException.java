package de.firemage.autograder.core.dynamic;

import de.firemage.autograder.core.LinterException;

public class DockerRunnerException extends LinterException {
    public DockerRunnerException(String message, String executorLog) {
        super(message + System.lineSeparator() + "=============== Executor Log ===============" + System.lineSeparator() + executorLog);
    }
}
