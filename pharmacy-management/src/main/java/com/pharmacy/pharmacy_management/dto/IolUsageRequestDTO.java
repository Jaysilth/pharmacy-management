package com.pharmacy.pharmacy_management.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IolUsageRequestDTO {

    @NotNull(message = "IOL ID is required")
    private Long iolId;

    @NotNull @Positive(message = "Quantity must be positive")
    private Integer quantityUsed;

    @NotNull(message = "Surgery ID is required")
    private Long surgeryId;

    private String usedBy;
    private String notes;
}
