package com.pharmacy.pharmacy_management.controller;

import com.pharmacy.pharmacy_management.dto.*;
import com.pharmacy.pharmacy_management.service.GlassesRepairService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/glasses-repairs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
@Tag(name = "Glasses Repairs")
public class GlassesRepairController {

    private final GlassesRepairService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GlassesRepairResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(service.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GlassesRepairResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GlassesRepairResponseDTO>> create(
            @Valid @RequestBody GlassesRepairRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Repair service added.", service.create(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GlassesRepairResponseDTO>> update(
            @PathVariable Long id, @Valid @RequestBody GlassesRepairRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Repair updated.", service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Repair deactivated.", null));
    }
}