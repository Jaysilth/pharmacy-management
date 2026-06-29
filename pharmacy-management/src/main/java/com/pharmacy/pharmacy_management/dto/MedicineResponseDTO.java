package com.pharmacy.pharmacy_management.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineResponseDTO {

    private Long    id;
    private String  name;
    private String  batchLabel;
    private Integer quantity;
    private BigDecimal price;
    private LocalDate  expiryDate;
    private Integer    lowStockThreshold;
    private String     description;
    private String     manufacturer;
    private String     category;
    private LocalDate  createdAt;
    private LocalDate  updatedAt;
    private Boolean    isExpired;
    private Boolean    isLowStock;
}