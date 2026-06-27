package com.pharmacy.pharmacy_management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GlassesRepairRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    private boolean active = true;
}