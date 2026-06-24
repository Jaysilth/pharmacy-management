package com.pharmacy.pharmacy_management.config;

import com.pharmacy.pharmacy_management.entity.Role;
import com.pharmacy.pharmacy_management.entity.User;
import com.pharmacy.pharmacy_management.repository.RoleRepository;
import com.pharmacy.pharmacy_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * DataInitializer — seeds the database with roles and initial users on startup.
 *
 * This replaces the InMemoryUserDetailsManager bootstrapping that was in
 * SecurityConfig. It reads from the same environment variables already set
 * on Render, so no new config is needed after deployment.
 *
 * It is fully IDEMPOTENT — safe to run on every application startup.
 * If roles/users already exist, they are not recreated or overwritten.
 *
 * Env vars consumed (already set on Render from the previous deployment):
 *   PHARMACY_SUPERADMIN_USERNAME  (default: superadmin)
 *   PHARMACY_SUPERADMIN_PASSWORD  (default: SuperAdmin123!)
 *   PHARMACY_ADMIN_USERNAME       (default: admin)
 *   PHARMACY_ADMIN_PASSWORD       (default: Admin123!)
 *
 * Note: passwords are only set at initial creation. If a user already exists,
 * their password is NOT reset on restart — changes persist.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    private static final String ROLE_ADMIN       = "ROLE_ADMIN";

    private final RoleRepository    roleRepository;
    private final UserRepository    userRepository;
    private final PasswordEncoder   passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        seedUsers();
    }

    // ── Roles ────────────────────────────────────────────────────────────────

    private void seedRoles() {
        createRoleIfAbsent(ROLE_SUPER_ADMIN, "Full system access including user management");
        createRoleIfAbsent(ROLE_ADMIN,       "Pharmacy operations: medicines, inventory, sales");
    }

    private void createRoleIfAbsent(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            roleRepository.save(Role.builder()
                    .name(name)
                    .description(description)
                    .build());
            log.info("Created role: {}", name);
        }
    }

    // ── Users ────────────────────────────────────────────────────────────────

    private void seedUsers() {
        String superAdminUsername = env("PHARMACY_SUPERADMIN_USERNAME", "superadmin");
        String superAdminPassword = env("PHARMACY_SUPERADMIN_PASSWORD", "SuperAdmin123!");
        String superAdminEmail    = env("PHARMACY_SUPERADMIN_EMAIL",    "superadmin@jotesseyespecialist.com");

        String adminUsername      = env("PHARMACY_ADMIN_USERNAME",      "admin");
        String adminPassword      = env("PHARMACY_ADMIN_PASSWORD",      "Admin123!");
        String adminEmail         = env("PHARMACY_ADMIN_EMAIL",         "admin@jotesseyespecialist.com");

        Role superAdminRole = roleRepository.findByName(ROLE_SUPER_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_SUPER_ADMIN not seeded"));
        Role adminRole = roleRepository.findByName(ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not seeded"));

        createUserIfAbsent(superAdminUsername, superAdminEmail, superAdminPassword, Set.of(superAdminRole, adminRole));
        createUserIfAbsent(adminUsername,      adminEmail,      adminPassword,      Set.of(adminRole));
    }

    private void createUserIfAbsent(String username, String email, String rawPassword, Set<Role> roles) {
        if (userRepository.existsByUsername(username)) {
            log.debug("User '{}' already exists — skipping seed", username);
            return;
        }
        userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .enabled(true)
                .accountNonLocked(true)
                .roles(roles)
                .build());
        log.info("Seeded user: {} ({})", username, email);
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}