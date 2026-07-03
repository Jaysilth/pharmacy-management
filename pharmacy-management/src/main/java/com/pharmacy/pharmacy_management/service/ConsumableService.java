package com.pharmacy.pharmacy_management.service;

import com.pharmacy.pharmacy_management.dto.ConsumableRequestDTO;
import com.pharmacy.pharmacy_management.dto.ConsumableResponseDTO;
import com.pharmacy.pharmacy_management.dto.ConsumableUsageRequestDTO;
import com.pharmacy.pharmacy_management.dto.ConsumableUsageResponseDTO;
import com.pharmacy.pharmacy_management.entity.Consumable;
import com.pharmacy.pharmacy_management.entity.ConsumableUsage;
import com.pharmacy.pharmacy_management.entity.Surgery;
import com.pharmacy.pharmacy_management.exception.InsufficientStockException;
import com.pharmacy.pharmacy_management.repository.ConsumableRepository;
import com.pharmacy.pharmacy_management.repository.ConsumableUsageRepository;
import com.pharmacy.pharmacy_management.repository.SurgeryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsumableService {

    private final ConsumableRepository      consumableRepo;
    private final ConsumableUsageRepository usageRepo;
    private final SurgeryRepository         surgeryRepo;

    // ── Consumables CRUD ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ConsumableResponseDTO> getAll(String search) {
        List<Consumable> list = (search != null && !search.isBlank())
                ? consumableRepo.searchByName(search)
                : consumableRepo.findAllByOrderByNameAsc();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConsumableResponseDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ConsumableResponseDTO> getLowStock() {
        return consumableRepo.findLowStock().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ConsumableResponseDTO create(ConsumableRequestDTO dto) {
        Consumable c = Consumable.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .unit(dto.getUnit())
                .quantityInStock(dto.getQuantityInStock())
                .reorderLevel(dto.getReorderLevel() != null ? dto.getReorderLevel() : 5)
                .build();
        return toDTO(consumableRepo.save(c));
    }

    public ConsumableResponseDTO update(Long id, ConsumableRequestDTO dto) {
        Consumable c = findOrThrow(id);
        c.setName(dto.getName());
        c.setDescription(dto.getDescription());
        c.setUnit(dto.getUnit());
        c.setQuantityInStock(dto.getQuantityInStock());
        if (dto.getReorderLevel() != null) c.setReorderLevel(dto.getReorderLevel());
        return toDTO(consumableRepo.save(c));
    }

    public void delete(Long id) {
        consumableRepo.delete(findOrThrow(id));
    }

    // ── Usage ─────────────────────────────────────────────────────────────────

    /**
     * Record usage of one consumable.
     *
     * Atomically:
     * 1. Validates stock is sufficient
     * 2. Deducts quantityUsed from quantityInStock
     * 3. Creates a ConsumableUsage log entry
     *
     * All inside a single transaction — if either step fails, both roll back.
     */
    public ConsumableUsageResponseDTO recordUsage(ConsumableUsageRequestDTO dto) {
        validateLinkedEntity(dto);

        Consumable consumable = findOrThrow(dto.getConsumableId());

        if (consumable.getQuantityInStock() < dto.getQuantityUsed()) {
            throw new InsufficientStockException(String.format(
                    "Insufficient stock for %s. Available: %d %s, Requested: %d",
                    consumable.getName(), consumable.getQuantityInStock(),
                    consumable.getUnit(), dto.getQuantityUsed()));
        }

        // Deduct stock
        consumable.setQuantityInStock(consumable.getQuantityInStock() - dto.getQuantityUsed());
        consumableRepo.save(consumable);

        // Resolve surgery entity if provided
        Surgery surgery = null;
        if (dto.getSurgeryId() != null) {
            surgery = surgeryRepo.findById(dto.getSurgeryId())
                    .orElseThrow(() -> new RuntimeException("Surgery not found: " + dto.getSurgeryId()));
        }

        // Create usage log
        ConsumableUsage usage = ConsumableUsage.builder()
                .consumable(consumable)
                .quantityUsed(dto.getQuantityUsed())
                .usedBy(dto.getUsedBy())
                .notes(dto.getNotes())
                .linkedEntityType(dto.getLinkedEntityType())
                .surgery(surgery)
                .procedureRef(dto.getProcedureRef())
                .labTestRef(dto.getLabTestRef())
                .build();

        return toUsageDTO(usageRepo.save(usage));
    }

    @Transactional(readOnly = true)
    public List<ConsumableUsageResponseDTO> getAllUsage() {
        return usageRepo.findAllByOrderByUsedAtDesc().stream()
                .map(this::toUsageDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsumableUsageResponseDTO> getUsageByConsumable(Long consumableId) {
        return usageRepo.findByConsumableIdOrderByUsedAtDesc(consumableId).stream()
                .map(this::toUsageDTO).collect(Collectors.toList());
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private void validateLinkedEntity(ConsumableUsageRequestDTO dto) {
        int linked = 0;
        if (dto.getSurgeryId() != null)   linked++;
        if (dto.getProcedureRef() != null && !dto.getProcedureRef().isBlank()) linked++;
        if (dto.getLabTestRef() != null   && !dto.getLabTestRef().isBlank())   linked++;
        if (linked > 1) {
            throw new IllegalArgumentException(
                    "Only one of surgeryId, procedureRef, or labTestRef may be set per usage record.");
        }
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private Consumable findOrThrow(Long id) {
        return consumableRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Consumable not found with id: " + id));
    }

    private ConsumableResponseDTO toDTO(Consumable c) {
        return ConsumableResponseDTO.builder()
                .id(c.getId()).name(c.getName()).description(c.getDescription())
                .unit(c.getUnit()).quantityInStock(c.getQuantityInStock())
                .reorderLevel(c.getReorderLevel()).lowStock(c.isLowStock())
                .createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt())
                .build();
    }

    private ConsumableUsageResponseDTO toUsageDTO(ConsumableUsage u) {
        return ConsumableUsageResponseDTO.builder()
                .id(u.getId())
                .consumableId(u.getConsumable().getId())
                .consumableName(u.getConsumable().getName())
                .unit(u.getConsumable().getUnit())
                .quantityUsed(u.getQuantityUsed())
                .usedBy(u.getUsedBy())
                .notes(u.getNotes())
                .usedAt(u.getUsedAt())
                .linkedEntityType(u.getLinkedEntityType())
                .surgeryId(u.getSurgery() != null ? u.getSurgery().getId() : null)
                .surgeryName(u.getSurgery() != null ? u.getSurgery().getName() : null)
                .procedureRef(u.getProcedureRef())
                .labTestRef(u.getLabTestRef())
                .build();
    }
}