package com.pharmacy.pharmacy_management.controller;

import com.pharmacy.pharmacy_management.dto.ApiResponse;
import com.pharmacy.pharmacy_management.dto.DashboardSummaryDTO;
import com.pharmacy.pharmacy_management.dto.SalesByDayDTO;
import com.pharmacy.pharmacy_management.service.MedicineService;
import com.pharmacy.pharmacy_management.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
@Tag(name = "Dashboard", description = "Endpoints for dashboard analytics")
public class DashboardController {

    private final MedicineService medicineService;
    private final SaleService saleService;

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary", description = "Retrieve aggregated dashboard metrics")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getDashboardSummary() {
        DashboardSummaryDTO summary = DashboardSummaryDTO.builder()
                .totalMedicines(medicineService.getTotalMedicines())
                .totalSalesToday(saleService.getTotalSalesToday())
                .totalRevenueToday(saleService.getTotalRevenueToday())
                .lowStockCount(medicineService.getLowStockCount())
                .expiringSoonCount(medicineService.getExpiringSoonCount())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved successfully", summary));
    }

    @GetMapping("/sales-by-day")
    @Operation(summary = "Get sales by day", description = "Retrieve revenue totals for the last 7 days")
    public ResponseEntity<ApiResponse<List<SalesByDayDTO>>> getSalesByDay() {
        List<SalesByDayDTO> salesByDay = saleService.getSalesByDay(7);
        return ResponseEntity.ok(ApiResponse.success("Sales by day retrieved successfully", salesByDay));
    }
}
