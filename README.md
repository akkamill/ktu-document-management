# Document Management Service (DMS)

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)
![Liquibase](https://img.shields.io/badge/Liquibase-Migrations-yellow.svg)
![Testcontainers](https://img.shields.io/badge/Testcontainers-Integration%20Tests-purple.svg)

## Overview

The Document Management Service (DMS) is a RESTful microservice developed as part of a Master's level component architecture project at Kaunas University of Technology (KTU).

It provides a secure, reliable backend for uploading, indexing, retrieving, and packaging document files (`.pdf`, `.docx`, `.xlsx`). The architecture strictly enforces single-responsibility principles, separation of concerns, and features a "Contract-First" API design backed by OpenAPI 3.0.

---

## Key Features

- **Secure File Storage:** Extracts and stores metadata in PostgreSQL while safely saving physical binaries to a local volume with UUID-obfuscated filenames.
- **Cryptographic Duplicate Detection:** Calculates a SHA-256 hash of incoming byte streams to reject duplicate files at the database level, optimizing storage.
- **Advanced Search & Filtering:** Dynamic JPA Specification queries allow filtering by filename, extension type, and author — with full null-safety and no raw SQL injection risk.
- **Bulk ZIP Export:** Compresses multiple documents into a single, dynamically named `.zip` archive on the fly, filtered by the same search criteria.
- **Excel Reporting:** Generates downloadable `.xlsx` inventory reports using Apache POI.
- **Global Error Handling:** Uses `@RestControllerAdvice` to intercept exceptions and return standardized, secure JSON error contracts.
- **Database Migrations:** Schema managed exclusively via Liquibase changelogs — no Hibernate DDL auto-generation in production.

---

## Architecture & Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 / Spring Framework 7.0.7 |
| Database | PostgreSQL 17 |
| ORM | Spring Data JPA / Hibernate 7 |
| Schema Migrations | Liquibase |
| API Documentation | Contract-First OpenAPI 3.0 (Swagger UI) |
| Excel Generation | Apache POI |
| Containerization | Docker & Docker Compose |
| Integration Testing | JUnit 5 + Testcontainers (PostgreSQL 16-alpine) |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/example/ktu_document_management/
│   │   ├── config/             # DocumentSpecifications (JPA Specification filters)
│   │   ├── controller/         # REST controllers
│   │   ├── dto/                # Request/Response DTOs
│   │   ├── entity/             # JPA entities
│   │   ├── exception/          # Custom exceptions + GlobalExceptionHandler
│   │   ├── repository/         # Spring Data JPA repositories
│   │   └── service/            # Service interfaces and implementations
│   └── resources/
│       ├── db/changelog/       # Liquibase migration files
│       ├── application.yaml    # Main configuration
│       └── openapi.yaml        # Contract-First API specification
└── test/
    ├── java/
    │   ├── it/                 # Integration tests (DocumentITest)
    │   └── service/            # Unit tests
    └── resources/
        └── application-test.yaml  # Test profile (Testcontainers)
```

---

## API Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/documents/upload` | Upload a PDF, DOCX, or XLSX file |
| `GET` | `/api/v1/documents/search` | Search documents by name, type, author |
| `GET` | `/api/v1/documents/{id}/download` | Download a single document |
| `GET` | `/api/v1/documents/report` | Download an Excel inventory report |
| `GET` | `/api/v1/documents/export-zip` | Export filtered documents as a ZIP archive |

Full API documentation is available via Swagger UI at `http://localhost:8080/swagger-ui.html` when the application is running.

---

## Getting Started

### Prerequisites

- Docker and Docker Compose installed on your machine.
- Port `8080` available for the Spring Boot application.
- Port `5432` available for the PostgreSQL database.

### Installation & Startup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/ktu-document-management.git
   cd ktu-document-management
   ```

2. **Start all services with Docker Compose:**
   ```bash
   docker compose up --build
   ```
   This starts PostgreSQL and the Spring Boot application. Liquibase will automatically apply all database migrations on startup.

3. **Access the API:**
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Docs: `http://localhost:8080/v3/api-docs`

### Running Locally (without Docker)

1. Start a PostgreSQL instance on port `5432` with a database named `dms_db`.
2. Set credentials via environment variables or use the defaults (`postgres` / `dms123`):
   ```bash
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/dms_db
   export SPRING_DATASOURCE_USERNAME=postgres
   export SPRING_DATASOURCE_PASSWORD=dms123
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

---

## Running Tests

Integration tests use **Testcontainers** — Docker must be running on your machine.

```bash
mvn test
```

The test profile (`application-test.yaml`) spins up an isolated PostgreSQL 16 container automatically. Liquibase migrations are applied to it before each test run. No local database configuration is needed for tests.

### Test Coverage

| Test Class | Type | Coverage |
|---|---|---|
| `DocumentITest` | Integration | Upload, search, download, ZIP export, duplicate detection, invalid type rejection |
| `DocumentControllerTest` | Unit (MockMvc) | REST contracts, HTTP status codes, JSON serialization, exception handler mapping |
| `DocumentServiceImplTest` | Unit | Service-layer business logic |
| `FileStorageServiceImplTest` | Unit | File storage read/write/errogoi
---

## Design Decisions

**Liquibase over Hibernate DDL:** Production schema is managed exclusively via Liquibase (`ddl-auto: validate`). This ensures migrations are explicit, reviewable, and reproducible across environments.

**JPA Specifications over JPQL `@Query`:** The search functionality uses `JpaSpecificationExecutor` with composable `Specification` predicates instead of a raw `@Query`. This avoids Hibernate 7 + PostgreSQL type-binding issues (where untyped null parameters are inferred as `bytea`) and produces cleaner, null-safe, type-safe query construction.

**SHA-256 Duplicate Detection:** File hashes are computed on the raw byte stream before storage and enforced as a unique constraint at the database level, preventing redundant storage at both the application and DB layers.

**UUID Storage Paths:** Physical files are stored under UUID-generated names regardless of the original filename, preventing path traversal attacks and filename collisions.