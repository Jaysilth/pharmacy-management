package com.pharmacy.pharmacy_management.repository;

import com.pharmacy.pharmacy_management.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    // ── Listing / ordering — reflects when the row was actually entered ──
    List<Sale> findAllByOrderByCreatedAtDesc();
    List<Sale> findTop5ByOrderByCreatedAtDesc();

    // ── Revenue & reporting — reflects the business-effective sale date,
    //    so a backdated sale counts toward the period it actually happened
    //    in, not the day it was entered ──
    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate ORDER BY s.saleDate DESC")
    List<Sale> findSalesBySaleDateRange(LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(s.totalPrice) FROM Sale s")
    BigDecimal getTotalRevenue();

    @Query("SELECT SUM(s.totalPrice) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueBySaleDateRange(LocalDate startDate, LocalDate endDate);

    long countBySaleDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.medicine.id = :medicineId")
    Long countSalesByMedicineId(Long medicineId);

    @Query("SELECT s.medicine.id, s.medicine.name, COUNT(s) as saleCount FROM Sale s GROUP BY s.medicine.id, s.medicine.name ORDER BY saleCount DESC")
    List<Object[]> findTopSellingMedicines();

    /**
     * Delete all sales associated with a specific medicine.
     * This prevents foreign key constraint violations when deleting a medicine.
     *
     * @param medicineId The ID of the medicine whose sales should be deleted
     */
    @Modifying
    @Query("DELETE FROM Sale s WHERE s.medicine.id = :medicineId")
    void deleteByMedicineId(Long medicineId);
}