package com.example.ktu_document_management.repository;

import com.example.ktu_document_management.entitiy.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

  // Custom search query to filter by name, type, and author
  @Query("SELECT d FROM DocumentEntity d WHERE " +
      "(:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
      "(:type IS NULL OR LOWER(d.type) = LOWER(:type)) AND " +
      "(:author IS NULL OR LOWER(d.author) LIKE LOWER(CONCAT('%', :author, '%')))")
  List<DocumentEntity> searchDocuments(@Param("name") String name,
      @Param("type") String type,
      @Param("author") String author);
}