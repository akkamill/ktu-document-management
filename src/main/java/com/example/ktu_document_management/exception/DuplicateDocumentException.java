package com.example.ktu_document_management.exception;

/**
 * Custom exception thrown when a user attempts to upload a file that already exists
 * in the system, detected via cryptographic SHA-256 hashing.
 * Results in a 409 Conflict HTTP response.
 * * @author Kamil Alakbarov
 * @version 1.0
 */
public class DuplicateDocumentException extends RuntimeException {
  public DuplicateDocumentException(String message) {
    super(message);
  }
}