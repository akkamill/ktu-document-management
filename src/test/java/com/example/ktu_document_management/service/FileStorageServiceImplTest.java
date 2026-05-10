package com.example.ktu_document_management.service;

import com.example.ktu_document_management.exception.DocumentNotFoundException;
import com.example.ktu_document_management.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the FileStorageServiceImpl.
 * Uses JUnit 5's @TempDir to safely test physical file I/O operations
 * without permanently polluting the host operating system's hard drive.
 */
class FileStorageServiceImplTest {

  private FileStorageServiceImpl fileStorageService;

  /**
   * JUnit 5 handles the lifecycle of this directory. It is created before
   * each test and securely deleted after the test finishes.
   */
  @TempDir
  Path tempUploadDir;

  @BeforeEach
  void setUp() {

    fileStorageService = new FileStorageServiceImpl(tempUploadDir.toString());

  }

  // ==========================================
  // 1. HAPPY PATH TESTS
  // ==========================================

  @Test
  void storeFile_Success_SavesToDiskAndReturnsUuidName() {
    // Arrange
    MockMultipartFile mockFile = new MockMultipartFile(
        "file",
        "thesis_draft.docx",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "Mock File Content".getBytes()
    );

    // Act
    String generatedFilename = fileStorageService.storeFile(mockFile);

    // Assert
    assertNotNull(generatedFilename);
    assertTrue(generatedFilename.endsWith(".docx"));
    assertNotEquals("thesis_draft.docx", generatedFilename); // Proves UUID was generated

    // Verify the file was actually written to the physical @TempDir disk
    Path targetLocation = tempUploadDir.resolve(generatedFilename);
    assertTrue(Files.exists(targetLocation));
  }

  @Test
  void loadFileAsResource_Success_ReturnsReadableStream() throws Exception {
    // Arrange: Manually create a file in the temp directory first
    String testFilename = "123e4567-e89b-12d3-a456-426614174000.pdf";
    Path testFile = tempUploadDir.resolve(testFilename);
    Files.writeString(testFile, "Mock PDF Content");

    // Act
    Resource resource = fileStorageService.loadFileAsResource(testFilename);

    // Assert
    assertNotNull(resource);
    assertTrue(resource.exists());
    assertTrue(resource.isReadable());
    assertEquals(testFilename, resource.getFilename());
  }

  // ==========================================
  // 2. UNHAPPY PATH TESTS (Exceptions)
  // ==========================================

  @Test
  void loadFileAsResource_FileNotFound_ThrowsDocumentNotFoundException() {
    // Arrange
    String missingFilename = "does-not-exist.pdf";

    // Act & Assert
    DocumentNotFoundException exception = assertThrows(
        DocumentNotFoundException.class,
        () -> fileStorageService.loadFileAsResource(missingFilename)
    );

    // Verify the exception message is helpful
    assertTrue(exception.getMessage().contains("does-not-exist.pdf"));
  }

  @Test
  void storeFile_EmptyFile_ThrowsException() {
    // Arrange
    MockMultipartFile emptyFile = new MockMultipartFile(
        "file",
        "empty.pdf",
        "application/pdf",
        new byte[0] // 0 bytes
    );

    // Act & Assert
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> fileStorageService.storeFile(emptyFile)
    );

    assertTrue(exception.getMessage().toLowerCase().contains("empty"));
  }
}