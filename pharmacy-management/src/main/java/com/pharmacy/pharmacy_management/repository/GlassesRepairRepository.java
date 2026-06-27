package com.pharmacy.pharmacy_management.repository;

import com.pharmacy.pharmacy_management.entity.GlassesRepair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GlassesRepairRepository extends JpaRepository<GlassesRepair, Long> {
    List<GlassesRepair> findByActiveTrueOrderByNameAsc();
}
