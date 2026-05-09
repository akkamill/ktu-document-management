package com.example.ktu_document_management.service.impl;

import com.example.ktu_document_management.exception.DocumentNotFoundException;
import com.example.ktu_document_management.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

  private final Path fileStorageLocation;

  public FileStorageServiceImpl(@Value("${app.storage.upload-dir}") String uploadDir) {
    this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    log.info("Initializing File Storage at location: {}", this.fileStorageLocation);

    try {
      Files.createDirectories(this.fileStorageLocation);
      log.debug("Storage directory verified/created successfully.");
    } catch (Exception ex) {
      log.error("Fatal error: Could not create the directory where uploaded files will be stored.", ex);
      throw new RuntimeException("Could not create upload directory.", ex);
    }
  }

  @Override
  public String storeFile(MultipartFile file) {
    String originalFileName = file.getOriginalFilename();
    String fileExtension = "";

    if (originalFileName != null && originalFileName.contains(".")) {
      fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
    }

    String newFileName = UUID.randomUUID() + fileExtension;
    log.debug("Generated unique filename {} for original file {}", newFileName, originalFileName);

    try {
      Path targetLocation = this.fileStorageLocation.resolve(newFileName);
      log.debug("Transferring file bytes to: {}", targetLocation);

      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

      log.info("File {} successfully stored as {}", originalFileName, newFileName);
      return newFileName;
    } catch (IOException ex) {
      log.error("I/O Error occurred while storing file {}: {}", originalFileName, ex.getMessage());
      throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
    }
  }

  @Override
  public Resource loadFileAsResource(String fileName) {
    log.debug("Attempting to load file as resource: {}", fileName);
    try {
      Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
      Resource resource = new UrlResource(filePath.toUri());

      if (resource.exists() || resource.isReadable()) {
        log.info("File found and ready for retrieval: {}", fileName);
        return resource;
      } else {
        log.warn("File retrieval failed: {} does not exist or is not readable on disk.", fileName);
        throw new DocumentNotFoundException("Physical file not found on server: " + fileName);
      }
    } catch (MalformedURLException ex) {
      log.error("Error creating URL resource for file: {}", fileName, ex);
      throw new DocumentNotFoundException("File path is invalid: " + fileName);
    }
  }
}