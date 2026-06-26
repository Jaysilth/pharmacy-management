package com.pharmacy.pharmacy_management.dto;

// Lombok imports for automatic code generation
import lombok.*;

// BigDecimal for precise financial calculations
import java.math.BigDecimal;

// LocalDate for date handling
import java.time.LocalDate;

/**
 * MedicineResponseDTO - Data Transfer Object for returning medicine data to clients.
 * 
 * This DTO is used when sending medicine data back to clients in HTTP responses.
 * It's a "response" object that carries data from the server to the client.
 * 
 * Key responsibilities:
 * - Provide a consistent structure for medicine data in API responses
 * - Include computed fields like isExpired and isLowStock
 * - Include audit timestamps (createdAt, updatedAt)
 * - Decouple the API response from internal entity structure
 * 
 * Note: This class doesn't have validation annotations because it's used
 * for output (responses), not input (requests). Validation is done on
 * the Request DTOs, not Response DTOs.
 */
@Getter // Lombok: Generates getter methods for all fields
@Setter // Lombok: Generates setter methods for all fields
@Builder // Lombok: Enables builder pattern for object construction
@NoArgsConstructor // Lombok: Generates no-argument constructor
@AllArgsConstructor // Lombok: Generates constructor with all fields
public class MedicineResponseDTO {

    /**
     * Unique identifier of the medicine.
     * 
     * This is the primary key from the database.
     * Assigned automatically when the medicine is created.
     */
    private Long id;

    /**
     * Name of the medicine.
     * 
     * The display name of the medicine (e.g., "Paracetamol 500mg").
     */
    private String name;

    /**
     * Current quantity/stock of the medicine.
     * 
     * The number of units currently available in inventory.
     * This value changes as sales are made and stock is added.
     */
    private Integer quantity;

    /**
     * Price per unit of the medicine.
     * 
     * The current price per unit. This is what customers pay.
     */
    private BigDecimal price;

    /**
     * Expiry date of the medicine.
     * 
     * The date after which the medicine should not be sold.
     * Used to check if medicine is expired.
     */
    private LocalDate expiryDate;

    /**
     * Low stock threshold for alerts.
     * 
     * The quantity level that triggers a low stock alert.
     * If quantity <= lowStockThreshold, the medicine needs restocking.
     */
    private Integer lowStockThreshold;

    /**
     * Description of the medicine.
     * 
     * Additional information about the medicine such as
     * usage instructions, side effects, or storage requirements.
     */
    private String description;

    /**
     * Manufacturer of the medicine.
     * 
     * The pharmaceutical company that produces this medicine.
     */



    private String manufacturer;
    private String category;

    /**
     * Timestamp when the medicine record was created.
     * 
     * Automatically set when the medicine is first added to the system.
     * Useful for auditing and knowing the age of inventory.
     */
    private LocalDate createdAt;

    /**
     * Timestamp when the medicine record was last updated.
     * 
     * Automatically updated every time the medicine data is modified.
     * Useful for tracking recent changes.
     */
    private LocalDate updatedAt;

    /**
     * Computed field: Whether the medicine has expired.
     * 
     * This is calculated from the expiryDate field.
     * true if the medicine's expiry date is before today
     * false otherwise
     * 
     * Note: This is a computed/derived field, not stored in database.
     * It's calculated on-the-fly when building the response DTO.
     */
    private Boolean isExpired;

    /**
     * Computed field: Whether the medicine has low stock.
     * 
     * This is calculated by comparing quantity to lowStockThreshold.
     * true if quantity <= lowStockThreshold
     * false otherwise
     * 
     * Note: This is a computed/derived field, not stored in database.
     * It's calculated on-the-fly when building the response DTO.
     */
    private Boolean isLowStock;
}