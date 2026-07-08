package com.pharmacy.pharmacy_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Records implantation of one IOL unit — always tied to a Surgery
 * (cataract implant). Deliberately a separate table from ConsumableUsage:
 * that table's linking (surgery/procedure/lab-test) and its consumable
 * FK are non-null and load-bearing for the existing Consumables feature.
 * IOL usage only ever needs Surgery, so a small dedicated table avoids
 * touching working code to support a case it doesn't need.
 */
@Entity
@Table(name = "iol_usage")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class IolUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iol_id", nullable = false)
    private Iol iol;

    @Column(name = "quantity_used", nullable = false)
    private Integer quantityUsed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surgery_id", nullable = false)
    private Surgery surgery;

    @Column(name = "used_by", length = 100)
    private String usedBy;

    @Column(length = 500)
    private String notes;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        if (usedAt == null) usedAt = LocalDateTime.now();
    }
}
