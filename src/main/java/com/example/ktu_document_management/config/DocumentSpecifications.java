package com.example.ktu_document_management.config;

import com.example.ktu_document_management.entitiy.DocumentEntity;
import org.springframework.data.jpa.domain.Specification;

public class DocumentSpecifications {

  public static Specification<DocumentEntity> hasName(String name) {
    return (root, query, cb) -> name == null ? null :
        cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
  }

  public static Specification<DocumentEntity> hasType(String type) {
    return (root, query, cb) -> type == null ? null :
        cb.equal(cb.lower(root.get("type")), type.toLowerCase());
  }

  public static Specification<DocumentEntity> hasAuthor(String author) {
    return (root, query, cb) -> author == null ? null :
        cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%");
  }
}