package com.pharmacy.pharmacy_management.repository;

// Import the Sale entity that this repository manages
import com.pharmacy.pharmacy_management.entity.Sale;

// Spring Data JPA - Provides standard database operations
import org.springframework.data.jpa.repository.JpaRepository; // Basic CRUD operations
import org.springframework.data.jpa.repository.Query; // Custom JPQL queries
import org.springframework.stereotype.Repository; // Marks as Spring bean

// BigDecimal for financial calculations
import java.math.BigDecimal;

// LocalDateTime for date/time queries
import java.time.LocalDateTime;

// List for multiple results
import java.util.List;

/**
 * SaleRepository - Data Access Object (DAO) for Sale entities.
 * 
 * This interface extends JpaRepository to provide standard CRUD operations
 * for Sale entities. It also includes custom queries for sales analytics
 * and reporting features.
 * 
 * Key responsibilities:
 * - Basic CRUD operations for Sale entity
 * - Query sales by date range
 * - Calculate total revenue
 * - Find top selling medicines
 * - Count sales by medicine
 * 
 * The repository provides data access for:
 * - Recording new sales
 * - Retrieving sales history
 * - Revenue calculations
 * - Business analytics
 * 
 * @param <Sale> The entity type this repository manages
 * @param <Long> The type of the entity's primary key
 */
@Repository // Marks this interface as a Spring Data repository component
public interface SaleRepository extends JpaRepository<Sale, Long> {

    /**
     * Get all sales ordered by creation date (newest first).
     * 
     * This is a derived query method. Spring Data JPA derives the query
     * from the method name "findAllByOrderByCreatedAtDesc".
     * 
     * @return List of all sales, sorted by createdAt in descending order
     * 
     * Example:
     * - Returns most recent sale first
     * - Useful for displaying sales history to users
     */
    List<Sale> findAllByOrderByCreatedAtDesc();

    /**
     * Find sales within a specific date range.
     * 
     * Custom JPQL query that retrieves sales made between two dates.
     * Results are ordered by creation date (newest first).
     * 
     * The query:
     * - SELECT s FROM Sale s: Select all Sale entities
     * - WHERE s.createdAt BETWEEN :startDate AND :endDate: Date range filter
     * - ORDER BY s.createdAt DESC: Sort by date, newest first
     * 
     * @param startDate The start of the date range (inclusive)
     * @param endDate The end of the date range (inclusive)
     * @return List of sales within the specified date range
     * 
     * Example:
     * - findSalesByDateRange(2024-01-01T00:00:00, 2024-01-31T23:59:59)
     * - Returns all sales made in January 2024
     */
    @Query("SELECT s FROM Sale s WHERE s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    List<Sale> findSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Calculate total revenue from all sales.
     * 
     * This is an aggregate query that sums up all totalPrice values.
     * Returns BigDecimal for precise financial calculations.
     * 
     * The query:
     * - SELECT SUM(s.totalPrice): Calculate sum of all totalPrice fields
     * - FROM Sale s: From the sales table
     * 
     * @return BigDecimal representing total revenue, or null if no sales exist
     * 
     * Note: Returns null when there are no sales (not 0).
     * The service layer handles converting null to BigDecimal.ZERO.
     * 
     * Example:
     * - If there are 3 sales: $10 + $20 + $15 = $45
     * - Returns 45.00 as BigDecimal
     */
    @Query("SELECT SUM(s.totalPrice) FROM Sale s")
    BigDecimal getTotalRevenue();

    /**
     * Calculate total revenue within a specific date range.
     * 
     * Similar to getTotalRevenue() but filtered by date range.
     * Useful for generating period reports (daily, weekly, monthly revenue).
     * 
     * @param startDate Start of the period (inclusive)
     * @param endDate End of the period (inclusive)
     * @return BigDecimal representing revenue in the period, or null if none
     * 
     * Example:
     * - getTotalRevenueByDateRange(2024-01-01T00:00:00, 2024-01-31T23:59:59)
     * - Returns total revenue for January 2024
     */
    @Query("SELECT SUM(s.totalPrice) FROM Sale s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count how many times a specific medicine has been sold.
     * 
     * Returns the total number of sale transactions that include
     * the specified medicine (regardless of quantity per sale).
     * 
     * @param medicineId The ID of the medicine to count sales for
     * @return Long representing the number of sale transactions
     * 
     * Example:
     * - countSalesByMedicineId(1)
     * - Returns 5 if medicine ID 1 was sold in 5 different transactions
     */
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.medicine.id = :medicineId")
    Long countSalesByMedicineId(Long medicineId);

    /**
     * Find the top selling medicines by number of sales.
     * 
     * This is a complex aggregation query that:
     * 1. Groups sales by medicine
     * 2. Counts sales per medicine
     * 3. Orders by count descending (highest first)
     * 
     * Returns results as Object[] arrays containing:
     * - [0]: medicine ID (Long)
     * - [1]: medicine name (String)
     * - [2]: sale count (BigDecimal/Long)
     * 
     * The query:
     * - SELECT s.medicine.id, s.medicine.name, COUNT(s): Select fields
     * - FROM Sale s: From sales table
     * - GROUP BY s.medicine.id, s.medicine.name: Group by medicine
     * - ORDER BY saleCount DESC: Sort by count, highest first
     * 
     * @return List of Object arrays with medicine info and sale counts
     * 
     * Example usage in service:
     * List<Object[]> results = repository.findTopSellingMedicines();
     * for (Object[] row : results) {
     *     Long medicineId = (Long) row[0];
     *     String name = (String) row[1];
     *     Long count = (Long) row[2];
     * }
     */
    @Query("SELECT s.medicine.id, s.medicine.name, COUNT(s) as saleCount FROM Sale s GROUP BY s.medicine.id, s.medicine.name ORDER BY saleCount DESC")
    List<Object[]> findTopSellingMedicines();
}