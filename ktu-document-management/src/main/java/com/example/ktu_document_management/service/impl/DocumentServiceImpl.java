package com.example.ktu_document_management.service.impl;

import com.example.ktu_document_management.dto.DocumentDTO;
import com.example.ktu_document_management.entitiy.DocumentEntity;
import com.example.ktu_document_management.repository.DocumentRepository;
import com.example.ktu_document_management.service.DocumentService;
import com.example.ktu_document_management.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

  private final DocumentRepository documentRepository;
  private final FileStorageService fileStorageService;

  private final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "docx", "xlsx");

  @Override
  public DocumentDTO uploadDocument(MultipartFile file, String author) {
    String originalName = file.getOriginalFilename();
    String extension = originalName != null ? originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase() : "";

    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new IllegalArgumentException("Invalid file type. Only PDF, DOCX, and XLSX are allowed.");
    }

    String storedFileName = fileStorageService.storeFile(file);

    DocumentEntity doc = new DocumentEntity();
    doc.setName(originalName);
    doc.setType(extension);
    doc.setAuthor(author);
    doc.setStoragePath(storedFileName);
    doc.setUploadDate(LocalDateTime.now());

    DocumentEntity savedDoc = documentRepository.save(doc);
    return mapToDTO(savedDoc);
  }

  @Override
  public List<DocumentDTO> searchDocuments(String name, String type, String author) {
    return documentRepository.searchDocuments(name, type, author)
        .stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  @Override
  public DocumentEntity getDocumentById(String id) {
    return documentRepository.findById(UUID.fromString(id))
        .orElseThrow(() -> new RuntimeException("Document not found"));
  }

  @Override
  public byte[] generateExcelReport() throws IOException {
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
      return out.toByteArray();
    }
  }

  public byte[] exportDocumentsAsZip(String name, String type, String author) throws IOException {
    List<DocumentEntity> documents = documentRepository.searchDocuments(name, type, author);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      for (DocumentEntity doc : documents) {
        Resource resource = fileStorageService.loadFileAsResource(doc.getStoragePath());

        // Create a new entry in the zip file
        ZipEntry entry = new ZipEntry(doc.getName());
        zos.putNextEntry(entry);

        // Copy the file bytes into the zip
        resource.getInputStream().transferTo(zos);
        zos.closeEntry();
      }
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