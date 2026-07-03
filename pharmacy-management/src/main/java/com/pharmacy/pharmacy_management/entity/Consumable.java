package com.pharmacy.pharmacy_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consumables")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Consumable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    /**
     * Unit of measurement: "pairs", "pieces", "ml", "boxes" etc.
     * Free text — not an enum — so staff can define their own units.
     */
    @Column(length = 50)
    private String unit;

    @Column(name = "quantity_in_stock", nullable = false)
    @Builder.Default
    private Integer quantityInStock = 0;

    /**
     * When quantityInStock falls to or below this level, the item is
     * flagged for reorder. Analogous to lowStockThreshold on Medicine.
     */
    @Column(name = "reorder_level", nullable = false)
    @Builder.Default
    private Integer reorderLevel = 5;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public boolean isLowStock() { return quantityInStock <= reorderLevel; }
}