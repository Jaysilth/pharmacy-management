package com.pharmacy.pharmacy_management.service;

import com.pharmacy.pharmacy_management.dto.MedicineRequestDTO;
import com.pharmacy.pharmacy_management.dto.MedicineResponseDTO;
import com.pharmacy.pharmacy_management.entity.Medicine;
import com.pharmacy.pharmacy_management.exception.InsufficientStockException;
import com.pharmacy.pharmacy_management.exception.MedicineNotFoundException;
import com.pharmacy.pharmacy_management.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public MedicineResponseDTO addMedicine(MedicineRequestDTO requestDTO) {
        Medicine medicine = Medicine.builder()
                .name(requestDTO.getName())
                .quantity(requestDTO.getQuantity())
                .price(requestDTO.getPrice())
                .expiryDate(requestDTO.getExpiryDate())
                .lowStockThreshold(requestDTO.getLowStockThreshold() != null ? requestDTO.getLowStockThreshold() : 10)
                .description(requestDTO.getDescription())
                .manufacturer(requestDTO.getManufacturer())
                .build();

        Medicine savedMedicine = medicineRepository.save(medicine);
        return mapToResponseDTO(savedMedicine);
    }

    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> getAllMedicines() {
        return medicineRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MedicineResponseDTO getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with id: " + id));
        return mapToResponseDTO(medicine);
    }

    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> getExpiredMedicines() {
        return medicineRepository.findExpiredMedicines(LocalDate.now())
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> getLowStockMedicines() {
        return medicineRepository.findLowStockMedicines()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> searchMedicines(String name) {
        return medicineRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public MedicineResponseDTO updateMedicine(Long id, MedicineRequestDTO requestDTO) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with id: " + id));

        medicine.setName(requestDTO.getName());
        medicine.setQuantity(requestDTO.getQuantity());
        medicine.setPrice(requestDTO.getPrice());
        medicine.setExpiryDate(requestDTO.getExpiryDate());
        if (requestDTO.getLowStockThreshold() != null) {
            medicine.setLowStockThreshold(requestDTO.getLowStockThreshold());
        }
        medicine.setDescription(requestDTO.getDescription());
        medicine.setManufacturer(requestDTO.getManufacturer());

        Medicine updatedMedicine = medicineRepository.save(medicine);
        return mapToResponseDTO(updatedMedicine);
    }

    public void deleteMedicine(Long id) {
        if (!medicineRepository.existsById(id)) {
            throw new MedicineNotFoundException("Medicine not found with id: " + id);
        }
        medicineRepository.deleteById(id);
    }

    public void reduceStock(Long medicineId, int quantity) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with id: " + medicineId));

        if (medicine.getQuantity() < quantity) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for %s. Available: %d, Requested: %d",
                            medicine.getName(), medicine.getQuantity(), quantity));
        }

        medicine.setQuantity(medicine.getQuantity() - quantity);
        medicineRepository.save(medicine);
    }

    public void addStock(Long medicineId, int quantity) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with id: " + medicineId));

        medicine.setQuantity(medicine.getQuantity() + quantity);
        medicineRepository.save(medicine);
    }

    private MedicineResponseDTO mapToResponseDTO(Medicine medicine) {
        return MedicineResponseDTO.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .quantity(medicine.getQuantity())
                .price(medicine.getPrice())
                .expiryDate(medicine.getExpiryDate())
                .lowStockThreshold(medicine.getLowStockThreshold())
                .description(medicine.getDescription())
                .manufacturer(medicine.getManufacturer())
                .createdAt(medicine.getCreatedAt())
                .updatedAt(medicine.getUpdatedAt())
                .isExpired(medicine.isExpired())
                .isLowStock(medicine.isLowStock())
                .build();
    }
}