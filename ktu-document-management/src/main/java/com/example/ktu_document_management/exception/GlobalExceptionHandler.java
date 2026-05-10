package com.example.ktu_document_management.exception;

import com.example.ktu_document_management.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

/**
 * Global Exception Handler for the Document Management Service.
 * Utilizes Aspect-Oriented Programming (AOP) via {@link RestControllerAdvice} to intercept
 * exceptions thrown across all controllers and services.
 * <p>
 * This ensures that the client always receives a standardized JSON {@link ErrorResponse}
 * instead of a raw stack trace, preventing sensitive system details from leaking.
 * * @author Kamil Alakbarov
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles exceptions when a requested document UUID does not exist in the database.
   *
   * @param ex The intercepted DocumentNotFoundException.
   * @return A 404 NOT FOUND standard JSON error response.
   */
  @ExceptionHandler(DocumentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleDocumentNotFound(DocumentNotFoundException ex) {
    log.warn("Document not found: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
  }

  /**
   * Handles exceptions when a user attempts to upload a file that already exists
   * in the system (detected via SHA-256 cryptographic hashing).
   *
   * @param ex The intercepted DuplicateDocumentException.
   * @return A 409 CONFLICT standard JSON error response.
   */
  @ExceptionHandler(DuplicateDocumentException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateDocument(DuplicateDocumentException ex) {
    log.warn("Duplicate upload attempt: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
  }

  /**
   * Handles exceptions related to invalid input parameters, such as unsupported file extensions.
   *
   * @param ex The intercepted IllegalArgumentException.
   * @return A 400 BAD REQUEST standard JSON error response.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Invalid request payload: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
  }

  /**
   * Intercepts Spring's internal file size limitation exception.
   * Prevents the server from crashing if a user uploads a file exceeding the 50MB limit.
   *
   * @param ex The intercepted MaxUploadSizeExceededException.
   * @return A 413 PAYLOAD TOO LARGE standard JSON error response.
   */
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> handleMaxSizeException(MaxUploadSizeExceededException ex) {
    log.error("Upload size exceeded: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, "Payload Too Large", "File size exceeds the 50MB limit.");
  }

  /**
   * Handles cases where Spring attempts to route to a static resource (like Swagger UI)
   * or endpoint that does not exist.
   *
   * @param ex The intercepted NoResourceFoundException.
   * @return A 404 NOT FOUND standard JSON error response.
   */
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
    log.warn("Resource not found: {}", ex.getMessage());
    return buildErrorResponse(
        HttpStatus.NOT_FOUND,
        "Resource Not Found",
        "The requested file or endpoint does not exist."
    );
  }

  /**
   * Catches all unhandled SQL and JPA database query failures (e.g., syntax errors, connection drops).
   * Masks the exact SQL error from the client to prevent SQL-injection mapping.
   *
   * @param ex The intercepted DataAccessException.
   * @return A 500 INTERNAL SERVER ERROR standard JSON error response.
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
   * The final safety net. Catches any unexpected RuntimeExceptions that slip past
   * the specific handlers above.
   *
   * @param ex The intercepted generic Exception.
   * @return A 500 INTERNAL SERVER ERROR standard JSON error response.
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

  /**
   * Helper method to construct the standardized ErrorResponse DTO.
   */
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