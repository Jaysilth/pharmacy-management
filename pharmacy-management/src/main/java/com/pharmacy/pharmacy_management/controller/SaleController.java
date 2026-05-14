package com.pharmacy.pharmacy_management.controller;

// Import DTOs and API response wrapper
import com.pharmacy.pharmacy_management.dto.ApiResponse; // Standardized response wrapper
import com.pharmacy.pharmacy_management.dto.SaleRequestDTO; // Request input
import com.pharmacy.pharmacy_management.dto.SaleResponseDTO; // Response output

// Import the service layer that contains business logic
import com.pharmacy.pharmacy_management.service.SaleService;

// Swagger/OpenAPI annotations for API documentation
import io.swagger.v3.oas.annotations.Operation; // Describes operation summary
import io.swagger.v3.oas.annotations.Parameter; // Describes parameters
import io.swagger.v3.oas.annotations.tags.Tag; // Groups endpoints in documentation

// Jakarta Validation for request validation
import jakarta.validation.Valid; // Triggers validation on request body

// Lombok - Reduces boilerplate
import lombok.RequiredArgsConstructor; // Generates constructor for DI

// Spring MVC annotations
import org.springframework.http.HttpStatus; // HTTP status codes
import org.springframework.http.ResponseEntity; // HTTP response wrapper
import org.springframework.security.access.prepost.PreAuthorize; // Role-based authorization
import org.springframework.web.bind.annotation.*; // REST controller annotations

// Java collections
import java.math.BigDecimal; // For revenue calculations
import java.util.List; // For returning lists of sales

/**
 * SaleController - REST API endpoints for Sales (POS) operations.
 * 
 * This controller handles all HTTP requests related to sales transactions
 * in the pharmacy Point of Sale (POS) system.
 * 
 * Key responsibilities:
 * - Create new sales (process transactions)
 * - View sales history
 * - Get revenue information
 * 
 * Features:
 * - All endpoints under /api/sales base path
 * - Returns standardized ApiResponse<T> wrapper
 * - Automatically validates stock before sale
 * - Automatically reduces stock after successful sale
 * - Includes Swagger/OpenAPI documentation
 * 
 * Business flow:
 * 1. Client sends sale request (medicineId, quantity)
 * 2. Controller validates the request
 * 3. Service validates stock, calculates price, creates sale
 * 4. Service automatically reduces medicine stock
 * 5. Response returned with sale details
 */
@RestController // Marks this class as a REST controller (returns JSON)
@RequestMapping("/api/sales") // Base URL for all endpoints in this controller
@RequiredArgsConstructor // Generates constructor with final fields (dependency injection)
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
@Tag(name = "Sales Management", description = "APIs for managing sales transactions in the pharmacy POS")
public class SaleController {

    /**
     * Service layer instance for sales operations.
     * 
     * Injected via constructor by Lombok.
     * All business logic is delegated to this service.
     */
    private final SaleService saleService;

    /**
     * Create a new sale transaction.
     * 
     * HTTP Method: POST
     * URL: /api/sales
     * 
     * This is the main endpoint for processing sales in the pharmacy POS.
     * It performs the following operations:
     * 1. Validates the request (medicineId, quantity)
     * 2. Checks if medicine exists
     * 3. Checks if medicine is not expired
     * 4. Validates sufficient stock is available
     * 5. Calculates total price
     * 6. Creates sale record
     * 7. Automatically reduces medicine stock
     * 
     * @param requestDTO Contains medicineId and quantity
     *                    Must be validated (@Valid triggers validation)
     * @return ResponseEntity<ApiResponse<SaleResponseDTO>>
     *         - 201 Created: Sale was successfully processed
     *         - 400 Bad Request: Validation failed, insufficient stock, or expired medicine
     *         - 404 Not Found: Medicine doesn't exist
     * 
     * Example request:
     * {
     *   "medicineId": 1,
     *   "quantity": 2
     * }
     * 
     * Example response:
     * {
     *   "success": true,
     *   "message": "Sale recorded successfully",
     *   "data": {
     *     "id": 1,
     *     "medicine": { "id": 1, "name": "Paracetamol 500mg", "manufacturer": "Pharma Corp" },
     *     "quantity": 2,
     *     "unitPrice": 5.99,
     *     "totalPrice": 11.98,
     *     "createdAt": "2024-01-15T10:30:00"
     *   }
     * }
     */
    @PostMapping // Maps to HTTP POST requests
    @Operation(summary = "Create a new sale", description = "Record a new sales transaction. Automatically reduces medicine stock.")
    public ResponseEntity<ApiResponse<SaleResponseDTO>> createSale(
            // @Valid triggers validation defined in SaleRequestDTO
            @Valid @RequestBody SaleRequestDTO requestDTO) {
        
        // Call service to process the sale (includes stock reduction)
        SaleResponseDTO response = saleService.createSale(requestDTO);
        
        // Return 201 Created with success response
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sale recorded successfully", response));
    }

    /**
     * Get all sales, ordered by date (newest first).
     * 
     * HTTP Method: GET
     * URL: /api/sales
     * 
     * Retrieves complete sales history for the pharmacy.
     * Results are sorted by creation date in descending order.
     * 
     * @return ResponseEntity<ApiResponse<List<SaleResponseDTO>>>
     *         - 200 OK: Returns list of all sales (could be empty)
     */
    @GetMapping // Maps to HTTP GET requests
    @Operation(summary = "Get all sales", description = "Retrieve all sales transactions ordered by date (newest first)")
    public ResponseEntity<ApiResponse<List<SaleResponseDTO>>> getAllSales() {
        // Call service to get all sales
        List<SaleResponseDTO> sales = saleService.getAllSales();
        
        // Return 200 OK with the list
        return ResponseEntity.ok(ApiResponse.success(sales));
    }

    /**
     * Get a specific sale by its ID.
     * 
     * HTTP Method: GET
     * URL: /api/sales/{id}
     * 
     * Retrieves details of a single sale transaction.
     * 
     * @param id The unique identifier of the sale (from URL path)
     *           @PathVariable binds this from the URL
     * @return ResponseEntity<ApiResponse<SaleResponseDTO>>
     *         - 200 OK: Sale found and returned
     *         - 404 Not Found: No sale exists with this ID
     */
    @GetMapping("/{id}") // {id} is a path variable
    @Operation(summary = "Get sale by ID", description = "Retrieve a specific sale transaction by its ID")
    public ResponseEntity<ApiResponse<SaleResponseDTO>> getSaleById(
            @Parameter(description = "Sale ID") @PathVariable Long id) {
        
        // Call service to get sale by ID
        SaleResponseDTO sale = saleService.getSaleById(id);
        
        // Return 200 OK with the sale
        return ResponseEntity.ok(ApiResponse.success(sale));
    }

    /**
     * Get total revenue from all sales.
     * 
     * HTTP Method: GET
     * URL: /api/sales/revenue
     * 
     * Calculates the sum of all sales in the system.
     * Returns BigDecimal.ZERO if there are no sales.
     * 
     * @return ResponseEntity<ApiResponse<BigDecimal>>
     *         - 200 OK: Always returns success with revenue amount
     * 
     * Example response:
     * {
     *   "success": true,
     *   "message": "Total revenue calculated",
     *   "data": 1234.56
     * }
     */
    @GetMapping("/revenue") // Maps to /api/sales/revenue
    @Operation(summary = "Get total revenue", description = "Calculate and return the total revenue from all sales")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalRevenue() {
        // Call service to calculate total revenue
        BigDecimal revenue = saleService.getTotalRevenue();
        
        // Return 200 OK with the revenue
        return ResponseEntity.ok(ApiResponse.success("Total revenue calculated", revenue));
    }
}