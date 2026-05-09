package com.example.ktu_document_management.entitiy;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Data
public class DocumentEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;

  private String name;
  private String type;
  private String author;

  @Column(name = "storage_path")
  private String storagePath;

  @Column(name = "upload_date")
  private LocalDateTime uploadDate;

  @Column(name = "file_hash", unique = true)
  private String fileHash;
}