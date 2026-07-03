# Pharmacy Management System – Quick Summary

**Purpose**: A Spring Boot‑based backend API that manages medicine inventory, sales, and revenue tracking for a pharmacy. All data is persisted in PostgreSQL and exposed via a clean REST interface.

---

## Core Features
- **Medicine CRUD** – add, read, update, delete medicines.
- **Expiry & Stock Alerts** – automatic detection of expired items and low‑stock warnings (default threshold = 10).
- **POS Sales** – create sales with validation (stock & expiry) and automatic stock deduction.
- **Revenue Reporting** – endpoint for total revenue and per‑sale totals.
- **Search** – case‑insensitive partial name matching.
- **CORS** – enabled for common frontend ports (3000, 4200, 5173).
- **Swagger UI** – interactive API docs at `/swagger-ui.html`.

---

## Technology Stack
| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| DB | PostgreSQL 14+ |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| Docs | SpringDoc OpenAPI 3.0 |
| Build | Maven |
| Boilerplate | Lombok |

---

## Project Structure (high‑level)
```
pharmacy-management/
├─ src/main/java/com/pharmacy/pharmacy_management/
│   ├─ PharmacyManagementApplication.java      # Boot entry point
│   ├─ config/                                 # CORS & OpenAPI config
│   ├─ controller/                             # REST endpoints (Medicine, Sale, Auth, User, Dashboard)
│   ├─ dto/                                    # Request/Response DTOs
│   ├─ entity/                                 # JPA entity definitions (Medicine, Sale, User, Role)
│   ├─ exception/                              # Custom exceptions + global handler
│   ├─ repository/                             # Spring Data repositories
│   └─ service/                                # Business logic (MedicineService, SaleService, UserService)
├─ src/main/resources/application.properties   # Config file (DB URL, JPA settings, etc.)
├─ pom.xml                                     # Maven dependencies
└─ README.md                                   # Detailed documentation
```

---

## Key API Endpoints
### Medicine
| Method | Path | Description |
|--------|------|-------------|
| POST   | `/api/medicines` | Add a new medicine |
| GET    | `/api/medicines` | List all medicines |
| GET    | `/api/medicines/{id}` | Get by ID |
| GET    | `/api/medicines/expired` | List expired medicines |
| GET    | `/api/medicines/low-stock` | Low‑stock alert list |
| GET    | `/api/medicines/search?name=…` | Search by name |
| PUT    | `/api/medicines/{id}` | Update |
| DELETE | `/api/medicines/{id}` | Delete |

### Sale
| Method | Path | Description |
|--------|------|-------------|
| POST   | `/api/sales` | Create a sale (stock & expiry validation) |
| GET    | `/api/sales` | List all sales |
| GET    | `/api/sales/{id}` | Get a sale |
| GET    | `/api/sales/revenue` | Total revenue |

### Auth & User (JWT protected)
- `/api/auth/login` – obtain JWT token.
- `/api/users` – manage user accounts (admin only).

---

## Running the Application
```bash
# Clone & cd into the project root
cd pharmacy-management

# Using Maven Wrapper (recommended)
./mvnw spring-boot:run          # Development mode

# Or build a jar and run it
./mvnw clean package
java -jar target/pharmacy-management-0.0.1-SNAPSHOT.jar
```
> Default server: `http://localhost:8080`
> Swagger UI: `http://localhost:8080/swagger-ui.html`

*Database* – ensure a PostgreSQL instance is running and update `src/main/resources/application.properties` with the correct URL, username, and password.

---

## Testing
```bash
./mvnw test
```
Unit tests cover service layer logic and controller validation.

---

## Extensibility Notes
- **Security** – JWT filter (`JwtAuthenticationFilter`) protects all `/api/**` endpoints except `/api/auth/**`.
- **Transactionality** – Service methods are annotated with `@Transactional` to guarantee atomic operations.
- **Future Work** – Add pagination, role‑based access control, and migration tooling (Flyway/Liquibase).

---

*License*: MIT (see LICENSE file).
