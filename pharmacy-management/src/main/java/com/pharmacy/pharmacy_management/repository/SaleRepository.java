package com.pharmacy.pharmacy_management.repository;

import com.pharmacy.pharmacy_management.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findAllByOrderByCreatedAtDesc();

    @Query("SELECT s FROM Sale s WHERE s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    List<Sale> findSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(s.totalPrice) FROM Sale s")
    BigDecimal getTotalRevenue();

    @Query("SELECT SUM(s.totalPrice) FROM Sale s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.medicine.id = :medicineId")
    Long countSalesByMedicineId(Long medicineId);

    @Query("SELECT s.medicine.id, s.medicine.name, COUNT(s) as saleCount FROM Sale s GROUP BY s.medicine.id, s.medicine.name ORDER BY saleCount DESC")
    List<Object[]> findTopSellingMedicines();
}