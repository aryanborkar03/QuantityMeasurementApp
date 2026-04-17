package com.app.auth.service;

import com.app.auth.dto.request.AuthRequest;
import com.app.auth.dto.request.RegisterRequest;
import com.app.auth.dto.response.AuthResponse;
import com.app.auth.dto.response.MessageResponse;
import com.app.auth.entity.User;
import com.app.auth.enums.AuthProvider;
import com.app.auth.enums.Role;
import com.app.auth.repository.UserRepository;
import com.app.auth.security.UserPrincipal;
import com.app.auth.security.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    public AuthenticationService(AuthenticationManager authenticationManager, UserRepository userRepository,
                                 PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailService = emailService;
    }
    
    // 1. REGISTER (Instant Verification, Manual Login Required)
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
        }

        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .verified(true) // Instantly verified!
                .build();

        userRepository.save(newUser);
        emailService.sendRegistrationEmail(newUser.getEmail(), newUser.getName() != null ? newUser.getName() : "User");

        return new MessageResponse("Registration successful! Please log in.");
    }

    // 2. LOGIN
    public AuthResponse login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtTokenProvider.generateToken(authentication);
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = principal.getUser();

            emailService.sendLoginNotificationEmail(
                    principal.getEmail(),
                    user.getName() != null ? user.getName() : "User"
            );

            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .email(principal.getEmail())
                    .name(user.getName())
                    .role(user.getRole().name())
                    .build();

        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password!");
        }
    }

    // 3. FORGOT PASSWORD - Request OTP
    public MessageResponse requestForgotPasswordOtp(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendForgotPasswordOtpEmail(user.getEmail(), user.getName() != null ? user.getName() : "User", otp);
        return new MessageResponse("OTP sent to your email.");
    }

    // 4. FORGOT PASSWORD - Verify OTP
    public MessageResponse verifyForgotPasswordOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP.");
        }
        if (user.getOtpExpiry() == null || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP has expired.");
        }
        return new MessageResponse("OTP verified successfully.");
    }

    // 5. FORGOT PASSWORD - Reset Password
    public MessageResponse resetPasswordWithOtp(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail());
        return new MessageResponse("Password reset successfully.");
    }
}
