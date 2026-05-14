# Pharmacy Management System - Backend API

A production-ready Pharmacy Management System REST API built with Spring Boot and PostgreSQL, inspired by Odoo Pharmacy/POS architecture.

## Table of Contents
- [System Overview](#system-overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Code Architecture](#code-architecture)

---

## System Overview

This is a **backend-only REST API** that provides comprehensive pharmacy management capabilities:

- **Medicine Inventory Management**: Add, update, delete, and search medicines
- **Expiry Tracking**: Automatic detection of expired medicines
- **Low Stock Alerts**: Configurable threshold for restocking notifications
- **POS Sales**: Process sales with automatic stock deduction
- **Revenue Calculation**: Track total revenue from all sales

The API returns JSON responses and is designed to be consumed by any frontend client (React, Angular, Vue, Mobile apps, etc.).

---

## Features

### 1. Medicine Management (CRUD)
- **Create**: Add new medicines with name, quantity, price, expiry date, manufacturer
- **Read**: Get all medicines, get by ID, search by name
- **Update**: Modify medicine details (quantity, price, etc.)
- **Delete**: Remove medicines from inventory

### 2. Expiry Tracking
- Automatic detection of expired medicines
- Endpoint to retrieve all expired medicines
- Prevents selling expired medicines (validation in sales)

### 3. Low Stock Alerts
- Configurable threshold per medicine (default: 10 units)
- Endpoint to retrieve medicines below threshold
- Helps with inventory replenishment planning

### 4. POS Sales System
- Create sales with automatic stock validation
- Prevents overselling (InsufficientStockException)
- Prevents selling expired medicines
- Automatically reduces stock after successful sale
- Preserves historical pricing (unit price at time of sale)

### 5. Revenue Tracking
- Calculate total revenue from all sales
- Per-transaction total price calculation

### 6. Search Functionality
- Case-insensitive partial match search on medicine names
- Find medicines containing specific keywords

### 7. CORS Support
- Enabled for common frontend development ports
- Supports React (3000), Angular (4200), Vue/Vite (5173)

### 8. API Documentation
- Swagger/OpenAPI 3.0 integration
- Interactive API documentation at `/swagger-ui.html`

---

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| API Documentation | SpringDoc OpenAPI 3.0.2 |
| Build Tool | Maven |
| Code Generation | Lombok |

---

## Project Structure

```
pharmacy-management/
├── src/main/java/com/pharmacy/pharmacy_management/
│   ├── PharmacyManagementApplication.java    # Main entry point
│   │
│   ├── config/                               # Configuration classes
│   │   ├── CorsConfig.java                   # CORS configuration for frontend
│   │   └── OpenApiConfig.java                # Swagger/OpenAPI configuration
│   │
│   ├── controller/                           # REST API endpoints
│   │   ├── MedicineController.java           # Medicine CRUD endpoints
│   │   └── SaleController.java               # Sales/POS endpoints
│   │
│   ├── dto/                                  # Data Transfer Objects
│   │   ├── MedicineRequestDTO.java           # Request: Create/Update medicine
│   │   ├── MedicineResponseDTO.java          # Response: Medicine data
│   │   ├── SaleRequestDTO.java               # Request: Create sale
│   │   ├── SaleResponseDTO.java              # Response: Sale data
│   │   └── ApiResponse.java                  # Generic response wrapper
│   │
│   ├── entity/                               # JPA Entities (Database tables)
│   │   ├── Medicine.java                     # Medicine table mapping
│   │   └── Sale.java                         # Sale table mapping
│   │
│   ├── exception/                            # Custom Exceptions
│   │   ├── MedicineNotFoundException.java    # 404: Medicine not found
│   │   ├── InsufficientStockException.java   # 400: Not enough stock
│   │   └── GlobalExceptionHandler.java       # Centralized error handling
│   │
│   ├── repository/                           # Data Access Layer
│   │   ├── MedicineRepository.java           # Medicine database queries
│   │   └── SaleRepository.java               # Sale database queries
│   │
│   └── service/                              # Business Logic Layer
│       ├── MedicineService.java              # Medicine business logic
│       └── SaleService.java                  # Sales business logic
│
├── src/main/resources/
│   └── application.properties                # Application configuration
│
├── src/test/java/                            # Unit tests
├── pom.xml                                   # Maven dependencies
└── README.md                                 # This file
```

---

## Database Schema

### medicines table
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| name | VARCHAR(255) | NOT NULL | Medicine name |
| quantity | INTEGER | NOT NULL | Current stock quantity |
| price | DECIMAL(10,2) | NOT NULL | Price per unit |
| expiry_date | DATE | NOT NULL | Expiration date |
| low_stock_threshold | INTEGER | DEFAULT 10 | Alert threshold |
| description | VARCHAR(500) | NULLABLE | Additional info |
| manufacturer | VARCHAR(255) | NULLABLE | Pharmaceutical company |
| created_at | DATE | AUTO | Creation timestamp |
| updated_at | DATE | AUTO | Last update timestamp |

### sales table
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| medicine_id | BIGINT | FOREIGN KEY | References medicines.id |
| quantity | INTEGER | NOT NULL | Units sold |
| unit_price | DECIMAL(10,2) | NOT NULL | Price at time of sale |
| total_price | DECIMAL(10,2) | NOT NULL | Total (quantity × unit_price) |
| created_at | TIMESTAMP | NOT NULL, AUTO | Sale timestamp |

---

## API Endpoints

### Medicine APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/medicines` | Add new medicine |
| GET | `/api/medicines` | Get all medicines |
| GET | `/api/medicines/{id}` | Get medicine by ID |
| GET | `/api/medicines/expired` | Get expired medicines |
| GET | `/api/medicines/low-stock` | Get low stock medicines |
| GET | `/api/medicines/search?name=x` | Search by name |
| PUT | `/api/medicines/{id}` | Update medicine |
| DELETE | `/api/medicines/{id}` | Delete medicine |

### Sale APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/sales` | Create new sale |
| GET | `/api/sales` | Get all sales |
| GET | `/api/sales/{id}` | Get sale by ID |
| GET | `/api/sales/revenue` | Get total revenue |

---

## Getting Started

### Prerequisites
- Java 21 or higher
- PostgreSQL 14 or higher

### Database Setup

Create a PostgreSQL database:
```sql
CREATE DATABASE pharmacy-management;
```

Update credentials in `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:8081/pharmacy-management
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### Running the Application

Using Maven Wrapper:
```bash
cd pharmacy-management
./mvnw spring-boot:run
```

Or build and run JAR:
```bash
./mvnw clean package
java -jar target/pharmacy-management-0.0.1-SNAPSHOT.jar
```

The API will be available at `http://localhost:8080`

### Running Tests
```bash
./mvnw test
```

---

## Configuration

Key properties in `application.properties`:

```properties
# Server configuration
server.port=8080

# PostgreSQL database connection
spring.datasource.url=jdbc:postgresql://localhost:8081/pharmacy-management
spring.datasource.username=postgres
spring.datasource.password=your_password

# JPA/Hibernate settings
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

---

## Code Architecture

### Layered Architecture

The application follows a standard layered architecture:

```
┌─────────────────────────────────────────┐
│         CONTROLLER LAYER                │
│  (REST API Endpoints)                   │
│  - MedicineController                   │
│  - SaleController                       │
└────────────────┬────────────────────────┘
                 │ Calls
                 ▼
┌─────────────────────────────────────────┐
│          SERVICE LAYER                  │
│  (Business Logic)                       │
│  - MedicineService                      │
│  - SaleService                          │
└────────────────┬────────────────────────┘
                 │ Uses
                 ▼
┌─────────────────────────────────────────┐
│        REPOSITORY LAYER                 │
│  (Data Access)                          │
│  - MedicineRepository                   │
│  - SaleRepository                       │
└────────────────┬────────────────────────┘
                 │ Queries
                 ▼
┌─────────────────────────────────────────┐
│          ENTITY LAYER                   │
│  (Database Tables)                      │
│  - Medicine                             │
│  - Sale                                 │
└─────────────────────────────────────────┘
```

### Request/Response Flow

1. **Client sends request** (e.g., POST /api/medicines)
2. **Controller receives** - Validates request, calls service
3. **Service processes** - Executes business logic, calls repository
4. **Repository interacts** - Performs database operations
5. **Entity maps to table** - JPA converts to database records
6. **Response flows back** - Entity → DTO → JSON → Client

### DTO Pattern

We use Data Transfer Objects (DTOs) to separate internal entities from API responses:

- **Request DTOs**: Validate client input (e.g., `MedicineRequestDTO`)
- **Response DTOs**: Format data for clients (e.g., `MedicineResponseDTO`)
- **ApiResponse<T>**: Wrapper for consistent response format

### Transaction Management

All service methods are transactional:
- `@Transactional` on class: All methods are atomic
- `@Transactional(readOnly = true)`: Optimized for read operations
- If any operation fails, all changes are rolled back

### Exception Handling

The `GlobalExceptionHandler` provides centralized error handling:
- Converts exceptions to appropriate HTTP status codes
- Returns consistent error format via `ApiResponse`
- Handles validation errors, not found errors, and unexpected errors

---

## Sample API Usage

### Add a Medicine
```bash
curl -X POST http://localhost:8080/api/medicines \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Paracetamol 500mg",
    "quantity": 100,
    "price": 5.99,
    "expiryDate": "2026-12-31",
    "lowStockThreshold": 20,
    "manufacturer": "Pharma Corp",
    "description": "Pain relief medication"
  }'
```

### Create a Sale
```bash
curl -X POST http://localhost:8080/api/sales \
  -H "Content-Type: application/json" \
  -d '{
    "medicineId": 1,
    "quantity": 2
  }'
```

### Get All Medicines
```bash
curl http://localhost:8080/api/medicines
```

### Get Total Revenue
```bash
curl http://localhost:8080/api/sales/revenue
```

---

## API Response Format

All responses follow this structure:

**Success Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

**Validation Error Response:**
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "name": "Medicine name is required",
    "quantity": "Quantity must be positive"
  }
}
```

---

## Swagger Documentation

Interactive API documentation is available at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

---

## Notes

- This is a **backend-only** REST API
- CORS is enabled for frontend development (React, Angular, Vue)
- Database schema is auto-generated by Hibernate (`ddl-auto=update`)
- For production, consider using Flyway or Liquibase for migrations
- The API uses Java 21 features and requires JDK 21+

---

## License

MIT License - See LICENSE file for details
