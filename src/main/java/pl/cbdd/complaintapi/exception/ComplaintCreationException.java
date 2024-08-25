package pl.cbdd.complaintapi.exception;

public class ComplaintCreationException extends RuntimeException {
    public ComplaintCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}