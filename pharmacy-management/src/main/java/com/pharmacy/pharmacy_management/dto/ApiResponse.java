package com.pharmacy.pharmacy_management.dto;

// Jackson annotation for JSON serialization control
// Excludes null fields from JSON output to keep responses clean
import com.fasterxml.jackson.annotation.JsonInclude;

// Lombok imports for automatic code generation
import lombok.*;

// LocalDateTime for timestamp in responses
import java.time.LocalDateTime;

/**
 * ApiResponse<T> - Generic wrapper for all API responses.
 * 
 * This is a standardized response wrapper used throughout the API.
 * Every endpoint returns this format, providing consistency across all responses.
 * 
 * Key features:
 * - Generic type <T> allows wrapping any data type
 * - Standardized success/error format
 * - Includes timestamp of response
 * - Optional message for additional context
 * 
 * Response structure:
 * {
 *   "success": true/false,
 *   "message": "optional message",
 *   "data": actual payload (can be null for errors),
 *   "timestamp": "ISO datetime"
 * }
 * 
 * Usage examples:
 * - ApiResponse.success(data) - For successful responses
 * - ApiResponse.success(message, data) - With custom message
 * - ApiResponse.error(message) - For error responses
 * 
 * @param <T> The type of data being returned in the response
 */
@Getter // Lombok: Generates getter methods for all fields
@Setter // Lombok: Generates setter methods for all fields
@Builder // Lombok: Enables builder pattern for object construction
@NoArgsConstructor // Lombok: Generates no-argument constructor
@AllArgsConstructor // Lombok: Generates constructor with all fields
@JsonInclude(JsonInclude.Include.NON_NULL) // Jackson: Don't include null fields in JSON output
public class ApiResponse<T> {

    /**
     * Indicates whether the operation was successful.
     * 
     * true - The request was processed successfully
     * false - An error occurred during processing
     * 
     * This is the primary indicator for clients to determine if they
     * should read the data or handle an error.
     */
    private boolean success;

    /**
     * Human-readable message about the operation result.
     * 
     * Optional field that provides additional context:
     * - For success: "Medicine added successfully", "Operation completed"
     * - For errors: "Medicine not found", "Validation failed"
     * 
     * Can be null if no message is needed.
     */
    private String message;

    /**
     * The actual payload/data of the response.
     * 
     * This contains the requested data for successful responses.
     * For example:
     * - List<MedicineResponseDTO> for getAllMedicines
     * - MedicineResponseDTO for getMedicineById
     * - BigDecimal for getTotalRevenue
     * 
     * For error responses, this is typically null.
     * The type is generic <T>, so it can be any Java object or collection.
     */
    private T data;

    /**
     * Timestamp when the response was generated.
     * 
     * Recorded in ISO 8601 format (e.g., "2024-01-15T10:30:00").
     * Useful for:
     * - Logging and debugging
     * - Client-side caching decisions
     * - Tracking response times
     * 
     * This is automatically set when building the response.
     */
    private LocalDateTime timestamp;

    /**
     * Factory method for successful responses without a message.
     * 
     * Convenience method for simple success responses.
     * The message field will be null.
     * 
     * @param <T> The type of data being returned
     * @param data The data to include in the response
     * @return ApiResponse with success=true and the provided data
     * 
     * Example usage:
     * return ApiResponse.success(medicineList);
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true) // Mark as successful
                .data(data) // Include the data payload
                .timestamp(LocalDateTime.now()) // Set current timestamp
                .build();
    }

    /**
     * Factory method for successful responses with a custom message.
     * 
     * Use this when you want to include a descriptive message along
     * with the data (e.g., "Medicine added successfully").
     * 
     * @param <T> The type of data being returned
     * @param message A descriptive message about the operation
     * @param data The data to include in the response
     * @return ApiResponse with success=true, message, and data
     * 
     * Example usage:
     * return ApiResponse.success("Medicine added successfully", medicine);
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true) // Mark as successful
                .message(message) // Include custom message
                .data(data) // Include the data payload
                .timestamp(LocalDateTime.now()) // Set current timestamp
                .build();
    }

    /**
     * Factory method for error responses.
     * 
     * Creates a response indicating that an operation failed.
     * The success field is false and data field is null.
     * 
     * @param <T> The type parameter (typically Void for errors)
     * @param message Description of the error that occurred
     * @return ApiResponse with success=false and error message
     * 
     * Example usage:
     * return ApiResponse.error("Medicine not found with id: 5");
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false) // Mark as failed
                .message(message) // Include error message
                .timestamp(LocalDateTime.now()) // Set current timestamp
                .build();
    }
}