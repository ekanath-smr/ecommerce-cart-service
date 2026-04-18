package com.example.ecommerce_cart_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

//        log.info("JWT Filter hit for path: {}", request.getRequestURI());

        String authHeader = request.getHeader("Authorization");

//        log.info("Authorization header received: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (jwtService.validateToken(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String username = jwtService.extractUsername(token);
                Long userId = jwtService.extractUserId(token);
                List<SimpleGrantedAuthority> authorities = jwtService.extractRoles(token)
                        .stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();
                log.info("Authenticated user: {}", username);
                log.info("Authorities: {}", authorities);
                UserPrincipal userPrincipal = new UserPrincipal(userId, username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                authorities
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("JWT authentication failed", ex);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}