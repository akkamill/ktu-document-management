package com.example.ktu_document_management.exception;

/**
 * Custom exception thrown when a requested document cannot be found in the database
 * or the physical file system. Results in a 404 Not Found HTTP response.
 * * @author Kamil Alakbarov
 * @version 1.0
 */
public class DocumentNotFoundException extends RuntimeException {
  public DocumentNotFoundException(String message) {
    super(message);
  }
}