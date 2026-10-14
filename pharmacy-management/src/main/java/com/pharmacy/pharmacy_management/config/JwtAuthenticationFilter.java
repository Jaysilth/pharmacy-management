package com.pharmacy.pharmacy_management.config;

import com.pharmacy.pharmacy_management.service.TokenRevocationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider        jwtTokenProvider;
    private final UserDetailsService      userDetailsService;
    private final TokenRevocationService  tokenRevocationService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserDetailsService userDetailsService,
                                   TokenRevocationService tokenRevocationService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String jwt = parseJwt(request);

        if (jwt != null
                && jwtTokenProvider.validateToken(jwt)
                && !tokenRevocationService.isRevoked(jwtTokenProvider.getJti(jwt))
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = jwtTokenProvider.getUsernameFromToken(jwt);
                var userDetails = userDetailsService.loadUserByUsername(username);

                // Re-check account state on every request, not only at login,
                // so deactivating a user takes effect immediately.
                if (userDetails.isEnabled()
                        && userDetails.isAccountNonLocked()
                        && userDetails.isAccountNonExpired()
                        && userDetails.isCredentialsNonExpired()) {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
                // Deleted user with a still-valid token: stay unauthenticated (401), not a 500.
            }
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
