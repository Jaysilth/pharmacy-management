package com.pharmacy.pharmacy_management.service;

import com.pharmacy.pharmacy_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CustomUserDetailsService — replaces InMemoryUserDetailsManager.
 *
 * Spring Security calls loadUserByUsername(identifier) during authentication.
 * The identifier can be either a username or an email address — we check both.
 *
 * This keeps AuthController and LoginRequestDTO unchanged: the frontend still
 * sends { "username": "...", "password": "..." } and either a real username
 * or an email address in that field will work.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username OR email.
     *
     * Called by Spring Security's AuthenticationManager during login.
     * The `identifier` parameter contains whatever the user typed into
     * the username field — we accept email addresses there too.
     *
     * @param identifier username or email address
     * @throws UsernameNotFoundException if no user matches either field
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found with username or email: " + identifier));
    }
}