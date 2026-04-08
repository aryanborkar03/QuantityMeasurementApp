package com.app.quantitymeasurement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    /**
     * The new password the user wants to set.
     * Must meet the application's password-strength requirements:
     * at least 8 characters, one uppercase letter, one digit,
     * and one special character.
     */
    @NotBlank(message = "Password must not be blank")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[@#$%^&*()+\\-=])(?=.*\\d).{8,}$",
        message = "Password must be at least 8 characters and contain at least " +
                  "1 uppercase letter, 1 special character (@#$%^&*()-+=), and 1 number"
    )
    private String password;
}
