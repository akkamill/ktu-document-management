package com.example.ktu_document_management.exception;

import com.example.ktu_document_management.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DocumentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleDocumentNotFound(DocumentNotFoundException ex) {
    log.warn("Document not found: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
  }

  @ExceptionHandler(DuplicateDocumentException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateDocument(DuplicateDocumentException ex) {
    log.warn("Duplicate upload attempt: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Invalid request payload: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> handleMaxSizeException(MaxUploadSizeExceededException ex) {
    log.error("Upload size exceeded: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, "Payload Too Large", "File size exceeds the 50MB limit.");
  }

  /**
   * Handle Database / SQL errors specifically.
   */
  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<ErrorResponse> handleDatabaseException(DataAccessException ex) {
    log.error("Database error occurred: {}", ex.getMessage());
    return buildErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Database Error",
        "There was an error processing your request in the database. Please check your query parameters."
    );
  }

  /**
   * Final fallback for anything else.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    log.error("Unhandled exception caught: ", ex);
    return buildErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error",
        "An unexpected error occurred: " + ex.getClass().getSimpleName()
    );
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message) {
    return new ResponseEntity<>(
        ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(error)
            .message(message)
            .build(),
        status
    );
  }
}