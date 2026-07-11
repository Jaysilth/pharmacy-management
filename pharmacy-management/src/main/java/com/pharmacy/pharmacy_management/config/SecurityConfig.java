package com.pharmacy.pharmacy_management.config;

import com.pharmacy.pharmacy_management.service.CustomUserDetailsService;
import com.pharmacy.pharmacy_management.service.TokenRevocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * SecurityConfig — Phase 2 (headers / Swagger / rate limiting / revocation).
 *
 * Builds on top of the CORS, brute-force-lockout, and JWT-secret-validation
 * fixes already merged. This pass closes what those didn't touch:
 *
 * 1. SECURITY FIX — Swagger/OpenAPI were fully permitAll(), so the entire
 *    API surface (every endpoint, every DTO field/enum) was public even
 *    after CORS was locked down. Moved behind the same auth as everything
 *    else. To use Swagger UI now: log in via /api/auth/login, then use the
 *    "Authorize" button in Swagger UI with the returned Bearer token.
 *
 * 2. SECURITY FIX — no HTTP security headers were ever set explicitly
 *    (CSP, HSTS, Referrer-Policy, Permissions-Policy, X-Content-Type-Options
 *    all absent or relying on partial Spring defaults). Added below.
 *
 * 3. SECURITY FIX — LoginAttemptService (already merged) locks a *username*
 *    after 5 failures, but every attempt up to that point — and every
 *    attempt against a *different* username — still runs a full BCrypt
 *    comparison inside AuthController before the lockout check ever helps.
 *    BCrypt is deliberately slow; that's a CPU-exhaustion / cheap-DoS vector
 *    the username-keyed lockout can't close on its own, since it only
 *    engages after 5 hits on the *same* identifier. LoginRateLimitFilter
 *    below runs earlier in the chain — before the request ever reaches
 *    AuthController or triggers a BCrypt hash — and throttles by IP+username
 *    together, so both a single attacker varying usernames and a lockout
 *    exempt "first 4 attempts forever" pattern are covered too. The two
 *    protections are complementary, not redundant: lockout stops a targeted
 *    account takeover, the filter stops volume/DoS against the endpoint
 *    itself.
 *
 * 4. SECURITY FIX — JwtAuthenticationFilter now also rejects tokens whose
 *    jti has been revoked (see JwtAuthenticationFilter / TokenRevocationService),
 *    so logout actually invalidates a token server-side instead of only
 *    clearing it client-side.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource  corsConfigurationSource;
    private final JwtTokenProvider         jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenRevocationService   tokenRevocationService;
    private final LoginRateLimitFilter     loginRateLimitFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService, tokenRevocationService);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // correct here — bearer-token-in-header auth, no cookies, no CSRF surface
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())

                // SECURITY FIX — explicit header hardening. Spring's defaults cover some of
                // this partially; being explicit here means it's not dependent on version
                // defaults changing under us later.
                .headers(headers -> headers
                        .contentTypeOptions(contentTypeOptions -> {}) // X-Content-Type-Options: nosniff
                        .frameOptions(frameOptions -> frameOptions.deny()) // X-Frame-Options: DENY
                        .referrerPolicy(referrer ->
                                referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .permissionsPolicyHeader(permissions -> permissions
                                .policy("geolocation=(), microphone=(), camera=(), payment=()"))
                        // This is a JSON API, not an HTML-serving app — 'none' is correct, not just strict.
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                )

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/logout").permitAll()
                        // SECURITY FIX — Swagger/OpenAPI removed from permitAll; now requires
                        // the same authentication as the rest of the API.
                        .anyRequest().authenticated()
                )
                .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
