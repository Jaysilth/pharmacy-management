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
    private String name;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be 0 or more")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    @Min(0)
    private Integer lowStockThreshold;

    private String description;
    private String manufacturer;

    @Pattern(
            regexp = "^(EYEDROP|TABLET|INJECTION|SYRUP)$",
            message = "Category must be one of: EYEDROP, TABLET, INJECTION, SYRUP"
    )
    private String category;

    /**
     * Batch action — controls how duplicate names are handled.
     * NULL / "AUTO" : service decides (exact match → add qty, else new batch)
     * "NEW_BATCH"   : force creation of next batch label (B, C, D…)
     * "UPDATE_BATCH": update the batch identified by targetBatchId
     */
    private String batchAction;

    /** Only used when batchAction = "UPDATE_BATCH" */
    private Long targetBatchId;
}