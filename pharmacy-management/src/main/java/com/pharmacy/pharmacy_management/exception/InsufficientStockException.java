package com.pharmacy.pharmacy_management.exception;

/**
 * InsufficientStockException - Custom exception for when there's not enough stock.
 * 
 * This exception is thrown when a client attempts to:
 * - Create a sale with a quantity higher than available stock
 * - Reduce stock by more than what's available
 * 
 * Usage:
 * throw new InsufficientStockException("Insufficient stock for Paracetamol. Available: 5, Requested: 10");
 * 
 * This exception extends RuntimeException, meaning:
 * - It is an unchecked exception (no need to declare or catch)
 * - It indicates a business logic violation (not a system error)
 * 
 * How it's handled:
 * - The GlobalExceptionHandler catches this exception
 * - Returns HTTP 400 Bad Request to the client
 * - Includes the error message showing available vs requested quantity
 * 
 * Business rule:
 * - A sale cannot be completed if requested quantity > available quantity
 * - This ensures inventory stays accurate and prevents overselling
 */
public class InsufficientStockException extends RuntimeException {
    
    /**
     * Constructor that accepts an error message.
     * 
     * @param message The error message describing the stock shortage
     *                Should include available and requested quantities
     *                Example: "Insufficient stock for Paracetamol. Available: 5, Requested: 10"
     */
    public InsufficientStockException(String message) {
        // Pass the message to the parent RuntimeException class
        super(message);
    }
}