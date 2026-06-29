package com.pharmacy.pharmacy_management.dto;

import lombok.*;
import java.util.List;

/**
 * Response from GET /api/medicines/batch-check
 *
 * status:
 *   NO_MATCH     - drug name does not exist; safe to create with Batch A
 *   EXACT_MATCH  - same name + price + expiryDate found; qty will be merged
 *   NAME_EXISTS  - same name but different price/expiry; user must choose action
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCheckResponseDTO {

    /** "NO_MATCH" | "EXACT_MATCH" | "NAME_EXISTS" */
    private String status;

    /** Populated when status = EXACT_MATCH */
    private Long   exactMatchId;
    private String exactMatchBatchLabel;

    /** All existing batches for this drug name — always populated when status != NO_MATCH */
    private List<MedicineResponseDTO> existingBatches;
}