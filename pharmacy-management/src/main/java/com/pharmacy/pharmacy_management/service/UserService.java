package com.pharmacy.pharmacy_management.service;

import com.pharmacy.pharmacy_management.dto.UserRequestDTO;
import com.pharmacy.pharmacy_management.dto.UserResponseDTO;
import com.pharmacy.pharmacy_management.entity.Role;
import com.pharmacy.pharmacy_management.entity.User;
import com.pharmacy.pharmacy_management.repository.RoleRepository;
import com.pharmacy.pharmacy_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository  userRepository;
    private final RoleRepository  roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    private static final String DEFAULT_ROLE      = "ROLE_ADMIN";

    // ── Read ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = findUserOrThrow(id);
        return mapToResponseDTO(user);
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public UserResponseDTO createUser(UserRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalStateException(
                    "Username '" + request.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException(
                    "Email '" + request.getEmail() + "' is already registered.");
        }

        Set<Role> roles = resolveRoles(request.getRoles());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .accountNonLocked(true)
                .roles(roles)
                .build();

        return mapToResponseDTO(userRepository.save(user));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public UserResponseDTO updateUser(Long id, UserRequestDTO request) {
        User user = findUserOrThrow(id);

        // Check username uniqueness only if it changed
        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalStateException(
                    "Username '" + request.getUsername() + "' is already taken.");
        }

        // Check email uniqueness only if it changed
        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException(
                    "Email '" + request.getEmail() + "' is already registered.");
        }

        // Guard: cannot remove SUPER_ADMIN role if this is the last SUPER_ADMIN
        boolean willLoseSuperAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(ROLE_SUPER_ADMIN))
                && (request.getRoles() == null
                || request.getRoles().stream().noneMatch(r -> r.equals(ROLE_SUPER_ADMIN)));

        if (willLoseSuperAdmin && isLastSuperAdmin(user)) {
            throw new IllegalStateException(
                    "Cannot remove SUPER_ADMIN role — this is the last SUPER_ADMIN account.");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // Only update password if a new one is provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(resolveRoles(request.getRoles()));
        }

        return mapToResponseDTO(userRepository.save(user));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);

        // Cannot delete yourself
        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        if (user.getUsername().equals(currentUsername)) {
            throw new IllegalStateException("You cannot delete your own account.");
        }

        // Cannot delete the last SUPER_ADMIN
        if (user.getRoles().stream().anyMatch(r -> r.getName().equals(ROLE_SUPER_ADMIN))
                && isLastSuperAdmin(user)) {
            throw new IllegalStateException(
                    "Cannot delete the last SUPER_ADMIN account.");
        }

        userRepository.delete(user);
    }

    // ── Activate / Deactivate ────────────────────────────────────────────────

    public UserResponseDTO activateUser(Long id) {
        User user = findUserOrThrow(id);
        user.setEnabled(true);
        return mapToResponseDTO(userRepository.save(user));
    }

    public UserResponseDTO deactivateUser(Long id) {
        User user = findUserOrThrow(id);

        // Cannot deactivate yourself
        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        if (user.getUsername().equals(currentUsername)) {
            throw new IllegalStateException("You cannot deactivate your own account.");
        }

        // Cannot deactivate the last SUPER_ADMIN
        if (user.getRoles().stream().anyMatch(r -> r.getName().equals(ROLE_SUPER_ADMIN))
                && isLastSuperAdmin(user)) {
            throw new IllegalStateException(
                    "Cannot deactivate the last SUPER_ADMIN account.");
        }

        user.setEnabled(false);
        return mapToResponseDTO(userRepository.save(user));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    /**
     * Resolves role name strings to Role entities.
     * Falls back to ROLE_ADMIN if no roles are provided.
     */
    private Set<Role> resolveRoles(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                    .orElseThrow(() -> new IllegalStateException(
                            "Default role " + DEFAULT_ROLE + " not found in database."));
            return new HashSet<>(Set.of(defaultRole));
        }

        Set<Role> roles = new HashSet<>();
        for (String name : roleNames) {
            Role role = roleRepository.findByName(name)
                    .orElseThrow(() -> new IllegalStateException(
                            "Role '" + name + "' does not exist."));
            roles.add(role);
        }
        return roles;
    }

    /**
     * Returns true if the given user is the only remaining SUPER_ADMIN.
     */
    private boolean isLastSuperAdmin(User user) {
        long superAdminCount = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream()
                        .anyMatch(r -> r.getName().equals(ROLE_SUPER_ADMIN)))
                .count();
        return superAdminCount <= 1;
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .accountNonLocked(user.isAccountNonLocked())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}