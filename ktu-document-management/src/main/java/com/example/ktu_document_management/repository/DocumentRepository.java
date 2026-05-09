package com.example.ktu_document_management.repository;

import com.example.ktu_document_management.entitiy.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

  @Query("SELECT d FROM DocumentEntity d WHERE " +
      "(:name IS NULL OR LOWER(CAST(d.name AS string)) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) AND " +
      "(:type IS NULL OR LOWER(CAST(d.type AS string)) = LOWER(CAST(:type AS string))) AND " +
      "(:author IS NULL OR LOWER(CAST(d.author AS string)) LIKE LOWER(CONCAT('%', CAST(:author AS string), '%')))")
  List<DocumentEntity> searchDocuments(@Param("name") String name,
                                       @Param("type") String type,
                                       @Param("author") String author);

  boolean existsByFileHash(String fileHash);
}