# Document Management Service (DMS)

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-7.0.7-brightgreen.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)

## Overview
The Document Management Service (DMS) is a RESTful microservice developed as part of a Master's level component architecture project at Kaunas University of Technology (KTU).

It provides a secure, reliable backend for uploading, indexing, retrieving, and packaging document files (`.pdf`, `.docx`, `.xlsx`). The architecture strictly enforces single-responsibility principles, separation of concerns, and features a "Contract-First" API design.

## Key Features
* **Secure File Storage:** Extracts and stores metadata in PostgreSQL while safely saving physical binaries to a local volume with UUID-obfuscated filenames.
* **Cryptographic Duplicate Detection:** Calculates a SHA-256 hash of incoming byte streams to reject duplicate files at the database level, optimizing storage.
* **Advanced Search & Filtering:** Dynamic JPQL queries allow filtering by filename, extension type, and author.
* **Bulk ZIP Export:** Compresses multiple documents into a single, dynamically named `.zip` archive on the fly.
* **Excel Reporting:** Generates downloadable `.xlsx` inventory reports using Apache POI.
* **Global Error Handling:** Utilizes Spring AOP (`@RestControllerAdvice`) to intercept exceptions and return standardized, secure JSON error contracts.

##  Architecture & Tech Stack
* **Language:** Java
* **Framework:** Spring Boot 7.0.7
* **Database:** PostgreSQL (via Spring Data JPA / Hibernate)
* **API Documentation:** Contract-First OpenAPI 3.0 (Swagger UI)
* **Containerization:** Docker & Docker Compose

## Getting Started

### Prerequisites
* Docker and Docker Compose installed on your machine.
* Port `8080` available for the Spring Boot application.
* Port `5432` available for the PostgreSQL database.

### Installation & Startup
1. **Clone the repository:**
   ```bash
   git clone [https://github.com/yourusername/ktu-document-management.git](https://github.com/yourusername/ktu-document-management.git)
   cd ktu-document-management