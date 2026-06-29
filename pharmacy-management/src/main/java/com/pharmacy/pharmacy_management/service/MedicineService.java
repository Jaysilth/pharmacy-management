package com.pharmacy.pharmacy_management.service;

import com.pharmacy.pharmacy_management.dto.BatchCheckResponseDTO;
import com.pharmacy.pharmacy_management.dto.MedicineRequestDTO;
import com.pharmacy.pharmacy_management.dto.MedicineResponseDTO;
import com.pharmacy.pharmacy_management.entity.Medicine;
import com.pharmacy.pharmacy_management.exception.InsufficientStockException;
import com.pharmacy.pharmacy_management.exception.MedicineNotFoundException;
import com.pharmacy.pharmacy_management.repository.MedicineRepository;
import com.pharmacy.pharmacy_management.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final SaleRepository     saleRepository;

    // ── Batch check (called before add to drive UI decisions) ─────────────────

    @Transactional(readOnly = true)
    public BatchCheckResponseDTO checkBatch(String name, BigDecimal price, LocalDate expiryDate) {
        List<Medicine> existing =
                medicineRepository.findByNameIgnoreCaseOrderByBatchLabelAsc(name.trim());

        if (existing.isEmpty()) {
            return BatchCheckResponseDTO.builder()
                    .status("NO_MATCH")
                    .existingBatches(List.of())
                    .build();
        }

        Optional<Medicine> exact = existing.stream()
                .filter(m -> m.getPrice().compareTo(price) == 0
                        && m.getExpiryDate().equals(expiryDate))
                .findFirst();

        if (exact.isPresent()) {
            Medicine m = exact.get();
            return BatchCheckResponseDTO.builder()
                    .status("EXACT_MATCH")
                    .exactMatchId(m.getId())
                    .exactMatchBatchLabel(m.getBatchLabel())
                    .existingBatches(existing.stream().map(this::mapToResponseDTO).collect(Collectors.toList()))
                    .build();
        }

        return BatchCheckResponseDTO.builder()
                .status("NAME_EXISTS")
                .existingBatches(existing.stream().map(this::mapToResponseDTO).collect(Collectors.toList()))
                .build();
    }

    // ── Add / create with batch logic ─────────────────────────────────────────

    public MedicineResponseDTO addMedicine(MedicineRequestDTO dto) {
        String action = dto.getBatchAction(); // null, "AUTO", "UPDATE_BATCH", "NEW_BATCH"

        // Explicit update of a chosen batch
        if ("UPDATE_BATCH".equals(action)) {
            return updateBatch(dto.getTargetBatchId(), dto);
        }

        // Force new batch regardless of duplicates
        if ("NEW_BATCH".equals(action)) {
            return createWithNextBatchLabel(dto);
        }

        // AUTO / null: apply the detection logic
        List<Medicine> existing =
                medicineRepository.findByNameIgnoreCaseOrderByBatchLabelAsc(dto.getName().trim());

        if (existing.isEmpty()) {
            // First ever entry for this drug name
            return createWithBatchLabel(dto, "A");
        }

        // Same name + same price + same expiry → merge quantity into that batch
        Optional<Medicine> exact = existing.stream()
                .filter(m -> m.getPrice().compareTo(dto.getPrice()) == 0
                        && m.getExpiryDate().equals(dto.getExpiryDate()))
                .findFirst();

        if (exact.isPresent()) {
            Medicine m = exact.get();
            m.setQuantity(m.getQuantity() + dto.getQuantity());
            return mapToResponseDTO(medicineRepository.save(m));
        }

        // Name exists but different price/expiry — should have been intercepted by
        // frontend confirmation; fall through to new batch as safe default.
        return createWithNextBatchLabel(dto);
    }

    // ── Read operations ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> getAllMedicines() {
        return medicineRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MedicineResponseDTO getMedicineById(Long id) {
        return mapToResponseDTO(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> getMedicinesByCategory(String category) {
        return medicineRepository.findByCategoryOrderByNameAsc(category).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> searchMedicines(String name) {
        return medicineRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> getExpiredMedicines() {
        return medicineRepository.findExpiredMedicines(LocalDate.now()).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> getLowStockMedicines() {
        return medicineRepository.findLowStockMedicines().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> getExpiringSoonMedicines() {
        LocalDate today    = LocalDate.now();
        LocalDate deadline = today.plusDays(7);
        return medicineRepository.findExpiringSoonMedicines(today, deadline).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getTotalMedicines() { return medicineRepository.count(); }

    @Transactional(readOnly = true)
    public long getLowStockCount() { return medicineRepository.findLowStockMedicines().size(); }

    @Transactional(readOnly = true)
    public long getExpiringSoonCount() {
        LocalDate today = LocalDate.now();
        return medicineRepository.findExpiringSoonMedicines(today, today.plusDays(7)).size();
    }

    // ── Update existing record ────────────────────────────────────────────────

    public MedicineResponseDTO updateMedicine(Long id, MedicineRequestDTO dto) {
        Medicine m = findOrThrow(id);
        m.setName(dto.getName());
        m.setQuantity(dto.getQuantity());
        m.setPrice(dto.getPrice());
        m.setExpiryDate(dto.getExpiryDate());
        if (dto.getLowStockThreshold() != null) m.setLowStockThreshold(dto.getLowStockThreshold());
        m.setDescription(dto.getDescription());
        m.setManufacturer(dto.getManufacturer());
        if (dto.getCategory() != null) m.setCategory(dto.getCategory());
        return mapToResponseDTO(medicineRepository.save(m));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void deleteMedicine(Long id) {
        if (!medicineRepository.existsById(id)) {
            throw new MedicineNotFoundException("Medicine not found with id: " + id);
        }
        saleRepository.deleteByMedicineId(id);
        medicineRepository.deleteById(id);
    }

    // ── Stock adjustments (used by SaleService) ───────────────────────────────

    public void reduceStock(Long medicineId, int quantity) {
        Medicine m = findOrThrow(medicineId);
        if (m.getQuantity() < quantity) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for %s (Batch %s). Available: %d, Requested: %d",
                            m.getName(), m.getBatchLabel(), m.getQuantity(), quantity));
        }
        m.setQuantity(m.getQuantity() - quantity);
        medicineRepository.save(m);
    }

    public void addStock(Long medicineId, int quantity) {
        Medicine m = findOrThrow(medicineId);
        m.setQuantity(m.getQuantity() + quantity);
        medicineRepository.save(m);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Update a specific batch's price, expiry, and quantity. */
    private MedicineResponseDTO updateBatch(Long targetId, MedicineRequestDTO dto) {
        Medicine m = findOrThrow(targetId);
        m.setPrice(dto.getPrice());
        m.setExpiryDate(dto.getExpiryDate());
        m.setQuantity(dto.getQuantity());
        if (dto.getLowStockThreshold() != null) m.setLowStockThreshold(dto.getLowStockThreshold());
        if (dto.getDescription() != null)       m.setDescription(dto.getDescription());
        if (dto.getManufacturer() != null)      m.setManufacturer(dto.getManufacturer());
        if (dto.getCategory() != null)          m.setCategory(dto.getCategory());
        return mapToResponseDTO(medicineRepository.save(m));
    }

    /** Create with auto-incremented batch label (next after max existing). */
    private MedicineResponseDTO createWithNextBatchLabel(MedicineRequestDTO dto) {
        List<Medicine> existing =
                medicineRepository.findByNameIgnoreCaseOrderByBatchLabelAsc(dto.getName().trim());
        String nextLabel = existing.stream()
                .map(Medicine::getBatchLabel)
                .filter(l -> l != null && !l.isEmpty())
                .max(String::compareTo)
                .map(this::incrementLabel)
                .orElse("B"); // fallback: existing with no label treated as A
        return createWithBatchLabel(dto, nextLabel);
    }

    /** Create a new Medicine row with an explicit batch label. */
    private MedicineResponseDTO createWithBatchLabel(MedicineRequestDTO dto, String label) {
        Medicine m = Medicine.builder()
                .name(dto.getName().trim())
                .batchLabel(label)
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .expiryDate(dto.getExpiryDate())
                .lowStockThreshold(dto.getLowStockThreshold() != null ? dto.getLowStockThreshold() : 10)
                .description(dto.getDescription())
                .manufacturer(dto.getManufacturer())
                .category(dto.getCategory())
                .build();
        return mapToResponseDTO(medicineRepository.save(m));
    }

    /**
     * Increment a batch label: A→B, Z→AA, AZ→BA, ZZ→AAA etc.
     * Single-char labels cover A–Z (26 batches) which is more than enough.
     * Multi-char overflow handled for completeness.
     */
    private String incrementLabel(String label) {
        char[] chars = label.toCharArray();
        int i = chars.length - 1;
        while (i >= 0) {
            if (chars[i] < 'Z') {
                chars[i]++;
                return new String(chars);
            }
            chars[i] = 'A';
            i--;
        }
        // All chars were Z (e.g. "ZZ") → prepend A
        return "A" + new String(chars);
    }

    private Medicine findOrThrow(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with id: " + id));
    }

    private MedicineResponseDTO mapToResponseDTO(Medicine m) {
        return MedicineResponseDTO.builder()
                .id(m.getId())
                .name(m.getName())
                .batchLabel(m.getBatchLabel())
                .quantity(m.getQuantity())
                .price(m.getPrice())
                .expiryDate(m.getExpiryDate())
                .lowStockThreshold(m.getLowStockThreshold())
                .description(m.getDescription())
                .manufacturer(m.getManufacturer())
                .category(m.getCategory())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .isExpired(m.isExpired())
                .isLowStock(m.isLowStock())
                .build();
    }
}