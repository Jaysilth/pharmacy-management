package com.pharmacy.pharmacy_management.dto;

// Lombok imports for automatic code generation
import lombok.*;

// BigDecimal for precise financial calculations
import java.math.BigDecimal;

// LocalDateTime for timestamp with date and time
import java.time.LocalDateTime;

/**
 * SaleResponseDTO - Data Transfer Object for returning sale data to clients.
 * 
 * This DTO is used when sending sale transaction data back to clients
 * in HTTP responses. It provides a complete view of a sale transaction.
 * 
 * Key responsibilities:
 * - Provide sale transaction details to clients
 * - Include nested medicine information (without exposing all medicine fields)
 * - Include pricing and quantity details
 * - Show when the sale was made
 * 
 * Note: This uses a nested static class (MedicineInfo) to provide only
 * essential medicine details to clients, rather than exposing the full
 * Medicine entity which might include sensitive internal data.
 */
@Getter // Lombok: Generates getter methods for all fields
@Setter // Lombok: Generates setter methods for all fields
@Builder // Lombok: Enables builder pattern for object construction
@NoArgsConstructor // Lombok: Generates no-argument constructor
@AllArgsConstructor // Lombok: Generates constructor with all fields
public class SaleResponseDTO {

    /**
     * Unique identifier of the sale transaction.
     * 
     * This is the primary key from the database.
     * Assigned automatically when the sale is recorded.
     */
    private Long id;

    /**
     * Nested object containing basic medicine information.
     * 
     * Instead of returning the full Medicine entity (which could expose
     * internal fields like cost, supplier, etc.), we return only essential
     * information that the client needs to see:
     * - id: For reference
     * - name: Display name of the medicine
     * - manufacturer: Source of the medicine
     * 
     * This follows the "Data Transfer Object" pattern where we tailor
     * the response to what the client actually needs.
     */
    private MedicineInfo medicine;

    /**
     * Quantity of medicine units sold.
     * 
     * The number of units purchased in this transaction.
     * This is the same value that was in the request, preserved for reference.
     */
    private Integer quantity;

    /**
     * Unit price at the time of sale.
     * 
     * The price per unit that was charged to the customer.
     * This is captured at the time of sale to preserve historical accuracy,
     * even if the medicine's current price changes later.
     */
    private BigDecimal unitPrice;

    /**
     * Total price for this sale.
     * 
     * Calculated as: unitPrice × quantity
     * This is the total amount the customer paid.
     */
    private BigDecimal totalPrice;

    /**
     * Timestamp when the sale was made.
     * 
     * Records the exact date and time the transaction occurred.
     * Useful for:
     * - Sales reports by date
     * - Order history for customers
     * - Business analytics
     */
    private LocalDateTime createdAt;

    /**
     * MedicineInfo - Nested static class for medicine details in response.
     * 
     * This is a nested DTO class that provides only essential medicine
     * information to clients. It keeps the response clean and prevents
     * exposure of internal implementation details.
     * 
     * Using a static inner class allows us to:
     * - Keep related DTOs together in one file
     * - Share the package without creating separate files
     * - Provide tailored data for the response
     */
    @Data // Lombok: Generates getters, setters, equals, hashCode, toString
    @Builder // Lombok: Enables builder pattern
    @NoArgsConstructor // Lombok: Generates no-argument constructor
    @AllArgsConstructor // Lombok: Generates constructor with all fields
    public static class MedicineInfo {
        
        /**
         * ID of the medicine.
         * 
         * The unique identifier from the database.
         * Useful for clients to reference the medicine in future requests.
         */
        private Long id;

        /**
         * Name of the medicine.
         * 
         * The display name (e.g., "Paracetamol 500mg").
         * What the customer sees and recognizes.
         */
        private String name;

        /**
         * Manufacturer of the medicine.
         * 
         * The pharmaceutical company that produces this medicine.
         * Useful for customers who have brand preferences.
         */
        private String manufacturer;
    }
}