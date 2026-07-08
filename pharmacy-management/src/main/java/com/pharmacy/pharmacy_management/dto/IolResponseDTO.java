package com.pharmacy.pharmacy_management.dto;

import com.pharmacy.pharmacy_management.entity.IolType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IolResponseDTO {
    private Long     id;
    private String   name;
    private IolType  type;
    private BigDecimal power;
    private String   manufacturer;
    private String   description;
    private Integer  quantityInStock;
    private Integer  reorderLevel;
    private boolean  lowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
