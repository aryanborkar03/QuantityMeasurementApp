package com.app.quantitymeasurement.controller;

import com.app.quantitymeasurement.dto.request.AuthRequest;
import com.app.quantitymeasurement.dto.response.AuthResponse;
import com.app.quantitymeasurement.dto.request.RegisterRequest;
import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.enums.AuthProvider;
import com.app.quantitymeasurement.enums.Role;
import com.app.quantitymeasurement.repository.UserRepository;
import com.app.quantitymeasurement.security.jwt.JwtTokenProvider;
import com.app.quantitymeasurement.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthControllerTest
 *
 * Integration tests for {@link AuthController} using {@code @SpringBootTest} with
 * {@code @AutoConfigureMockMvc}. The full security filter chain is active so that
 * JWT validation, entry-point, and access-denied responses are tested end-to-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    private static final String BASE = "/api/v1/auth";

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    // =========================================================================
    // POST /register
    // =========================================================================

    @Test
    public void testRegister_ValidRequest_Returns201WithToken() throws Exception {
        RegisterRequest req = new RegisterRequest("alice@example.com", "password123", "Alice");

        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.email").value("alice@example.com"))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    public void testRegister_DuplicateEmail_Returns409() throws Exception {
        saveLocalUser("dup@example.com", "password123");

        RegisterRequest req = new RegisterRequest("dup@example.com", "password123", "Dup");
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isConflict());
    }

    @Test
    public void testRegister_PasswordTooShort_Returns400() throws Exception {
        RegisterRequest req = new RegisterRequest("x@example.com", "short", "X");
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegister_InvalidEmail_Returns400() throws Exception {
        RegisterRequest req = new RegisterRequest("not-an-email", "password123", "X");
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegister_BlankPassword_Returns400() throws Exception {
        RegisterRequest req = new RegisterRequest("y@example.com", "", "Y");
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegister_MissingBody_Returns400() throws Exception {
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegister_NullNameIsAllowed_Returns201() throws Exception {
        RegisterRequest req = new RegisterRequest("noname@example.com", "password123", null);
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }

    // =========================================================================
    // POST /login
    // =========================================================================

    @Test
    public void testLogin_ValidCredentials_Returns200WithToken() throws Exception {
        saveLocalUser("bob@example.com", "password123");

        AuthRequest req = new AuthRequest("bob@example.com", "password123");
        mockMvc.perform(post(BASE + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.email").value("bob@example.com"))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    public void testLogin_WrongPassword_Returns401() throws Exception {
        saveLocalUser("carol@example.com", "correctPwd123");

        AuthRequest req = new AuthRequest("carol@example.com", "wrongPwd123");
        mockMvc.perform(post(BASE + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLogin_UnknownEmail_Returns401() throws Exception {
        AuthRequest req = new AuthRequest("nobody@example.com", "password123");
        mockMvc.perform(post(BASE + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLogin_BlankEmail_Returns400() throws Exception {
        AuthRequest req = new AuthRequest("", "password123");
        mockMvc.perform(post(BASE + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testLogin_BlankPassword_Returns400() throws Exception {
        AuthRequest req = new AuthRequest("user@example.com", "");
        mockMvc.perform(post(BASE + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // GET /me
    // =========================================================================

    @Test
    public void testGetMe_WithValidJwt_Returns200WithProfile() throws Exception {
        User user = saveLocalUser("dan@example.com", "password123");
        String token = generateToken(user);

        mockMvc.perform(get(BASE + "/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("dan@example.com"))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    public void testGetMe_WithoutJwt_Returns401() throws Exception {
        mockMvc.perform(get(BASE + "/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetMe_WithInvalidJwt_Returns401() throws Exception {
        mockMvc.perform(get(BASE + "/me")
                .header("Authorization", "Bearer this.is.not.valid"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetMe_WithExpiredJwt_Returns401() throws Exception {
        mockMvc.perform(get(BASE + "/me")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiZXhwIjoxfQ.invalid"))
            .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Token returned by register/login is usable
    // =========================================================================

    @Test
    public void testRegisterToken_CanBeUsedForMe() throws Exception {
        RegisterRequest req = new RegisterRequest("eve@example.com", "password123", "Eve");

        String body = mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        AuthResponse response = objectMapper.readValue(body, AuthResponse.class);
        String token = response.getAccessToken();

        mockMvc.perform(get(BASE + "/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("eve@example.com"));
    }

    @Test
    public void testLoginToken_CanBeUsedForMe() throws Exception {
        saveLocalUser("frank@example.com", "password123");

        String body = mockMvc.perform(post(BASE + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AuthRequest("frank@example.com", "password123"))))
            .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readValue(body, AuthResponse.class).getAccessToken();

        mockMvc.perform(get(BASE + "/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("frank@example.com"));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private User saveLocalUser(String email, String rawPassword) {
        User user = User.builder()
            .email(email)
            .password(passwordEncoder.encode(rawPassword))
            .provider(AuthProvider.LOCAL)
            .role(Role.USER)
            .build();
        return userRepository.save(user);
    }

    private String generateToken(User user) {
        UserPrincipal principal = UserPrincipal.create(user);
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        return jwtTokenProvider.generateToken(auth);
    }
}
