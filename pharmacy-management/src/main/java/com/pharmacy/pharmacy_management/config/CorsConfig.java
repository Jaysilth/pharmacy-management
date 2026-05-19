package com.pharmacy.pharmacy_management.config;

// Spring imports for configuration
import org.springframework.context.annotation.Bean; // Creates Spring beans
import org.springframework.context.annotation.Configuration; // Marks class as configuration
import org.springframework.web.cors.CorsConfiguration; // CORS configuration object
import org.springframework.web.cors.CorsConfigurationSource; // Interface for CORS config
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // URL-based CORS config
import org.springframework.web.servlet.config.annotation.CorsRegistry; // Registry for CORS mappings
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // Web MVC configurer interface

// Java utilities
import java.util.Arrays; // For creating arrays from lists
import java.util.List; // For list operations

/**
 * CorsConfig - Configuration for Cross-Origin Resource Sharing (CORS).
 * 
 * This configuration class enables CORS support for the Pharmacy Management API,
 * allowing frontend applications running on different origins to make HTTP requests
 * to this backend API.
 * 
 * What is CORS?
 * - CORS (Cross-Origin Resource Sharing) is a security mechanism implemented
 *   by web browsers to restrict web pages from making requests to a different
 *   domain than the one that served the web page.
 * - Without CORS, a React/Angular/Vue app running on localhost:3000/4200/5173
 *   cannot make requests to this backend on localhost:8080.
 * 
 * What this configuration does:
 * - Enables CORS for API endpoints (/api/**)
 * - Allows requests from common frontend development ports
 * - Allows common HTTP methods (GET, POST, PUT, DELETE, PATCH, OPTIONS)
 * - Allows all headers in requests
 * - Enables credentials (cookies, authorization headers)
 * - Also enables CORS for Swagger/OpenAPI documentation
 * 
 * Why it matters for frontend development:
 * - React apps typically run on port 3000
 * - Angular apps typically run on port 4200
 * - Vue apps typically run on port 5173 (Vite) or 8080
 * - This config allows all these origins to access the API
 */
@Configuration // Marks this as a Spring configuration class (will be scanned for beans)
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Configure CORS mappings for the application.
     * 
     * This method adds CORS configurations for different URL patterns.
     * It's called by Spring to configure CORS at the servlet level.
     * 
     * @param registry The CorsRegistry to add mappings to
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Add CORS configuration for all API endpoints
        registry.addMapping("/api/**")
                // Allow requests from these origins (frontend development servers)
                .allowedOrigins(
                        "http://localhost:3000",      // React default port
                        "http://localhost:4200",      // Angular default port
                        "http://localhost:8081",      // Alternative/common port
                        "http://127.0.0.1:3000",     // React (IP-based)
                        "http://127.0.0.1:4200",     // Angular (IP-based)
                        "http://localhost:5173",     // Vite (Vue/React) default port
                        "http://127.0.0.1:8081"      // Alternative (IP-based)
                )
                // Allow these HTTP methods
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                // Allow all headers in requests (Content-Type, Authorization, etc.)
                .allowedHeaders("*")
                // Allow credentials (cookies, authorization headers) to be sent
                .allowCredentials(true)
                // How long the browser should cache the CORS preflight response (in seconds)
                // Pre-flight requests are OPTIONS requests sent before actual requests
                .maxAge(3600);

        // Also allow Swagger/OpenAPI endpoints for API documentation
        // These need CORS too if accessed from browser-based API testers
        registry.addMapping("/swagger-ui/**")
                .allowedOrigins("*")  // Allow any origin for API docs
                .allowedMethods("GET", "OPTIONS");

        registry.addMapping("/v3/api-docs/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "OPTIONS");

        registry.addMapping("/swagger-ui.html")
                .allowedOrigins("*")
                .allowedMethods("GET", "OPTIONS");
    }

    /**
     * Create a CorsConfigurationSource bean.
     * 
     * This provides an alternative CORS configuration that can be used
     * with Spring Security (if added in the future).
     * It creates the same configuration as addCorsMappings() but as a bean.
     * 
     * @return CorsConfigurationSource The CORS configuration source
     */
    @Bean // Creates a Spring bean of type CorsConfigurationSource
    public CorsConfigurationSource corsConfigurationSource() {
        // Create a new CORS configuration object
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Set allowed origins (same as in addCorsMappings)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:4200",
                "http://localhost:8081",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:4200",
                "http://localhost:5173",
                "http://127.0.0.1:8081",
                "*" // Allow all origins for Swagger UI access
        ));
        
        // Set allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // Set allowed headers (all)
        configuration.setAllowedHeaders(List.of("*"));
        
        // Allow credentials
        configuration.setAllowCredentials(true);
        
        // Set preflight cache duration
        configuration.setMaxAge(3600L);
        
        // Create URL-based CORS configuration source
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // Register the configuration for /api/** path
        source.registerCorsConfiguration("/api/**", configuration);
        
        // Also register for Swagger UI endpoints to ensure they work with CORS
        source.registerCorsConfiguration("/swagger-ui/**", configuration);
        source.registerCorsConfiguration("/v3/api-docs/**", configuration);
        source.registerCorsConfiguration("/swagger-ui.html", configuration);
        
        return source;
    }
}