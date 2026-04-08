package com.app.quantitymeasurement.service;

import com.app.quantitymeasurement.dto.request.AuthRequest;
import com.app.quantitymeasurement.dto.request.ForgotPasswordRequest;
import com.app.quantitymeasurement.dto.request.RegisterRequest;
import com.app.quantitymeasurement.dto.response.AuthResponse;
import com.app.quantitymeasurement.dto.response.MessageResponse;
import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.enums.AuthProvider;
import com.app.quantitymeasurement.enums.Role;
import com.app.quantitymeasurement.repository.UserRepository;
import com.app.quantitymeasurement.security.UserPrincipal;
import com.app.quantitymeasurement.security.jwt.JwtTokenProvider;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            EmailService emailService) {

        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailService = emailService;
    }
    
    // =========================================================================
    // Register
    // =========================================================================

    /**
     * Registers a new local user account.
     *
     * <ol>
     *   <li>Reject duplicate email addresses (409 Conflict).</li>
     *   <li>BCrypt-hash the raw password before persisting.</li>
     *   <li>Authenticate programmatically so a JWT can be issued immediately.</li>
     *   <li>Send a welcome email asynchronously.</li>
     * </ol>
     *
     * @param request the validated registration payload
     * @return an {@link AuthResponse} containing the signed JWT
     * @throws ResponseStatusException 409 if the email is already registered
     */
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration rejected — email already in use: {}", request.getEmail());
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Email is already in use."
            );
        }

        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();

        userRepository.save(newUser);
        log.info("Registered new user: {}", newUser.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        // Send welcome email (async — does not block the response)
        emailService.sendRegistrationEmail(
                newUser.getEmail(),
                newUser.getName() != null ? newUser.getName() : "there"
        );

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .email(principal.getEmail())
                .name(principal.getUser().getName())
                .role(principal.getUser().getRole().name())
                .build();
    }

    // =========================================================================
    // Login
    // =========================================================================

    /**
     * Authenticates an existing local user.
     *
     * <ol>
     *   <li>Delegate to {@link AuthenticationManager}; DaoAuthenticationProvider
     *       loads the user and verifies the BCrypt hash.</li>
     *   <li>Issue a JWT on success.</li>
     *   <li>Send a login-notification email asynchronously.</li>
     * </ol>
     *
     * @param request the validated login payload
     * @return an {@link AuthResponse} containing the signed JWT
     * @throws ResponseStatusException 401 if credentials are invalid
     */
    public AuthResponse login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtTokenProvider.generateToken(authentication);
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

            // Send login notification (async)
            emailService.sendLoginNotificationEmail(request.getEmail());

            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .email(principal.getEmail())
                    .name(principal.getUser().getName())
                    .role(principal.getUser().getRole().name())
                    .build();

        } catch (AuthenticationException ex) {
            log.warn("Login failed for email: {}", request.getEmail());
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password!"
            );
        }
    }

    // =========================================================================
    // Forgot password
    // =========================================================================

    /**
     * Resets a user's password without requiring the current password.
     * Intended for the "Forgot Password" flow where the user is not logged in.
     *
     * <ol>
     *   <li>Look up the user by email. Return 404 if not found.</li>
     *   <li>BCrypt-hash and persist the new password.</li>
     *   <li>Send a password-changed confirmation email asynchronously.</li>
     * </ol>
     *
     * @param email   the registered email address (path variable)
     * @param request the new password payload
     * @return {@link MessageResponse} with a success message
     * @throws ResponseStatusException 404 if the email is not found
     */
    public MessageResponse forgotPassword(String email, ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Forgot-password: user not found — {}", email);
                    return new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Sorry! We cannot find the user email: " + email
                    );
                });

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        log.info("Password updated via forgotPassword for: {}", email);

        // Send confirmation email (async)
        emailService.sendForgotPasswordEmail(email);

        return new MessageResponse("Password has been changed successfully!");
    }

    // =========================================================================
    // Reset password (logged-in user)
    // =========================================================================

    /**
     * Resets a user's password while they are logged in.
     * Requires the current password to be verified first.
     *
     * <ol>
     *   <li>Look up the user by email. Return 404 if not found.</li>
     *   <li>Verify {@code currentPassword} against the stored BCrypt hash.</li>
     *   <li>BCrypt-hash and persist {@code newPassword}.</li>
     *   <li>Send a password-reset confirmation email asynchronously.</li>
     * </ol>
     *
     * @param email           the registered email address (path variable)
     * @param currentPassword the user's current password (request param)
     * @param newPassword     the desired new password (request param)
     * @return {@link MessageResponse} with a success message
     * @throws ResponseStatusException 404 if email not found, 400 if current password is wrong
     */
    public MessageResponse resetPassword(String email,
                                         String currentPassword,
                                         String newPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Reset-password: user not found — {}", email);
                    return new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with email: " + email
                    );
                });

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Reset-password: incorrect current password for {}", email);
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Current password is incorrect!"
            );
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset successfully for: {}", email);

        // Send confirmation email (async)
        emailService.sendPasswordResetEmail(email);

        return new MessageResponse("Password reset successfully!");
    }
}
