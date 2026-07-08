package com.pharmacy.pharmacy_management.repository;

import com.pharmacy.pharmacy_management.entity.IolUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IolUsageRepository extends JpaRepository<IolUsage, Long> {

    List<IolUsage> findAllByOrderByUsedAtDesc();

    List<IolUsage> findByIolIdOrderByUsedAtDesc(Long iolId);

    List<IolUsage> findBySurgeryIdOrderByUsedAtDesc(Long surgeryId);
}
