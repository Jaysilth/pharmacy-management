package com.pharmacy.pharmacy_management.repository;

import com.pharmacy.pharmacy_management.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

    boolean existsByJti(String jti);

    /** Housekeeping: rows whose underlying token would have expired anyway are safe to drop. */
    @Modifying
    @Query("DELETE FROM RevokedToken r WHERE r.expiresAt < :cutoff")
    int deleteExpiredBefore(LocalDateTime cutoff);
}
