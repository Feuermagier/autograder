package de.firemage.codelinter.linter.file;

import de.firemage.codelinter.linter.LinterException;

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
