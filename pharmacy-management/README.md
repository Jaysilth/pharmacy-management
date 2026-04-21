# Pharmacy Management System - Production Ready

A production-ready Pharmacy Management System inspired by Odoo Pharmacy/POS architecture, built with Spring Boot, PostgreSQL, and React.

## System Overview

### Backend (Java/Spring Boot)
- **Port**: 8080
- **Database**: PostgreSQL
- **API Documentation**: Swagger UI at `/swagger-ui.html`

### Frontend (React)
- **Port**: 3000 (development proxy to 8080)

## Running the Application

### Prerequisites
- Java 21+
- Node.js 18+
- PostgreSQL 14+

### Database Setup
Ensure PostgreSQL is running with these credentials:
- Host: localhost
- Port: 8081
- Database: pharmacy-management
- Username: postgres
- Password: jovaji143

### Backend
```bash
cd pharmacy-management
./mvnw spring-boot:run
```

Or build and run:
```bash
./mvnw clean package
java -jar target/pharmacy-management-0.0.1-SNAPSHOT.jar
```

### Frontend
```bash
cd frontend
npm install
npm start
```

## API Endpoints

### Medicine APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/medicines | Add new medicine |
| GET | /api/medicines | Get all medicines |
| GET | /api/medicines/{id} | Get medicine by ID |
| GET | /api/medicines/expired | Get expired medicines |
| GET | /api/medicines/low-stock | Get low stock medicines |
| GET | /api/medicines/search?name=x | Search medicines |
| PUT | /api/medicines/{id} | Update medicine |
| DELETE | /api/medicines/{id} | Delete medicine |

### Sale APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/sales | Create new sale |
| GET | /api/sales | Get all sales |
| GET | /api/sales/{id} | Get sale by ID |
| GET | /api/sales/revenue | Get total revenue |

## Sample JSON Payloads

### Add Medicine
```json
{
  "name": "Paracetamol 500mg",
  "quantity": 100,
  "price": 5.99,
  "expiryDate": "2026-12-31",
  "lowStockThreshold": 20,
  "manufacturer": "Pharma Corp",
  "description": "Pain relief medication"
}
```

### Create Sale
```json
{
  "medicineId": 1,
  "quantity": 2
}
```

## Swagger Documentation
Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

## Architecture

### Backend Layers
- **Controller**: REST API endpoints with Swagger annotations
- **Service**: Business logic (stock validation, automatic stock deduction)
- **Repository**: Data access layer with JPA
- **Entity**: JPA entities (Medicine, Sale)
- **DTO**: Request/Response data transfer objects

### Key Features
1. **Inventory Management**: Add, view, search medicines
2. **Expiry Tracking**: Automatic detection of expired medicines
3. **Low Stock Alerts**: Automatic detection of low stock items
4. **POS Sales**: Create sales with automatic stock deduction
5. **Stock Validation**: Prevents overselling
6. **Revenue Tracking**: Total revenue calculation

## Tech Stack
- Java 21
- Spring Boot 4.0.5
- Spring Data JPA
- PostgreSQL
- Lombok
- Springdoc OpenAPI
- React 18
- Fetch API