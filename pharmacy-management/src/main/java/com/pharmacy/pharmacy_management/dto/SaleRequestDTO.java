package com.pharmacy.pharmacy_management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SaleRequestDTO {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer phone is required")
    private String customerPhone;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String notes;

    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal discountAmount;

    // Optional — for backdating a sale that was forgotten on the day it
    // happened. SUPER_ADMIN only; server-side validated to fall within the
    // configured backdate window. Omit for a normal, present-moment sale.
    private LocalDate saleDate;

    // Required whenever saleDate is set — a one-line justification stored
    // in the audit note. The wider the backdate window, the more this
    // matters; not optional.
    private String backdateReason;

    @NotEmpty(message = "At least one item is required")
    private List<SaleItemInput> items;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class SaleItemInput {

        @NotBlank(message = "Item type is required")
        private String itemType;
        // MEDICINE | GLASSES | GLASSES_ACCESSORY | GLASSES_REPAIR
        // SURGERY  | CLINIC_VISIT | PROCEDURE | LAB_TEST

        // Required for DB-backed items (MEDICINE, GLASSES, SURGERY, GLASSES_ACCESSORY, GLASSES_REPAIR)
        private Long itemId;

        @NotNull @Positive
        private Integer quantity;

        // Required for localStorage-backed items (CLINIC_VISIT, PROCEDURE, LAB_TEST)
        // Also accepted for all types as a display-name override
        private String itemName;
        private BigDecimal unitPrice;
    }
}