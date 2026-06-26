package com.pharmacy.pharmacy_management.repository;

import com.pharmacy.pharmacy_management.entity.Surgery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SurgeryRepository extends JpaRepository<Surgery, Long> {

    List<Surgery> findByActiveTrueOrderByNameAsc();

    @Query("SELECT s FROM Surgery s WHERE s.active = true AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.category) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Surgery> searchActiveSurgeries(String search);
}