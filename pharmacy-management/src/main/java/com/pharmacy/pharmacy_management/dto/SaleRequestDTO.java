package com.pharmacy.pharmacy_management.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SaleRequestDTO {

    @NotBlank(message = "Customer name is required")
    @Size(max = 150, message = "Customer name must be at most 150 characters")
    private String customerName;

    @NotBlank(message = "Customer phone is required")
    @Size(max = 30, message = "Customer phone must be at most 30 characters")
    private String customerPhone;

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^(CASH|CARD|INSURANCE|TRANSFER)$",
            message = "Payment method must be one of: CASH, CARD, INSURANCE, TRANSFER")
    private String paymentMethod;

    // Capped below the 500-char DB column because the backdate audit note is appended to it.
    @Size(max = 200, message = "Notes must be at most 200 characters")
    private String notes;

    @Pattern(regexp = "^(FIXED|PERCENT)$", message = "Discount type must be FIXED or PERCENT")
    private String discountType;

    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    private BigDecimal discountValue;

    // Ignored by the server (it recomputes the discount); kept so existing clients
    // and tests that send it still deserialize.
    private BigDecimal discountAmount;

    // Optional — SUPER_ADMIN only; window and role are enforced in SaleService.
    private LocalDate saleDate;

    @Size(max = 200, message = "Backdate reason must be at most 200 characters")
    private String backdateReason;

    // @Valid is what makes the SaleItemInput constraints below actually run.
    @NotEmpty(message = "At least one item is required")
    @Size(max = 100, message = "A sale can have at most 100 line items")
    private List<@Valid SaleItemInput> items;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class SaleItemInput {

        @NotBlank(message = "Item type is required")
        private String itemType;
        // MEDICINE | GLASSES | GLASSES_ACCESSORY | GLASSES_REPAIR
        // SURGERY  | CLINIC_VISIT | PROCEDURE | LAB_TEST

        private Long itemId;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        @Max(value = 10000, message = "Quantity is too large")
        private Integer quantity;

        // Required for localStorage-backed items (CLINIC_VISIT, PROCEDURE, LAB_TEST)
        @Size(max = 200, message = "Item name must be at most 200 characters")
        private String itemName;

        @DecimalMin(value = "0.0", message = "Unit price cannot be negative")
        @Digits(integer = 8, fraction = 2, message = "Unit price is out of range")
        private BigDecimal unitPrice;
    }
}