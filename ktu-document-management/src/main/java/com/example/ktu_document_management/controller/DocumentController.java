package com.example.ktu_document_management.controller;

import com.example.ktu_document_management.dto.DocumentDTO;
import com.example.ktu_document_management.entitiy.DocumentEntity;
import com.example.ktu_document_management.service.DocumentService;
import com.example.ktu_document_management.service.impl.FileStorageServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

  private final DocumentService documentService;
  private final FileStorageServiceImpl fileStorageService;

  @PostMapping("/upload")
  public ResponseEntity<DocumentDTO> uploadDocument(
      @RequestParam("file") MultipartFile file,
      @RequestParam("author") String author) {

    DocumentDTO savedDoc = documentService.uploadDocument(file, author);
    return ResponseEntity.ok(savedDoc);
  }

  @GetMapping("/search")
  public ResponseEntity<List<DocumentDTO>> searchDocuments(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String author) {

    return ResponseEntity.ok(documentService.searchDocuments(name, type, author));
  }

  @GetMapping("/{id}/download")
  public ResponseEntity<Resource> downloadDocument(@PathVariable String id) {
    DocumentEntity doc = documentService.getDocumentById(id);
    Resource resource = fileStorageService.loadFileAsResource(doc.getStoragePath());

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getName() + "\"")
        .body(resource);
  }

  @GetMapping("/report")
  public ResponseEntity<byte[]> generateReport() throws IOException {
    byte[] excelData = documentService.generateExcelReport();

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document_report.xlsx\"")
        .body(excelData);
  }
}