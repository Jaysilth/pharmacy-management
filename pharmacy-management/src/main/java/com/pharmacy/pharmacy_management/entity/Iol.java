package com.pharmacy.pharmacy_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * IOL (Intraocular Lens) Entity.
 *
 * Unlike Consumable (which tracks stock per NAME with a free-text unit),
 * IOL stock must be tracked per (name, type, power) — the same lens name
 * can have many rows in different powers, each with its own quantity.
 * This mirrors how Medicine differentiates same-named stock by batchLabel:
 * here, "power" (+ type) plays that role.
 *
 * Internal inventory tracking only — no price, no POS/Sales integration.
 */
@Entity
@Table(name = "iols")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Iol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    /** RIGID or FOLDABLE */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IolType type;

    /**
     * Lens power in diopters, e.g. 21.00, 21.50.
     * BigDecimal (not enum) since power ranges and steps vary by manufacturer.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal power;

    @Column(length = 150)
    private String manufacturer;

    @Column(length = 500)
    private String description;

    @Column(name = "quantity_in_stock", nullable = false)
    @Builder.Default
    private Integer quantityInStock = 0;

    @Column(name = "reorder_level", nullable = false)
    @Builder.Default
    private Integer reorderLevel = 3;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public boolean isLowStock() { return quantityInStock <= reorderLevel; }
}
