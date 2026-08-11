package com.pharmacy.pharmacy_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryDTO {
    /** e.g. "Week of Aug 3" */
    private String label;
    private String periodStart;
    private String periodEnd;
    /** itemType (MEDICINE, GLASSES, SURGERY, ...) -> count of line items that week. */
    private Map<String, Long> counts;
}
