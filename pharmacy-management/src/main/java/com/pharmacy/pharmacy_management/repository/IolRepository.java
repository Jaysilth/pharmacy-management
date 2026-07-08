package com.pharmacy.pharmacy_management.repository;

import com.pharmacy.pharmacy_management.entity.Iol;
import com.pharmacy.pharmacy_management.entity.IolType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IolRepository extends JpaRepository<Iol, Long> {

    List<Iol> findAllByOrderByNameAscPowerAsc();

    List<Iol> findByTypeOrderByPowerAsc(IolType type);

    @Query("SELECT i FROM Iol i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY i.name, i.power")
    List<Iol> searchByName(String q);

    @Query("SELECT i FROM Iol i WHERE i.quantityInStock <= i.reorderLevel")
    List<Iol> findLowStock();

    /** Used to warn on duplicate (name, type, power) combos before saving a new row. */
    List<Iol> findByNameIgnoreCaseAndTypeAndPower(
            String name, IolType type, java.math.BigDecimal power);
}
