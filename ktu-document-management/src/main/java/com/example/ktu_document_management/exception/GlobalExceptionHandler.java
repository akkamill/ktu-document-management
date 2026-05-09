package com.example.ktu_document_management.exception;

import com.example.ktu_document_management.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
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

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    log.error("An unexpected internal error occurred", ex);
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred. Please try again later.");
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message) {
    ErrorResponse response = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(status.value())
        .error(error)
        .message(message)
        .build();
    return new ResponseEntity<>(response, status);
  }
}