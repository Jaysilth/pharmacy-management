package com.pharmacy.pharmacy_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    /** MEDICINE, GLASSES, or SURGERY */
    @Column(name = "item_type", nullable = false, length = 20)
    private String itemType;

    /** ID in the respective table */
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    /** Denormalised name — preserves history if item is renamed/deleted */
    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}