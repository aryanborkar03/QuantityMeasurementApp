package com.app.quantitymeasurement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


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
     * {@link org.springframework.security.crypto.password.PasswordEncoder#matches}.
     * This field is never stored or logged.
     */
    @NotBlank(message = "Password must not be blank")
    private String password;
}
