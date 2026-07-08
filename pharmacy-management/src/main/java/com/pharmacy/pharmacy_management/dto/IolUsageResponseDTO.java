package com.pharmacy.pharmacy_management.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IolUsageResponseDTO {
    private Long   id;
    private Long   iolId;
    private String iolName;
    private BigDecimal iolPower;
    private Integer quantityUsed;
    private Long   surgeryId;
    private String surgeryName;
    private String usedBy;
    private String notes;
    private LocalDateTime usedAt;
}
