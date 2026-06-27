package com.pharmacy.pharmacy_management.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GlassesAccessoryResponseDTO {
    private Long id;
    private String name;
    private String accessoryType;
    private BigDecimal price;
    private Integer quantity;
    private Integer lowStockThreshold;
    private String description;
    private boolean lowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}