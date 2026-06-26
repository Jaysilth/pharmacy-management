package com.pharmacy.pharmacy_management.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlassesResponseDTO {
    private Long id;
    private String name;
    private String brand;
    private String frameType;
    private String lensType;
    private String color;
    private BigDecimal price;
    private Integer quantity;
    private Integer lowStockThreshold;
    private String description;
    private boolean lowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}