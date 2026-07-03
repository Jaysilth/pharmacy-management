package com.pharmacy.pharmacy_management.repository;

import com.pharmacy.pharmacy_management.entity.Consumable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConsumableRepository extends JpaRepository<Consumable, Long> {

    List<Consumable> findAllByOrderByNameAsc();

    @Query("SELECT c FROM Consumable c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Consumable> searchByName(String q);

    @Query("SELECT c FROM Consumable c WHERE c.quantityInStock <= c.reorderLevel")
    List<Consumable> findLowStock();
}