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
 * DataInitializer — seeds roles on every boot, and creates ONE initial
 * SUPER_ADMIN if and only if the users table is completely empty.
 *
 * SECURITY CONTRACT:
 * - Roles are never sensitive; always seeded.
 * - Users are ONLY seeded when userRepository.count() == 0 (first boot on
 *   a fresh database). If any user exists — including previously deleted
 *   ones that were re-created — this block is entirely skipped.
 * - There are NO default credential fallbacks. If INITIAL_SUPERADMIN_USERNAME
 *   or INITIAL_SUPERADMIN_PASSWORD are missing, the app logs a warning and
 *   skips user seeding rather than using a hardcoded password.
 * - Once your first real user is created via the UI, remove
 *   INITIAL_SUPERADMIN_USERNAME and INITIAL_SUPERADMIN_PASSWORD from your
 *   Render environment variables entirely. They serve no purpose after that.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    private static final String ROLE_ADMIN       = "ROLE_ADMIN";

    private final RoleRepository  roleRepository;
    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        seedInitialUserIfEmpty();
    }

    // ── Roles ─────────────────────────────────────────────────────────────────

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

    // ── Bootstrap user (first boot only) ──────────────────────────────────────

    private void seedInitialUserIfEmpty() {
        if (userRepository.count() > 0) {
            // Users already exist — do nothing. This covers:
            // - Normal restarts after initial setup
            // - Redeployments on Render
            // - Cases where a user was deleted: they stay deleted.
            log.debug("Users table is not empty — skipping initial user seeding.");
            return;
        }

        String username = System.getenv("INITIAL_SUPERADMIN_USERNAME");
        String password = System.getenv("INITIAL_SUPERADMIN_PASSWORD");
        String email    = System.getenv("INITIAL_SUPERADMIN_EMAIL");

        if (isBlank(username) || isBlank(password)) {
            log.warn(
                    "================================================================\n" +
                            "  FRESH DATABASE DETECTED — no users exist.\n" +
                            "  Set INITIAL_SUPERADMIN_USERNAME and INITIAL_SUPERADMIN_PASSWORD\n" +
                            "  as environment variables, then restart to create the first user.\n" +
                            "  Remove these env vars after your first login.\n" +
                            "================================================================"
            );
            return;
        }

        if (isBlank(email)) {
            email = username + "@jotesseyespecialist.com";
        }

        Role superAdminRole = roleRepository.findByName(ROLE_SUPER_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_SUPER_ADMIN not seeded"));
        Role adminRole = roleRepository.findByName(ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not seeded"));

        userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of(superAdminRole, adminRole))
                .build());

        log.info(
                "================================================================\n" +
                        "  Initial SUPER_ADMIN created: {}\n" +
                        "  Remove INITIAL_SUPERADMIN_USERNAME and INITIAL_SUPERADMIN_PASSWORD\n" +
                        "  from your environment variables after your first login.\n" +
                        "================================================================",
                username
        );
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}