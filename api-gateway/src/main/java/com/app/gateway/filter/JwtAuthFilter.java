package com.app.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

/**
 * Gateway filter applied to protected QMA routes.
 *
 * Validates the Bearer JWT and, on success, injects two internal headers:
 *   X-Auth-User-Email — the authenticated user's email (JWT subject)
 *   X-Auth-User-Roles — the user's role string from the JWT "roles" claim
 *
 * Downstream services can trust these headers without touching the JWT again.
 */
@Slf4j
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String token = extractToken(request);

            if (!StringUtils.hasText(token)) {
                return unauthorized(exchange, "Missing or malformed Authorization header");
            }

            try {
                Claims claims = parseClaims(token);
                String email = claims.getSubject();
                String roles = claims.get("roles", String.class);

                // Propagate identity downstream via internal headers
                ServerHttpRequest mutated = request.mutate()
                        .header("X-Auth-User-Email", email)
                        .header("X-Auth-User-Roles", roles != null ? roles : "")
                        .build();

                log.debug("JWT valid for {} — routing to downstream", email);
                return chain.filter(exchange.mutate().request(mutated).build());

            } catch (ExpiredJwtException ex) {
                return unauthorized(exchange, "Token has expired");
            } catch (MalformedJwtException | UnsupportedJwtException |
                     io.jsonwebtoken.security.SecurityException ex) {
                return unauthorized(exchange, "Invalid token signature");
            } catch (Exception ex) {
                log.warn("JWT filter unexpected error: {}", ex.getMessage());
                return unauthorized(exchange, "Authentication failed");
            }
        };
    }

    private String extractToken(ServerHttpRequest request) {
        String bearer = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        log.warn("Blocked request — {}", reason);
        return response.setComplete();
    }

    public static class Config {
        // Placeholder for future per-route config (e.g. required roles)
    }
}
