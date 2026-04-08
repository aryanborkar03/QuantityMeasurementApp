package com.app.quantitymeasurement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

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


@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository         userRepository;
    @Mock private PasswordEncoder        passwordEncoder;
    @Mock private JwtTokenProvider       jwtTokenProvider;
    @Mock private EmailService           emailService;
    @Mock private Authentication         authentication;

    @InjectMocks
    private AuthenticationService authService;

    private User localUser;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        localUser = User.builder()
            .email("alice@example.com")
            .name("Alice")
            .password("$2a$10$hashedPassword")
            .provider(AuthProvider.LOCAL)
            .role(Role.USER)
            .build();

        userPrincipal = UserPrincipal.create(localUser);
    }

    // =========================================================================
    // register — happy path
    // =========================================================================

    @Test
    void testRegister_NewEmail_Returns201WithToken() {
        RegisterRequest req = new RegisterRequest("alice@example.com", "Strong@123", "Alice");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Strong@123")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(localUser);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt.token.here");

        AuthResponse response = authService.register(req);

        assertNotNull(response);
        assertEquals("jwt.token.here",        response.getAccessToken());
        assertEquals("Bearer",                response.getTokenType());
        assertEquals("alice@example.com",     response.getEmail());
        assertEquals("Alice",                 response.getName());
        assertEquals("USER",                  response.getRole());
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsConflict() {
        RegisterRequest req = new RegisterRequest("dup@example.com", "Strong@123", "Dup");

        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> authService.register(req)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Email is already in use"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_PasswordIsHashedBeforeSaving() {
        RegisterRequest req = new RegisterRequest("bob@example.com", "Strong@123", "Bob");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Strong@123")).thenReturn("$2a$10$bcryptHash");
        when(userRepository.save(any(User.class))).thenReturn(localUser);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(jwtTokenProvider.generateToken(any())).thenReturn("token");

        authService.register(req);

        /*
         * Capture the User argument passed to save() and verify that
         * the raw password is never stored — only the BCrypt hash.
         */
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("$2a$10$bcryptHash", saved.getPassword());
        assertNotEquals("Strong@123",     saved.getPassword());
    }

    @Test
    void testRegister_NewUser_HasLocalProviderAndUserRole() {
        RegisterRequest req = new RegisterRequest("carol@example.com", "Strong@123", "Carol");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenReturn(localUser);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(jwtTokenProvider.generateToken(any())).thenReturn("token");

        authService.register(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals(AuthProvider.LOCAL, saved.getProvider());
        assertEquals(Role.USER,          saved.getRole());
    }

    @Test
    void testRegister_SendsRegistrationEmailAsynchronously() {
        RegisterRequest req = new RegisterRequest("dave@example.com", "Strong@123", "Dave");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenReturn(localUser);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(jwtTokenProvider.generateToken(any())).thenReturn("token");

        authService.register(req);

        /*
         * The email is dispatched after the user is saved and authenticated.
         * We verify the call happened (fire-and-forget) but do not assert on
         * SMTP delivery — that is EmailService's responsibility.
         */
        verify(emailService, times(1))
            .sendRegistrationEmail(anyString(), anyString());
    }

    @Test
    void testRegister_NullName_FallsBackToDefaultGreeting() {
        /*
         * When a user registers without providing a name, the service should
         * not throw a NullPointerException when building the greeting for
         * the registration email. The EmailService receives "there" as the
         * fallback display name.
         */
        RegisterRequest req = new RegisterRequest("noname@example.com", "Strong@123", null);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        User namelessUser = User.builder()
            .email("noname@example.com")
            .provider(AuthProvider.LOCAL)
            .role(Role.USER)
            .build();
        UserPrincipal namelessPrincipal = UserPrincipal.create(namelessUser);

        when(userRepository.save(any(User.class))).thenReturn(namelessUser);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(namelessPrincipal);
        when(jwtTokenProvider.generateToken(any())).thenReturn("token");

        authService.register(req);

        verify(emailService).sendRegistrationEmail(anyString(), eq("there"));
    }

    // =========================================================================
    // login — happy path
    // =========================================================================

    @Test
    void testLogin_ValidCredentials_ReturnsTokenAndProfile() {
        AuthRequest req = new AuthRequest("alice@example.com", "Strong@123");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt.login.token");

        AuthResponse response = authService.login(req);

        assertNotNull(response);
        assertEquals("jwt.login.token",   response.getAccessToken());
        assertEquals("Bearer",            response.getTokenType());
        assertEquals("alice@example.com", response.getEmail());
        assertEquals("USER",              response.getRole());
    }

    @Test
    void testLogin_InvalidCredentials_ThrowsUnauthorized() {
        AuthRequest req = new AuthRequest("alice@example.com", "WrongPwd@1");

        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> authService.login(req)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Invalid email or password"));
    }

    @Test
    void testLogin_ValidCredentials_SendsLoginNotificationEmail() {
        AuthRequest req = new AuthRequest("alice@example.com", "Strong@123");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(jwtTokenProvider.generateToken(any())).thenReturn("token");

        authService.login(req);

        verify(emailService, times(1))
            .sendLoginNotificationEmail("alice@example.com");
    }

    @Test
    void testLogin_InvalidCredentials_DoesNotSendEmail() {
        AuthRequest req = new AuthRequest("alice@example.com", "WrongPwd@1");

        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(ResponseStatusException.class, () -> authService.login(req));

        /*
         * If authentication fails, no email should be dispatched.
         * Sending a login-notification email on a failed attempt would
         * leak information about account existence.
         */
        verify(emailService, never()).sendLoginNotificationEmail(anyString());
    }

    // =========================================================================
    // forgotPassword
    // =========================================================================

    @Test
    void testForgotPassword_ExistingEmail_ReturnsSuccessMessage() {
        ForgotPasswordRequest req = new ForgotPasswordRequest("NewStrong@123");

        when(userRepository.findByEmail("alice@example.com"))
            .thenReturn(Optional.of(localUser));
        when(passwordEncoder.encode("NewStrong@123")).thenReturn("$2a$10$newHash");
        when(userRepository.save(any(User.class))).thenReturn(localUser);

        MessageResponse response = authService.forgotPassword("alice@example.com", req);

        assertNotNull(response);
        assertEquals("Password has been changed successfully!", response.getMessage());
    }

    @Test
    void testForgotPassword_UnknownEmail_ThrowsNotFound() {
        ForgotPasswordRequest req = new ForgotPasswordRequest("NewStrong@123");

        when(userRepository.findByEmail("nobody@example.com"))
            .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> authService.forgotPassword("nobody@example.com", req)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("nobody@example.com"));
    }

    @Test
    void testForgotPassword_NewPasswordIsHashedBeforeSaving() {
        ForgotPasswordRequest req = new ForgotPasswordRequest("NewStrong@123");

        when(userRepository.findByEmail("alice@example.com"))
            .thenReturn(Optional.of(localUser));
        when(passwordEncoder.encode("NewStrong@123")).thenReturn("$2a$10$newBcryptHash");

        authService.forgotPassword("alice@example.com", req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertEquals("$2a$10$newBcryptHash", captor.getValue().getPassword());
        assertNotEquals("NewStrong@123",     captor.getValue().getPassword());
    }

    @Test
    void testForgotPassword_ExistingEmail_SendsConfirmationEmail() {
        ForgotPasswordRequest req = new ForgotPasswordRequest("NewStrong@123");

        when(userRepository.findByEmail("alice@example.com"))
            .thenReturn(Optional.of(localUser));
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any())).thenReturn(localUser);

        authService.forgotPassword("alice@example.com", req);

        verify(emailService, times(1))
            .sendForgotPasswordEmail("alice@example.com");
    }

    @Test
    void testForgotPassword_UnknownEmail_DoesNotSaveOrSendEmail() {
        ForgotPasswordRequest req = new ForgotPasswordRequest("NewStrong@123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(
            ResponseStatusException.class,
            () -> authService.forgotPassword("ghost@example.com", req)
        );

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendForgotPasswordEmail(anyString());
    }

    // =========================================================================
    // resetPassword
    // =========================================================================

    @Test
    void testResetPassword_CorrectCurrentPassword_ReturnsSuccessMessage() {
        when(userRepository.findByEmail("alice@example.com"))
            .thenReturn(Optional.of(localUser));
        when(passwordEncoder.matches("Strong@123", "$2a$10$hashedPassword"))
            .thenReturn(true);
        when(passwordEncoder.encode("NewStrong@456")).thenReturn("$2a$10$newHash");
        when(userRepository.save(any(User.class))).thenReturn(localUser);

        MessageResponse response =
            authService.resetPassword("alice@example.com", "Strong@123", "NewStrong@456");

        assertNotNull(response);
        assertEquals("Password reset successfully!", response.getMessage());
    }

    @Test
    void testResetPassword_UnknownEmail_ThrowsNotFound() {
        when(userRepository.findByEmail("nobody@example.com"))
            .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> authService.resetPassword("nobody@example.com", "any", "NewStrong@456")
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("nobody@example.com"));
    }

    @Test
    void testResetPassword_WrongCurrentPassword_ThrowsBadRequest() {
        when(userRepository.findByEmail("alice@example.com"))
            .thenReturn(Optional.of(localUser));
        when(passwordEncoder.matches("WrongPwd@1", "$2a$10$hashedPassword"))
            .thenReturn(false);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> authService.resetPassword("alice@example.com", "WrongPwd@1", "NewStrong@456")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Current password is incorrect"));
    }

    @Test
    void testResetPassword_NewPasswordIsHashedBeforeSaving() {
        when(userRepository.findByEmail("alice@example.com"))
            .thenReturn(Optional.of(localUser));
        when(passwordEncoder.matches("Strong@123", "$2a$10$hashedPassword"))
            .thenReturn(true);
        when(passwordEncoder.encode("NewStrong@456")).thenReturn("$2a$10$freshHash");

        authService.resetPassword("alice@example.com", "Strong@123", "NewStrong@456");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertEquals("$2a$10$freshHash", captor.getValue().getPassword());
        assertNotEquals("NewStrong@456", captor.getValue().getPassword());
    }

    @Test
    void testResetPassword_CorrectCurrentPassword_SendsConfirmationEmail() {
        when(userRepository.findByEmail("alice@example.com"))
            .thenReturn(Optional.of(localUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any())).thenReturn(localUser);

        authService.resetPassword("alice@example.com", "Strong@123", "NewStrong@456");

        verify(emailService, times(1))
            .sendPasswordResetEmail("alice@example.com");
    }

    @Test
    void testResetPassword_WrongCurrentPassword_DoesNotSaveOrSendEmail() {
        when(userRepository.findByEmail("alice@example.com"))
            .thenReturn(Optional.of(localUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(
            ResponseStatusException.class,
            () -> authService.resetPassword("alice@example.com", "Wrong@123", "New@Strong1")
        );

        /*
         * If the current password check fails, neither the user record
         * nor an email confirmation should be issued — the state must
         * remain unchanged.
         */
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString());
    }
}
