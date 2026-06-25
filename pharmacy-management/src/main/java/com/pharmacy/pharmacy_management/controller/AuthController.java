package com.pharmacy.pharmacy_management.controller;

import com.pharmacy.pharmacy_management.config.JwtTokenProvider;
import com.pharmacy.pharmacy_management.dto.ApiResponse;
import com.pharmacy.pharmacy_management.dto.JwtResponseDTO;
import com.pharmacy.pharmacy_management.dto.LoginRequestDTO;
import com.pharmacy.pharmacy_management.dto.UserResponseDTO;
import com.pharmacy.pharmacy_management.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and current user profile")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider      jwtTokenProvider;
    private final UserService           userService;

    @PostMapping("/login")
    @Operation(summary = "Login with username or email + password", security = {})
    public ResponseEntity<ApiResponse<JwtResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword())
        );

        String jwt = jwtTokenProvider.generateToken(authentication);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", new JwtResponseDTO("Bearer", jwt)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser(
            Authentication authentication) {
        UserResponseDTO profile = userService.getCurrentUserProfile(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}