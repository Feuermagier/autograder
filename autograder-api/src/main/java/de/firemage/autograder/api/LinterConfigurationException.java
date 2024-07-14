package de.firemage.autograder.api;

public class LinterConfigurationException extends LinterException {
    public LinterConfigurationException(String message) {
        super(message);
    }

    public LinterConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public LinterConfigurationException(Throwable cause) {
        super(cause);
    }
}
