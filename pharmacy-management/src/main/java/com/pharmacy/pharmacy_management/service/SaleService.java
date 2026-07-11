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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleService {

    private final SaleRepository              saleRepository;
    private final MedicineRepository          medicineRepository;
    private final GlassesRepository           glassesRepository;
    private final GlassesAccessoryRepository  glassesAccessoryRepository;
    private final GlassesRepairRepository     glassesRepairRepository;
    private final SurgeryRepository           surgeryRepository;
    private final MedicineService             medicineService;
    private final GlassesService              glassesService;
    private final GlassesAccessoryService     glassesAccessoryService;

    public SaleResponseDTO createSale(SaleRequestDTO request) {
        String saleNumber = "SAL-" + System.currentTimeMillis();

        Sale sale = Sale.builder()
                .saleNumber(saleNumber)
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .items(new ArrayList<>())
                .quantity(0)
                .unitPrice(BigDecimal.ZERO)
                .build();

        BigDecimal grandTotal = BigDecimal.ZERO;

        for (SaleRequestDTO.SaleItemInput input : request.getItems()) {
            SaleItem saleItem = resolveItem(sale, input);
            sale.getItems().add(saleItem);
            grandTotal = grandTotal.add(saleItem.getSubtotal());
        }

        if (request.getDiscountType() != null && request.getDiscountAmount() != null) {
            sale.setDiscountType(request.getDiscountType());
            sale.setDiscountValue(request.getDiscountValue());
            sale.setDiscountAmount(request.getDiscountAmount());

            BigDecimal finalTotal = grandTotal.subtract(request.getDiscountAmount());
            if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
                finalTotal = BigDecimal.ZERO;
            }
            sale.setGrandTotal(finalTotal);
            sale.setTotalPrice(finalTotal);
        } else {
            sale.setGrandTotal(grandTotal);
            sale.setTotalPrice(grandTotal);
        }

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
                itemName  = med.getName();
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
                itemName  = g.getName() + (g.getBrand() != null ? " (" + g.getBrand() + ")" : "");
                unitPrice = g.getPrice();
                break;
            }
            case "GLASSES_ACCESSORY": {
                GlassesAccessory acc = glassesAccessoryRepository.findById(input.getItemId())
                        .orElseThrow(() -> new RuntimeException("Accessory not found: " + input.getItemId()));
                if (acc.getQuantity() < input.getQuantity())
                    throw new InsufficientStockException(String.format(
                            "Insufficient stock for %s. Available: %d, Requested: %d",
                            acc.getName(), acc.getQuantity(), input.getQuantity()));
                glassesAccessoryService.reduceStock(acc.getId(), input.getQuantity());
                itemName  = acc.getName();
                unitPrice = acc.getPrice();
                break;
            }
            case "GLASSES_REPAIR": {
                GlassesRepair repair = glassesRepairRepository.findById(input.getItemId())
                        .orElseThrow(() -> new RuntimeException("Repair service not found: " + input.getItemId()));
                itemName  = repair.getName();
                unitPrice = repair.getPrice();
                break;
            }
            case "SURGERY": {
                Surgery s = surgeryRepository.findById(input.getItemId())
                        .orElseThrow(() -> new RuntimeException("Surgery not found: " + input.getItemId()));
                itemName  = s.getName();
                unitPrice = s.getPrice();
                break;
            }
            // localStorage-backed clinical items — name and price come from frontend
            case "CLINIC_VISIT":
            case "PROCEDURE":
            case "LAB_TEST": {
                if (input.getItemName() == null || input.getUnitPrice() == null)
                    throw new IllegalArgumentException(
                            input.getItemType() + " requires itemName and unitPrice fields.");
                itemName  = input.getItemName();
                unitPrice = input.getUnitPrice();
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown item type: " + input.getItemType());
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(input.getQuantity()));
        return SaleItem.builder()
                .sale(sale)
                .itemType(input.getItemType().toUpperCase())
                .itemId(input.getItemId() != null ? input.getItemId() : 0L)
                .itemName(itemName)
                .quantity(input.getQuantity())
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .build();
    }

    // ── Read ──────────────────────────────────────────────────────────────────

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

    public void deleteSale(Long id) {
        saleRepository.delete(saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found: " + id)));
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
        Map<LocalDate, BigDecimal> totals = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) totals.put(start.plusDays(i), BigDecimal.ZERO);
        saleRepository.findSalesByDateRange(start.atStartOfDay(), end.atTime(LocalTime.MAX))
                .forEach(s -> {
                    LocalDate d = s.getCreatedAt().toLocalDate();
                    BigDecimal amt = effectiveTotal(s);
                    totals.compute(d, (k, v) -> v == null ? amt : v.add(amt));
                });
        return totals.entrySet().stream()
                .map(e -> SalesByDayDTO.builder().date(e.getKey().toString()).totalRevenue(e.getValue()).build())
                .collect(Collectors.toList());
    }

    /**
     * Revenue grouped into calendar-aligned buckets.
     *
     * "week" = ISO calendar week, Monday–Sunday. "month" = calendar month.
     * "year" = calendar year. Buckets are calendar-aligned, not rolling
     * windows — e.g. "month" always starts on the 1st, not "30 days ago."
     *
     * count = how many buckets to return, most recent last (oldest first
     * in the returned list), same ordering convention as getSalesByDay.
     */
    public List<RevenueByPeriodDTO> getRevenueByPeriod(String period, int count) {
        if (count < 1) {
            throw new IllegalArgumentException("count must be at least 1");
        }

        LocalDate today = LocalDate.now();
        List<LocalDate> bucketStarts = new ArrayList<>();

        switch (period) {
            case "week" -> {
                LocalDate thisWeekStart = sundayWeekStart(today);
                for (int i = count - 1; i >= 0; i--) bucketStarts.add(thisWeekStart.minusWeeks(i));
            }
            case "month" -> {
                LocalDate thisMonthStart = today.withDayOfMonth(1);
                for (int i = count - 1; i >= 0; i--) bucketStarts.add(thisMonthStart.minusMonths(i));
            }
            case "year" -> {
                LocalDate thisYearStart = today.withDayOfYear(1);
                for (int i = count - 1; i >= 0; i--) bucketStarts.add(thisYearStart.minusYears(i));
            }
            default -> throw new IllegalArgumentException("period must be one of: week, month, year");
        }

        LocalDate rangeStart = bucketStarts.get(0);

        Map<LocalDate, BigDecimal> totals = new LinkedHashMap<>();
        bucketStarts.forEach(b -> totals.put(b, BigDecimal.ZERO));

        saleRepository.findSalesByDateRange(rangeStart.atStartOfDay(), today.atTime(LocalTime.MAX))
                .forEach(s -> {
                    LocalDate bucketKey = bucketStartFor(s.getCreatedAt().toLocalDate(), period);
                    BigDecimal amt = effectiveTotal(s);
                    totals.computeIfPresent(bucketKey, (k, v) -> v.add(amt));
                });

        return bucketStarts.stream()
                .map(b -> RevenueByPeriodDTO.builder()
                        .label(formatPeriodLabel(b, period))
                        .periodStart(b.toString())
                        .periodEnd(bucketEndFor(b, period).toString())
                        .totalRevenue(totals.get(b))
                        .build())
                .collect(Collectors.toList());
    }

    // NOTE: date.with(DayOfWeek.SUNDAY) is NOT what we want here — that
    // moves within the ISO Monday–Sunday week and lands on the *last* day
    // of the current week, not the first day of a Sunday-start week. This
    // computes the actual Sunday that begins the Sunday–Saturday week
    // containing `date`.
    private LocalDate sundayWeekStart(LocalDate date) {
        int isoDayOfWeek = date.getDayOfWeek().getValue(); // Mon=1 ... Sun=7
        int daysSinceSunday = isoDayOfWeek % 7;             // Sun=0, Mon=1, ... Sat=6
        return date.minusDays(daysSinceSunday);
    }

    private LocalDate bucketStartFor(LocalDate date, String period) {
        return switch (period) {
            case "week"  -> sundayWeekStart(date);
            case "month" -> date.withDayOfMonth(1);
            case "year"  -> date.withDayOfYear(1);
            default -> date;
        };
    }

    private LocalDate bucketEndFor(LocalDate bucketStart, String period) {
        return switch (period) {
            case "week"  -> bucketStart.plusDays(6);
            case "month" -> bucketStart.withDayOfMonth(bucketStart.lengthOfMonth());
            case "year"  -> bucketStart.withDayOfYear(bucketStart.lengthOfYear());
            default -> bucketStart;
        };
    }

    private String formatPeriodLabel(LocalDate bucketStart, String period) {
        return switch (period) {
            case "week"  -> "Week of " + bucketStart.format(DateTimeFormatter.ofPattern("MMM d"));
            case "month" -> bucketStart.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            case "year"  -> String.valueOf(bucketStart.getYear());
            default -> bucketStart.toString();
        };
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private SaleResponseDTO mapToResponseDTO(Sale sale) {
        List<SaleItemResponseDTO> itemDTOs;
        if (sale.getItems() != null && !sale.getItems().isEmpty()) {
            itemDTOs = sale.getItems().stream()
                    .map(i -> SaleItemResponseDTO.builder()
                            .itemType(i.getItemType()).itemId(i.getItemId())
                            .itemName(i.getItemName()).quantity(i.getQuantity())
                            .unitPrice(i.getUnitPrice()).subtotal(i.getSubtotal()).build())
                    .collect(Collectors.toList());
        } else if (sale.getMedicine() != null) {
            itemDTOs = List.of(SaleItemResponseDTO.builder()
                    .itemType("MEDICINE").itemId(sale.getMedicine().getId())
                    .itemName(sale.getMedicine().getName())
                    .quantity(sale.getQuantity()).unitPrice(sale.getUnitPrice())
                    .subtotal(sale.getTotalPrice()).build());
        } else {
            itemDTOs = List.of();
        }

        return SaleResponseDTO.builder()
                .id(sale.getId())
                .saleNumber(sale.getSaleNumber() != null ? sale.getSaleNumber() : "SAL-" + sale.getId())
                .customerName(sale.getCustomerName()).customerPhone(sale.getCustomerPhone())
                .paymentMethod(sale.getPaymentMethod()).notes(sale.getNotes())
                .grandTotal(effectiveTotal(sale)).items(itemDTOs).createdAt(sale.getCreatedAt())
                .medicine(sale.getMedicine() != null ? SaleResponseDTO.MedicineInfo.builder()
                        .id(sale.getMedicine().getId()).name(sale.getMedicine().getName())
                        .manufacturer(sale.getMedicine().getManufacturer()).build() : null)
                .quantity(sale.getQuantity()).unitPrice(sale.getUnitPrice()).totalPrice(sale.getTotalPrice())
                .discountType(sale.getDiscountType())
                .discountValue(sale.getDiscountValue())
                .discountAmount(sale.getDiscountAmount())
                .build();
    }

    private BigDecimal effectiveTotal(Sale sale) {
        if (sale.getGrandTotal() != null) return sale.getGrandTotal();
        if (sale.getTotalPrice() != null) return sale.getTotalPrice();
        return BigDecimal.ZERO;
    }
}