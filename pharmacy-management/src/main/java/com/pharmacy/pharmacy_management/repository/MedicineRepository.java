package com.pharmacy.pharmacy_management.repository;

import com.pharmacy.pharmacy_management.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByNameContainingIgnoreCase(String name);

    @Query("SELECT m FROM Medicine m WHERE m.expiryDate < :date")
    List<Medicine> findExpiredMedicines(LocalDate date);

    @Query("SELECT m FROM Medicine m WHERE m.expiryDate < :date")
    List<Medicine> findExpiredMedicinesBefore(LocalDate date);

    @Query("SELECT m FROM Medicine m WHERE m.quantity <= m.lowStockThreshold")
    List<Medicine> findLowStockMedicines();

    boolean existsByName(String name);
}