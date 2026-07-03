package com.pharmacy.pharmacy_management.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumableUsageResponseDTO {
    private Long   id;
    private Long   consumableId;
    private String consumableName;
    private String unit;
    private Integer quantityUsed;
    private String usedBy;
    private String notes;
    private LocalDateTime usedAt;
    private String linkedEntityType;
    private Long   surgeryId;
    private String surgeryName;
    private String procedureRef;
    private String labTestRef;
}