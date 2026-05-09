package com.example.ktu_document_management.service.impl;

import com.example.ktu_document_management.dto.DocumentDTO;
import com.example.ktu_document_management.entitiy.DocumentEntity;
import com.example.ktu_document_management.exception.DocumentNotFoundException;
import com.example.ktu_document_management.exception.DuplicateDocumentException;
import com.example.ktu_document_management.repository.DocumentRepository;
import com.example.ktu_document_management.service.DocumentService;
import com.example.ktu_document_management.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

  private final DocumentRepository documentRepository;
  private final FileStorageService fileStorageService;

  private final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "docx", "xlsx");

  @Override
  public DocumentDTO uploadDocument(MultipartFile file, String author) {
    try {
      log.info("Initiating document upload for file: '{}' by author: '{}'", file.getOriginalFilename(), author);

      // 1. Cryptographic Duplicate Detection (SHA-256)
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(file.getBytes());
      // Using Java 17+ HexFormat (Better than older DatatypeConverter)
      String fileHash = HexFormat.of().formatHex(hashBytes).toUpperCase();

      if (documentRepository.existsByFileHash(fileHash)) {
        log.warn("Duplicate upload blocked. A document with hash {} already exists.", fileHash);
        throw new DuplicateDocumentException("A document with this exact content already exists in the system.");
      }

      // 2. File Type Validation
      String originalName = file.getOriginalFilename();
      String extension = originalName != null ? originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase() : "";

      if (!ALLOWED_EXTENSIONS.contains(extension)) {
        log.warn("Upload rejected. Unsupported file extension: '{}'", extension);
        throw new IllegalArgumentException("Invalid file type. Only PDF, DOCX, and XLSX are allowed.");
      }

      // 3. Storage and Database Save
      log.debug("Validation passed. Storing file binary...");
      String storedFileName = fileStorageService.storeFile(file);

      DocumentEntity doc = new DocumentEntity();
      doc.setName(originalName);
      doc.setType(extension);
      doc.setAuthor(author);
      doc.setStoragePath(storedFileName);
      doc.setUploadDate(LocalDateTime.now());
      doc.setFileHash(fileHash);

      DocumentEntity savedDoc = documentRepository.save(doc);
      log.info("Document uploaded successfully. Assigned ID: {}", savedDoc.getId());

      return mapToDTO(savedDoc);

    } catch (DuplicateDocumentException | IllegalArgumentException e) {
      // Rethrow business exceptions gracefully so the GlobalExceptionHandler catches them
      throw e;
    } catch (Exception e) {
      log.error("An unexpected internal error occurred while uploading document: {}", file.getOriginalFilename(), e);
      throw new RuntimeException("Failed to process document upload", e);
    }
  }

  @Override
  public List<DocumentDTO> searchDocuments(String name, String type, String author) {
    log.info("Searching documents with filters -> name: '{}', type: '{}', author: '{}'", name, type, author);

    List<DocumentEntity> results = documentRepository.searchDocuments(name, type, author);

    log.debug("Search query returned {} results.", results.size());
    return results.stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  @Override
  public DocumentEntity getDocumentById(String id) {
    log.debug("Attempting to fetch document with ID: {}", id);

    return documentRepository.findById(UUID.fromString(id))
        .orElseThrow(() -> {
          log.warn("Fetch failed: Document with ID '{}' not found in database.", id);
          return new DocumentNotFoundException("Document with ID " + id + " was not found.");
        });
  }

  @Override
  public byte[] generateExcelReport() throws IOException {
    log.info("Initiating Excel report generation for all documents.");
    List<DocumentEntity> documents = documentRepository.findAll();

    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Document Report");

      Row headerRow = sheet.createRow(0);
      headerRow.createCell(0).setCellValue("ID");
      headerRow.createCell(1).setCellValue("Name");
      headerRow.createCell(2).setCellValue("Type");
      headerRow.createCell(3).setCellValue("Author");
      headerRow.createCell(4).setCellValue("Upload Date");

      int rowIdx = 1;
      for (DocumentEntity doc : documents) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(doc.getId().toString());
        row.createCell(1).setCellValue(doc.getName());
        row.createCell(2).setCellValue(doc.getType());
        row.createCell(3).setCellValue(doc.getAuthor());
        row.createCell(4).setCellValue(doc.getUploadDate().toString());
      }

      workbook.write(out);
      log.info("Excel report generated successfully containing {} records.", documents.size());
      return out.toByteArray();

    } catch (IOException e) {
      log.error("Failed to generate Excel report due to an I/O error.", e);
      throw e;
    }
  }

  @Override
  public byte[] exportDocumentsAsZip(String name, String type, String author) throws IOException {
    log.info("Initiating ZIP export for documents matching filters -> name: '{}', type: '{}', author: '{}'", name, type, author);
    List<DocumentEntity> documents = documentRepository.searchDocuments(name, type, author);

    if (documents.isEmpty()) {
      log.warn("ZIP export requested, but no documents matched the filter criteria.");
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      for (DocumentEntity doc : documents) {
        log.debug("Packing file '{}' into ZIP archive.", doc.getName());
        Resource resource = fileStorageService.loadFileAsResource(doc.getStoragePath());

        // Create a new entry in the zip file
        ZipEntry entry = new ZipEntry(doc.getName());
        zos.putNextEntry(entry);

        // Copy the file bytes into the zip
        resource.getInputStream().transferTo(zos);
        zos.closeEntry();
      }
      log.info("ZIP archive generated successfully with {} files.", documents.size());
    } catch (IOException e) {
      log.error("Failed to compress documents into ZIP archive.", e);
      throw e;
    }
    return baos.toByteArray();
  }

  private DocumentDTO mapToDTO(DocumentEntity entity) {
    DocumentDTO dto = new DocumentDTO();
    dto.setId(entity.getId());
    dto.setName(entity.getName());
    dto.setType(entity.getType());
    dto.setAuthor(entity.getAuthor());
    dto.setUploadDate(entity.getUploadDate());
    return dto;
  }
}