package com.app.qma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security config for qma-service.
 *
 * This service sits behind the api-gateway and never receives raw JWTs from
 * external clients. Instead it trusts the X-Auth-User-Email and X-Auth-User-Roles
 * headers injected by the gateway's JwtAuthFilter.
 *
 * The GatewayAuthFilter reads those headers and populates the SecurityContext
 * so that @PreAuthorize / hasRole() still works as expected downstream.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final GatewayAuthFilter gatewayAuthFilter;

    public SecurityConfig(GatewayAuthFilter gatewayAuthFilter) {
        this.gatewayAuthFilter = gatewayAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public QMA operations — gateway routes these without the JwtAuthFilter
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/quantities/compare",
                    "/api/v1/quantities/convert",
                    "/api/v1/quantities/add",
                    "/api/v1/quantities/subtract",
                    "/api/v1/quantities/divide"
                ).permitAll()
                // User-scoped /me/ routes — authenticated
                .requestMatchers("/api/v1/quantities/me/**").authenticated()
                // Error history — admin only
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/quantities/history/errored"
                ).hasRole("ADMIN")
                // All other history/count routes — authenticated
                .requestMatchers("/api/v1/quantities/**").authenticated()
                // Infra endpoints
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**",
                                 "/h2-console/**", "/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .headers(h -> h.frameOptions(fo -> fo.sameOrigin()))
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
