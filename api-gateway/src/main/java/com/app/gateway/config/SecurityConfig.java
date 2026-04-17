package com.app.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Gateway security configuration.
 *
 * WHY THIS CLASS IS NEEDED:
 * The gateway has spring-boot-starter-security on the classpath (needed to
 * parse JWTs in JwtAuthFilter). Without a custom config, Spring Boot
 * auto-configures a default SecurityWebFilterChain that:
 *   - Enables HTTP Basic authentication
 *   - Enables form login at /login
 *   - Protects ALL routes — including /oauth2/** and /api/v1/auth/**
 *
 * The result: clicking "Login with Google" redirects the browser to
 * localhost:8080/login (Spring's "Please sign in" page) instead of Google,
 * because Spring Security intercepts the /oauth2/authorization/google request
 * before the gateway route even gets to forward it to auth-service.
 *
 * This config replaces that default chain with one that:
 *   - Disables form login and HTTP Basic completely
 *   - Permits ALL requests at the gateway level (auth is handled either by
 *     the custom JwtAuthFilter on protected routes, or by auth-service itself)
 *   - Configures CORS so the React frontend can talk to the gateway
 *   - Disables CSRF (stateless JWT architecture doesn't need it)
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * FIX 1 — Read allowed origins from the environment variable (CORS_ALLOWED_ORIGINS)
     * instead of hardcoding "http://localhost:5173".
     *
     * Previously the hardcoded value conflicted with the application.properties CORS config
     * when the env var was set to a different value (e.g. a production domain), causing the
     * gateway to respond with mismatched Access-Control-Allow-Origin headers and blocking
     * the OAuth2 redirect flow entirely ("ERR_FAILED / page not working" in the browser).
     *
     * The value supports a comma-separated list so multiple origins can be allowed:
     *   CORS_ALLOWED_ORIGINS=http://localhost:5173,https://myapp.com
     */
    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOriginsRaw;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            // Disable CSRF — stateless JWT API, no session cookies to protect
            .csrf(ServerHttpSecurity.CsrfSpec::disable)

            // Disable Spring's built-in form login page (/login).
            // Without this, Spring intercepts every unauthenticated request and
            // redirects to localhost:8080/login instead of forwarding to auth-service.
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

            // Disable HTTP Basic authentication popup/header handling.
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

            // Allow all requests at the gateway level.
            // The gateway's own JwtAuthFilter (applied per-route in application.properties)
            // enforces auth on protected routes. Public routes and the OAuth2/auth
            // endpoints are intentionally open here so they reach auth-service.
            .authorizeExchange(exchanges -> exchanges
                .anyExchange().permitAll()
            )

            // Apply CORS config so the React app on port 5173 can make requests
            .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Split on comma to support multiple origins from the env var
        config.setAllowedOrigins(Arrays.asList(allowedOriginsRaw.split(",")));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "Accept",
                "X-Requested-With", "Origin",
                "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));
        config.setExposedHeaders(List.of("Authorization"));
        // Required for the oauth2_auth_request cookie to survive the redirect round-trip
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}