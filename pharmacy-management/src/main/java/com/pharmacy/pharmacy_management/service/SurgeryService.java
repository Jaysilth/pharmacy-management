package com.pharmacy.pharmacy_management.service;

import com.pharmacy.pharmacy_management.dto.SurgeryRequestDTO;
import com.pharmacy.pharmacy_management.dto.SurgeryResponseDTO;
import com.pharmacy.pharmacy_management.entity.Surgery;
import com.pharmacy.pharmacy_management.repository.SurgeryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SurgeryService {

    private final SurgeryRepository surgeryRepository;

    @Transactional(readOnly = true)
    public List<SurgeryResponseDTO> getAllSurgeries(String search) {
        List<Surgery> result = (search != null && !search.isBlank())
                ? surgeryRepository.searchActiveSurgeries(search)
                : surgeryRepository.findByActiveTrueOrderByNameAsc();
        return result.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SurgeryResponseDTO getSurgeryById(Long id) {
        return mapToDTO(findOrThrow(id));
    }

    public SurgeryResponseDTO createSurgery(SurgeryRequestDTO dto) {
        Surgery surgery = Surgery.builder()
                .name(dto.getName()).category(dto.getCategory())
                .description(dto.getDescription()).price(dto.getPrice())
                .durationMinutes(dto.getDurationMinutes()).active(dto.isActive())
                .build();
        return mapToDTO(surgeryRepository.save(surgery));
    }

    public SurgeryResponseDTO updateSurgery(Long id, SurgeryRequestDTO dto) {
        Surgery surgery = findOrThrow(id);
        surgery.setName(dto.getName()); surgery.setCategory(dto.getCategory());
        surgery.setDescription(dto.getDescription()); surgery.setPrice(dto.getPrice());
        surgery.setDurationMinutes(dto.getDurationMinutes()); surgery.setActive(dto.isActive());
        return mapToDTO(surgeryRepository.save(surgery));
    }

    public void deleteSurgery(Long id) {
        Surgery surgery = findOrThrow(id);
        surgery.setActive(false); // Soft delete
        surgeryRepository.save(surgery);
    }

    private Surgery findOrThrow(Long id) {
        return surgeryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Surgery not found with id: " + id));
    }

    private SurgeryResponseDTO mapToDTO(Surgery s) {
        return SurgeryResponseDTO.builder()
                .id(s.getId()).name(s.getName()).category(s.getCategory())
                .description(s.getDescription()).price(s.getPrice())
                .durationMinutes(s.getDurationMinutes()).active(s.isActive())
                .createdAt(s.getCreatedAt()).updatedAt(s.getUpdatedAt())
                .build();
    }
}