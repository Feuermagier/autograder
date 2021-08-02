package de.firemage.codelinter.main.upload;

public class ClientUploadException extends UploadException {
    public ClientUploadException() {
    }

    public ClientUploadException(String message) {
        super(message);
    }

    public ClientUploadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientUploadException(Throwable cause) {
        super(cause);
    }
}
