package com.example.ktu_document_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a standardized HTTP error response.
 * Sent to the client by the GlobalExceptionHandler to provide consistent and safe
 * error structures without exposing internal server stack traces.
 * * @author Kamil Alakbarov
 * @version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

  /** The exact date and time when the error occurred. */
  private LocalDateTime timestamp;

  /** The HTTP status code (e.g., 400, 404, 500). */
  private int status;

  /** The brief HTTP error type (e.g., "Bad Request", "Internal Server Error"). */
  private String error;

  /** A descriptive, user-friendly message explaining the cause of the error. */
  private String message;
}