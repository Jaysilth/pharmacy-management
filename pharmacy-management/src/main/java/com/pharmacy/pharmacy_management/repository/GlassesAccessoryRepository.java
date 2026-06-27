package com.pharmacy.pharmacy_management.repository;

import com.pharmacy.pharmacy_management.entity.GlassesAccessory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GlassesAccessoryRepository extends JpaRepository<GlassesAccessory, Long> {

    List<GlassesAccessory> findAllByOrderByAccessoryTypeAscNameAsc();

    List<GlassesAccessory> findByAccessoryTypeOrderByNameAsc(String accessoryType);

    @Query("SELECT a FROM GlassesAccessory a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%',:q,'%'))")
    List<GlassesAccessory> searchByName(String q);

    @Query("SELECT a FROM GlassesAccessory a WHERE a.quantity <= a.lowStockThreshold")
    List<GlassesAccessory> findLowStock();
}