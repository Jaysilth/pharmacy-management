package com.pharmacy.pharmacy_management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlassesRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String brand;
    private String frameType;
    private String lensType;
    private String color;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be 0 or more")
    private Integer quantity;

    @Min(value = 0)
    private Integer lowStockThreshold;

    private String description;
}