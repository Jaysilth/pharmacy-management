package com.pharmacy.pharmacy_management.controller;

import com.pharmacy.pharmacy_management.dto.*;
import com.pharmacy.pharmacy_management.service.ConsumableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/consumables")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
@Tag(name = "Consumables", description = "Non-sale medical supplies for internal use tracking")
public class ConsumableController {

    private final ConsumableService service;

    @GetMapping
    @Operation(summary = "List all consumables, optional ?search=")
    public ResponseEntity<ApiResponse<List<ConsumableResponseDTO>>> getAll(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(service.getAll(search)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConsumableResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Consumables at or below reorder level")
    public ResponseEntity<ApiResponse<List<ConsumableResponseDTO>>> getLowStock() {
        return ResponseEntity.ok(ApiResponse.success(service.getLowStock()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ConsumableResponseDTO>> create(
            @Valid @RequestBody ConsumableRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Consumable added.", service.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<ConsumableResponseDTO>> update(
            @PathVariable Long id, @Valid @RequestBody ConsumableRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Consumable updated.", service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Consumable deleted.", null));
    }

    // ── Usage ──────────────────────────────────────────────────────────────────

    @PostMapping("/usage")
    @Operation(summary = "Record usage of a consumable — deducts stock atomically")
    public ResponseEntity<ApiResponse<ConsumableUsageResponseDTO>> recordUsage(
            @Valid @RequestBody ConsumableUsageRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usage recorded.", service.recordUsage(dto)));
    }

    @GetMapping("/usage")
    @Operation(summary = "Full usage log, newest first")
    public ResponseEntity<ApiResponse<List<ConsumableUsageResponseDTO>>> getAllUsage() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllUsage()));
    }

    @GetMapping("/{id}/usage")
    @Operation(summary = "Usage log for a specific consumable")
    public ResponseEntity<ApiResponse<List<ConsumableUsageResponseDTO>>> getUsageByConsumable(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getUsageByConsumable(id)));
    }

    @DeleteMapping("/usage/{usageId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a usage log entry — restores the deducted stock")
    public ResponseEntity<ApiResponse<Void>> deleteUsage(@PathVariable Long usageId) {
        service.deleteUsage(usageId);
        return ResponseEntity.ok(ApiResponse.success("Usage entry deleted.", null));
    }
}
