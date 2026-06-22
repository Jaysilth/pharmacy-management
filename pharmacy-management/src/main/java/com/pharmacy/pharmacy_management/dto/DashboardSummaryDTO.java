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
public class DashboardSummaryDTO {

    private long totalMedicines;
    private int totalSalesToday;
    private BigDecimal totalRevenueToday;
    private long lowStockCount;
    private long expiringSoonCount;
}
