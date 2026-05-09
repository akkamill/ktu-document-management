package com.example.ktu_document_management.controller;

import com.example.ktu_document_management.dto.DocumentDTO;
import com.example.ktu_document_management.dto.FileResponseDTO;
import com.example.ktu_document_management.entitiy.DocumentEntity;
import com.example.ktu_document_management.service.DocumentService;
import com.example.ktu_document_management.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

  private final DocumentService documentService;
  private final FileStorageService fileStorageService; // Use Interface, not Impl

  @PostMapping("/upload")
  public ResponseEntity<DocumentDTO> uploadDocument(
      @RequestParam("file") MultipartFile file,
      @RequestParam("author") String author) {

    log.info("REST request to upload document: {} by {}", file.getOriginalFilename(), author);
    DocumentDTO savedDoc = documentService.uploadDocument(file, author);
    return new ResponseEntity<>(savedDoc, HttpStatus.CREATED);
  }

  @GetMapping("/search")
  public ResponseEntity<List<DocumentDTO>> searchDocuments(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String author) {

    log.info("REST request to search documents with criteria - Name: {}, Type: {}, Author: {}", name, type, author);
    return ResponseEntity.ok(documentService.searchDocuments(name, type, author));
  }

  @GetMapping("/report")
  public ResponseEntity<byte[]> generateReport() throws IOException {
    log.info("REST request to generate Excel report of all documents");

    byte[] excelData = documentService.generateExcelReport();

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document_report.xlsx\"")
        .body(excelData);
  }

  @GetMapping("/{id}/download")
  public ResponseEntity<byte[]> downloadDocument(@PathVariable String id) {
    FileResponseDTO fileResponse = documentService.downloadDocument(id);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResponse.getFilename() + "\"")
        .body(fileResponse.getData());
  }

  @GetMapping("/export-zip")
  public ResponseEntity<byte[]> exportDocumentsAsZip(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String author) throws IOException {

    FileResponseDTO zipResponse = documentService.exportDocumentsAsZip(name, type, author);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/zip"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipResponse.getFilename() + "\"")
        .body(zipResponse.getData());
  }
}