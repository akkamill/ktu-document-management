package com.example.ktu_document_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object used for transferring binary file data from the Service layer
 * to the Controller layer. It encapsulates both the raw file bytes and the dynamically
 * generated, timestamped filename for secure downloads and ZIP exports.
 * * @author Kamil Alakbarov
 * @version 1.0
 */
@Data
@Builder
@AllArgsConstructor
public class FileResponseDTO {

  /** The raw binary stream of the file or ZIP archive. */
  private byte[] data;

  /** The dynamically generated filename (e.g., "report_20260509_1615.pdf"). */
  private String filename;
}