package com.pharmacy.pharmacy_management.controller;

import com.pharmacy.pharmacy_management.dto.ApiResponse;
import com.pharmacy.pharmacy_management.dto.SurgeryRequestDTO;
import com.pharmacy.pharmacy_management.dto.SurgeryResponseDTO;
import com.pharmacy.pharmacy_management.service.SurgeryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/surgeries")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
@Tag(name = "Surgery Management")
public class SurgeryController {

    private final SurgeryService surgeryService;

    @GetMapping
    @Operation(summary = "Get all active surgeries, optional ?search=")
    public ResponseEntity<ApiResponse<List<SurgeryResponseDTO>>> getAllSurgeries(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(surgeryService.getAllSurgeries(search)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SurgeryResponseDTO>> getSurgeryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(surgeryService.getSurgeryById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SurgeryResponseDTO>> createSurgery(
            @Valid @RequestBody SurgeryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Surgery created.", surgeryService.createSurgery(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SurgeryResponseDTO>> updateSurgery(
            @PathVariable Long id, @Valid @RequestBody SurgeryRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Surgery updated.", surgeryService.updateSurgery(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSurgery(@PathVariable Long id) {
        surgeryService.deleteSurgery(id);
        return ResponseEntity.ok(ApiResponse.success("Surgery deactivated.", null));
    }
}