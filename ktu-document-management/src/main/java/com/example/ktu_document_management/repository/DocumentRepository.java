package com.example.ktu_document_management.repository;

import com.example.ktu_document_management.entitiy.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for managing {@link DocumentEntity} persistence.
 * Provides standard database CRUD operations and custom JPQL queries for advanced
 * document filtering and cryptographic duplicate detection.
 * * @author Kamil Alakbarov
 * @version 1.0
 */
@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

  /**
   * Checks if a document with the exact same binary content already exists
   * in the database. Used to enforce strict file uniqueness.
   *
   * @param fileHash The SHA-256 cryptographic hash of the incoming file.
   * @return true if a matching hash is found in the database, false otherwise.
   */
  boolean existsByFileHash(String fileHash);

  /**
   * Custom JPQL query to dynamically search for documents based on optional metadata filters.
   * Utilizes standard SQL wildcards for partial text matching. If a parameter is null,
   * that specific filter is safely ignored by the database engine.
   *
   * @param name   (Optional) Partial match for the document filename.
   * @param type   (Optional) Exact match for the document extension.
   * @param author (Optional) Partial match for the uploader's name.
   * @return A list of {@link DocumentEntity} records matching the provided criteria.
   */
  @Query("SELECT d FROM DocumentEntity d WHERE " +
      "(:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
      "(:type IS NULL OR LOWER(d.type) = LOWER(:type)) AND " +
      "(:author IS NULL OR LOWER(d.author) LIKE LOWER(CONCAT('%', :author, '%')))")
  List<DocumentEntity> searchDocuments(@Param("name") String name,
      @Param("type") String type,
      @Param("author") String author);
}