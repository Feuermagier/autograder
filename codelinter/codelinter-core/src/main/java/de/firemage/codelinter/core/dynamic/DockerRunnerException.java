package de.firemage.codelinter.core.dynamic;

import de.firemage.codelinter.core.LinterException;

public class DockerRunnerException extends LinterException {
    public DockerRunnerException(String message, String executorLog) {
        super(message + System.lineSeparator() + "=============== Executor Log ===============" + System.lineSeparator() + executorLog);
    }
}
