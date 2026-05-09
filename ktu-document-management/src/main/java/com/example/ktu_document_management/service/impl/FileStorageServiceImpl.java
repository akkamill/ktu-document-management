package com.example.ktu_document_management.service.impl;


import com.example.ktu_document_management.service.FileStorageService;
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

@Service
public class FileStorageServiceImpl implements FileStorageService {

  private final Path fileStorageLocation;

  public FileStorageServiceImpl(@Value("${app.storage.upload-dir}") String uploadDir) {
    this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(this.fileStorageLocation);
    } catch (Exception ex) {
      throw new RuntimeException("Could not create upload directory.", ex);
    }
  }

  @Override
  public String storeFile(MultipartFile file) {
    String originalFileName = file.getOriginalFilename();
    String fileExtension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
    String newFileName = UUID.randomUUID() + fileExtension;

    try {
      Path targetLocation = this.fileStorageLocation.resolve(newFileName);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
      return newFileName;
    } catch (IOException ex) {
      throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
    }
  }

  @Override
  public Resource loadFileAsResource(String fileName) {
    try {
      Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
      Resource resource = new UrlResource(filePath.toUri());
      if (resource.exists()) {
        return resource;
      } else {
        throw new RuntimeException("File not found " + fileName);
      }
    } catch (MalformedURLException ex) {
      throw new RuntimeException("File not found " + fileName, ex);
    }
  }
}