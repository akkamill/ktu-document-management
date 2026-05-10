package com.example.ktu_document_management.entitiy;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing a Document record in the PostgreSQL database.
 * Maps directly to the "documents" table and stores all associated metadata,
 * physical storage paths, and cryptographic hashes used for duplicate detection.
 * * @author Kamil Alakbarov
 * @version 1.0
 */
@Entity
@Table(name = "documents")
@Data
public class DocumentEntity {

  /** The primary key, auto-generated as a secure UUID. */
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;

  /** The original filename of the uploaded document. */
  private String name;

  /** The file extension (e.g., pdf, docx). */
  private String type;

  /** The name of the user who uploaded the document. */
  private String author;

  /** The generated UUID filename used to store the physical file on the local disk. */
  @Column(name = "storage_path")
  private String storagePath;

  /** The timestamp recording when the document was successfully uploaded. */
  @Column(name = "upload_date")
  private LocalDateTime uploadDate;

  /** * The SHA-256 cryptographic hash of the file's binary content.
   * Enforced as unique at the database level to prevent duplicate file uploads.
   */
  @Column(name = "file_hash", unique = true)
  private String fileHash;
}