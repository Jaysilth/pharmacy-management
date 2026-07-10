package com.pharmacy.pharmacy_management.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LoginAttemptService — simple in-memory brute-force guard for /api/auth/login.
 *
 * SECURITY FIX (was: no lockout at all — unlimited password guessing).
 *
 * Tracks failed attempts per normalized username/email. After MAX_ATTEMPTS
 * failures inside WINDOW, the identifier is locked for LOCK_DURATION.
 *
 * KNOWN LIMITATION: state is per-instance (ConcurrentHashMap), not shared
 * across replicas. If this app is ever horizontally scaled behind a load
 * balancer, replace this with a shared store (Redis / DB-backed counter)
 * so attempts are counted across all instances. For a single-instance
 * deployment (e.g. one Render service) this is sufficient.
 */
@Component
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 15 * 60 * 1000L;         // 15 minutes
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000L;  // 15 minutes

    private static final class Attempts {
        int count = 0;
        Instant windowStart = Instant.now();
        Instant lockedUntil = null;
    }

    private final ConcurrentHashMap<String, Attempts> attemptsByKey = new ConcurrentHashMap<>();

    private String normalize(String identifier) {
        return identifier == null ? "" : identifier.trim().toLowerCase();
    }

    public boolean isLocked(String identifier) {
        Attempts a = attemptsByKey.get(normalize(identifier));
        if (a == null || a.lockedUntil == null) {
            return false;
        }
        if (Instant.now().isAfter(a.lockedUntil)) {
            attemptsByKey.remove(normalize(identifier));
            return false;
        }
        return true;
    }

    public long getLockSecondsRemaining(String identifier) {
        Attempts a = attemptsByKey.get(normalize(identifier));
        if (a == null || a.lockedUntil == null) {
            return 0;
        }
        long remaining = Instant.now().until(a.lockedUntil, ChronoUnit.SECONDS);
        return Math.max(remaining, 0);
    }

    public synchronized void recordFailure(String identifier) {
        String key = normalize(identifier);
        Attempts a = attemptsByKey.computeIfAbsent(key, k -> new Attempts());

        if (Instant.now().isAfter(a.windowStart.plusMillis(WINDOW_MS))) {
            a.count = 0;
            a.windowStart = Instant.now();
            a.lockedUntil = null;
        }

        a.count++;
        if (a.count >= MAX_ATTEMPTS) {
            a.lockedUntil = Instant.now().plusMillis(LOCK_DURATION_MS);
        }
    }

    public void recordSuccess(String identifier) {
        attemptsByKey.remove(normalize(identifier));
    }
}
