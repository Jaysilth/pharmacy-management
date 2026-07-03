package com.pharmacy.pharmacy_management.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumableRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank(message = "Unit is required (e.g. pieces, ml, pairs)")
    private String unit;

    @NotNull @Min(0)
    private Integer quantityInStock;

    @Min(0)
    private Integer reorderLevel;
}