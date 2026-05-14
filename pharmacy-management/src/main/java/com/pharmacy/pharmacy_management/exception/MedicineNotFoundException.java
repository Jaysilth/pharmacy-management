package com.pharmacy.pharmacy_management.exception;

/**
 * MedicineNotFoundException - Custom exception for when a medicine is not found.
 * 
 * This exception is thrown when a client requests a medicine by ID
 * but no medicine with that ID exists in the database.
 * 
 * Usage:
 * throw new MedicineNotFoundException("Medicine not found with id: " + id);
 * 
 * This exception extends RuntimeException, meaning:
 * - It is an unchecked exception (no need to declare or catch)
 * - It can be thrown when a medicine is not found during:
 *   - Looking up a medicine by ID
 *   - Updating a medicine
 *   - Deleting a medicine
 *   - Processing a sale
 * 
 * How it's handled:
 * - The GlobalExceptionHandler catches this exception
 * - Returns HTTP 404 Not Found to the client
 * - Includes the error message in the response
 */
public class MedicineNotFoundException extends RuntimeException {
    
    /**
     * Constructor that accepts an error message.
     * 
     * @param message The error message describing what went wrong
     *                (e.g., "Medicine not found with id: 5")
     */
    public MedicineNotFoundException(String message) {
        // Pass the message to the parent RuntimeException class
        super(message);
    }
}