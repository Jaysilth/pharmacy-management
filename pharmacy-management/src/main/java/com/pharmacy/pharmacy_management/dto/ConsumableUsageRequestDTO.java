package com.pharmacy.pharmacy_management.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumableUsageRequestDTO {

    @NotNull(message = "Consumable ID is required")
    private Long consumableId;

    @NotNull @Positive(message = "Quantity must be positive")
    private Integer quantityUsed;

    private String usedBy;
    private String notes;

    /**
     * SURGERY | PROCEDURE | LAB_TEST
     * Only one of the three fields below should be provided.
     */
    private String linkedEntityType;

    /** DB ID of the surgery (when linkedEntityType = SURGERY) */
    private Long surgeryId;

    /** Name of the procedure from ClinicalContext (when linkedEntityType = PROCEDURE) */
    private String procedureRef;

    /** Name of the lab test from ClinicalContext (when linkedEntityType = LAB_TEST) */
    private String labTestRef;
}