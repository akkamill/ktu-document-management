package com.example.ktu_document_management.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for handling physical file system operations.
 * Responsible for securely writing and reading binary file data to and from the server's local disk.
 * * @author Kamil Alakbarov
 * @version 1.0
 */
public interface FileStorageService {

  /**
   * Stores an uploaded file to the server's designated local upload directory.
   * Generates a secure, randomized UUID filename while preserving the original file extension
   * to prevent naming collisions and unauthorized file overwrites.
   *
   * @param file The multipart file binary stream received from the HTTP request.
   * @return The newly generated unique filename (e.g., "123e4567-e89b.pdf") as saved on the disk.
   * @throws RuntimeException if an I/O error occurs during the file copy process.
   */
  String storeFile(MultipartFile file);

  /**
   * Loads a physical file from the local upload directory and converts it into a Spring Resource.
   * This resource can then be streamed back to the client for downloads or ZIP packaging.
   *
   * @param fileName The exact name of the file as it is stored on the disk (including extension).
   * @return A {@link org.springframework.core.io.Resource} representing the readable file stream.
   * @throws com.example.ktu_document_management.exception.DocumentNotFoundException if the file does not exist or is unreadable.
   */
  Resource loadFileAsResource(String fileName);
}