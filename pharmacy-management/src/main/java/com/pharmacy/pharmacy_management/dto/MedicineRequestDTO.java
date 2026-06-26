package com.pharmacy.pharmacy_management.dto;

// Jakarta Validation API imports for input validation annotations
// These are used to validate request data before processing
import jakarta.validation.constraints.NotBlank; // Validates that a string is not blank
import jakarta.validation.constraints.NotNull; // Validates that a value is not null
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive; // Validates that a number is positive

// Lombok imports for automatic code generation
import lombok.*;

// BigDecimal for precise financial calculations
import java.math.BigDecimal;

// LocalDate for date handling (expiry dates)
import java.time.LocalDate;

/**
 * MedicineRequestDTO - Data Transfer Object for creating/updating medicines.
 * 
 * This DTO is used when receiving medicine data from clients via HTTP requests.
 * It acts as a "_request" object that carries data from the client to the server.
 * 
 * Key responsibilities:
 * - Validate incoming medicine data using Jakarta Bean Validation
 * - Define which fields are required vs optional
 * - Decouple the API from the internal entity structure
 * 
 * Validation annotations:
 * - @NotBlank: Field cannot be null, empty, or whitespace-only (for Strings)
 * - @NotNull: Field cannot be null (for objects and primitives)
 * - @Positive: Number must be greater than zero
 * - message: Custom error message shown when validation fails
 */
@Getter // Lombok: Generates getter methods for all fields
@Setter // Lombok: Generates setter methods for all fields
@Builder // Lombok: Enables builder pattern for object construction
@NoArgsConstructor // Lombok: Generates no-argument constructor
@AllArgsConstructor // Lombok: Generates constructor with all fields
public class MedicineRequestDTO {

    /**
     * Name of the medicine.
     * 
     * Required field - cannot be blank.
     * This is the display name of the medicine (e.g., "Paracetamol 500mg").
     * 
     * Validation:
     * - @NotBlank: Ensures name is provided and not empty/whitespace
     * - Custom message shown when validation fails
     */
    @NotBlank(message = "Medicine name is required")
    private String name;

    /**
     * Current quantity/stock of the medicine.
     * 
     * Required field - must be provided and must be positive.
     * Represents the number of units in stock.
     * 
     * Validation:
     * - @NotNull: Quantity must be provided
     * - @Positive: Quantity must be greater than 0 (can't have negative stock)
     */
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    /**
     * Price per unit of the medicine.
     * 
     * Required field - must be provided and must be positive.
     * Uses BigDecimal for precise financial calculations.
     * 
     * Validation:
     * - @NotNull: Price must be provided
     * - @Positive: Price must be greater than 0
     */
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    /**
     * Expiry date of the medicine.
     * 
     * Required field - must be provided.
     * The date after which the medicine is considered expired.
     * 
     * Validation:
     * - @NotNull: Expiry date must be provided
     * Note: Actual date validation (e.g., not in the past) could be added
     */
    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;



    /**
     * Low stock threshold for alerts.
     * 
     * Optional field - if not provided, defaults to 10 in the service layer.
     * When stock falls to or below this number, the medicine is flagged as low stock.
     * 
     * This is optional because:
     * - Not all medicines require the same threshold
     * - A default value is provided if not specified
     */
    private Integer lowStockThreshold;

    /**
     * Description of the medicine.
     * 
     * Optional field containing additional information about the medicine.
     * Can include usage instructions, side effects, storage requirements, etc.
     */
    private String description;



    /**
     * Manufacturer of the medicine.
     * 
     * Optional field identifying the pharmaceutical company that produces
     * this medicine. Useful for tracking suppliers and source information.
     */
    private String manufacturer;

    @Pattern(
            regexp = "^(EYEDROP|TABLET|INJECTION|SYRUP)$",
            message = "Category must be one of: EYEDROP, TABLET, INJECTION, SYRUP"
    )
    private String category;
}