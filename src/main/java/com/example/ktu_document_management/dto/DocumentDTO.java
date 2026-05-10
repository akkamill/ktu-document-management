package com.example.ktu_document_management.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object representing the metadata of a document.
 * Sent back to the client to avoid exposing internal database entity structures.
 */
@Data
public class DocumentDTO {

  private UUID id;

  private String name;

  private String type;

  private String author;

  private LocalDateTime uploadDate;

}