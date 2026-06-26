package com.pharmacy.pharmacy_management.controller;

// Import DTOs and API response wrapper
import com.pharmacy.pharmacy_management.dto.ApiResponse; // Standardized response wrapper
import com.pharmacy.pharmacy_management.dto.MedicineRequestDTO; // Request input
import com.pharmacy.pharmacy_management.dto.MedicineResponseDTO; // Response output

// Import the service layer that contains business logic
import com.pharmacy.pharmacy_management.service.MedicineService;

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
import java.util.List; // For returning lists of medicines

/**
 * MedicineController - REST API endpoints for Medicine operations.
 *
 * This controller handles all HTTP requests related to medicine management.
 * It provides a RESTful API that clients can use to:
 * - Add new medicines to inventory
 * - View all medicines
 * - Search for specific medicines
 * - Get details of specific medicines
 * - Update medicine information
 * - Delete medicines
 * - View expired and low-stock medicines
 *
 * Key features:
 * - All endpoints are under /api/medicines base path
 * - Returns standardized ApiResponse<T> wrapper
 * - Uses @Valid to validate request bodies
 * - Includes Swagger/OpenAPI documentation annotations
 *
 * Design:
 * - Follows RESTful conventions
 * - Uses HTTP status codes appropriately (200, 201, 404, etc.)
 * - Delegates business logic to MedicineService
 * - Converts between DTOs and entities in the service layer
 */
@RestController // Marks this class as a REST controller (returns JSON)
@RequestMapping("/api/medicines") // Base URL for all endpoints in this controller
@RequiredArgsConstructor // Generates constructor with final fields (dependency injection)
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
@Tag(name = "Medicine Management", description = "APIs for managing medicines in the pharmacy inventory")
public class MedicineController {

    /**
     * Service layer instance for medicine operations.
     *
     * Injected via constructor by Lombok.
     * All business logic is delegated to this service.
     */
    private final MedicineService medicineService;

    /**
     * Add a new medicine to the inventory.
     *
     * HTTP Method: POST
     * URL: /api/medicines
     *
     * This endpoint creates a new medicine record in the database.
     * It validates the request body using Jakarta Bean Validation annotations
     * defined in MedicineRequestDTO.
     *
     * @param requestDTO The medicine data from the request body
     *                    Must be validated (@Valid triggers validation)
     * @return ResponseEntity<ApiResponse<MedicineResponseDTO>>
     *         - 201 Created: Medicine was successfully created
     *         - 400 Bad Request: Validation failed
     *
     * Example request:
     * {
     *   "name": "Paracetamol 500mg",
     *   "quantity": 100,
     *   "price": 5.99,
     *   "expiryDate": "2026-12-31",
     *   "lowStockThreshold": 20,
     *   "manufacturer": "Pharma Corp",
     *   "description": "Pain relief medication"
     * }
     */
    @PostMapping // Maps to HTTP POST requests
    @Operation(summary = "Add a new medicine", description = "Create a new medicine entry in the inventory")
    public ResponseEntity<ApiResponse<MedicineResponseDTO>> addMedicine(
            // @Valid triggers validation defined in MedicineRequestDTO
            @Valid @RequestBody MedicineRequestDTO requestDTO) {

        // Call service to create the medicine
        MedicineResponseDTO response = medicineService.addMedicine(requestDTO);

        // Return 201 Created with success response
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Medicine added successfully", response));
    }

    /**
     * Get all medicines from the inventory.
     *
     * HTTP Method: GET
     * URL: /api/medicines
     *
     * Retrieves a list of all medicines in the system.
     * Returns empty list if no medicines exist.
     *
     * @return ResponseEntity<ApiResponse<List<MedicineResponseDTO>>>
     *         - 200 OK: Always returns success with list (could be empty)
     */
    @GetMapping
    @Operation(summary = "Get all medicines. Optional ?search= and ?category=")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> getAllMedicines(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {

        List<MedicineResponseDTO> medicines;

        if (search != null && !search.isBlank()) {
            medicines = medicineService.searchMedicines(search);
        } else if (category != null && !category.isBlank()) {
            medicines = medicineService.getMedicinesByCategory(category);
        } else {
            medicines = medicineService.getAllMedicines();
        }

        return ResponseEntity.ok(ApiResponse.success(medicines));
    }

    /**
     * Get a specific medicine by its ID.
     *
     * HTTP Method: GET
     * URL: /api/medicines/{id}
     *
     * Retrieves detailed information about a single medicine.
     *
     * @param id The unique identifier of the medicine (from URL path)
     *           @PathVariable binds this from the URL
     * @return ResponseEntity<ApiResponse<MedicineResponseDTO>>
     *         - 200 OK: Medicine found and returned
     *         - 404 Not Found: No medicine exists with this ID
     */
    @GetMapping("/{id}") // {id} is a path variable
    @Operation(summary = "Get medicine by ID", description = "Retrieve a specific medicine by its ID")
    public ResponseEntity<ApiResponse<MedicineResponseDTO>> getMedicineById(
            // @Parameter used for Swagger documentation
            @Parameter(description = "Medicine ID") @PathVariable Long id) {

        // Call service to get medicine by ID (throws exception if not found)
        MedicineResponseDTO medicine = medicineService.getMedicineById(id);

        // Return 200 OK with the medicine
        return ResponseEntity.ok(ApiResponse.success(medicine));
    }

    /**
     * Get all expired medicines.
     *
     * HTTP Method: GET
     * URL: /api/medicines/expired
     *
     * Returns medicines that have passed their expiry date.
     * Useful for inventory management and quality control.
     *
     * @return ResponseEntity<ApiResponse<List<MedicineResponseDTO>>>
     *         - 200 OK: Returns list of expired medicines (could be empty)
     */
    @GetMapping("/expired") // Maps to /api/medicines/expired
    @Operation(summary = "Get expired medicines", description = "Retrieve all medicines that have expired")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> getExpiredMedicines() {
        // Call service to find expired medicines
        List<MedicineResponseDTO> medicines = medicineService.getExpiredMedicines();

        // Return 200 OK with the list
        return ResponseEntity.ok(ApiResponse.success(medicines));
    }

    /**
     * Get all medicines with low stock.
     *
     * HTTP Method: GET
     * URL: /api/medicines/low-stock
     *
     * Returns medicines where quantity is at or below the lowStockThreshold.
     * Useful for identifying items that need restocking.
     *
     * @return ResponseEntity<ApiResponse<List<MedicineResponseDTO>>>
     *         - 200 OK: Returns list of low stock medicines (could be empty)
     */
    @GetMapping("/low-stock") // Maps to /api/medicines/low-stock
    @Operation(summary = "Get low stock medicines", description = "Retrieve all medicines with low stock levels")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> getLowStockMedicines() {
        // Call service to find low stock medicines
        List<MedicineResponseDTO> medicines = medicineService.getLowStockMedicines();

        // Return 200 OK with the list
        return ResponseEntity.ok(ApiResponse.success(medicines));
    }

    /**
     * Get all medicines expiring soon.
     *
     * HTTP Method: GET
     * URL: /api/medicines/expiring-soon
     *
     * Returns medicines that are approaching their expiry date.
     * Useful for inventory management and preventing sale of expiring products.
     *
     * @return ResponseEntity<ApiResponse<List<MedicineResponseDTO>>>
     *         - 200 OK: Returns list of expiring soon medicines (could be empty)
     */
    @GetMapping("/expiring-soon") // Maps to /api/medicines/expiring-soon
    @Operation(summary = "Get expiring soon medicines", description = "Retrieve all medicines expiring soon")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> getExpiringSoonMedicines() {
        // Call service to find expiring soon medicines
        List<MedicineResponseDTO> medicines = medicineService.getExpiringSoonMedicines();

        // Return 200 OK with the list
        return ResponseEntity.ok(ApiResponse.success(medicines));
    }

    /**
     * Search medicines by name.
     *
     * HTTP Method: GET
     * URL: /api/medicines/search?name={name}
     *
     * Performs a case-insensitive partial match search on medicine names.
     *
     * @param name The search term as a query parameter
     *             @RequestParam binds this from URL query string
     * @return ResponseEntity<ApiResponse<List<MedicineResponseDTO>>>
     *         - 200 OK: Returns matching medicines (could be empty)
     */
    @GetMapping("/search") // Maps to /api/medicines/search
    @Operation(summary = "Search medicines", description = "Search medicines by name")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> searchMedicines(
            @Parameter(description = "Medicine name to search") @RequestParam String name) {

        // Call service to search medicines by name
        List<MedicineResponseDTO> medicines = medicineService.searchMedicines(name);

        // Return 200 OK with the results
        return ResponseEntity.ok(ApiResponse.success(medicines));
    }

    /**
     * Update an existing medicine.
     *
     * HTTP Method: PUT
     * URL: /api/medicines/{id}
     *
     * Updates all fields of an existing medicine record.
     * Partial updates are supported (only provided fields are updated).
     *
     * @param id The ID of the medicine to update (from URL path)
     * @param requestDTO The new medicine data (from request body)
     * @return ResponseEntity<ApiResponse<MedicineResponseDTO>>
     *         - 200 OK: Medicine was successfully updated
     *         - 404 Not Found: No medicine exists with this ID
     *         - 400 Bad Request: Validation failed
     */
    @PutMapping("/{id}") // Maps to HTTP PUT requests
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update medicine", description = "Update an existing medicine's information")
    public ResponseEntity<ApiResponse<MedicineResponseDTO>> updateMedicine(
            @Parameter(description = "Medicine ID") @PathVariable Long id,
            @Valid @RequestBody MedicineRequestDTO requestDTO) {

        // Call service to update the medicine
        MedicineResponseDTO response = medicineService.updateMedicine(id, requestDTO);

        // Return 200 OK with success message
        return ResponseEntity.ok(ApiResponse.success("Medicine updated successfully", response));
    }

    /**
     * Delete a medicine from the inventory.
     *
     * HTTP Method: DELETE
     * URL: /api/medicines/{id}
     *
     * Removes a medicine record from the database.
     *
     * @param id The ID of the medicine to delete (from URL path)
     * @return ResponseEntity<ApiResponse<Void>>
     *         - 200 OK: Medicine was successfully deleted
     *         - 404 Not Found: No medicine exists with this ID
     */
    @DeleteMapping("/{id}") // Maps to HTTP DELETE requests
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete medicine", description = "Delete a medicine from the inventory")
    public ResponseEntity<ApiResponse<Void>> deleteMedicine(
            @Parameter(description = "Medicine ID") @PathVariable Long id) {

        // Call service to delete the medicine
        medicineService.deleteMedicine(id);

        // Return 200 OK with success message (null data for delete responses)
        return ResponseEntity.ok(ApiResponse.success("Medicine deleted successfully", null));
    }
}