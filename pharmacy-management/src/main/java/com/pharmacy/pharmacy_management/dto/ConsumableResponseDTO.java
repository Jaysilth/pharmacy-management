package com.pharmacy.pharmacy_management.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumableResponseDTO {
    private Long   id;
    private String name;
    private String description;
    private String unit;
    private Integer quantityInStock;
    private Integer reorderLevel;
    private boolean lowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}