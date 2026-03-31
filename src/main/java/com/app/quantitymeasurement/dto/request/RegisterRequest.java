package com.app.quantitymeasurement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RegisterRequest
 *
 * Data Transfer Object for the local user registration request body.
 *
 * Clients POST this payload to {@code /api/v1/auth/register} to create a new
 * local account. On success, the endpoint returns an {@link AuthResponse} with
 * an immediately usable JWT — the user does not need to log in separately after
 * registering.
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /**
     * The email address for the new account. Must be unique across all existing
     * users. Used as the principal identifier for subsequent logins.
     */
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid address")
    private String email;

    /**
     * The plain-text password chosen by the user. BCrypt-hashed before storage;
     * the raw value is never persisted or logged.
     */
    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    /**
     * Optional display name for the user profile.
     * If not provided, it is stored as {@code null} in the database.
     */
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
}
