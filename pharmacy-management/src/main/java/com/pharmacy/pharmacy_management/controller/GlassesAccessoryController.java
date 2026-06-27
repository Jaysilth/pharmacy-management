package com.pharmacy.pharmacy_management.controller;

import com.pharmacy.pharmacy_management.dto.*;
import com.pharmacy.pharmacy_management.service.GlassesAccessoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/glasses-accessories")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
@Tag(name = "Glasses Accessories")
public class GlassesAccessoryController {

    private final GlassesAccessoryService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GlassesAccessoryResponseDTO>>> getAll(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(service.getAll(search)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GlassesAccessoryResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<GlassesAccessoryResponseDTO>>> getLowStock() {
        return ResponseEntity.ok(ApiResponse.success(service.getLowStock()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GlassesAccessoryResponseDTO>> create(
            @Valid @RequestBody GlassesAccessoryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Accessory added.", service.create(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GlassesAccessoryResponseDTO>> update(
            @PathVariable Long id, @Valid @RequestBody GlassesAccessoryRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Accessory updated.", service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Accessory deleted.", null));
    }
}
