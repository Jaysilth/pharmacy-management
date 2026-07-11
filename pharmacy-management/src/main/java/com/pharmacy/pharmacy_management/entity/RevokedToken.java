package com.pharmacy.pharmacy_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Security fix — OWASP A07:2021 (Identification & Authentication Failures).
 *
 * Previously: logout only cleared the token from the browser's localStorage.
 * The JWT itself stayed cryptographically valid for its full 24h lifetime —
 * anyone who'd copied it (shared device, XSS, intercepted request) could
 * keep using it after the legitimate user "logged out."
 *
 * Fix: store the token's unique id (jti) here on logout. The auth filter
 * rejects any request presenting a revoked jti, even though the token's
 * signature still checks out. We store only the jti + its natural expiry
 * (not the raw token), so this table can never itself leak usable
 * credentials, and rows past their expiry are safe to purge.
 */
@Entity
@Table(name = "revoked_tokens", indexes = {
        @Index(name = "idx_revoked_jti", columnList = "jti", unique = true),
        @Index(name = "idx_revoked_expiry", columnList = "expiresAt")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String jti;

    /** The token's original expiry — once past this, the row is dead weight and safe to delete. */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime revokedAt;

    @PrePersist
    protected void onCreate() {
        if (revokedAt == null) revokedAt = LocalDateTime.now();
    }
}
