package com.pharmacy.pharmacy_management.exception;

// Import API response wrapper for standardized error responses
import com.pharmacy.pharmacy_management.dto.ApiResponse;

// Spring MVC imports for HTTP response handling
import org.springframework.http.HttpStatus; // HTTP status codes (200, 404, 500, etc.)
import org.springframework.http.ResponseEntity; // Wrapper for HTTP responses

// Validation API imports for handling validation errors
import org.springframework.validation.FieldError; // Represents a field validation error
import org.springframework.web.bind.MethodArgumentNotValidException; // Thrown when validation fails

// Spring exception handling
import org.springframework.web.bind.annotation.ExceptionHandler; // Handles specific exceptions
import org.springframework.web.bind.annotation.RestControllerAdvice; // Applies to all controllers

// Java utilities
import java.util.HashMap; // For building error map
import java.util.Map; // For storing field-name to error-message mappings

/**
 * GlobalExceptionHandler - Centralized exception handling for the REST API.
 * 
 * This class acts as a global error handler that catches exceptions thrown
 * from any controller in the application. It provides consistent error
 * responses to clients in a standardized format.
 * 
 * How it works:
 * - @RestControllerAdvice makes this class apply to all @RestController classes
 * - Each @ExceptionHandler method handles a specific type of exception
 * - Returns appropriate HTTP status codes and error messages
 * 
 * Exception handling flow:
 * 1. An exception is thrown in a controller or service
 * 2. If not caught locally, it's passed to this handler
 * 3. The appropriate @ExceptionHandler method is called
 * 4. The handler creates an error response and returns it to the client
 * 
 * Benefits:
 * - Single place for all error handling logic
 * - Consistent error response format across all endpoints
 * - Clean separation of error handling from business logic
 * - Easy to add new exception handlers as needed
 */
@RestControllerAdvice // Indicates this handles exceptions for all REST controllers
public class GlobalExceptionHandler {

    /**
     * Handle MedicineNotFoundException.
     * 
     * This handler catches exceptions thrown when a medicine is not found
     * (e.g., when requesting a medicine by ID that doesn't exist).
     * 
     * HTTP Status: 404 Not Found
     * 
     * @param ex The caught MedicineNotFoundException
     * @return ResponseEntity with error message and 404 status
     */
    @ExceptionHandler(MedicineNotFoundException.class) // Catches MedicineNotFoundException
    public ResponseEntity<ApiResponse<Void>> handleMedicineNotFoundException(MedicineNotFoundException ex) {
        // Return 404 Not Found with the exception message
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle InsufficientStockException.
     * 
     * This handler catches exceptions thrown when there's not enough stock
     * to complete a sale or operation.
     * 
     * HTTP Status: 400 Bad Request
     * 
     * @param ex The caught InsufficientStockException
     * @return ResponseEntity with error message and 400 status
     */
    @ExceptionHandler(InsufficientStockException.class) // Catches InsufficientStockException
    public ResponseEntity<ApiResponse<Void>> handleInsufficientStockException(InsufficientStockException ex) {
        // Return 400 Bad Request with the exception message
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle IllegalStateException.
     * 
     * This handler catches IllegalStateException which can be thrown
     * in various situations like trying to sell an expired medicine.
     * 
     * HTTP Status: 400 Bad Request
     * 
     * @param ex The caught IllegalStateException
     * @return ResponseEntity with error message and 400 status
     */
    @ExceptionHandler(IllegalStateException.class) // Catches IllegalStateException
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
        // Return 400 Bad Request with the exception message
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle validation errors from request bodies.
     * 
     * This handler catches MethodArgumentNotValidException which is thrown
     * when @Valid annotation detects validation failures in request data.
     * This includes:
     * - Missing required fields (@NotNull, @NotBlank)
     * - Invalid values (@Positive, @Size, etc.)
     * - Custom validation constraints
     * 
     * HTTP Status: 400 Bad Request
     * 
     * @param ex The caught MethodArgumentNotValidException
     * @return ResponseEntity with field-specific error messages and 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class) // Catches validation errors
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Create a map to store field name -> error message pairs
        Map<String, String> errors = new HashMap<>();
        
        // Iterate through all validation errors
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            // Get the field name that failed validation
            String fieldName = ((FieldError) error).getField();
            // Get the validation error message
            String errorMessage = error.getDefaultMessage();
            // Add to the map
            errors.put(fieldName, errorMessage);
        });
        
        // Build the response with validation errors in the data field
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false) // Mark as failed
                .message("Validation failed") // General error message
                .data(errors) // Field-specific errors
                .build();
        
        // Return 400 Bad Request with the validation errors
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle all other unhandled exceptions.
     * 
     * This is a catch-all handler for any exception not specifically handled
     * by the other handlers. It prevents internal errors from leaking
     * implementation details to clients.
     * 
     * HTTP Status: 500 Internal Server Error
     * 
     * @param ex The caught RuntimeException (or any other exception)
     * @return ResponseEntity with generic error message and 500 status
     */
    @ExceptionHandler(RuntimeException.class) // Catches all other RuntimeExceptions
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        // Return 500 Internal Server Error
        // Note: We don't expose the actual exception message to avoid leaking internals
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred: " + ex.getMessage()));
    }
}