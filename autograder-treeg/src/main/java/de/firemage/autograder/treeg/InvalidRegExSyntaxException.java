package de.firemage.autograder.treeg;

public class InvalidRegExSyntaxException extends Exception {
    public InvalidRegExSyntaxException() {
    }

    public InvalidRegExSyntaxException(String message) {
        super(message);
    }

    public InvalidRegExSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidRegExSyntaxException(Throwable cause) {
        super(cause);
    }
}
