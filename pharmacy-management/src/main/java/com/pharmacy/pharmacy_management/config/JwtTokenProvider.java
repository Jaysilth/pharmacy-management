package com.pharmacy.pharmacy_management.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    private Key signingKey;

    // SECURITY FIX: previously any jwt.secret value was accepted, including
    // short/weak ones — a short HS256 secret can be brute-forced offline
    // to forge valid tokens. Fail fast at startup instead of running with
    // a signing key that doesn't provide real security.
    private static final int MIN_SECRET_BYTES = 32; // 256 bits

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret is missing or too short. It must be at least " + MIN_SECRET_BYTES +
                            " bytes (256 bits) of high-entropy data for HS256. " +
                            "Generate one with e.g. `openssl rand -base64 48` and set it as the JWT_SECRET " +
                            "environment variable.");
        }
        signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        // Include roles in the JWT so the frontend can decode them
        // without making an extra API call to /api/auth/me
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())   // jti — SECURITY FIX: enables per-token revocation on logout
                .setSubject(authentication.getName())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /** The token's unique id — what gets stored in the revocation table, never the raw token itself. */
    public String getJti(String token) {
        try {
            return parseClaims(token).getId();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    public LocalDateTime getExpiry(String token) {
        Date exp = parseClaims(token).getExpiration();
        return LocalDateTime.ofInstant(exp.toInstant(), ZoneId.systemDefault());
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

