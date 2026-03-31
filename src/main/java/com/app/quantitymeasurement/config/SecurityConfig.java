package com.app.quantitymeasurement.config;

import com.app.quantitymeasurement.security.oauth2.CustomOAuth2UserService;
import com.app.quantitymeasurement.security.CustomUserDetailsService;
import com.app.quantitymeasurement.security.jwt.JwtAccessDeniedHandler;
import com.app.quantitymeasurement.security.jwt.JwtAuthenticationEntryPoint;
import com.app.quantitymeasurement.security.jwt.JwtAuthenticationFilter;
import com.app.quantitymeasurement.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.app.quantitymeasurement.security.oauth2.OAuth2AuthenticationSuccessHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig
 *
 * Central Spring Security configuration for UC-18: Google Authentication and
 * User Management for Quantity Measurement.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    /**
     * BCrypt password encoder. Used for hashing at registration and
     * verifying at login. Default cost factor = 10 (1024 rounds).
     *
     * @return a BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures a DaoAuthenticationProvider that delegates user lookup to
     * CustomUserDetailsService and password verification to BCrypt.
     * Used by AuthenticationManager to process local login requests.
     *
     * @return a configured DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the AuthenticationManager as a bean so AuthController can
     * programmatically authenticate login requests.
     *
     * @param authenticationConfiguration Spring Boot's auth configuration
     * @return the application-wide AuthenticationManager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Defines the main HTTP security filter chain.
 	 *
     * @param http the HttpSecurity builder
     * @return the fully configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            /*
             * CSRF disabled — stateless JWT APIs are not vulnerable to CSRF because
             * there is no session cookie for an attacker to exploit.
             */
            .csrf(AbstractHttpConfigurer::disable)

            /*
             * STATELESS session policy — Spring Security will never create or use
             * an HTTP session. Each request must carry its own valid JWT.
             */
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            /*
             * Exception handlers:
             *   authenticationEntryPoint — unauthenticated requests → 401 JSON
             *   accessDeniedHandler      — authenticated but forbidden → 403 JSON
             */
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )

            /*
             * URL-level authorisation rules (first match wins).
             */
            .authorizeHttpRequests(auth -> auth

                /* ---- PUBLIC: auth and OAuth2 ---- */
        		.requestMatchers("/api/v1/auth/me").authenticated()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/login/oauth2/**").permitAll()

                
                /* ---- PROTECTED: auth and OAuth2 ---- */
                .requestMatchers(
                		"/api/v1/auth/login",
                		"/api/v1/auth/register",
                		"/api/v1/auth/oauth2/**",
                		"/oauth2/**",
                		"/login/oauth2/**"
                		).permitAll()

                /* ---- PUBLIC: API docs / Swagger UI ---- */
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs"
                ).permitAll()

                /* ---- PUBLIC: H2 console (dev profile only) ---- */
                .requestMatchers("/h2-console/**").permitAll()

                /* ---- PUBLIC: actuator health & info ---- */
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                /*
                 * ---- ADMIN ONLY: error history ----
                 * Returns failed operations across ALL users — restricted to admins.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/quantities/history/errored"
                ).hasRole("ADMIN")

                /*
                 * ---- USER + ADMIN: all other quantity operations ----
                 * Compare, convert, add, subtract, divide, history, count.
                 */
                .requestMatchers("/api/v1/quantities/**")
                    .hasAnyRole("USER", "ADMIN")

                /* ---- CATCH-ALL: any other endpoint requires authentication ---- */
                .anyRequest().authenticated()
            )

            /*
             * Frame options sameOrigin — required for H2 console which uses an iframe.
             */
            .headers(headers ->
                headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )

            /*
             * Disable HTTP Basic and form login — REST APIs must not trigger
             * browser authentication dialogs or redirect to a login page.
             */
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)

            /*
             * Google OAuth2 login:
             *   - CustomOAuth2UserService  : resolves Google profile → local User
             *   - SuccessHandler           : issues JWT, redirects to frontend
             *   - FailureHandler           : redirects with ?error= on failure
             */
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo ->
                    userInfo.userService(customOAuth2UserService)
                )
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
            )

            /* Register the DaoAuthenticationProvider for local logins */
            .authenticationProvider(authenticationProvider())

            /*
             * Insert the JWT filter BEFORE UsernamePasswordAuthenticationFilter
             * so that JWT-authenticated requests are identified early in the chain.
             */
            .addFilterBefore(jwtAuthenticationFilter,
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}