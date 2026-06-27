package com.pharmacy.pharmacy_management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GlassesAccessoryRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Accessory type is required")
    @Pattern(regexp = "^(ROPE_THIN|ROPE_FAT|CASE_PLASTIC|CASE_WOODEN)$",
            message = "Type must be: ROPE_THIN, ROPE_FAT, CASE_PLASTIC, or CASE_WOODEN")
    private String accessoryType;

    @NotNull @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @NotNull @Min(0)
    private Integer quantity;

    @Min(0)
    private Integer lowStockThreshold;

    private String description;
}
