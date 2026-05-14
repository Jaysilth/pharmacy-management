package com.pharmacy.pharmacy_management.repository;

// Import the Medicine entity that this repository manages
import com.pharmacy.pharmacy_management.entity.Medicine;

// Spring Data JPA - Repository interface for database operations
import org.springframework.data.jpa.repository.JpaRepository; // Provides basic CRUD operations
import org.springframework.data.jpa.repository.Query; // For custom JPQL queries
import org.springframework.stereotype.Repository; // Marks this as a Spring bean

// Import for LocalDate - used in date-based queries
import java.time.LocalDate;

// Import for List - collection type for multiple results
import java.util.List;

/**
 * MedicineRepository - Data Access Object (DAO) for Medicine entities.
 * 
 * This interface extends JpaRepository, which provides standard CRUD operations
 * (Create, Read, Update, Delete) for the Medicine entity without requiring
 * implementation code. Spring Data JPA automatically generates the implementation.
 * 
 * Key responsibilities:
 * - Provide database access methods for Medicine entity
 * - Define custom queries for complex searches
 * - Act as the bridge between the service layer and database
 * 
 * How it works:
 * - Spring Data JPA creates a proxy implementation at runtime
 * - Method names are parsed to generate appropriate SQL queries
 * - @Query annotation allows custom JPQL (Java Persistence Query Language) queries
 * 
 * @param <Medicine> The entity type this repository manages
 * @param <Long> The type of the entity's primary key
 */
@Repository // Marks this interface as a Spring Data repository component
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    /**
     * Search medicines by name (case-insensitive partial match).
     * 
     * This method uses Spring Data JPA's query derivation mechanism.
     * The method name "findByNameContainingIgnoreCase" is parsed to generate:
     * SELECT m FROM Medicine m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))
     * 
     * Features:
     * - Partial match: Finds medicines containing the search term
     * - Case-insensitive: "paracetamol" matches "Paracetamol"
     * 
     * @param name The search term to match against medicine names
     * @return List of medicines whose names contain the search term (case-insensitive)
     * 
     * Example:
     * - findByNameContainingIgnoreCase("para") returns medicines with "para" in name
     * - Returns empty list if no matches found
     */
    List<Medicine> findByNameContainingIgnoreCase(String name);

    /**
     * Find all medicines that have expired before a specific date.
     * 
     * This is a custom JPQL query (not derived from method name).
     * JPQL (Java Persistence Query Language) is similar to SQL but operates on entities.
     * 
     * The query:
     * - SELECT m FROM Medicine m: Select all Medicine entities
     * - WHERE m.expiryDate < :date: Where expiry date is before the given date
     * 
     * @param date The date to compare against (typically today's date)
     * @return List of medicines that have expired before the given date
     * 
     * Example:
     * - findExpiredMedicines(LocalDate.now()) returns all expired medicines
     */
    @Query("SELECT m FROM Medicine m WHERE m.expiryDate < :date")
    List<Medicine> findExpiredMedicines(LocalDate date);

    /**
     * Find all medicines that will expire before a specific date.
     * 
     * This is essentially the same as findExpiredMedicines() - another query
     * for finding expired medicines. Having multiple similar methods provides
     * flexibility in naming and potential future customization.
     * 
     * @param date The date to compare against
     * @return List of medicines expiring before the given date
     */
    @Query("SELECT m FROM Medicine m WHERE m.expiryDate < :date")
    List<Medicine> findExpiredMedicinesBefore(LocalDate date);

    /**
     * Find all medicines with low stock (quantity at or below threshold).
     * 
     * This custom query finds medicines that need restocking.
     * It compares the current quantity against the lowStockThreshold.
     * 
     * The query:
     * - SELECT m FROM Medicine m: Select all medicines
     * - WHERE m.quantity <= m.lowStockThreshold: Where stock is at or below threshold
     * 
     * @return List of medicines with low stock levels
     * 
     * Example:
     * - If a medicine has quantity=5 and lowStockThreshold=10, it appears in results
     * - If quantity=15 and lowStockThreshold=10, it does NOT appear
     */
    @Query("SELECT m FROM Medicine m WHERE m.quantity <= m.lowStockThreshold")
    List<Medicine> findLowStockMedicines();

    /**
     * Check if a medicine with the given name already exists.
     * 
     * This is useful for validation before creating a new medicine
     * to prevent duplicate entries.
     * 
     * @param name The name to check for existence
     * @return true if a medicine with this name exists, false otherwise
     * 
     * Example:
     * - existsByName("Paracetamol 500mg") returns true if medicine exists
     */
    boolean existsByName(String name);
}