package com.example.ktu_document_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FileResponseDTO {

  private byte[] data;

  private String filename;

}