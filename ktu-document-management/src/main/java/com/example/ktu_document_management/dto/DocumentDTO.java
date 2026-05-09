package com.example.ktu_document_management.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DocumentDTO {
  private UUID id;
  private String name;
  private String type;
  private String author;
  private LocalDateTime uploadDate;
}