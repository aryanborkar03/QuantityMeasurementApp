package com.app.auth.controller;

import com.app.auth.dto.request.AuthRequest;
import com.app.auth.dto.request.RegisterRequest;
import com.app.auth.dto.request.VerifyOtpRequest;
import com.app.auth.dto.request.ResetPasswordOtpRequest;
import com.app.auth.dto.response.AuthResponse;
import com.app.auth.dto.response.MessageResponse;
import com.app.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgotPassword/request")
    public ResponseEntity<MessageResponse> requestForgotPwOtp(@RequestParam @Email String email) {
        return ResponseEntity.ok(authService.requestForgotPasswordOtp(email));
    }

    @PostMapping("/forgotPassword/verify")
    public ResponseEntity<MessageResponse> verifyForgotPwOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyForgotPasswordOtp(request.getEmail(), request.getOtp()));
    }

    @PostMapping("/forgotPassword/reset")
    public ResponseEntity<MessageResponse> resetPwWithOtp(@Valid @RequestBody ResetPasswordOtpRequest request) {
        return ResponseEntity.ok(authService.resetPasswordWithOtp(request.getEmail(), request.getOtp(), request.getNewPassword()));
    }
}