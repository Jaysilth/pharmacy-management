package com.pharmacy.pharmacy_management.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * SECURITY FIX: was allowedOriginPatterns("*") + allowCredentials(true),
 * which let any web origin make credentialed requests against /api/**
 * (and against the Swagger/OpenAPI endpoints).
 *
 * Origins are now restricted to an explicit allow-list, configurable via
 * the ALLOWED_ORIGINS environment variable (comma-separated), so this
 * works the same way across dev/staging/prod without a code change.
 *
 * IMPORTANT: set ALLOWED_ORIGINS in your deployment environment (Render)
 * to your real frontend origin(s), e.g.:
 *   ALLOWED_ORIGINS=https://your-frontend.onrender.com,http://localhost:5173
 * The default below is dev-only and will NOT match a production frontend
 * origin, so requests will be correctly rejected until this is set.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOriginsProperty;

    private List<String> allowedOrigins() {
        return Arrays.stream(allowedOriginsProperty.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = allowedOrigins();

        registry.addMapping("/api/**")
                .allowedOrigins(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        // Docs endpoints don't need credentials — keep them same-origin-restricted
        // too rather than world-readable, but without allowCredentials.
        registry.addMapping("/swagger-ui/**").allowedOrigins(origins.toArray(new String[0])).allowedMethods("GET", "OPTIONS");
        registry.addMapping("/v3/api-docs/**").allowedOrigins(origins.toArray(new String[0])).allowedMethods("GET", "OPTIONS");
        registry.addMapping("/swagger-ui.html").allowedOrigins(origins.toArray(new String[0])).allowedMethods("GET", "OPTIONS");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origins = allowedOrigins();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        CorsConfiguration docsConfig = new CorsConfiguration();
        docsConfig.setAllowedOrigins(origins);
        docsConfig.setAllowedMethods(List.of("GET", "OPTIONS"));
        docsConfig.setAllowedHeaders(List.of("*"));
        docsConfig.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/swagger-ui/**", docsConfig);
        source.registerCorsConfiguration("/v3/api-docs/**", docsConfig);
        source.registerCorsConfiguration("/swagger-ui.html", docsConfig);
        return source;
    }
}