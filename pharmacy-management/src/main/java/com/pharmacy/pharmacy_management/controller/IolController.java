package com.pharmacy.pharmacy_management.controller;

import com.pharmacy.pharmacy_management.dto.*;
import com.pharmacy.pharmacy_management.service.IolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/iols")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
@Tag(name = "IOLs", description = "Intraocular lens inventory — tracked per name/type/power, surgery-linked usage")
public class IolController {

    private final IolService service;

    @GetMapping
    @Operation(summary = "List all IOL stock rows, optional ?search=")
    public ResponseEntity<ApiResponse<List<IolResponseDTO>>> getAll(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(service.getAll(search)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IolResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "IOL rows at or below reorder level")
    public ResponseEntity<ApiResponse<List<IolResponseDTO>>> getLowStock() {
        return ResponseEntity.ok(ApiResponse.success(service.getLowStock()));
    }

    @PostMapping
    @Operation(summary = "Add IOL stock — merges into an existing (name, type, power) row if one exists")
    public ResponseEntity<ApiResponse<IolResponseDTO>> create(
            @Valid @RequestBody IolRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("IOL stock added.", service.create(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<IolResponseDTO>> update(
            @PathVariable Long id, @Valid @RequestBody IolRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("IOL updated.", service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("IOL deleted.", null));
    }

    // ── Usage ──────────────────────────────────────────────────────────────────

    @PostMapping("/usage")
    @Operation(summary = "Record implantation of an IOL — deducts stock, always surgery-linked")
    public ResponseEntity<ApiResponse<IolUsageResponseDTO>> recordUsage(
            @Valid @RequestBody IolUsageRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("IOL usage recorded.", service.recordUsage(dto)));
    }

    @GetMapping("/usage")
    @Operation(summary = "Full IOL usage log, newest first")
    public ResponseEntity<ApiResponse<List<IolUsageResponseDTO>>> getAllUsage() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllUsage()));
    }

    @GetMapping("/{id}/usage")
    public ResponseEntity<ApiResponse<List<IolUsageResponseDTO>>> getUsageByIol(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getUsageByIol(id)));
    }

    @DeleteMapping("/usage/{usageId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a usage log entry — restores the deducted stock")
    public ResponseEntity<ApiResponse<Void>> deleteUsage(@PathVariable Long usageId) {
        service.deleteUsage(usageId);
        return ResponseEntity.ok(ApiResponse.success("Usage entry deleted.", null));
    }
}
