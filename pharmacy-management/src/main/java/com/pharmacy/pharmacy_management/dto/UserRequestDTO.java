package com.pharmacy.pharmacy_management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    // SECURITY FIX: was min-length-only (8 chars), which permits weak
    // passwords like "aaaaaaaa". Now requires upper, lower, digit, and symbol.
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).+$",
            message = "Password must include at least one uppercase letter, one lowercase letter, one digit, and one symbol"
    )
    private String password;

    /**
     * Role names to assign. Must match values stored in the roles table.
     * Example: ["ROLE_ADMIN"] or ["ROLE_SUPER_ADMIN", "ROLE_ADMIN"]
     * If null or empty, defaults to ROLE_ADMIN only.
     */
    private Set<String> roles;
}
