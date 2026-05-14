package com.pharmacy.pharmacy_management.dto;

// Jakarta Validation API imports for input validation
import jakarta.validation.constraints.NotNull; // Validates that a value is not null
import jakarta.validation.constraints.Positive; // Validates that a number is positive

// Lombok imports for automatic code generation
import lombok.*;

/**
 * SaleRequestDTO - Data Transfer Object for creating a new sale transaction.
 * 
 * This DTO is used when receiving sale data from clients via HTTP POST requests.
 * Clients send medicineId and quantity to create a new sale.
 * 
 * Key responsibilities:
 * - Validate incoming sale request data
 * - Ensure required fields are provided
 * - Prevent invalid quantities (negative or zero)
 * 
 * The sale process involves:
 * 1. Looking up the medicine by medicineId
 * 2. Validating stock availability
 * 3. Checking if medicine is expired
 * 4. Creating a sale record
 * 5. Automatically reducing the medicine stock
 */
@Getter // Lombok: Generates getter methods for all fields
@Setter // Lombok: Generates setter methods for all fields
@Builder // Lombok: Enables builder pattern for object construction
@NoArgsConstructor // Lombok: Generates no-argument constructor
@AllArgsConstructor // Lombok: Generates constructor with all fields
public class SaleRequestDTO {

    /**
     * ID of the medicine being sold.
     * 
     * Required field - must reference an existing medicine in the database.
     * This is the foreign key that links the sale to a specific medicine.
     * 
     * Validation:
     * - @NotNull: medicineId must be provided
     * - The referenced medicine must exist in the database
     * 
     * Example: 1 (refers to medicine with ID 1)
     */
    @NotNull(message = "Medicine ID is required")
    private Long medicineId;

    /**
     * Quantity of medicine units being sold.
     * 
     * Required field - must be a positive integer.
     * Represents how many units of the medicine the customer is purchasing.
     * 
     * Validation:
     * - @NotNull: Quantity must be provided
     * - @Positive: Quantity must be greater than 0 (can't sell 0 or negative)
     * 
     * Example: 2 (customer is buying 2 units)
     */
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}