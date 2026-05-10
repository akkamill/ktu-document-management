package com.example.ktu_document_management.repository;

import com.example.ktu_document_management.entitiy.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;


/**
 * Spring Data JPA Repository for managing {@link DocumentEntity} persistence.
 * Provides standard database CRUD operations and custom JPQL queries for advanced
 * document filtering and cryptographic duplicate detection.
 * * @author Kamil Alakbarov
 * @version 1.0
 */
@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID>, JpaSpecificationExecutor<DocumentEntity>  {

  /**
   * Checks if a document with the exact same binary content already exists
   * in the database. Used to enforce strict file uniqueness.
   *
   * @param fileHash The SHA-256 cryptographic hash of the incoming file.
   * @return true if a matching hash is found in the database, false otherwise.
   */
  boolean existsByFileHash(String fileHash);


}