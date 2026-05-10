package com.example.ktu_document_management.service;

import com.example.ktu_document_management.dto.DocumentDTO;
import com.example.ktu_document_management.dto.FileResponseDTO;
import com.example.ktu_document_management.entitiy.DocumentEntity;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

/**
 * Core business logic interface for Document Management.
 * Handles file validation, metadata extraction, duplicate prevention, and bulk exports.
 * * @author Kamil Alakbarov
 * @version 1.0
 */
public interface DocumentService {

  /**
   * Uploads a document to the system.
   * Calculates a SHA-256 hash of the file to prevent identical files from being uploaded twice.
   *
   * @param file   The multipart file binary stream. Must be PDF, DOCX, or XLSX.
   * @param author The name of the user uploading the document.
   * @return A {@link DocumentDTO} containing the saved metadata and assigned UUID.
   * @throws com.example.ktu_document_management.exception.DuplicateDocumentException if the file content already exists.
   * @throws IllegalArgumentException if the file extension is unsupported.
   */
  DocumentDTO uploadDocument(MultipartFile file, String author);

  /**
   * Searches the database for documents matching the provided criteria.
   * All parameters are optional. If all are null, returns all documents.
   *
   * @param name   Partial match for the filename (case-insensitive).
   * @param type   Exact match for the file extension (e.g., "pdf").
   * @param author Partial match for the author's name.
   * @return A list of {@link DocumentDTO} matching the criteria.
   */
  List<DocumentDTO> searchDocuments(String name, String type, String author);

  /**
   * Retrieves a single document's binary data and its dynamically generated timestamped filename.
   *
   * @param id The UUID of the document.
   * @return A {@link FileResponseDTO} containing the byte array and filename.
   * @throws com.example.ktu_document_management.exception.DocumentNotFoundException if the ID does not exist.
   */
  FileResponseDTO downloadDocument(String id);

  /**
   * Internal helper method to retrieve a raw {@link DocumentEntity} from the database.
   * Parses the provided String ID into a UUID and executes a database lookup.
   * This method is utilized internally by the service layer for operations that require
   * physical file retrieval or entity modification.
   *
   * @param id The String representation of the document's UUID.
   * @return The located {@link DocumentEntity}.
   * @throws com.example.ktu_document_management.exception.DocumentNotFoundException if the ID format is invalid or no matching record exists in the database.
   */
  DocumentEntity getDocumentById(String id);

  /**
   * Generates a ZIP archive containing all documents that match the search criteria.
   *
   * @param name   Partial match for the filename.
   * @param type   Exact match for the file extension.
   * @param author Partial match for the author.
   * @return A {@link FileResponseDTO} containing the ZIP byte array and a timestamped filename.
   * @throws IOException if the file streams cannot be compressed.
   */
  FileResponseDTO exportDocumentsAsZip(String name, String type, String author) throws IOException;

  /**
   * Generates an Excel (.xlsx) inventory report of all documents currently in the system.
   *
   * @return A byte array representing the Excel file.
   * @throws IOException if the workbook cannot be written to the output stream.
   */
  byte[] generateExcelReport() throws IOException;
}