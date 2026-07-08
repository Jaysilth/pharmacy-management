package com.pharmacy.pharmacy_management.dto;

import com.pharmacy.pharmacy_management.entity.IolType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IolRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Type is required (RIGID or FOLDABLE)")
    private IolType type;

    @NotNull(message = "Power is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Power must be positive")
    private BigDecimal power;

    private String manufacturer;
    private String description;

    @NotNull @Min(0)
    private Integer quantityInStock;

    @Min(0)
    private Integer reorderLevel;
}
