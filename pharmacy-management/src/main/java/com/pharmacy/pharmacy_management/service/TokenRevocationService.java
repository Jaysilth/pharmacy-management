package com.pharmacy.pharmacy_management.service;

import com.pharmacy.pharmacy_management.config.JwtTokenProvider;
import com.pharmacy.pharmacy_management.entity.RevokedToken;
import com.pharmacy.pharmacy_management.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private final RevokedTokenRepository repo;
    private final JwtTokenProvider       jwtTokenProvider;

    /** Called on logout. Silently no-ops on an already-invalid token — logout should never itself throw. */
    @Transactional
    public void revoke(String rawToken) {
        String jti = jwtTokenProvider.getJti(rawToken);
        if (jti == null || repo.existsByJti(jti)) return;

        LocalDateTime expiresAt = jwtTokenProvider.getExpiry(rawToken);
        repo.save(RevokedToken.builder().jti(jti).expiresAt(expiresAt).build());
    }

    @Transactional(readOnly = true)
    public boolean isRevoked(String jti) {
        return jti != null && repo.existsByJti(jti);
    }

    /** Runs daily — deletes rows whose underlying token has expired anyway, so this table doesn't grow forever. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpired() {
        repo.deleteExpiredBefore(LocalDateTime.now());
    }
}
