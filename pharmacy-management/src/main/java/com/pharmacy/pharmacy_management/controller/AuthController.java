package com.pharmacy.pharmacy_management.controller;

import com.pharmacy.pharmacy_management.config.JwtTokenProvider;
import com.pharmacy.pharmacy_management.dto.ApiResponse;
import com.pharmacy.pharmacy_management.dto.JwtResponseDTO;
import com.pharmacy.pharmacy_management.dto.LoginRequestDTO;
import com.pharmacy.pharmacy_management.dto.UserResponseDTO;
import com.pharmacy.pharmacy_management.security.LoginAttemptService;
import com.pharmacy.pharmacy_management.service.TokenRevocationService;
import com.pharmacy.pharmacy_management.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, logout, and current user profile")
public class AuthController {

    private final AuthenticationManager  authenticationManager;
    private final JwtTokenProvider       jwtTokenProvider;
    private final UserService            userService;
    private final LoginAttemptService    loginAttemptService;
    private final TokenRevocationService tokenRevocationService;

    @PostMapping("/login")
    @Operation(summary = "Login with username or email + password", security = {})
    public ResponseEntity<ApiResponse<JwtResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO loginRequest) {

        String identifier = loginRequest.getUsername();

        // SECURITY FIX: brute-force / credential-stuffing guard.
        // Was: unlimited login attempts against /api/auth/login.
        if (loginAttemptService.isLocked(identifier)) {
            long secondsRemaining = loginAttemptService.getLockSecondsRemaining(identifier);
            throw new LockedException(
                    "Too many failed login attempts. Try again in " + secondsRemaining + " seconds.");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword())
            );
        } catch (AuthenticationException ex) {
            loginAttemptService.recordFailure(identifier);
            throw ex;
        }

        loginAttemptService.recordSuccess(identifier);

        String jwt = jwtTokenProvider.generateToken(authentication);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", new JwtResponseDTO("Bearer", jwt)));
    }

    /**
     * SECURITY FIX (OWASP A07) — previously there was no server-side logout at
     * all; the frontend just discarded the token from localStorage, but the
     * token itself stayed valid (usable by anyone who'd copied it — shared
     * device, XSS, intercepted request) until it naturally expired. This
     * revokes the token's jti immediately, so it's rejected on the very next
     * request even though it's still cryptographically valid.
     */
    @PostMapping("/logout")
    @Operation(summary = "Invalidate the current session token server-side")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            tokenRevocationService.revoke(authorizationHeader.substring(7));
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out.", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser(
            Authentication authentication) {
        UserResponseDTO profile = userService.getCurrentUserProfile(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
