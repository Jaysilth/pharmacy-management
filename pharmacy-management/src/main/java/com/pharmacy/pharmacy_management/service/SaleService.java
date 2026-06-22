package com.pharmacy.pharmacy_management.service;

// Import DTOs for request and response handling
import com.pharmacy.pharmacy_management.dto.SaleRequestDTO; // Request data from clients
import com.pharmacy.pharmacy_management.dto.SaleResponseDTO; // Response data to clients

// Import entities for database operations
import com.pharmacy.pharmacy_management.entity.Medicine; // Medicine entity
import com.pharmacy.pharmacy_management.entity.Sale; // Sale entity

// Import custom exceptions for error handling
import com.pharmacy.pharmacy_management.exception.InsufficientStockException; // When stock too low
import com.pharmacy.pharmacy_management.exception.MedicineNotFoundException; // When medicine doesn't exist

// Import repositories for database access
import com.pharmacy.pharmacy_management.repository.MedicineRepository; // Medicine database access
import com.pharmacy.pharmacy_management.repository.SaleRepository; // Sale database access

// Lombok - Reduces boilerplate code
import lombok.RequiredArgsConstructor; // Generates constructor for dependency injection

// Spring annotations
import org.springframework.stereotype.Service; // Marks as Spring service component
import org.springframework.transaction.annotation.Transactional; // Manages database transactions

// Java utilities
import com.pharmacy.pharmacy_management.dto.SalesByDayDTO;
import java.math.BigDecimal; // For precise financial calculations
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List; // For collections of sales
import java.util.Map;
import java.util.stream.Collectors; // For stream operations

/**
 * SaleService - Business logic layer for Sale (POS) operations.
 * 
 * This service handles all sales-related business logic in the pharmacy
 * Point of Sale (POS) system. It manages the sales transaction lifecycle
 * including validation, pricing, and automatic stock deduction.
 * 
 * Key responsibilities:
 * - Process new sales (create sale records)
 * - Validate sales (check medicine exists, stock available, not expired)
 * - Automatically reduce medicine stock after sale
 * - Calculate pricing (unit price, total price)
 * - Retrieve sales history and revenue data
 * 
 * Business flow for creating a sale:
 * 1. Validate medicine exists
 * 2. Check medicine is not expired
 * 3. Check sufficient stock available
 * 4. Calculate total price
 * 5. Create sale record
 * 6. Reduce medicine stock (automatically)
 * 7. Return sale confirmation
 * 
 * Design patterns:
 * - Service Layer Pattern: Contains business logic
 * - DTO Pattern: Converts between entities and API responses
 * - Transaction Management: Ensures atomic operations
 */
@Service // Marks this class as a Spring service bean
@RequiredArgsConstructor // Generates constructor with final fields for dependency injection
@Transactional // All methods in this class are transactional by default
public class SaleService {

    /**
     * Repository for Sale database operations.
     * 
     * Provides methods to:
     * - Save new sales
     * - Find all sales
     * - Calculate revenue
     * - Query sales by various criteria
     */
    private final SaleRepository saleRepository;

    /**
     * Repository for Medicine database operations.
     * 
     * Used to:
     * - Look up medicine by ID
     * - Access medicine details (price, stock, expiry)
     * - Update medicine stock after sales
     */
    private final MedicineRepository medicineRepository;

    /**
     * Service for medicine-related operations.
     * 
     * Used to call reduceStock() method when a sale is made.
     * This ensures stock is properly managed through the MedicineService.
     */
    private final MedicineService medicineService;

    /**
     * Create a new sale transaction.
     * 
     * This is the main entry point for processing a sale in the pharmacy POS.
     * It performs several validations and operations in sequence:
     * 
     * 1. Find the medicine being sold
     * 2. Validate medicine is not expired
     * 3. Validate sufficient stock exists
     * 4. Calculate total price (unitPrice × quantity)
     * 5. Create and save the sale record
     * 6. Automatically reduce the medicine stock
     * 7. Return the sale confirmation
     * 
     * @param requestDTO Contains medicineId and quantity for the sale
     * @return SaleResponseDTO The created sale with all details
     * @throws MedicineNotFoundException If the medicine ID doesn't exist
     * @throws InsufficientStockException If requested quantity exceeds available stock
     * @throws IllegalStateException If the medicine has expired
     */
    public SaleResponseDTO createSale(SaleRequestDTO requestDTO) {
        // Step 1: Get medicine from database
        // Find the medicine by ID from the request, or throw exception if not found
        Medicine medicine = medicineRepository.findById(requestDTO.getMedicineId())
                .orElseThrow(() -> new MedicineNotFoundException(
                        "Medicine not found with id: " + requestDTO.getMedicineId()));

        // Step 2: Check if medicine is expired
        // Use the entity's isExpired() method to check expiry status
        if (medicine.isExpired()) {
            // Cannot sell expired medicines - throw exception
            throw new IllegalStateException("Cannot sell expired medicine: " + medicine.getName());
        }

        // Step 3: Validate stock availability (stock validation)
        // Check if there's enough quantity available for the sale
        if (medicine.getQuantity() < requestDTO.getQuantity()) {
            // Throw exception with available vs requested quantities
            throw new InsufficientStockException(
                    String.format("Insufficient stock for %s. Available: %d, Requested: %d",
                            medicine.getName(), medicine.getQuantity(), requestDTO.getQuantity()));
        }

        // Step 4: Calculate total price
        // Get the current unit price from the medicine
        BigDecimal unitPrice = medicine.getPrice();
        // Calculate: unitPrice × quantity
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(requestDTO.getQuantity()));

        // Step 5: Create sale record
        // Use builder pattern to create Sale entity with all required fields
        Sale sale = Sale.builder()
                .medicine(medicine) // Link to the medicine being sold
                .quantity(requestDTO.getQuantity()) // Quantity sold
                .unitPrice(unitPrice) // Price at time of sale (preserves historical price)
                .totalPrice(totalPrice) // Total amount charged
                .build();

        // Step 6: Save sale first
        // This ensures the sale is recorded before stock is reduced
        // The @PrePersist hook will set the createdAt timestamp
        Sale savedSale = saleRepository.save(sale);

        // Step 7: Automatically reduce stock (stock deduction)
        // After successfully creating the sale, reduce the medicine quantity
        // This calls MedicineService which handles the stock reduction
        // and saves the updated medicine to the database
        medicineService.reduceStock(medicine.getId(), requestDTO.getQuantity());

        // Step 8: Map to response DTO and return
        // Convert the saved Sale entity to a response DTO for the client
        return mapToResponseDTO(savedSale);
    }

    /**
     * Get all sales, ordered by date (newest first).
     * 
     * Retrieves the complete sales history for the pharmacy.
     * Results are sorted by creation date in descending order.
     * 
     * Note: Read-only transaction for better performance.
     * 
     * @return List<SaleResponseDTO> List of all sales, newest first
     */
    @Transactional(readOnly = true) // Read-only transaction
    public List<SaleResponseDTO> getAllSales() {
        // Use repository method that returns sales sorted by date descending
        return saleRepository.findAllByOrderByCreatedAtDesc()
                .stream() // Convert to stream for processing
                .map(this::mapToResponseDTO) // Convert each Sale to SaleResponseDTO
                .collect(Collectors.toList()); // Collect back to List
    }

    /**
     * Get the number of sales that happened today.
     *
     * @return total number of sales for the current day
     */
    @Transactional(readOnly = true)
    public int getTotalSalesToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return (int) saleRepository.countByCreatedAtBetween(startOfDay, endOfDay);
    }

    /**
     * Get total revenue for today.
     *
     * @return total revenue generated today
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenueToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        BigDecimal revenue = saleRepository.getTotalRevenueByDateRange(startOfDay, endOfDay);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    /**
     * Get revenue totals grouped by day for the last N days.
     *
     * @param days number of days to include, including today
     * @return list of revenue totals ordered by date ascending
     */
    @Transactional(readOnly = true)
    public List<SalesByDayDTO> getSalesByDay(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Map<LocalDate, BigDecimal> totals = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            totals.put(startDate.plusDays(i), BigDecimal.ZERO);
        }

        saleRepository.findSalesByDateRange(startDateTime, endDateTime).forEach(sale -> {
            LocalDate saleDate = sale.getCreatedAt().toLocalDate();
            totals.compute(saleDate, (key, existing) -> existing == null ? sale.getTotalPrice() : existing.add(sale.getTotalPrice()));
        });

        return totals.entrySet().stream()
                .map(entry -> SalesByDayDTO.builder()
                        .date(entry.getKey().toString())
                        .totalRevenue(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get the most recent sales transactions.
     *
     * @return latest five sales sorted by date descending
     */
    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getRecentSales() {
        return saleRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific sale by its ID.
     * 
     * Retrieves details of a single sale transaction.
     * 
     * @param id The unique identifier of the sale
     * @return SaleResponseDTO The sale details
     * @throws RuntimeException If no sale exists with the given ID
     */
    @Transactional(readOnly = true)
    public SaleResponseDTO getSaleById(Long id) {
        // Find sale by ID, throw exception if not found
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found with id: " + id));
        return mapToResponseDTO(sale);
    }

    /**
     * Calculate total revenue from all sales.
     * 
     * Sums up the totalPrice of all sales in the system.
     * Useful for business reporting and analytics.
     * 
     * Note: Returns BigDecimal.ZERO if there are no sales
     * (handles null from the aggregate query).
     * 
     * @return BigDecimal Total revenue from all sales
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        // Call repository to get sum of all totalPrice values
        BigDecimal revenue = saleRepository.getTotalRevenue();
        // Return zero if null (no sales exist), otherwise return the sum
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    /**
     * Map a Sale entity to a SaleResponseDTO.
     * 
     * Private helper method to convert internal Sale entity
     * to the API response format.
     * 
     * Creates a nested MedicineInfo object with essential
     * medicine details for the response.
     * 
     * @param sale The Sale entity to convert
     * @return SaleResponseDTO The converted response DTO
     */
    private SaleResponseDTO mapToResponseDTO(Sale sale) {
        return SaleResponseDTO.builder()
                .id(sale.getId()) // Sale ID
                // Build nested MedicineInfo with basic medicine details
                .medicine(SaleResponseDTO.MedicineInfo.builder()
                        .id(sale.getMedicine().getId()) // Medicine ID
                        .name(sale.getMedicine().getName()) // Medicine name
                        .manufacturer(sale.getMedicine().getManufacturer()) // Manufacturer
                        .build())
                .quantity(sale.getQuantity()) // Quantity sold
                .unitPrice(sale.getUnitPrice()) // Unit price at time of sale
                .totalPrice(sale.getTotalPrice()) // Total price charged
                .createdAt(sale.getCreatedAt()) // Sale timestamp
                .build();
    }
}
