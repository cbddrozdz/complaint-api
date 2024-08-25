package pl.cbdd.complaintapi.exception;

public class ComplaintUpdateException extends RuntimeException {
    public ComplaintUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}