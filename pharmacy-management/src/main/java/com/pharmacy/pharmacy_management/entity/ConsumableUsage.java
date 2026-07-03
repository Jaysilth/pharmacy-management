package com.pharmacy.pharmacy_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consumable_usage")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ConsumableUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_id", nullable = false)
    private Consumable consumable;

    @Column(name = "quantity_used", nullable = false)
    private Integer quantityUsed;

    @Column(name = "used_by", length = 100)
    private String usedBy;

    @Column(length = 500)
    private String notes;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    /**
     * Which type of clinical activity consumed this item.
     * Values: SURGERY | PROCEDURE | LAB_TEST
     * Only one of the three linked-entity fields below should be non-null.
     */
    @Column(name = "linked_entity_type", length = 20)
    private String linkedEntityType;

    /**
     * Surgery FK — real DB reference via the surgeries table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surgery_id", nullable = true)
    private Surgery surgery;

    /**
     * Procedure name — stored as text because Procedures live in
     * localStorage (ClinicalContext) and have no backend DB table.
     * When/if Procedures get a backend table, migrate this to a FK.
     */
    @Column(name = "procedure_ref", length = 200)
    private String procedureRef;

    /**
     * Lab test name — same reason as procedureRef above.
     */
    @Column(name = "lab_test_ref", length = 200)
    private String labTestRef;

    @PrePersist
    protected void onCreate() {
        if (usedAt == null) usedAt = LocalDateTime.now();
    }
}
