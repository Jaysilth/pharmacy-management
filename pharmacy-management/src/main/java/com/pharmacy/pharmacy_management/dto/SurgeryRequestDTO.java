package com.pharmacy.pharmacy_management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurgeryRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must be at most 150 characters")
    private String name;

    @Size(max = 100, message = "Category must be at most 100 characters")
    private String category;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price is out of range")
    private BigDecimal price;

    @Min(value = 0, message = "Duration cannot be negative")
    @Max(value = 1440, message = "Duration cannot exceed 24 hours")
    private Integer durationMinutes;

    private boolean active = true;
}