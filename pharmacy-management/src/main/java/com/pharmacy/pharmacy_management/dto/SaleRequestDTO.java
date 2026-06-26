package com.pharmacy.pharmacy_management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleRequestDTO {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer phone is required")
    private String customerPhone;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // CASH, CARD, INSURANCE, TRANSFER

    private String notes;

    @NotEmpty(message = "At least one item is required")
    private List<SaleItemInput> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleItemInput {
        @NotBlank(message = "Item type is required")
        private String itemType; // MEDICINE, GLASSES, SURGERY

        @NotNull(message = "Item ID is required")
        private Long itemId;

        @NotNull
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
    }
}