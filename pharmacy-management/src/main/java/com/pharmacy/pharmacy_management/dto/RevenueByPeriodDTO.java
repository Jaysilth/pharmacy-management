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
public class RevenueByPeriodDTO {
    /** Human-readable bucket label, e.g. "Week of Jul 7", "Jul 2026", "2026". */
    private String label;
    /** ISO date (yyyy-MM-dd) of the first day in this bucket. */
    private String periodStart;
    /** ISO date (yyyy-MM-dd) of the last day in this bucket. */
    private String periodEnd;
    private BigDecimal totalRevenue;
}
