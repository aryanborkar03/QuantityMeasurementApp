package com.app.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for receiving OTP verification requests.
 * Used during the registration flow to verify a user's email address,
 * and during the forgot password flow to verify identity before resetting the password.
 */
@Data
public class VerifyOtpRequest {

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "OTP must not be blank")
    private String otp;
    
}