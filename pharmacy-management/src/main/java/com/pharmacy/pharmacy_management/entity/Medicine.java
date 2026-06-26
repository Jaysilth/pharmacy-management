package com.pharmacy.pharmacy_management.entity;

// Jakarta Persistence API imports for JPA (Java Persistence API) annotations
// These annotations are used to map Java classes to database tables
import jakarta.persistence.*;

// Lombok imports - these annotations automatically generate boilerplate code
// - @Getter/@Setter: Generates getter and setter methods for all fields
// - @Builder: Implements the Builder pattern for object construction
// - @AllArgsConstructor: Generates a constructor with all fields as parameters
// - @NoArgsConstructor: Generates a no-argument constructor (required by JPA)
import lombok.*;

// Import for BigDecimal - precise decimal arithmetic for financial calculations
import java.math.BigDecimal;

// Import for LocalDate - handling dates (e.g., expiry dates, creation dates)
import java.time.LocalDate;

/**
 * Medicine Entity - Represents a medicine/product in the pharmacy inventory.
 * 
 * This class is mapped to the "medicines" database table using JPA annotations.
 * Each instance of this class represents a single row in the medicines table.
 * 
 * The entity uses Lombok annotations to automatically generate:
 * - Getters and setters for all fields
 * - A builder pattern for convenient object creation
 * - Constructors with all arguments and no arguments
 * 
 * Key features:
 * - Tracks medicine details (name, price, quantity, manufacturer)
 * - Automatic expiry date tracking
 * - Low stock threshold alerts
 * - Automatic timestamp management (createdAt, updatedAt)
 */
@Entity // Marks this class as a JPA entity - will be managed by the persistence context
@Table(name = "medicines") // Specifies the database table name for this entity
@Data // Lombok: Generates getters, setters, equals, hashCode, and toString methods
@Getter // Lombok: Generates getter methods for all fields
@Setter // Lombok: Generates setter methods for all fields
@Builder // Lombok: Enables builder pattern for object construction
@AllArgsConstructor // Lombok: Generates constructor with all fields
public class Medicine {

    /**
     * Primary Key - Unique identifier for each medicine.
     * 
     * @Id marks this field as the primary key of the entity.
     * @GeneratedValue with IDENTITY strategy uses auto-increment in the database.
     * This means the database automatically assigns a unique ID to each new record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the medicine.
     * 
     * @Column marks this field as a database column.
     * - nullable=false: This field cannot be null (required field)
     * - length=255: Maximum string length of 255 characters
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Current quantity/stock of the medicine in inventory.
     * 
     * Represents how many units of this medicine are currently available.
     * This value decreases when sales are made and increases when stock is added.
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Price per unit of the medicine.
     * 
     * Uses BigDecimal for precise financial calculations (avoids floating-point errors).
     * - precision=10: Total number of digits (both integer and decimal)
     * - scale=2: Number of digits after the decimal point
     * Example: 12345678.99 (10 digits total, 2 after decimal)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Expiry date of the medicine.
     * 
     * This field is crucial for pharmacy management to:
     * - Track expired medicines
     * - Prevent selling expired products
     * - Identify medicines nearing expiration
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    /**
     * Low stock threshold for alerts.
     * 
     * When the quantity falls below this threshold, the medicine is considered
     * to have low stock. This triggers alerts for inventory replenishment.
     * Default value is 10 units if not specified.
     */
    @Column(name = "low_stock_threshold")
    @Builder.Default
    private Integer lowStockThreshold = 10;

    /**
     * Description/details about the medicine.
     * 
     * Optional field that can contain additional information such as:
     * - Usage instructions
     * - Side effects
     * - Storage conditions
     * - Active ingredients
     */
    @Column(length = 500)
    private String description;

    /**
     * Manufacturer of the medicine.
     * 
     * Records the pharmaceutical company that produces this medicine.
     * Useful for tracking supplier information and quality control.
     */

    @Column(length = 20)
    private String category; // EYEDROP, TABLET, INJECTION, SYRUP

    @Column(name = "manufacturer", length = 255)
    private String manufacturer;

    /**
     * Timestamp when this medicine record was created.
     * 
     * Automatically set when the entity is first persisted to the database.
     * Used for audit purposes and tracking when medicines were added.
     */
    @Column(name = "created_at")
    private LocalDate createdAt;

    /**
     * Timestamp when this medicine record was last updated.
     * 
     * Automatically updated every time the entity is modified and saved.
     * Used for audit purposes and tracking recent changes.
     */
    @Column(name = "updated_at")
    private LocalDate updatedAt;

    /**
     * Default no-argument constructor.
     * 
     * Required by JPA for creating entity instances from database results.
     * JPA uses this constructor to instantiate objects when loading entities.
     * Without this, the application would fail at runtime when loading data.
     */
    public Medicine() {
    }

    /**
     * Lifecycle callback - Executed before the entity is first persisted.
     * 
     * This method is automatically called by JPA when a new record is inserted.
     * It sets the createdAt and updatedAt timestamps to the current date.
     * This ensures every record has proper audit timestamps.
     */
    @PrePersist
    protected void onCreate() {
        // Set creation timestamp to current date when the record is first created
        createdAt = LocalDate.now();
        // Also set updatedAt to current date (initially same as creation)
        updatedAt = LocalDate.now();
    }

    /**
     * Lifecycle callback - Executed when the entity is updated.
     * 
     * This method is automatically called by JPA when an existing record is updated.
     * It updates the updatedAt timestamp to the current date.
     * This ensures the modification date is always accurate.
     */
    @PreUpdate
    protected void onUpdate() {
        // Update the last modified timestamp to current date
        updatedAt = LocalDate.now();
    }

    /**
     * Check if the medicine has expired.
     * 
     * Compares the expiry date with the current date.
     * 
     * @return true if the medicine has expired (expiryDate is before today)
     *         false if the medicine is still valid or expiryDate is null
     */
    public boolean isExpired() {
        // Returns true if expiry date exists AND is before today's date
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Check if the medicine has low stock.
     * 
     * Compares the current quantity with the lowStockThreshold.
     * 
     * @return true if quantity is at or below the threshold
     *         false if quantity is above the threshold or quantity is null
     */
    public boolean isLowStock() {
        // Returns true if quantity exists AND is at or below the threshold
        return quantity != null && quantity <= lowStockThreshold;
    }
}
