package com.pharmacy.pharmacy_management.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private boolean accountNonLocked;
    private Set<String> roles; // e.g. ["ROLE_SUPER_ADMIN", "ROLE_ADMIN"]
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}