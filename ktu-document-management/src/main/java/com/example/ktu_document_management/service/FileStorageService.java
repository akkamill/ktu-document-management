package com.example.ktu_document_management.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

  /**
   * Stores a file and returns its generated storage path/filename.
   */
  String storeFile(MultipartFile file);

  /**
   * Loads a file as a Resource for downloading.
   */
  Resource loadFileAsResource(String fileName);
}
