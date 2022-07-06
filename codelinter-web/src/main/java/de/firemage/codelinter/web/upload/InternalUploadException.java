package de.firemage.codelinter.web.upload;

public class InternalUploadException extends UploadException {
    public InternalUploadException() {
    }

    public InternalUploadException(String message) {
        super(message);
    }

    public InternalUploadException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalUploadException(Throwable cause) {
        super(cause);
    }
}
