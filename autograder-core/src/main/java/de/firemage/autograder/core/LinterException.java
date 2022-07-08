package de.firemage.autograder.core;

public abstract class LinterException extends Exception {
    public LinterException() {
        super();
    }

    public LinterException(String message) {
        super(message);
    }

    public LinterException(String message, Throwable cause) {
        super(message, cause);
    }

    public LinterException(Throwable cause) {
        super(cause);
    }
}
