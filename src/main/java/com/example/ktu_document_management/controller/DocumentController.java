package com.example.ktu_document_management.controller;

import com.example.ktu_document_management.dto.DocumentDTO;
import com.example.ktu_document_management.dto.FileResponseDTO;
import com.example.ktu_document_management.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REST Controller managing HTTP endpoints for Document operations.
 * Acts as the primary entry point for the frontend or external microservices to
 * upload, search, download, and export documents.
 * * @author Kamil Alakbarov
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

  private final DocumentService documentService;

  /**
   * Endpoint to securely upload a new document to the system.
   *
   * @param file   The binary file payload (Max 50MB). Supported types: PDF, DOCX, XLSX.
   * @param author The name of the user uploading the file.
   * @return ResponseEntity containing the generated {@link DocumentDTO} and a 201 Created status.
   */
  @PostMapping("/upload")
  public ResponseEntity<DocumentDTO> uploadDocument(
      @RequestParam("file") MultipartFile file,
      @RequestParam("author") String author) {

    log.info("REST request to upload document: {} by {}", file.getOriginalFilename(), author);
    DocumentDTO savedDoc = documentService.uploadDocument(file, author);
    return new ResponseEntity<>(savedDoc, HttpStatus.CREATED);
  }

  /**
   * Endpoint to search for existing documents based on optional metadata filters.
   * If no parameters are provided, it returns all documents in the system.
   *
   * @param name   (Optional) Partial or full match of the filename.
   * @param type   (Optional) Exact match of the file extension (e.g., "pdf").
   * @param author (Optional) Partial or full match of the author's name.
   * @return ResponseEntity containing a list of {@link DocumentDTO} matching the criteria.
   */
  @GetMapping("/search")
  public ResponseEntity<List<DocumentDTO>> searchDocuments(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String author) {

    log.info("REST request to search documents with criteria - Name: {}, Type: {}, Author: {}", name, type, author);
    return ResponseEntity.ok(documentService.searchDocuments(name, type, author));
  }

  /**
   * Endpoint to generate an Excel (.xlsx) inventory report of all stored documents.
   *
   * @return ResponseEntity containing the binary Excel file stream.
   * @throws IOException if the server fails to generate the workbook stream.
   */
  @GetMapping("/report")
  public ResponseEntity<byte[]> generateReport() throws IOException {
    log.info("REST request to generate Excel report of all documents");

    byte[] excelData = documentService.generateExcelReport();

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document_report.xlsx\"")
        .body(excelData);
  }

  /**
   * Endpoint to download a specific document by its unique UUID.
   *
   * @param id The UUID of the requested document.
   * @return ResponseEntity containing the binary file stream and dynamic filename headers.
   */
  @GetMapping("/{id}/download")
  public ResponseEntity<byte[]> downloadDocument(@PathVariable String id) {
    log.info("REST request to download document ID: {}", id);

    FileResponseDTO fileResponse = documentService.downloadDocument(id);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResponse.getFilename() + "\"")
        .body(fileResponse.getData());
  }

  /**
   * Endpoint to bulk-export documents that match specific search criteria into a single ZIP archive.
   *
   * @param name   (Optional) Partial match for the filename.
   * @param type   (Optional) Exact match for the file extension.
   * @param author (Optional) Partial match for the author.
   * @return ResponseEntity containing the compressed ZIP binary stream.
   * @throws IOException if the server fails to compress the requested files.
   */
  @GetMapping("/export-zip")
  public ResponseEntity<byte[]> exportDocumentsAsZip(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String author) throws IOException {

    log.info("REST request to export documents to ZIP with criteria - Name: {}, Type: {}, Author: {}", name, type, author);
    FileResponseDTO zipResponse = documentService.exportDocumentsAsZip(name, type, author);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/zip"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipResponse.getFilename() + "\"")
        .body(zipResponse.getData());
  }
}