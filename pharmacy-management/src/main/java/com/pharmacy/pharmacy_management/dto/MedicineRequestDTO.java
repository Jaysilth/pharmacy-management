package com.pharmacy.pharmacy_management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be 0 or more")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price is out of range")
    private BigDecimal price;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    @Min(0)
    private Integer lowStockThreshold;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @Size(max = 255, message = "Manufacturer must be at most 255 characters")
    private String manufacturer;

    @NotBlank(message = "Category is required")
    @Pattern(
            regexp = "^(EYEDROP|TABLET|INJECTION|SYRUP)$",
            message = "Category must be one of: EYEDROP, TABLET, INJECTION, SYRUP"
    )
    private String category;

    /**
     * NULL / "AUTO" : service decides (exact match → add qty, else new batch)
     * "NEW_BATCH"   : force creation of next batch label (B, C, D…)
     * "UPDATE_BATCH": update the batch identified by targetBatchId
     */
    @Pattern(regexp = "^(AUTO|NEW_BATCH|UPDATE_BATCH)$",
            message = "batchAction must be AUTO, NEW_BATCH or UPDATE_BATCH")
    private String batchAction;

    /** Only used when batchAction = "UPDATE_BATCH" */
    private Long targetBatchId;
}