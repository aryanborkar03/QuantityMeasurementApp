package com.app.quantitymeasurement.controller;

import lombok.extern.slf4j.Slf4j;

import com.app.quantitymeasurement.dto.request.AuthRequest;
import com.app.quantitymeasurement.dto.request.ForgotPasswordRequest;
import com.app.quantitymeasurement.dto.request.RegisterRequest;
import com.app.quantitymeasurement.dto.response.AuthResponse;
import com.app.quantitymeasurement.dto.response.MessageResponse;
import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.security.UserPrincipal;
import com.app.quantitymeasurement.service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Validated
@RestController
@CrossOrigin(origins = "localhost:5173")
@RequestMapping("/api/v1/auth")
@Tag(
    name        = "Authentication",
    description = "Local registration, login, profile, and password-management endpoints. " +
                  "For Google OAuth2, navigate to /oauth2/authorization/google."
)
public class AuthController {

	private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }
    
    // =========================================================================
    // Register
    // =========================================================================

    @PostMapping("/register")
    @Operation(
        summary     = "Register a new local account",
        description = "Creates an account with email + BCrypt-hashed password. Returns a JWT immediately."
    )
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/v1/auth/register — email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================================================================
    // Login
    // =========================================================================

    @PostMapping("/login")
    @Operation(
        summary     = "Log in with email and password",
        description = "Authenticates a LOCAL account and returns a signed JWT."
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        log.info("POST /api/v1/auth/login — email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // Current user profile
    // =========================================================================

    @GetMapping("/me")
    @Operation(
        summary     = "Get current user profile",
        description = "Returns the profile of the authenticated user. Requires a valid Bearer token."
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

    // =========================================================================
    // Forgot password — PUBLIC (user not logged in)
    // =========================================================================

    @PutMapping("/forgotPassword/{email}")
    @Operation(
        summary     = "Forgot password — reset without current password",
        description = "Looks up the user by email, hashes and saves the new password, " +
                      "and sends a confirmation email. Public — no JWT required."
    )
    public ResponseEntity<MessageResponse> forgotPassword(
            @Parameter(description = "Registered email address")
            @PathVariable String email,
            @Valid @RequestBody ForgotPasswordRequest request) {

        log.info("PUT /api/v1/auth/forgotPassword/{}", email);
        MessageResponse response = authService.forgotPassword(email, request);
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // Reset password — PROTECTED (user is logged in)
    // =========================================================================

    @PutMapping("/resetPassword/{email}")
    @Operation(
        summary     = "Reset password — verifies current password first",
        description = "Compares currentPassword against the stored BCrypt hash. " +
                      "If correct, hashes and saves newPassword. Requires a valid JWT."
    )
    public ResponseEntity<MessageResponse> resetPassword(
            @Parameter(description = "Registered email address")
            @PathVariable String email,

            @Parameter(description = "User's current password")
            @RequestParam
            @NotBlank(message = "Current password must not be blank")
            String currentPassword,

            @Parameter(description = "Desired new password (min 8 chars, 1 uppercase, 1 special, 1 digit)")
            @RequestParam
            @NotBlank(message = "New password must not be blank")
            @Pattern(
                regexp  = "^(?=.*[A-Z])(?=.*[@#$%^&*()+\\-=])(?=.*\\d).{8,}$",
                message = "New password must be at least 8 chars, contain 1 uppercase, 1 special char, 1 digit"
            )
            String newPassword) {

        log.info("PUT /api/v1/auth/resetPassword/{}", email);
        MessageResponse response = authService.resetPassword(email, currentPassword, newPassword);
        return ResponseEntity.ok(response);
    }
}
