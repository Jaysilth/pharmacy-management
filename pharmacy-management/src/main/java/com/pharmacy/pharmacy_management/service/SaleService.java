package com.pharmacy.pharmacy_management.service;

import com.pharmacy.pharmacy_management.dto.SaleRequestDTO;
import com.pharmacy.pharmacy_management.dto.SaleResponseDTO;
import com.pharmacy.pharmacy_management.entity.Medicine;
import com.pharmacy.pharmacy_management.entity.Sale;
import com.pharmacy.pharmacy_management.exception.InsufficientStockException;
import com.pharmacy.pharmacy_management.exception.MedicineNotFoundException;
import com.pharmacy.pharmacy_management.repository.MedicineRepository;
import com.pharmacy.pharmacy_management.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleService {

    private final SaleRepository saleRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineService medicineService;

    public SaleResponseDTO createSale(SaleRequestDTO requestDTO) {
        // Get medicine from database
        Medicine medicine = medicineRepository.findById(requestDTO.getMedicineId())
                .orElseThrow(() -> new MedicineNotFoundException(
                        "Medicine not found with id: " + requestDTO.getMedicineId()));

        // Check if medicine is expired
        if (medicine.isExpired()) {
            throw new IllegalStateException("Cannot sell expired medicine: " + medicine.getName());
        }

        // Validate stock availability (stock validation)
        if (medicine.getQuantity() < requestDTO.getQuantity()) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for %s. Available: %d, Requested: %d",
                            medicine.getName(), medicine.getQuantity(), requestDTO.getQuantity()));
        }

        // Calculate total price
        BigDecimal unitPrice = medicine.getPrice();
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(requestDTO.getQuantity()));

        // Create sale record
        Sale sale = Sale.builder()
                .medicine(medicine)
                .quantity(requestDTO.getQuantity())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .build();

        // Save sale first
        Sale savedSale = saleRepository.save(sale);

        // Automatically reduce stock (stock deduction)
        medicineService.reduceStock(medicine.getId(), requestDTO.getQuantity());

        return mapToResponseDTO(savedSale);
    }

    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getAllSales() {
        return saleRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SaleResponseDTO getSaleById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found with id: " + id));
        return mapToResponseDTO(sale);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = saleRepository.getTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    private SaleResponseDTO mapToResponseDTO(Sale sale) {
        return SaleResponseDTO.builder()
                .id(sale.getId())
                .medicine(SaleResponseDTO.MedicineInfo.builder()
                        .id(sale.getMedicine().getId())
                        .name(sale.getMedicine().getName())
                        .manufacturer(sale.getMedicine().getManufacturer())
                        .build())
                .quantity(sale.getQuantity())
                .unitPrice(sale.getUnitPrice())
                .totalPrice(sale.getTotalPrice())
                .createdAt(sale.getCreatedAt())
                .build();
    }
}