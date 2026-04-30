package com.app.qma.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reads the internal headers set by the api-gateway's JwtAuthFilter and
 * populates the SecurityContext so Spring Security behaves correctly.
 *
 * Headers consumed (set only by the gateway — never sent by external clients):
 *   X-Auth-User-Email  — the authenticated user's email
 *   X-Auth-User-Roles  — space-separated authority strings (e.g. "ROLE_USER")
 *
 * This approach means the qma-service never needs to parse JWT tokens itself,
 * keeping it decoupled from the auth mechanism.
 */
@Slf4j
@Component
public class GatewayAuthFilter extends OncePerRequestFilter {

    private static final String EMAIL_HEADER = "X-Auth-User-Email";
    private static final String ROLES_HEADER = "X-Auth-User-Roles";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String email = request.getHeader(EMAIL_HEADER);
        String rolesHeader = request.getHeader(ROLES_HEADER);

        if (StringUtils.hasText(email) && StringUtils.hasText(rolesHeader)) {
            List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesHeader.split(" "))
                    .filter(StringUtils::hasText)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("GatewayAuthFilter — authenticated {} with roles {}", email, rolesHeader);
        }

        filterChain.doFilter(request, response);
    }
}
