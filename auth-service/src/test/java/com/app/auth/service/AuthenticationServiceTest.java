package com.app.auth.service;

import com.app.auth.dto.request.AuthRequest;
import com.app.auth.dto.response.AuthResponse;
import com.app.auth.entity.User;
import com.app.auth.enums.AuthProvider;
import com.app.auth.enums.Role;
import com.app.auth.repository.UserRepository;
import com.app.auth.security.UserPrincipal;
import com.app.auth.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {

    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final EmailService emailService = mock(EmailService.class);

    private final AuthenticationService authenticationService = new AuthenticationService(
            authenticationManager,
            userRepository,
            passwordEncoder,
            jwtTokenProvider,
            emailService
    );

    @Test
    void loginSendsNotificationEmailAfterSuccessfulAuthentication() {
        User user = User.builder()
                .email("saved@example.com")
                .name("Anuj")
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .verified(true)
                .build();
        UserPrincipal principal = UserPrincipal.create(user);
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");

        AuthResponse response = authenticationService.login(new AuthRequest("typed@example.com", "secret"));

        verify(emailService).sendLoginNotificationEmail("saved@example.com", "Anuj");
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("saved@example.com", response.getEmail());
        assertEquals("Anuj", response.getName());
        assertEquals("USER", response.getRole());
    }

    @Test
    void loginDoesNotSendNotificationEmailWhenAuthenticationFails() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.login(new AuthRequest("saved@example.com", "wrong"))
        );

        verify(emailService, never()).sendLoginNotificationEmail(any(), any());
    }
}
