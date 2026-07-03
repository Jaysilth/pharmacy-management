package com.pharmacy.pharmacy_management.repository;

import com.pharmacy.pharmacy_management.entity.ConsumableUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConsumableUsageRepository extends JpaRepository<ConsumableUsage, Long> {

    List<ConsumableUsage> findByConsumableIdOrderByUsedAtDesc(Long consumableId);

    List<ConsumableUsage> findBySurgeryIdOrderByUsedAtDesc(Long surgeryId);

    List<ConsumableUsage> findByLinkedEntityTypeOrderByUsedAtDesc(String type);

    List<ConsumableUsage> findAllByOrderByUsedAtDesc();
}