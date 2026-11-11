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
public class UserUpdateRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    // Optional on update. Empty/absent = keep the current password.
    // When present: 8-72 chars (72 = BCrypt's input limit) with upper, lower, digit, symbol.
    @Pattern(
            regexp = "^$|^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,72}$",
            message = "Password must be 8-72 characters with an uppercase letter, a lowercase letter, a digit and a symbol"
    )
    private String password;

    private Set<String> roles;
}