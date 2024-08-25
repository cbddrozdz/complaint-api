package pl.cbdd.complaintapi.exception;

public class ComplaintAlreadyExistsException extends RuntimeException {
    public ComplaintAlreadyExistsException(String message) {
        super(message);
    }
}