package com.pharmacy.pharmacy_management.service;

import com.pharmacy.pharmacy_management.dto.GlassesRequestDTO;
import com.pharmacy.pharmacy_management.dto.GlassesResponseDTO;
import com.pharmacy.pharmacy_management.entity.Glasses;
import com.pharmacy.pharmacy_management.exception.InsufficientStockException;
import com.pharmacy.pharmacy_management.repository.GlassesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GlassesService {

    private final GlassesRepository glassesRepository;

    @Transactional(readOnly = true)
    public List<GlassesResponseDTO> getAllGlasses(String search) {
        List<Glasses> result = (search != null && !search.isBlank())
                ? glassesRepository.searchByNameOrBrand(search)
                : glassesRepository.findAllByOrderByNameAsc();
        return result.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GlassesResponseDTO getGlassesById(Long id) {
        return mapToDTO(findOrThrow(id));
    }

    public GlassesResponseDTO createGlasses(GlassesRequestDTO dto) {
        Glasses glasses = Glasses.builder()
                .name(dto.getName())
                .brand(dto.getBrand())
                .frameType(dto.getFrameType())
                .lensType(dto.getLensType())
                .color(dto.getColor())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .lowStockThreshold(dto.getLowStockThreshold() != null ? dto.getLowStockThreshold() : 5)
                .description(dto.getDescription())
                .build();
        return mapToDTO(glassesRepository.save(glasses));
    }

    public GlassesResponseDTO updateGlasses(Long id, GlassesRequestDTO dto) {
        Glasses glasses = findOrThrow(id);
        glasses.setName(dto.getName());
        glasses.setBrand(dto.getBrand());
        glasses.setFrameType(dto.getFrameType());
        glasses.setLensType(dto.getLensType());
        glasses.setColor(dto.getColor());
        glasses.setPrice(dto.getPrice());
        glasses.setQuantity(dto.getQuantity());
        if (dto.getLowStockThreshold() != null) glasses.setLowStockThreshold(dto.getLowStockThreshold());
        glasses.setDescription(dto.getDescription());
        return mapToDTO(glassesRepository.save(glasses));
    }

    public void deleteGlasses(Long id) {
        glassesRepository.delete(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<GlassesResponseDTO> getLowStockGlasses() {
        return glassesRepository.findLowStockGlasses().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public void reduceStock(Long id, Integer quantity) {
        Glasses glasses = findOrThrow(id);
        if (glasses.getQuantity() < quantity) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for %s. Available: %d, Requested: %d",
                            glasses.getName(), glasses.getQuantity(), quantity));
        }
        glasses.setQuantity(glasses.getQuantity() - quantity);
        glassesRepository.save(glasses);
    }

    private Glasses findOrThrow(Long id) {
        return glassesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Glasses not found with id: " + id));
    }

    private GlassesResponseDTO mapToDTO(Glasses g) {
        return GlassesResponseDTO.builder()
                .id(g.getId()).name(g.getName()).brand(g.getBrand())
                .frameType(g.getFrameType()).lensType(g.getLensType()).color(g.getColor())
                .price(g.getPrice()).quantity(g.getQuantity())
                .lowStockThreshold(g.getLowStockThreshold())
                .description(g.getDescription()).lowStock(g.isLowStock())
                .createdAt(g.getCreatedAt()).updatedAt(g.getUpdatedAt())
                .build();
    }
}