package com.app.quantitymeasurement.controller;

import lombok.extern.slf4j.Slf4j;

import com.app.quantitymeasurement.dto.request.AuthRequest;
import com.app.quantitymeasurement.dto.response.AuthResponse;
import com.app.quantitymeasurement.dto.request.RegisterRequest;
import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.enums.AuthProvider;
import com.app.quantitymeasurement.enums.Role;
import com.app.quantitymeasurement.repository.UserRepository;
import com.app.quantitymeasurement.security.jwt.JwtTokenProvider;
import com.app.quantitymeasurement.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


/**
 * AuthController
 *
 * REST controller exposing local authentication endpoints:
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication",
     description = "Local registration, login, and profile endpoints. " +
                   "For Google OAuth2, navigate to /oauth2/authorization/google.")
public class AuthController {



    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Registers a new local user account.
     *
     * @param registerRequest the registration payload; validated by Bean Validation
     * @return {@code 201 Created} with a JWT, or {@code 409 Conflict} if the
     *         email is already registered
     */
    @PostMapping("/register")
    @Operation(
        summary     = "Register a new local account",
        description = "Creates an account with email + BCrypt-hashed password. " +
                      "Returns a JWT immediately — no separate login required."
    )
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest) {

        log.info("POST /api/v1/auth/register — email: " + registerRequest.getEmail());

        /*
         * Step 1 — duplicate-email guard.
         * Check before inserting to give a clean 409 instead of a database
         * constraint violation that would propagate as a 500 error.
         */
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }

        /*
         * Step 2 — build and persist the new user.
         * The BCrypt hash is generated here; the raw password is never stored.
         */
        User newUser = User.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .name(registerRequest.getName())
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();

        userRepository.save(newUser);
        log.info("Registered new user: " + newUser.getEmail());

        /*
         * Step 3 — authenticate programmatically so we can generate a JWT.
         * authenticate() calls DaoAuthenticationProvider which:
         *   a) loads the user via CustomUserDetailsService
         *   b) verifies the password (which we just set, so this always succeeds)
         *   c) returns a fully-authenticated UsernamePasswordAuthenticationToken
         */
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(),
                        registerRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        /*
         * Step 4 — generate the JWT and build the response.
         */
        String token = jwtTokenProvider.generateToken(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .email(principal.getEmail())
                .name(principal.getUser().getName())
                .role(principal.getUser().getRole().name())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    /**
     * Authenticates an existing local user.
     * @param authRequest the login payload containing email and password
     * @return {@code 200 OK} with a JWT on success
     */
    @PostMapping("/login")
    @Operation(
        summary     = "Log in with email and password",
        description = "Authenticates a LOCAL account and returns a signed JWT. " +
                      "Google accounts must use /oauth2/authorization/google."
    )
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest authRequest) {

        log.info("POST /api/v1/auth/login — email: " + authRequest.getEmail());

        try {
            /*
             * Attempt authentication. If this throws (wrong credentials, user not
             * found), Spring Security's exception translation layer converts it to
             * a 401 response via JwtAuthenticationEntryPoint.
             */
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );
    
            SecurityContextHolder.getContext().setAuthentication(authentication);
    
            String token = jwtTokenProvider.generateToken(authentication);
    
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
    
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .email(principal.getEmail())
                    .name(principal.getUser().getName())
                    .role(principal.getUser().getRole().name())
                    .build();
    
            return ResponseEntity.ok(authResponse);
        } catch (AuthenticationException ex) {

            log.warn("Login failed for email: "
                    + authRequest.getEmail());

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }

    /**
     * Returns the profile of the currently authenticated user.
     * @param authentication injected by Spring MVC from the SecurityContext;
     *                       always non-null here because of the URL-level security rule
     * @return {@code 200 OK} with the user's email, name, and role
     */
    @GetMapping("/me")
    @Operation(
        summary     = "Get current user profile",
        description = "Returns the profile of the authenticated user. " +
                      "Requires a valid Bearer token in the Authorization header."
    )
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();

        AuthResponse profileResponse = AuthResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();

        return ResponseEntity.ok(profileResponse);
    }
}