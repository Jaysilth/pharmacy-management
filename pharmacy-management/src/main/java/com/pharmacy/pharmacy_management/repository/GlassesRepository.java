package com.pharmacy.pharmacy_management.repository;

import com.pharmacy.pharmacy_management.entity.Glasses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GlassesRepository extends JpaRepository<Glasses, Long> {

    List<Glasses> findAllByOrderByNameAsc();

    @Query("SELECT g FROM Glasses g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(g.brand) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Glasses> searchByNameOrBrand(String search);

    @Query("SELECT g FROM Glasses g WHERE g.quantity <= g.lowStockThreshold")
    List<Glasses> findLowStockGlasses();
}