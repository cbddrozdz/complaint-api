package pl.cbdd.complaintapi.exceptionhandling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.cbdd.complaintapi.exception.*;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ComplaintNotFoundException.class)
    public ResponseEntity<ExceptionResponseDTO> handleComplaintNotFoundException(ComplaintNotFoundException e) {
        ExceptionResponseDTO response = new ExceptionResponseDTO(
                List.of(e.getMessage()),
                "NOT_FOUND",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ComplaintAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponseDTO> handleComplaintAlreadyExistsException(ComplaintAlreadyExistsException e) {
        ExceptionResponseDTO response = new ExceptionResponseDTO(
                List.of(e.getMessage()),
                "CONFLICT",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ComplaintCreationException.class)
    public ResponseEntity<ExceptionResponseDTO> handleComplaintCreationException(ComplaintCreationException e) {
        ExceptionResponseDTO response = new ExceptionResponseDTO(
                List.of(e.getMessage()),
                "BAD_REQUEST",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ComplaintUpdateException.class)
    public ResponseEntity<ExceptionResponseDTO> handleComplaintUpdateException(ComplaintUpdateException e) {
        ExceptionResponseDTO response = new ExceptionResponseDTO(
                List.of(e.getMessage()),
                "BAD_REQUEST",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDTO> handleGeneralException(Exception e) {
        ExceptionResponseDTO response = new ExceptionResponseDTO(
                List.of("An unexpected error occurred: " + e.getMessage()),
                "INTERNAL_SERVER_ERROR",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}