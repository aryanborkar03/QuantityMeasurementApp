package com.app.quantitymeasurement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AuthRequest
 *
 * Data Transfer Object for the local login request body.
 * Clients POST this payload to {@code /api/v1/auth/login} to obtain a
 * JWT access token. Both fields are validated before the service layer is
 * reached; constraint violations are handled by {@code GlobalExceptionHandler}
 * and result in a {@code 400 Bad Request} response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    /**
     * The user's email address. Must be a syntactically valid email and must not
     * be blank. This value is used as the principal name when loading the user
     * from the database via {@code CustomUserDetailsService}.
     */
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid address")
    private String email;

    /**
     * The user's plain-text password. Must not be blank.
     * It is compared against the stored BCrypt hash by
     * org.springframework.security.crypto.password.PasswordEncoder#matches.
     * This field is never stored or logged.
     */
    @NotBlank(message = "Password must not be blank")
    private String password;
}
