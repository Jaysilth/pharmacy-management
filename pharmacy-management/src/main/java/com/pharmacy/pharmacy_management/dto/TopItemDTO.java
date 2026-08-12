package com.pharmacy.pharmacy_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopItemDTO {
    /** MEDICINE, GLASSES, SURGERY, etc. */
    private String itemType;
    private Long itemId;
    private String itemName;
    /** Sum of quantity across all sale-item rows for this item, all-time. */
    private int totalQuantity;
    private BigDecimal totalRevenue;
}
