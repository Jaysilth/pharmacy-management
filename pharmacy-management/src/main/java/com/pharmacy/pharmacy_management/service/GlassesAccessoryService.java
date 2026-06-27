package com.pharmacy.pharmacy_management.service;

import com.pharmacy.pharmacy_management.dto.*;
import com.pharmacy.pharmacy_management.entity.GlassesAccessory;
import com.pharmacy.pharmacy_management.exception.InsufficientStockException;
import com.pharmacy.pharmacy_management.repository.GlassesAccessoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GlassesAccessoryService {

    private final GlassesAccessoryRepository repo;

    @Transactional(readOnly = true)
    public List<GlassesAccessoryResponseDTO> getAll(String search) {
        List<GlassesAccessory> list = (search != null && !search.isBlank())
                ? repo.searchByName(search)
                : repo.findAllByOrderByAccessoryTypeAscNameAsc();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GlassesAccessoryResponseDTO getById(Long id) { return toDTO(findOrThrow(id)); }

    @Transactional(readOnly = true)
    public List<GlassesAccessoryResponseDTO> getLowStock() {
        return repo.findLowStock().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public GlassesAccessoryResponseDTO create(GlassesAccessoryRequestDTO dto) {
        return toDTO(repo.save(GlassesAccessory.builder()
                .name(dto.getName()).accessoryType(dto.getAccessoryType())
                .price(dto.getPrice()).quantity(dto.getQuantity())
                .lowStockThreshold(dto.getLowStockThreshold() != null ? dto.getLowStockThreshold() : 5)
                .description(dto.getDescription()).build()));
    }

    public GlassesAccessoryResponseDTO update(Long id, GlassesAccessoryRequestDTO dto) {
        GlassesAccessory a = findOrThrow(id);
        a.setName(dto.getName()); a.setAccessoryType(dto.getAccessoryType());
        a.setPrice(dto.getPrice()); a.setQuantity(dto.getQuantity());
        if (dto.getLowStockThreshold() != null) a.setLowStockThreshold(dto.getLowStockThreshold());
        a.setDescription(dto.getDescription());
        return toDTO(repo.save(a));
    }

    public void delete(Long id) { repo.delete(findOrThrow(id)); }

    public void reduceStock(Long id, Integer qty) {
        GlassesAccessory a = findOrThrow(id);
        if (a.getQuantity() < qty)
            throw new InsufficientStockException(String.format(
                    "Insufficient stock for %s. Available: %d, Requested: %d",
                    a.getName(), a.getQuantity(), qty));
        a.setQuantity(a.getQuantity() - qty);
        repo.save(a);
    }

    private GlassesAccessory findOrThrow(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new RuntimeException("Glasses accessory not found with id: " + id));
    }

    private GlassesAccessoryResponseDTO toDTO(GlassesAccessory a) {
        return GlassesAccessoryResponseDTO.builder()
                .id(a.getId()).name(a.getName()).accessoryType(a.getAccessoryType())
                .price(a.getPrice()).quantity(a.getQuantity())
                .lowStockThreshold(a.getLowStockThreshold()).description(a.getDescription())
                .lowStock(a.isLowStock()).createdAt(a.getCreatedAt()).updatedAt(a.getUpdatedAt())
                .build();
    }
}