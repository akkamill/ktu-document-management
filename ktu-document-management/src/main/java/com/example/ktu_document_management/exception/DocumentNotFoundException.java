package com.example.ktu_document_management.exception;

public class DocumentNotFoundException extends RuntimeException {

  public DocumentNotFoundException(String message) {
    super(message);
  }

}