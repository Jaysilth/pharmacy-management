package com.pharmacy.pharmacy_management.controller;

import com.pharmacy.pharmacy_management.dto.ApiResponse;
import com.pharmacy.pharmacy_management.dto.MedicineRequestDTO;
import com.pharmacy.pharmacy_management.dto.MedicineResponseDTO;
import com.pharmacy.pharmacy_management.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
@Tag(name = "Medicine Management", description = "APIs for managing medicines in the pharmacy inventory")
public class MedicineController {

    private final MedicineService medicineService;

    @PostMapping
    @Operation(summary = "Add a new medicine", description = "Create a new medicine entry in the inventory")
    public ResponseEntity<ApiResponse<MedicineResponseDTO>> addMedicine(
            @Valid @RequestBody MedicineRequestDTO requestDTO) {
        MedicineResponseDTO response = medicineService.addMedicine(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Medicine added successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all medicines", description = "Retrieve all medicines from the inventory")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> getAllMedicines() {
        List<MedicineResponseDTO> medicines = medicineService.getAllMedicines();
        return ResponseEntity.ok(ApiResponse.success(medicines));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medicine by ID", description = "Retrieve a specific medicine by its ID")
    public ResponseEntity<ApiResponse<MedicineResponseDTO>> getMedicineById(
            @Parameter(description = "Medicine ID") @PathVariable Long id) {
        MedicineResponseDTO medicine = medicineService.getMedicineById(id);
        return ResponseEntity.ok(ApiResponse.success(medicine));
    }

    @GetMapping("/expired")
    @Operation(summary = "Get expired medicines", description = "Retrieve all medicines that have expired")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> getExpiredMedicines() {
        List<MedicineResponseDTO> medicines = medicineService.getExpiredMedicines();
        return ResponseEntity.ok(ApiResponse.success(medicines));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock medicines", description = "Retrieve all medicines with low stock levels")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> getLowStockMedicines() {
        List<MedicineResponseDTO> medicines = medicineService.getLowStockMedicines();
        return ResponseEntity.ok(ApiResponse.success(medicines));
    }

    @GetMapping("/search")
    @Operation(summary = "Search medicines", description = "Search medicines by name")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> searchMedicines(
            @Parameter(description = "Medicine name to search") @RequestParam String name) {
        List<MedicineResponseDTO> medicines = medicineService.searchMedicines(name);
        return ResponseEntity.ok(ApiResponse.success(medicines));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update medicine", description = "Update an existing medicine's information")
    public ResponseEntity<ApiResponse<MedicineResponseDTO>> updateMedicine(
            @Parameter(description = "Medicine ID") @PathVariable Long id,
            @Valid @RequestBody MedicineRequestDTO requestDTO) {
        MedicineResponseDTO response = medicineService.updateMedicine(id, requestDTO);
        return ResponseEntity.ok(ApiResponse.success("Medicine updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete medicine", description = "Delete a medicine from the inventory")
    public ResponseEntity<ApiResponse<Void>> deleteMedicine(
            @Parameter(description = "Medicine ID") @PathVariable Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.ok(ApiResponse.success("Medicine deleted successfully", null));
    }
}