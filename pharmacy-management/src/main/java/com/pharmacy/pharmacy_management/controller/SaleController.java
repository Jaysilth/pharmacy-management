package com.pharmacy.pharmacy_management.controller;

import com.pharmacy.pharmacy_management.dto.ApiResponse;
import com.pharmacy.pharmacy_management.dto.SaleRequestDTO;
import com.pharmacy.pharmacy_management.dto.SaleResponseDTO;
import com.pharmacy.pharmacy_management.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
@Tag(name = "Sales Management")
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    @Operation(summary = "Record a new sale (supports medicine, glasses, and surgery items)")
    public ResponseEntity<ApiResponse<SaleResponseDTO>> createSale(
            @Valid @RequestBody SaleRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sale recorded successfully.", saleService.createSale(request)));
    }

    @GetMapping
    @Operation(summary = "Get all sales, newest first")
    public ResponseEntity<ApiResponse<List<SaleResponseDTO>>> getAllSales() {
        return ResponseEntity.ok(ApiResponse.success(saleService.getAllSales()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SaleResponseDTO>> getSaleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getSaleById(id)));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<SaleResponseDTO>>> getRecentSales() {
        return ResponseEntity.ok(ApiResponse.success(saleService.getRecentSales()));
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalRevenue() {
        return ResponseEntity.ok(ApiResponse.success("Total revenue.", saleService.getTotalRevenue()));
    }
}