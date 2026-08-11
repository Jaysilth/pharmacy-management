package com.pharmacy.pharmacy_management.service;

import com.pharmacy.pharmacy_management.dto.*;
import com.pharmacy.pharmacy_management.entity.*;
import com.pharmacy.pharmacy_management.exception.InsufficientStockException;
import com.pharmacy.pharmacy_management.exception.MedicineNotFoundException;
import com.pharmacy.pharmacy_management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleService {

    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

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

        applyBackdatingIfRequested(sale, request.getSaleDate(), request.getBackdateReason());

        return mapToResponseDTO(saleRepository.save(sale));
    }

    /**
     * How far back a SUPER_ADMIN can backdate a forgotten sale. Wider than
     * "current month" on purpose, so a sale missed in the last days of a
     * month can still be fixed early the next month — but bounded, since
     * an unbounded window means anyone with SUPER_ADMIN can restate any
     * period's revenue at will with no way to catch it after the fact.
     */
    private static final int BACKDATE_WINDOW_DAYS = 90;

    /**
     * Emergency backdating — for a sale that genuinely happened but was
     * never entered on the day. SUPER_ADMIN only, bounded to the last
     * {@link #BACKDATE_WINDOW_DAYS} days, and requires a written reason.
     * createdAt is left untouched — it always reflects when the row was
     * actually inserted. saleDate carries the business-effective date and
     * is what every revenue/reporting query reads off, so a backdated sale
     * correctly lands in the period it actually happened in.
     */
    private void applyBackdatingIfRequested(Sale sale, LocalDate saleDate, String reason) {
        if (saleDate == null) {
            return; // normal sale — saleDate defaults to today via @PrePersist
        }

        boolean isSuperAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_SUPER_ADMIN));
        if (!isSuperAdmin) {
            throw new AccessDeniedException("Only a SUPER_ADMIN can backdate a sale.");
        }

        LocalDate today = LocalDate.now();
        if (saleDate.isAfter(today)) {
            throw new IllegalArgumentException("Sale date cannot be in the future.");
        }
        if (saleDate.isBefore(today.minusDays(BACKDATE_WINDOW_DAYS))) {
            throw new IllegalArgumentException(
                    "A sale can only be backdated within the last " + BACKDATE_WINDOW_DAYS
                            + " days. For anything older, correct it manually or note it as an adjustment.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A reason is required to backdate a sale.");
        }

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        sale.setSaleDate(saleDate);

        String auditNote = "[Backdated by " + currentUsername + " on " + today
                + " — reason: " + reason.trim() + "]";
        sale.setNotes(sale.getNotes() != null && !sale.getNotes().isBlank()
                ? sale.getNotes() + " " + auditNote
                : auditNote);
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
        LocalDate today = LocalDate.now();
        return (int) saleRepository.countBySaleDateBetween(today, today);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenueToday() {
        LocalDate today = LocalDate.now();
        BigDecimal rev = saleRepository.getTotalRevenueBySaleDateRange(today, today);
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
        saleRepository.findSalesBySaleDateRange(start, end)
                .forEach(s -> {
                    LocalDate d = s.getSaleDate();
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
     * "week" = Sunday–Saturday. "month" = calendar month. "year" = calendar
     * year. Buckets are calendar-aligned, not rolling windows — e.g.
     * "month" always starts on the 1st, not "30 days ago."
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

        saleRepository.findSalesBySaleDateRange(rangeStart, today)
                .forEach(s -> {
                    LocalDate bucketKey = bucketStartFor(s.getSaleDate(), period);
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

    // ── Category summary ─────────────────────────────────────────────────────

    /**
     * Weekly count of line items per category (MEDICINE, GLASSES, SURGERY,
     * etc.) — "how many of each tab was sold/done." Counts line items, not
     * summed quantity: a surgery/procedure/repair line is always qty-1
     * conceptually, so counting rows is the one definition that means the
     * same thing across all 8 sellable categories.
     *
     * Buckets by saleDate, same as getRevenueByPeriod — a backdated sale
     * lands in the week it actually happened in, not the week it was typed
     * into the system.
     *
     * weeks = how many Sunday–Saturday buckets to return, oldest first.
     */
    public List<CategorySummaryDTO> getCategorySummaryByWeek(int weeks) {
        if (weeks < 1) {
            throw new IllegalArgumentException("weeks must be at least 1");
        }

        LocalDate today = LocalDate.now();
        LocalDate thisWeekStart = sundayWeekStart(today);
        List<LocalDate> bucketStarts = new ArrayList<>();
        for (int i = weeks - 1; i >= 0; i--) bucketStarts.add(thisWeekStart.minusWeeks(i));

        LocalDate rangeStart = bucketStarts.get(0);

        Map<LocalDate, Map<String, Long>> counts = new LinkedHashMap<>();
        bucketStarts.forEach(b -> counts.put(b, new LinkedHashMap<>()));

        saleRepository.findSalesBySaleDateRange(rangeStart, today)
                .forEach(sale -> {
                    LocalDate bucketKey = sundayWeekStart(sale.getSaleDate());
                    Map<String, Long> bucketCounts = counts.get(bucketKey);
                    if (bucketCounts == null) return; // outside range — shouldn't happen, but be safe
                    for (String itemType : resolveItemTypes(sale)) {
                        bucketCounts.merge(itemType, 1L, Long::sum);
                    }
                });

        return bucketStarts.stream()
                .map(b -> CategorySummaryDTO.builder()
                        .label(formatPeriodLabel(b, "week"))
                        .periodStart(b.toString())
                        .periodEnd(bucketEndFor(b, "week").toString())
                        .counts(counts.get(b))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Mirrors the same fallback rule used in mapToResponseDTO below (prefer
     * the itemized `items` list, fall back to the legacy single-medicine
     * fields on Sale for older rows that predate the multi-item cart) —
     * kept separate since this only needs itemType, not the full DTO shape.
     */
    private List<String> resolveItemTypes(Sale sale) {
        if (sale.getItems() != null && !sale.getItems().isEmpty()) {
            return sale.getItems().stream()
                    .map(SaleItem::getItemType)
                    .collect(Collectors.toList());
        } else if (sale.getMedicine() != null) {
            return List.of("MEDICINE");
        }
        return List.of();
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
                .saleDate(sale.getSaleDate())
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