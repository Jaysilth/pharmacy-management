package com.pharmacy.pharmacy_management.service;

import com.pharmacy.pharmacy_management.dto.*;
import com.pharmacy.pharmacy_management.entity.*;
import com.pharmacy.pharmacy_management.exception.InsufficientStockException;
import com.pharmacy.pharmacy_management.exception.MedicineNotFoundException;
import com.pharmacy.pharmacy_management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleService {

    private final SaleRepository      saleRepository;
    private final MedicineRepository  medicineRepository;
    private final GlassesRepository   glassesRepository;
    private final SurgeryRepository   surgeryRepository;
    private final MedicineService     medicineService;
    private final GlassesService      glassesService;

    public SaleResponseDTO createSale(SaleRequestDTO request) {
        String saleNumber = "SAL-" + System.currentTimeMillis();

        Sale sale = Sale.builder()
                .saleNumber(saleNumber)
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .items(new ArrayList<>())
                // Legacy fields — set to zero for new multi-item sales so existing
                // NOT NULL constraints (if present) are satisfied. The real totals
                // are stored in grandTotal and in the sale_items table.
                .quantity(0)
                .unitPrice(BigDecimal.ZERO)
                .build();

        BigDecimal grandTotal = BigDecimal.ZERO;

        for (SaleRequestDTO.SaleItemInput input : request.getItems()) {
            SaleItem saleItem = resolveItem(sale, input);
            sale.getItems().add(saleItem);
            grandTotal = grandTotal.add(saleItem.getSubtotal());
        }

        sale.setGrandTotal(grandTotal);
        sale.setTotalPrice(grandTotal); // kept for dashboard backward compat

        return mapToResponseDTO(saleRepository.save(sale));
    }

    private SaleItem resolveItem(Sale sale, SaleRequestDTO.SaleItemInput input) {
        String itemName;
        BigDecimal unitPrice;

        switch (input.getItemType().toUpperCase()) {
            case "MEDICINE": {
                Medicine med = medicineRepository.findById(input.getItemId())
                        .orElseThrow(() -> new MedicineNotFoundException("Medicine not found: " + input.getItemId()));
                if (med.isExpired())
                    throw new IllegalStateException("Cannot sell expired medicine: " + med.getName());
                if (med.getQuantity() < input.getQuantity())
                    throw new InsufficientStockException(String.format(
                            "Insufficient stock for %s. Available: %d, Requested: %d",
                            med.getName(), med.getQuantity(), input.getQuantity()));
                medicineService.reduceStock(med.getId(), input.getQuantity());
                itemName = med.getName();
                unitPrice = med.getPrice();
                break;
            }
            case "GLASSES": {
                Glasses g = glassesRepository.findById(input.getItemId())
                        .orElseThrow(() -> new RuntimeException("Glasses not found: " + input.getItemId()));
                if (g.getQuantity() < input.getQuantity())
                    throw new InsufficientStockException(String.format(
                            "Insufficient stock for %s. Available: %d, Requested: %d",
                            g.getName(), g.getQuantity(), input.getQuantity()));
                glassesService.reduceStock(g.getId(), input.getQuantity());
                itemName = g.getName() + (g.getBrand() != null ? " (" + g.getBrand() + ")" : "");
                unitPrice = g.getPrice();
                break;
            }
            case "SURGERY": {
                Surgery s = surgeryRepository.findById(input.getItemId())
                        .orElseThrow(() -> new RuntimeException("Surgery not found: " + input.getItemId()));
                itemName = s.getName();
                unitPrice = s.getPrice();
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown item type: " + input.getItemType());
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(input.getQuantity()));

        return SaleItem.builder()
                .sale(sale)
                .itemType(input.getItemType().toUpperCase())
                .itemId(input.getItemId())
                .itemName(itemName)
                .quantity(input.getQuantity())
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getAllSales() {
        return saleRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SaleResponseDTO getSaleById(Long id) {
        return mapToResponseDTO(saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found: " + id)));
    }

    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getRecentSales() {
        return saleRepository.findTop5ByOrderByCreatedAtDesc()
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public int getTotalSalesToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end   = LocalDate.now().atTime(LocalTime.MAX);
        return (int) saleRepository.countByCreatedAtBetween(start, end);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenueToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end   = LocalDate.now().atTime(LocalTime.MAX);
        BigDecimal rev = saleRepository.getTotalRevenueByDateRange(start, end);
        return rev != null ? rev : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        BigDecimal rev = saleRepository.getTotalRevenue();
        return rev != null ? rev : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<SalesByDayDTO> getSalesByDay(int days) {
        LocalDate end   = LocalDate.now();
        LocalDate start = end.minusDays(days - 1);
        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT   = end.atTime(LocalTime.MAX);

        Map<LocalDate, BigDecimal> totals = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) totals.put(start.plusDays(i), BigDecimal.ZERO);

        saleRepository.findSalesByDateRange(startDT, endDT).forEach(sale -> {
            LocalDate saleDate = sale.getCreatedAt().toLocalDate();
            BigDecimal amount  = effectiveTotal(sale);
            totals.compute(saleDate, (k, v) -> v == null ? amount : v.add(amount));
        });

        return totals.entrySet().stream()
                .map(e -> SalesByDayDTO.builder().date(e.getKey().toString()).totalRevenue(e.getValue()).build())
                .collect(Collectors.toList());
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private SaleResponseDTO mapToResponseDTO(Sale sale) {
        List<SaleItemResponseDTO> itemDTOs;

        if (sale.getItems() != null && !sale.getItems().isEmpty()) {
            // New multi-item sale
            itemDTOs = sale.getItems().stream()
                    .map(i -> SaleItemResponseDTO.builder()
                            .itemType(i.getItemType()).itemId(i.getItemId())
                            .itemName(i.getItemName()).quantity(i.getQuantity())
                            .unitPrice(i.getUnitPrice()).subtotal(i.getSubtotal())
                            .build())
                    .collect(Collectors.toList());
        } else if (sale.getMedicine() != null) {
            // Legacy single-medicine sale
            itemDTOs = List.of(SaleItemResponseDTO.builder()
                    .itemType("MEDICINE")
                    .itemId(sale.getMedicine().getId())
                    .itemName(sale.getMedicine().getName())
                    .quantity(sale.getQuantity())
                    .unitPrice(sale.getUnitPrice())
                    .subtotal(sale.getTotalPrice())
                    .build());
        } else {
            itemDTOs = List.of();
        }

        return SaleResponseDTO.builder()
                .id(sale.getId())
                .saleNumber(sale.getSaleNumber() != null ? sale.getSaleNumber() : "SAL-" + sale.getId())
                .customerName(sale.getCustomerName())
                .customerPhone(sale.getCustomerPhone())
                .paymentMethod(sale.getPaymentMethod())
                .notes(sale.getNotes())
                .grandTotal(effectiveTotal(sale))
                .items(itemDTOs)
                .createdAt(sale.getCreatedAt())
                // Legacy fields for old records
                .medicine(sale.getMedicine() != null ? SaleResponseDTO.MedicineInfo.builder()
                        .id(sale.getMedicine().getId())
                        .name(sale.getMedicine().getName())
                        .manufacturer(sale.getMedicine().getManufacturer())
                        .build() : null)
                .quantity(sale.getQuantity())
                .unitPrice(sale.getUnitPrice())
                .totalPrice(sale.getTotalPrice())
                .build();
    }

    private BigDecimal effectiveTotal(Sale sale) {
        if (sale.getGrandTotal() != null) return sale.getGrandTotal();
        if (sale.getTotalPrice() != null) return sale.getTotalPrice();
        return BigDecimal.ZERO;
    }

    public void deleteSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found with id: " + id));
        saleRepository.delete(sale);
    }
}