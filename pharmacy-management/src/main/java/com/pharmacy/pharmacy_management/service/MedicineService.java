package com.pharmacy.pharmacy_management.service;

// Import DTOs for request and response handling
import com.pharmacy.pharmacy_management.dto.MedicineRequestDTO; // Request data from clients
import com.pharmacy.pharmacy_management.dto.MedicineResponseDTO; // Response data to clients

// Import the Medicine entity (JPA entity mapped to database)
import com.pharmacy.pharmacy_management.entity.Medicine;

// Import custom exceptions for error handling
import com.pharmacy.pharmacy_management.exception.InsufficientStockException; // When stock is too low
import com.pharmacy.pharmacy_management.exception.MedicineNotFoundException; // When medicine doesn't exist

// Import the repository for database operations
import com.pharmacy.pharmacy_management.repository.MedicineRepository;

// Lombok - Reduces boilerplate code
import lombok.RequiredArgsConstructor; // Generates constructor with required (final) fields

// Spring annotations
import org.springframework.stereotype.Service; // Marks this as a Spring service component
import org.springframework.transaction.annotation.Transactional; // Manages database transactions

// Java utilities
import java.time.LocalDate; // For date handling
import java.util.List; // For collections of medicines
import java.util.stream.Collectors; // For stream operations

/**
 * MedicineService - Business logic layer for Medicine operations.
 * 
 * This service class contains all business logic related to medicines.
 * It acts as an intermediary between the Controller (API layer) and
 * the Repository (Database layer).
 * 
 * Key responsibilities:
 * - Add new medicines to inventory
 * - Retrieve medicine information (all, by ID, by search criteria)
 * - Update existing medicine details
 * - Delete medicines from inventory
 * - Manage stock (add/reduce quantities)
 * - Check for expired and low-stock medicines
 * 
 * Design patterns used:
 * - Service Layer Pattern: Contains business logic
 * - DTO Pattern: Converts between entities and API responses
 * - Transaction Management: Ensures data consistency
 * 
 * Annotations:
 * - @Service: Marks as a Spring-managed service component
 * - @RequiredArgsConstructor: Generates constructor for dependency injection
 * - @Transactional: Ensures all operations are atomic (all-or-nothing)
 */
@Service // Marks this class as a Spring service bean
@RequiredArgsConstructor // Generates constructor with final fields for dependency injection
@Transactional // All methods in this class are transactional by default
public class MedicineService {

    /**
     * Repository for database operations on Medicine entities.
     * 
     * Injected via constructor by Lombok's @RequiredArgsConstructor.
     * Provides methods like save(), findAll(), findById(), deleteById(), etc.
     * 
     * This is a dependency that the service needs to:
     * - Save new medicines to the database
     * - Retrieve medicines from the database
     * - Update existing medicine records
     * - Delete medicines from the database
     */
    private final MedicineRepository medicineRepository;

    /**
     * Add a new medicine to the inventory.
     * 
     * This method creates a new medicine record in the database.
     * It converts the request DTO to an entity, saves it, and returns
     * a response DTO with the saved data.
     * 
     * Business logic:
     * - Sets default lowStockThreshold to 10 if not provided
     * - Converts request DTO to entity using builder pattern
     * - Saves entity to database (JPA handles INSERT)
     * - Converts saved entity back to response DTO
     * 
     * @param requestDTO The medicine data from the client request
     * @return MedicineResponseDTO The created medicine with generated ID and timestamps
     * 
     * Example:
     * POST /api/medicines with {name: "Paracetamol", quantity: 100, ...}
     * Returns: {id: 1, name: "Paracetamol", quantity: 100, ...}
     */
    public MedicineResponseDTO addMedicine(MedicineRequestDTO requestDTO) {
        // Use Builder pattern to create Medicine entity from request DTO
        // This is a clean way to construct objects with named parameters
        Medicine medicine = Medicine.builder()
                .name(requestDTO.getName()) // Set medicine name from request
                .quantity(requestDTO.getQuantity()) // Set initial quantity
                .price(requestDTO.getPrice()) // Set unit price
                .expiryDate(requestDTO.getExpiryDate()) // Set expiry date
                // If lowStockThreshold is provided, use it; otherwise default to 10
                .lowStockThreshold(requestDTO.getLowStockThreshold() != null ? requestDTO.getLowStockThreshold() : 10)
                .description(requestDTO.getDescription()) // Optional description
                .manufacturer(requestDTO.getManufacturer()) // Optional manufacturer
                .build();

        // Save the medicine to the database
        // JPA generates the INSERT statement and executes it
        // The returned medicine has the generated ID assigned
        Medicine savedMedicine = medicineRepository.save(medicine);

        // Convert the saved entity to response DTO and return
        return mapToResponseDTO(savedMedicine);
    }

    /**
     * Get all medicines from the inventory.
     * 
     * Retrieves all medicine records from the database.
     * Results are converted to response DTOs for the API.
     * 
     * Note: This method is read-only (uses @Transactional(readOnly = true))
     * which provides performance benefits and ensures no accidental modifications.
     * 
     * @return List<MedicineResponseDTO> List of all medicines in inventory
     */
    @Transactional(readOnly = true) // Read-only transaction for better performance
    public List<MedicineResponseDTO> getAllMedicines() {
        // findAll() returns all records from the medicines table
        // Stream through results and convert each to response DTO
        return medicineRepository.findAll()
                .stream() // Convert list to stream for processing
                .map(this::mapToResponseDTO) // Convert each entity to DTO
                .collect(Collectors.toList()); // Collect back to list
    }

    /**
     * Get a specific medicine by its ID.
     * 
     * Looks up a single medicine by its primary key.
     * Throws an exception if the medicine doesn't exist.
     * 
     * @param id The unique identifier of the medicine
     * @return MedicineResponseDTO The requested medicine
     * @throws MedicineNotFoundException If no medicine exists with the given ID
     */
    @Transactional(readOnly = true)
    public MedicineResponseDTO getMedicineById(Long id) {
        // findById returns Optional<Medicine>
        // orElseThrow() throws the exception if not found
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with id: " + id));
        return mapToResponseDTO(medicine);
    }

    /**
     * Get all expired medicines.
     * 
     * Finds medicines whose expiry date is before today.
     * Uses a custom repository query for this.
     * 
     * @return List<MedicineResponseDTO> List of expired medicines
     */
    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> getExpiredMedicines() {
        // Use repository method to find expired medicines
        // Pass LocalDate.now() as the current date
        return medicineRepository.findExpiredMedicines(LocalDate.now())
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all medicines with low stock.
     * 
     * Finds medicines where quantity is at or below the lowStockThreshold.
     * Useful for inventory management and restocking alerts.
     * 
     * @return List<MedicineResponseDTO> List of medicines with low stock
     */
    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> getLowStockMedicines() {
        // Use repository method to find low stock medicines
        return medicineRepository.findLowStockMedicines()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search medicines by name.
     * 
     * Performs a case-insensitive partial match search on medicine names.
     * Uses the repository's findByNameContainingIgnoreCase method.
     * 
     * @param name The search term to match against medicine names
     * @return List<MedicineResponseDTO> List of matching medicines
     */
    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> searchMedicines(String name) {
        // Search for medicines containing the name (case-insensitive)
        return medicineRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing medicine.
     * 
     * Updates all fields of an existing medicine record.
     * Only updates the lowStockThreshold if provided (to preserve existing value).
     * 
     * @param id The ID of the medicine to update
     * @param requestDTO The new data for the medicine
     * @return MedicineResponseDTO The updated medicine
     * @throws MedicineNotFoundException If no medicine exists with the given ID
     */
    public MedicineResponseDTO updateMedicine(Long id, MedicineRequestDTO requestDTO) {
        // First, find the existing medicine or throw exception
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with id: " + id));

        // Update all the fields with new values from request
        medicine.setName(requestDTO.getName());
        medicine.setQuantity(requestDTO.getQuantity());
        medicine.setPrice(requestDTO.getPrice());
        medicine.setExpiryDate(requestDTO.getExpiryDate());
        
        // Only update lowStockThreshold if provided in request
        // This preserves the existing threshold if not explicitly changed
        if (requestDTO.getLowStockThreshold() != null) {
            medicine.setLowStockThreshold(requestDTO.getLowStockThreshold());
        }
        
        medicine.setDescription(requestDTO.getDescription());
        medicine.setManufacturer(requestDTO.getManufacturer());

        // Save the updated entity (JPA handles UPDATE)
        Medicine updatedMedicine = medicineRepository.save(medicine);
        
        // Convert to response DTO and return
        return mapToResponseDTO(updatedMedicine);
    }

    /**
     * Delete a medicine from the inventory.
     * 
     * Removes a medicine record from the database.
     * 
     * @param id The ID of the medicine to delete
     * @throws MedicineNotFoundException If no medicine exists with the given ID
     */
    public void deleteMedicine(Long id) {
        // Check if medicine exists before attempting delete
        if (!medicineRepository.existsById(id)) {
            throw new MedicineNotFoundException("Medicine not found with id: " + id);
        }
        // deleteById performs the DELETE operation
        // If ID doesn't exist, JPA throws EntityNotFoundException
        // But we check first to provide a better error message
        medicineRepository.deleteById(id);
    }

    /**
     * Reduce the stock quantity of a medicine.
     * 
     * This is typically called when a sale is made.
     * Validates that sufficient stock exists before reducing.
     * 
     * @param medicineId The ID of the medicine to reduce stock for
     * @param quantity The amount to reduce
     * @throws MedicineNotFoundException If the medicine doesn't exist
     * @throws InsufficientStockException If not enough stock available
     */
    public void reduceStock(Long medicineId, int quantity) {
        // Find the medicine or throw exception
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with id: " + medicineId));

        // Check if there's sufficient stock
        if (medicine.getQuantity() < quantity) {
            // Throw exception with helpful message showing available vs requested
            throw new InsufficientStockException(
                    String.format("Insufficient stock for %s. Available: %d, Requested: %d",
                            medicine.getName(), medicine.getQuantity(), quantity));
        }

        // Reduce the quantity
        medicine.setQuantity(medicine.getQuantity() - quantity);
        
        // Save the updated entity
        medicineRepository.save(medicine);
    }

    /**
     * Add stock to a medicine.
     * 
     * Increases the quantity of a medicine (e.g., when restocking).
     * 
     * @param medicineId The ID of the medicine to add stock to
     * @param quantity The amount to add
     * @throws MedicineNotFoundException If the medicine doesn't exist
     */
    public void addStock(Long medicineId, int quantity) {
        // Find the medicine or throw exception
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with id: " + medicineId));

        // Increase the quantity
        medicine.setQuantity(medicine.getQuantity() + quantity);
        
        // Save the updated entity
        medicineRepository.save(medicine);
    }

    /**
     * Map a Medicine entity to a MedicineResponseDTO.
     * 
     * This is a private helper method that converts internal entities
     * to the DTO format used in API responses.
     * 
     * It also computes derived fields:
     * - isExpired: Whether the medicine has expired
     * - isLowStock: Whether the medicine has low stock
     * 
     * @param medicine The Medicine entity to convert
     * @return MedicineResponseDTO The converted response DTO
     */
    private MedicineResponseDTO mapToResponseDTO(Medicine medicine) {
        return MedicineResponseDTO.builder()
                .id(medicine.getId()) // Database-generated ID
                .name(medicine.getName()) // Medicine name
                .quantity(medicine.getQuantity()) // Current stock
                .price(medicine.getPrice()) // Unit price
                .expiryDate(medicine.getExpiryDate()) // Expiry date
                .lowStockThreshold(medicine.getLowStockThreshold()) // Threshold value
                .description(medicine.getDescription()) // Description
                .manufacturer(medicine.getManufacturer()) // Manufacturer
                .createdAt(medicine.getCreatedAt()) // Creation timestamp
                .updatedAt(medicine.getUpdatedAt()) // Last update timestamp
                // Call entity methods to compute these values
                .isExpired(medicine.isExpired()) // Computed: is medicine expired?
                .isLowStock(medicine.isLowStock()) // Computed: is stock low?
                .build();
    }
}