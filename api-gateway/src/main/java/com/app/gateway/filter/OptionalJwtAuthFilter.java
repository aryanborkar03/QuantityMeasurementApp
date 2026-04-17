package com.app.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;

/**
 * FIX 2 — Optional JWT enrichment filter for public QMA operation routes.
 *
 * THE PROBLEM (history not saving on dashboard):
 * ─────────────────────────────────────────────
 * The public operation routes (/compare, /convert, /add, /subtract, /divide)
 * were routed without any JWT filter. This meant that even when a logged-in
 * user sent a valid Bearer token, the gateway stripped it and never forwarded
 * the X-Auth-User-Email header to qma-service.
 *
 * In QuantityMeasurementServiceImpl.currentUserEmail(), the SecurityContext was
 * always empty for these routes, so userEmail was always null. Every operation
 * was persisted with userEmail=null — completely invisible when the dashboard
 * later queries /me/history (which filters by the real email).
 *
 * THE FIX:
 * ─────────────────────────────────────────────
 * This filter is applied to the qma-public route. It behaves exactly like
 * JwtAuthFilter EXCEPT:
 *   - If NO token is present  → proceed anonymously (no 401)
 *   - If an INVALID token is present → proceed anonymously (no 401)
 *   - If a VALID token is present   → inject X-Auth-User-Email and
 *                                      X-Auth-User-Roles headers just like
 *                                      JwtAuthFilter does, so qma-service
 *                                      stamps the operation with the user's email
 *
 * This way:
 *   - Unauthenticated guests can still use the public endpoints (no change)
 *   - Logged-in users get their operations recorded in their history (fixed)
 */
@Slf4j
@Component
public class OptionalJwtAuthFilter extends AbstractGatewayFilterFactory<OptionalJwtAuthFilter.Config> {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    public OptionalJwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String bearer = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // No token or wrong format — proceed as anonymous, no error
            if (!StringUtils.hasText(bearer) || !bearer.startsWith("Bearer ")) {
                return chain.filter(exchange);
            }

            try {
                SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(bearer.substring(7))
                        .getPayload();

                String email = claims.getSubject();
                String roles = claims.get("roles", String.class);

                // Inject identity headers so qma-service can stamp the operation
                // with the authenticated user's email (enables /me/history)
                ServerHttpRequest mutated = request.mutate()
                        .header("X-Auth-User-Email", email)
                        .header("X-Auth-User-Roles", roles != null ? roles : "")
                        .build();

                log.debug("OptionalJwtAuthFilter — enriched public request for user: {}", email);
                return chain.filter(exchange.mutate().request(mutated).build());

            } catch (Exception ex) {
                // Token present but invalid/expired — treat as anonymous and proceed
                // This is intentional: public routes must never return 401
                log.debug("OptionalJwtAuthFilter — token present but invalid ({}), proceeding anonymously",
                        ex.getClass().getSimpleName());
                return chain.filter(exchange);
            }
        };
    }

    public static class Config {
        // Placeholder for future per-route config
    }
}
