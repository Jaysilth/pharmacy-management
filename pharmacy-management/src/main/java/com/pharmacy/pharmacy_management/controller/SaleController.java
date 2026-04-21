package com.pharmacy.pharmacy_management.controller;

import com.pharmacy.pharmacy_management.dto.ApiResponse;
import com.pharmacy.pharmacy_management.dto.SaleRequestDTO;
import com.pharmacy.pharmacy_management.dto.SaleResponseDTO;
import com.pharmacy.pharmacy_management.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Tag(name = "Sales Management", description = "APIs for managing sales transactions in the pharmacy POS")
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    @Operation(summary = "Create a new sale", description = "Record a new sales transaction. Automatically reduces medicine stock.")
    public ResponseEntity<ApiResponse<SaleResponseDTO>> createSale(
            @Valid @RequestBody SaleRequestDTO requestDTO) {
        SaleResponseDTO response = saleService.createSale(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sale recorded successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all sales", description = "Retrieve all sales transactions ordered by date (newest first)")
    public ResponseEntity<ApiResponse<List<SaleResponseDTO>>> getAllSales() {
        List<SaleResponseDTO> sales = saleService.getAllSales();
        return ResponseEntity.ok(ApiResponse.success(sales));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sale by ID", description = "Retrieve a specific sale transaction by its ID")
    public ResponseEntity<ApiResponse<SaleResponseDTO>> getSaleById(
            @Parameter(description = "Sale ID") @PathVariable Long id) {
        SaleResponseDTO sale = saleService.getSaleById(id);
        return ResponseEntity.ok(ApiResponse.success(sale));
    }

    @GetMapping("/revenue")
    @Operation(summary = "Get total revenue", description = "Calculate and return the total revenue from all sales")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalRevenue() {
        BigDecimal revenue = saleService.getTotalRevenue();
        return ResponseEntity.ok(ApiResponse.success("Total revenue calculated", revenue));
    }
}