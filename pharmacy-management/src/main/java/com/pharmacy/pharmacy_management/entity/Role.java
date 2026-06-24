package com.pharmacy.pharmacy_management.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Role entity — database-driven roles.
 *
 * Roles are stored in the database so new roles can be added later
 * (e.g. PHARMACIST, MANAGER) without any code changes.
 *
 * Spring Security expects role names prefixed with "ROLE_".
 * Store names as "ROLE_SUPER_ADMIN", "ROLE_ADMIN" etc.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Role name — stored with ROLE_ prefix as Spring Security requires.
     * Examples: ROLE_SUPER_ADMIN, ROLE_ADMIN
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;
}