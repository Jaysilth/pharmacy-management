package com.pharmacy.pharmacy_management.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Security fix — OWASP A07:2021 (Identification & Authentication Failures)
 * and OWASP A04:2021 (Insecure Design).
 *
 * Previously: /api/auth/login was permitAll() with zero throttling — an
 * attacker could script unlimited password guesses against any username
 * with no cost. This is a small, self-hosted clinic app with a handful of
 * predictable usernames, which makes it a soft target for exactly that.
 *
 * Fix: a lightweight, dependency-free fixed-window limiter keyed on
 * IP + attempted username, so it catches both "one attacker hammering many
 * accounts" and "distributed attempts against one account." This is
 * in-memory and per-instance — fine for a single Render instance. If this
 * ever runs multiple instances behind a load balancer, this needs to move
 * to a shared store (Redis) or the limiting needs to happen at the
 * edge/proxy layer instead, since each instance would otherwise track its
 * own counts independently.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String  LOGIN_PATH      = "/api/auth/login";
    private static final int     MAX_ATTEMPTS    = 5;
    private static final long    WINDOW_MS       = 15 * 60 * 1000L; // 15 minutes

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, Window> attempts = new ConcurrentHashMap<>();

    private static class Window {
        final AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart = System.currentTimeMillis();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!"POST".equalsIgnoreCase(request.getMethod()) || !request.getRequestURI().endsWith(LOGIN_PATH)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Wrap so we can read the body (for the attempted username) without consuming
        // the stream the actual controller needs afterward.
        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request, 4096);
        wrapped.getInputStream().readAllBytes(); // triggers caching
        String username = extractUsername(wrapped);
        String ip = clientIp(request);
        String key = ip + ":" + (username == null ? "unknown" : username.toLowerCase());

        Window window = attempts.computeIfAbsent(key, k -> new Window());
        long now = System.currentTimeMillis();

        synchronized (window) {
            if (now - window.windowStart > WINDOW_MS) {
                window.windowStart = now;
                window.count.set(0);
            }
            int attemptNo = window.count.incrementAndGet();

            if (attemptNo > MAX_ATTEMPTS) {
                long retryAfterSeconds = (WINDOW_MS - (now - window.windowStart)) / 1000;
                response.setStatus(429); // 429 Too Many Requests
                response.setHeader("Retry-After", String.valueOf(Math.max(retryAfterSeconds, 1)));
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Too many login attempts. Try again in a few minutes.\"}");
                return;
            }
        }

        filterChain.doFilter(wrapped, response);
    }

    private String extractUsername(ContentCachingRequestWrapper wrapped) {
        try {
            byte[] body = wrapped.getContentAsByteArray();
            if (body.length == 0) return null;
            JsonNode node = objectMapper.readTree(new String(body, StandardCharsets.UTF_8));
            JsonNode usernameNode = node.get("username");
            return usernameNode != null ? usernameNode.asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Prefers X-Forwarded-For (Render sits behind a proxy) with a fallback to the direct remote address. */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /** Prevents unbounded memory growth from an attacker spraying many fake IP/username combinations. */
    @Scheduled(fixedRate = 30 * 60 * 1000L) // every 30 minutes
    public void cleanupStaleEntries() {
        long cutoff = Instant.now().toEpochMilli() - (WINDOW_MS * 2);
        attempts.entrySet().removeIf(e -> e.getValue().windowStart < cutoff);
    }
}
