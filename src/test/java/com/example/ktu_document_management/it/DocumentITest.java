package com.example.ktu_document_management.it;

import com.example.ktu_document_management.repository.DocumentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentITest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private DocumentRepository documentRepository;

  @AfterEach
  void cleanUp() {
    documentRepository.deleteAll();
  }

  @Test
  void uploadDocument_Returns201_AndPersistsToDatabase() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file", "report.pdf", MediaType.APPLICATION_PDF_VALUE,
        "Real PDF Content".getBytes()
    );

    mockMvc.perform(multipart("/api/v1/documents/upload")
            .file(file)
            .param("author", "Jane Doe"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.name").value("report.pdf"))
        .andExpect(jsonPath("$.author").value("Jane Doe"));

    assert documentRepository.count() == 1;
  }

  @Test
  void uploadDocument_Duplicate_Returns409() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file", "report.pdf", MediaType.APPLICATION_PDF_VALUE,
        "Identical Content".getBytes()
    );

    mockMvc.perform(multipart("/api/v1/documents/upload")
            .file(file).param("author", "Jane Doe"))
        .andExpect(status().isCreated());

    mockMvc.perform(multipart("/api/v1/documents/upload")
            .file(file).param("author", "Jane Doe"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409));
  }

  @Test
  void uploadDocument_InvalidType_Returns400() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file", "virus.exe", "application/x-msdownload",
        "Bad Content".getBytes()
    );

    mockMvc.perform(multipart("/api/v1/documents/upload")
            .file(file).param("author", "Jane Doe"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void searchDocuments_Returns200_WithMatchingResults() throws Exception {
    uploadTestFile("budget.pdf", "Alice");
    uploadTestFile("thesis.docx", "Bob");

    mockMvc.perform(get("/api/v1/documents/search")
            .param("author", "Alice")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].author").value("Alice"));
  }

  @Test
  void downloadDocument_Returns200_WithFileBytes() throws Exception {
    String id = uploadTestFile("report.xlsx", "Charlie");

    mockMvc.perform(get("/api/v1/documents/{id}/download", id))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
        .andExpect(header().string("Content-Disposition", containsString("attachment")));
  }

  @Test
  void downloadDocument_NotFound_Returns404() throws Exception {
    mockMvc.perform(get("/api/v1/documents/{id}/download",
            "00000000-0000-0000-0000-000000000000"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void generateReport_Returns200_WithExcelContentType() throws Exception {
    uploadTestFile("data.xlsx", "Dave");

    mockMvc.perform(get("/api/v1/documents/report"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
  }

  @Test
  void exportZip_Returns200_WithZipContentType() throws Exception {
    uploadTestFile("doc1.pdf", "Eve");
    uploadTestFile("doc2.pdf", "Eve");

    mockMvc.perform(get("/api/v1/documents/export-zip")
            .param("author", "Eve"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/zip"))
        .andExpect(header().string("Content-Disposition", containsString(".zip")));
  }

  private String uploadTestFile(String filename, String author) throws Exception {
    String ext = filename.substring(filename.lastIndexOf(".") + 1);
    MockMultipartFile file = new MockMultipartFile(
        "file", filename, "application/" + ext,
        ("Content of " + filename).getBytes()
    );

    String response = mockMvc.perform(multipart("/api/v1/documents/upload")
            .file(file).param("author", author))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    return response.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
  }
}