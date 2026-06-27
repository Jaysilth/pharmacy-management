package com.pharmacy.pharmacy_management.service;

import com.pharmacy.pharmacy_management.dto.*;
import com.pharmacy.pharmacy_management.entity.GlassesRepair;
import com.pharmacy.pharmacy_management.repository.GlassesRepairRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GlassesRepairService {

    private final GlassesRepairRepository repo;

    @Transactional(readOnly = true)
    public List<GlassesRepairResponseDTO> getAll() {
        return repo.findByActiveTrueOrderByNameAsc().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GlassesRepairResponseDTO getById(Long id) { return toDTO(findOrThrow(id)); }

    public GlassesRepairResponseDTO create(GlassesRepairRequestDTO dto) {
        return toDTO(repo.save(GlassesRepair.builder()
                .name(dto.getName()).description(dto.getDescription())
                .price(dto.getPrice()).active(dto.isActive()).build()));
    }

    public GlassesRepairResponseDTO update(Long id, GlassesRepairRequestDTO dto) {
        GlassesRepair r = findOrThrow(id);
        r.setName(dto.getName()); r.setDescription(dto.getDescription());
        r.setPrice(dto.getPrice()); r.setActive(dto.isActive());
        return toDTO(repo.save(r));
    }

    public void delete(Long id) {
        GlassesRepair r = findOrThrow(id);
        r.setActive(false);
        repo.save(r);
    }

    private GlassesRepair findOrThrow(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new RuntimeException("Glasses repair not found with id: " + id));
    }

    private GlassesRepairResponseDTO toDTO(GlassesRepair r) {
        return GlassesRepairResponseDTO.builder()
                .id(r.getId()).name(r.getName()).description(r.getDescription())
                .price(r.getPrice()).active(r.isActive())
                .createdAt(r.getCreatedAt()).updatedAt(r.getUpdatedAt()).build();
    }
}