package com.pharmacy.pharmacy_management.controller;

import com.pharmacy.pharmacy_management.dto.ApiResponse;
import com.pharmacy.pharmacy_management.dto.GlassesRequestDTO;
import com.pharmacy.pharmacy_management.dto.GlassesResponseDTO;
import com.pharmacy.pharmacy_management.service.GlassesService;
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
@RequestMapping("/api/glasses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
@Tag(name = "Glasses Management")
public class GlassesController {

    private final GlassesService glassesService;

    @GetMapping
    @Operation(summary = "Get all glasses, optional ?search=")
    public ResponseEntity<ApiResponse<List<GlassesResponseDTO>>> getAllGlasses(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(glassesService.getAllGlasses(search)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GlassesResponseDTO>> getGlassesById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(glassesService.getGlassesById(id)));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<GlassesResponseDTO>>> getLowStock() {
        return ResponseEntity.ok(ApiResponse.success(glassesService.getLowStockGlasses()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GlassesResponseDTO>> createGlasses(
            @Valid @RequestBody GlassesRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Glasses added.", glassesService.createGlasses(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GlassesResponseDTO>> updateGlasses(
            @PathVariable Long id, @Valid @RequestBody GlassesRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Glasses updated.", glassesService.updateGlasses(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGlasses(@PathVariable Long id) {
        glassesService.deleteGlasses(id);
        return ResponseEntity.ok(ApiResponse.success("Glasses deleted.", null));
    }
}