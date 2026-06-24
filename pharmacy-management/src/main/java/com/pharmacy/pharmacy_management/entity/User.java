package com.pharmacy.pharmacy_management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User entity — replaces Spring Security's InMemoryUserDetailsManager.
 *
 * Implements UserDetails directly so Spring Security's AuthenticationManager
 * can use it without an adapter class.
 *
 * Supports login by username OR email — the CustomUserDetailsService
 * handles the lookup logic; this entity just stores both fields.
 *
 * SaaS note: a nullable tenantId column can be added here in future
 * without restructuring this entity.
 */
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * BCrypt-hashed password. Never store plain text.
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * Whether this account is active. SUPER_ADMIN can deactivate accounts.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    /**
     * Whether this account is locked.
     * Reserved for future rate-limiting / lockout feature (Phase 2).
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * ManyToMany relationship with Role.
     * EAGER fetch so authorities are always available for Spring Security.
     * For 1-5 users with 2 roles, eager loading is perfectly acceptable.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── UserDetails interface ────────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // No expiry concept in Phase 1
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // No credential expiry in Phase 1
    }
}
