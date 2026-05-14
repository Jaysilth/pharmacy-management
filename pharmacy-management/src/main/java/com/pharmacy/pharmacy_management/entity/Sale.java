package com.pharmacy.pharmacy_management.entity;

// Jakarta Persistence API imports for JPA annotations
// Used to map this class to a database table and define relationships
import jakarta.persistence.*;

// Lombok imports for automatic code generation
import lombok.*;

// Import Medicine entity for the ManyToOne relationship
import com.pharmacy.pharmacy_management.entity.Medicine;

// BigDecimal for precise financial calculations (prices, totals)
import java.math.BigDecimal;

// LocalDateTime for timestamp with both date and time
import java.time.LocalDateTime;

/**
 * Sale Entity - Represents a sales transaction in the pharmacy POS system.
 * 
 * This class is mapped to the "sales" database table using JPA.
 * Each instance represents a single sale transaction.
 * 
 * Key aspects:
 * - Links to a Medicine entity (Many-to-One relationship)
 * - Tracks quantity sold and pricing information
 * - Records timestamp of each transaction
 * - Automatically calculates total price
 * 
 * The Sale entity has a bidirectional relationship with Medicine:
 * - Each Sale references one Medicine (sold item)
 * - A Medicine can have multiple Sales (sold multiple times)
 */
@Entity // Marks this class as a JPA entity managed by the persistence context
@Table(name = "sales") // Maps to the "sales" database table
@Getter // Lombok: Generates getter methods for all fields
@Setter // Lombok: Generates setter methods for all fields
@Builder // Lombok: Enables builder pattern for object construction
@AllArgsConstructor // Lombok: Generates constructor with all fields
public class Sale {

    /**
     * Primary Key - Unique identifier for each sale transaction.
     * 
     * @Id marks this field as the primary key
     * @GeneratedValue with IDENTITY uses database auto-increment
     * Each sale gets a unique sequential ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many-to-One Relationship: The medicine that was sold.
     * 
     * Each sale is associated with one medicine being sold.
     * The @ManyToOne annotation defines this relationship:
     * - Many Sales can reference the same Medicine (multiple sales of same item)
     * - Each Sale has exactly one Medicine
     * 
     * @FetchType.LAZY means the Medicine is loaded lazily on demand
     * (not immediately when the Sale is loaded, saving database queries)
     * 
     * @JoinColumn specifies the foreign key column in the sales table
     * that references the medicines table
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    /**
     * Quantity of medicine units sold in this transaction.
     * 
     * Represents how many units of the medicine were purchased
     * in this single sale transaction.
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Unit price of the medicine at the time of sale.
     * 
     * This stores the price per unit at the moment of sale.
     * This is important because medicine prices may change over time,
     * and we need to preserve the price that was actually charged.
     * 
     * Uses BigDecimal for precise financial calculations.
     */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Total price for this sale transaction.
     * 
     * Calculated as: unitPrice × quantity
     * This is the total amount the customer paid.
     * 
     * Stored separately to preserve historical record even if
     * the medicine price changes later.
     */
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    /**
     * Timestamp when this sale was made.
     * 
     * Records the exact date and time of the transaction.
     * This is used for:
     * - Sales reports and analytics
     * - Order history
     * - Revenue calculations
     * - Audit trail
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Default no-argument constructor.
     * 
     * Required by JPA for creating entity instances.
     * JPA needs this to instantiate objects when loading from database.
     * Without this constructor, database queries would fail.
     */
    public Sale() {
    }

    /**
     * Lifecycle callback - Executed before the entity is first persisted.
     * 
     * Automatically sets the createdAt timestamp when a new sale is recorded.
     * This is called by JPA right before inserting the record into the database.
     */
    @PrePersist
    protected void onCreate() {
        // Set the sale timestamp to current date/time when transaction is created
        createdAt = LocalDateTime.now();
    }
}