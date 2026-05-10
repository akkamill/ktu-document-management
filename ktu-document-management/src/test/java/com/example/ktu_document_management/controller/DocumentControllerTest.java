package com.example.ktu_document_management.controller;

import com.example.ktu_document_management.dto.DocumentDTO;
import com.example.ktu_document_management.dto.FileResponseDTO;
import com.example.ktu_document_management.exception.DocumentNotFoundException;
import com.example.ktu_document_management.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the DocumentController.
 * Uses MockMvc to simulate HTTP requests and verify REST API contracts,
 * HTTP status codes, and JSON serialization.
 */
@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private DocumentService documentService;

  private DocumentDTO mockDto;
  private final UUID docId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    mockDto = new DocumentDTO();
    mockDto.setId(docId);
    mockDto.setName("financial_report.pdf");
    mockDto.setType("pdf");
    mockDto.setAuthor("Jane Doe");
    mockDto.setUploadDate(LocalDateTime.now());
  }

  // ==========================================
  // 1. HAPPY PATH TESTS
  // ==========================================

  @Test
  void uploadDocument_Returns201Created() throws Exception {
    // Arrange
    MockMultipartFile mockFile = new MockMultipartFile(
        "file",
        "financial_report.pdf",
        MediaType.APPLICATION_PDF_VALUE,
        "PDF Content".getBytes()
    );

    when(documentService.uploadDocument(any(), anyString())).thenReturn(mockDto);

    // Act & Assert
    mockMvc.perform(multipart("/api/v1/documents/upload")
            .file(mockFile)
            .param("author", "Jane Doe"))
        .andExpect(status().isCreated()) // Expect 201 HTTP Status
        .andExpect(jsonPath("$.id").value(docId.toString()))
        .andExpect(jsonPath("$.name").value("financial_report.pdf"))
        .andExpect(jsonPath("$.author").value("Jane Doe"));
  }

  @Test
  void searchDocuments_Returns200Ok_WithJsonArray() throws Exception {
    // Arrange
    when(documentService.searchDocuments("report", "pdf", "Jane"))
        .thenReturn(List.of(mockDto));

    // Act & Assert
    mockMvc.perform(get("/api/v1/documents/search")
            .param("name", "report")
            .param("type", "pdf")
            .param("author", "Jane")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()) // Expect 200 HTTP Status
        .andExpect(jsonPath("$[0].id").value(docId.toString()))
        .andExpect(jsonPath("$[0].name").value("financial_report.pdf"));
  }

  @Test
  void downloadDocument_Returns200Ok_WithBinaryFile() throws Exception {
    // Arrange
    FileResponseDTO responseDTO = new FileResponseDTO("PDF Content".getBytes(), "financial_report_20260509.pdf");
    when(documentService.downloadDocument(docId.toString())).thenReturn(responseDTO);

    // Act & Assert
    mockMvc.perform(get("/api/v1/documents/{id}/download", docId.toString()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
        .andExpect(header().string("Content-Disposition", "attachment; filename=\"financial_report_20260509.pdf\""))
        .andExpect(content().bytes("PDF Content".getBytes()));
  }

  // ==========================================
  // 2. EXCEPTION HANDLING TESTS
  // ==========================================

  @Test
  void downloadDocument_NotFound_Returns404() throws Exception {
    // Arrange
    when(documentService.downloadDocument(anyString()))
        .thenThrow(new DocumentNotFoundException("Document not found"));

    // Act & Assert
    mockMvc.perform(get("/api/v1/documents/{id}/download", docId.toString()))
        .andExpect(status().isNotFound()) // Expect 404 HTTP Status
        // Verifies that the GlobalExceptionHandler correctly formatted the JSON
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"));
  }
}