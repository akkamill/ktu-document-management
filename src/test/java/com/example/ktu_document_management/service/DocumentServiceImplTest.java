package com.example.ktu_document_management.service;

import com.example.ktu_document_management.dto.DocumentDTO;
import com.example.ktu_document_management.dto.FileResponseDTO;
import com.example.ktu_document_management.entitiy.DocumentEntity;
import com.example.ktu_document_management.exception.DocumentNotFoundException;
import com.example.ktu_document_management.exception.DuplicateDocumentException;
import com.example.ktu_document_management.repository.DocumentRepository;
import com.example.ktu_document_management.service.impl.DocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

  @Mock
  private DocumentRepository documentRepository;

  @Mock
  private FileStorageService fileStorageService;

  @InjectMocks
  private DocumentServiceImpl documentService;

  private MultipartFile mockFile;
  private DocumentEntity mockEntity;
  private final String author = "Kamil Alakbarov";
  private final UUID docId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    // Setup a fake file for testing
    mockFile = new MockMultipartFile(
        "file",
        "test_report.pdf",
        "application/pdf",
        "Dummy PDF Content".getBytes()
    );

    // Setup a fake database entity
    mockEntity = new DocumentEntity();
    mockEntity.setId(docId);
    mockEntity.setName("test_report.pdf");
    mockEntity.setType("pdf");
    mockEntity.setAuthor(author);
    mockEntity.setStoragePath("random-uuid.pdf");
  }

  // ==========================================
  // 1. HAPPY PATH TESTS
  // ==========================================

  @Test
  void uploadDocument_Success() {
    // Arrange
    when(documentRepository.existsByFileHash(anyString())).thenReturn(false);
    when(fileStorageService.storeFile(mockFile)).thenReturn("random-uuid.pdf");

    // Mock the save to return our entity with an assigned ID
    when(documentRepository.save(any(DocumentEntity.class))).thenAnswer(invocation -> {
      DocumentEntity saved = invocation.getArgument(0);
      saved.setId(docId);
      return saved;
    });

    // Act
    DocumentDTO result = documentService.uploadDocument(mockFile, author);

    // Assert
    assertNotNull(result);
    assertEquals("test_report.pdf", result.getName());
    assertEquals("pdf", result.getType());
    assertEquals(author, result.getAuthor());

    // Verify dependencies were called correctly
    verify(fileStorageService, times(1)).storeFile(mockFile);
    verify(documentRepository, times(1)).save(any(DocumentEntity.class));
  }

  @Test
  void downloadDocument_Success() {
    // Arrange
    Resource mockResource = new ByteArrayResource("Dummy PDF Content".getBytes());
    when(documentRepository.findById(docId)).thenReturn(Optional.of(mockEntity));
    when(fileStorageService.loadFileAsResource(mockEntity.getStoragePath())).thenReturn(mockResource);

    // Act
    FileResponseDTO result = documentService.downloadDocument(docId.toString());

    // Assert
    assertNotNull(result);
    assertNotNull(result.getData());
    assertTrue(result.getFilename().contains("test_report"));
    assertTrue(result.getFilename().endsWith(".pdf"));
  }

  // ==========================================
  // 2. UNHAPPY PATH TESTS (Exceptions)
  // ==========================================

  @Test
  void uploadDocument_DuplicateFile_ThrowsException() {
    // Arrange
    // Simulate that the database already has this exact file hash
    when(documentRepository.existsByFileHash(anyString())).thenReturn(true);

    // Act & Assert
    DuplicateDocumentException exception = assertThrows(
        DuplicateDocumentException.class,
        () -> documentService.uploadDocument(mockFile, author)
    );

    assertEquals("A document with this exact content already exists.", exception.getMessage());

    // Verify that we NEVER attempted to save the file to disk or DB
    verify(fileStorageService, never()).storeFile(any());
    verify(documentRepository, never()).save(any());
  }

  @Test
  void uploadDocument_UnsupportedFileType_ThrowsException() {
    // Arrange
    MultipartFile badFile = new MockMultipartFile(
        "file",
        "script.exe",
        "application/x-msdownload",
        "Malicious Content".getBytes()
    );

    // Act & Assert
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> documentService.uploadDocument(badFile, author)
    );

    assertTrue(exception.getMessage().contains("Unsupported file type"));

    // Verify early exit
    verify(documentRepository, never()).existsByFileHash(anyString());
  }

  @Test
  void downloadDocument_NotFound_ThrowsException() {
    // Arrange
    UUID randomId = UUID.randomUUID();
    when(documentRepository.findById(randomId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        DocumentNotFoundException.class,
        () -> documentService.downloadDocument(randomId.toString())
    );
  }
}