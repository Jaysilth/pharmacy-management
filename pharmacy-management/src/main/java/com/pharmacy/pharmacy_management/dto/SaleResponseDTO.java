package com.pharmacy.pharmacy_management.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleResponseDTO {
    private Long id;
    private String saleNumber;
    private String customerName;
    private String customerPhone;
    private String paymentMethod;
    private String notes;
    private BigDecimal grandTotal;
    private List<SaleItemResponseDTO> items;
    private LocalDateTime createdAt;

    // ── Legacy fields — populated for old single-medicine sales ──
    private MedicineInfo medicine;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicineInfo {
        private Long id;
        private String name;
        private String manufacturer;
    }
}