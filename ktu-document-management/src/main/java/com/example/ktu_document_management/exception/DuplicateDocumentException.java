package com.example.ktu_document_management.exception;

public class DuplicateDocumentException extends RuntimeException {

  public DuplicateDocumentException(String message) {
    super(message);
  }

}