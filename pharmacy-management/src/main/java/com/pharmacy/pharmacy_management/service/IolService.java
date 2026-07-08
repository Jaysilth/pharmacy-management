package com.pharmacy.pharmacy_management.service;

import com.pharmacy.pharmacy_management.dto.IolRequestDTO;
import com.pharmacy.pharmacy_management.dto.IolResponseDTO;
import com.pharmacy.pharmacy_management.dto.IolUsageRequestDTO;
import com.pharmacy.pharmacy_management.dto.IolUsageResponseDTO;
import com.pharmacy.pharmacy_management.entity.Iol;
import com.pharmacy.pharmacy_management.entity.IolUsage;
import com.pharmacy.pharmacy_management.entity.Surgery;
import com.pharmacy.pharmacy_management.exception.InsufficientStockException;
import com.pharmacy.pharmacy_management.repository.IolRepository;
import com.pharmacy.pharmacy_management.repository.IolUsageRepository;
import com.pharmacy.pharmacy_management.repository.SurgeryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class IolService {

    private final IolRepository      iolRepo;
    private final IolUsageRepository usageRepo;
    private final SurgeryRepository  surgeryRepo;

    // ── IOL CRUD ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<IolResponseDTO> getAll(String search) {
        List<Iol> list = (search != null && !search.isBlank())
                ? iolRepo.searchByName(search)
                : iolRepo.findAllByOrderByNameAscPowerAsc();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IolResponseDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<IolResponseDTO> getLowStock() {
        return iolRepo.findLowStock().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Creates a new IOL stock row. If an identical (name, type, power) row
     * already exists, merges into it by adding the incoming quantity rather
     * than creating a duplicate row — same intent as Medicine's batch
     * dedup, simplified since IOLs don't need distinct batch labels.
     */
    public IolResponseDTO create(IolRequestDTO dto) {
        List<Iol> existing = iolRepo.findByNameIgnoreCaseAndTypeAndPower(
                dto.getName().trim(), dto.getType(), dto.getPower());

        if (!existing.isEmpty()) {
            Iol match = existing.get(0);
            match.setQuantityInStock(match.getQuantityInStock() + dto.getQuantityInStock());
            if (dto.getReorderLevel() != null) match.setReorderLevel(dto.getReorderLevel());
            if (dto.getManufacturer() != null) match.setManufacturer(dto.getManufacturer());
            if (dto.getDescription() != null)  match.setDescription(dto.getDescription());
            return toDTO(iolRepo.save(match));
        }

        Iol i = Iol.builder()
                .name(dto.getName().trim())
                .type(dto.getType())
                .power(dto.getPower())
                .manufacturer(dto.getManufacturer())
                .description(dto.getDescription())
                .quantityInStock(dto.getQuantityInStock())
                .reorderLevel(dto.getReorderLevel() != null ? dto.getReorderLevel() : 3)
                .build();
        return toDTO(iolRepo.save(i));
    }

    public IolResponseDTO update(Long id, IolRequestDTO dto) {
        Iol i = findOrThrow(id);
        i.setName(dto.getName().trim());
        i.setType(dto.getType());
        i.setPower(dto.getPower());
        i.setManufacturer(dto.getManufacturer());
        i.setDescription(dto.getDescription());
        i.setQuantityInStock(dto.getQuantityInStock());
        if (dto.getReorderLevel() != null) i.setReorderLevel(dto.getReorderLevel());
        return toDTO(iolRepo.save(i));
    }

    public void delete(Long id) {
        iolRepo.delete(findOrThrow(id));
    }

    // ── Usage (always surgery-linked) ───────────────────────────────────────

    /**
     * Records implantation of one IOL. Atomically validates stock, deducts
     * it, and logs the usage tied to a Surgery. Mirrors ConsumableService's
     * recordUsage but simplified: no polymorphic linked-entity handling
     * since IOLs only ever attach to a Surgery.
     */
    public IolUsageResponseDTO recordUsage(IolUsageRequestDTO dto) {
        Iol iol = findOrThrow(dto.getIolId());

        if (iol.getQuantityInStock() < dto.getQuantityUsed()) {
            throw new InsufficientStockException(String.format(
                    "Insufficient stock for %s %s %.2fD. Available: %d, Requested: %d",
                    iol.getName(), iol.getType(), iol.getPower(),
                    iol.getQuantityInStock(), dto.getQuantityUsed()));
        }

        Surgery surgery = surgeryRepo.findById(dto.getSurgeryId())
                .orElseThrow(() -> new RuntimeException("Surgery not found: " + dto.getSurgeryId()));

        iol.setQuantityInStock(iol.getQuantityInStock() - dto.getQuantityUsed());
        iolRepo.save(iol);

        IolUsage usage = IolUsage.builder()
                .iol(iol)
                .quantityUsed(dto.getQuantityUsed())
                .surgery(surgery)
                .usedBy(dto.getUsedBy())
                .notes(dto.getNotes())
                .build();

        return toUsageDTO(usageRepo.save(usage));
    }

    @Transactional(readOnly = true)
    public List<IolUsageResponseDTO> getAllUsage() {
        return usageRepo.findAllByOrderByUsedAtDesc().stream()
                .map(this::toUsageDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IolUsageResponseDTO> getUsageByIol(Long iolId) {
        return usageRepo.findByIolIdOrderByUsedAtDesc(iolId).stream()
                .map(this::toUsageDTO).collect(Collectors.toList());
    }

    /** Restores deducted stock before removing the log row. */
    public void deleteUsage(Long usageId) {
        IolUsage usage = usageRepo.findById(usageId)
                .orElseThrow(() -> new RuntimeException("Usage entry not found with id: " + usageId));

        Iol iol = usage.getIol();
        iol.setQuantityInStock(iol.getQuantityInStock() + usage.getQuantityUsed());
        iolRepo.save(iol);

        usageRepo.delete(usage);
    }

    // ── Mapping ──────────────────────────────────────────────────────────────

    private Iol findOrThrow(Long id) {
        return iolRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("IOL not found with id: " + id));
    }

    private IolResponseDTO toDTO(Iol i) {
        return IolResponseDTO.builder()
                .id(i.getId()).name(i.getName()).type(i.getType()).power(i.getPower())
                .manufacturer(i.getManufacturer()).description(i.getDescription())
                .quantityInStock(i.getQuantityInStock()).reorderLevel(i.getReorderLevel())
                .lowStock(i.isLowStock())
                .createdAt(i.getCreatedAt()).updatedAt(i.getUpdatedAt())
                .build();
    }

    private IolUsageResponseDTO toUsageDTO(IolUsage u) {
        return IolUsageResponseDTO.builder()
                .id(u.getId())
                .iolId(u.getIol().getId())
                .iolName(u.getIol().getName())
                .iolPower(u.getIol().getPower())
                .quantityUsed(u.getQuantityUsed())
                .surgeryId(u.getSurgery().getId())
                .surgeryName(u.getSurgery().getName())
                .usedBy(u.getUsedBy())
                .notes(u.getNotes())
                .usedAt(u.getUsedAt())
                .build();
    }
}
