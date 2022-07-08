package de.firemage.autograder.core.file;

import de.firemage.autograder.core.LinterException;

public class ZipFormatException extends LinterException {
    public ZipFormatException() {
    }

    public ZipFormatException(String message) {
        super(message);
    }

    public ZipFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZipFormatException(Throwable cause) {
        super(cause);
    }
}
