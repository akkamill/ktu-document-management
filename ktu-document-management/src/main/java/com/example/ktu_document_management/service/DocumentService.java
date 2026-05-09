package com.example.ktu_document_management.service;

import com.example.ktu_document_management.dto.DocumentDTO;
import com.example.ktu_document_management.entitiy.DocumentEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Repository
public interface DocumentService {

  DocumentDTO uploadDocument(MultipartFile file, String author);

  List<DocumentDTO> searchDocuments(String name, String type, String author);

  DocumentEntity getDocumentById(String id);

  byte[] generateExcelReport() throws IOException;

  byte[] exportDocumentsAsZip(String name, String type, String author) throws IOException;
}