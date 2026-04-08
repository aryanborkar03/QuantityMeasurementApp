package com.app.quantitymeasurement.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.app.quantitymeasurement.dto.request.AuthRequest;
import com.app.quantitymeasurement.dto.request.ForgotPasswordRequest;
import com.app.quantitymeasurement.dto.request.RegisterRequest;
import com.app.quantitymeasurement.dto.response.AuthResponse;
import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.enums.AuthProvider;
import com.app.quantitymeasurement.enums.Role;
import com.app.quantitymeasurement.repository.UserRepository;
import com.app.quantitymeasurement.security.UserPrincipal;
import com.app.quantitymeasurement.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired private MockMvc         mockMvc;
    @Autowired private ObjectMapper    objectMapper;
    @Autowired private UserRepository  userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    /*
     * Replace the real JavaMailSender with a no-op mock for all tests in this
     * class.  Without this, @SpringBootTest would attempt a real SMTP connection
     * on every register/login call, causing flaky failures in CI environments.
     */
    @MockBean
    private JavaMailSender mailSender;

    private static final String BASE           = "/api/v1/auth";
    private static final String VALID_PASSWORD = "Strong@123";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // =========================================================================
    // POST /register
    // =========================================================================

    @Test
    void testRegister_ValidRequest_Returns201WithToken() throws Exception {
        RegisterRequest req = new RegisterRequest("alice@example.com", VALID_PASSWORD, "Alice");

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
    void testRegister_DuplicateEmail_Returns409() throws Exception {
        saveLocalUser("dup@example.com", VALID_PASSWORD);

        RegisterRequest req = new RegisterRequest("dup@example.com", VALID_PASSWORD, "Dup");
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isConflict());
    }

    @Test
    void testRegister_PasswordTooShort_Returns400() throws Exception {
        // 5 chars — violates @Size(min=8) and the strength @Pattern
        RegisterRequest req = new RegisterRequest("x@example.com", "short", "X");
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_PasswordNoUppercase_Returns400() throws Exception {
        // Meets length and has special/digit, but lacks uppercase — violates @Pattern
        RegisterRequest req = new RegisterRequest("x@example.com", "weak@123", "X");
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_PasswordNoSpecialChar_Returns400() throws Exception {
        // Has uppercase and digit, but no special character — violates @Pattern
        RegisterRequest req = new RegisterRequest("x@example.com", "StrongPass1", "X");
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_PasswordNoDigit_Returns400() throws Exception {
        // Has uppercase and special char, but no digit — violates @Pattern
        RegisterRequest req = new RegisterRequest("x@example.com", "Strong@pass", "X");
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_InvalidEmail_Returns400() throws Exception {
        RegisterRequest req = new RegisterRequest("not-an-email", VALID_PASSWORD, "X");
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_BlankPassword_Returns400() throws Exception {
        RegisterRequest req = new RegisterRequest("y@example.com", "", "Y");
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_MissingBody_Returns400() throws Exception {
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_NullNameIsAllowed_Returns201() throws Exception {
        // name is optional — omitting it must not prevent registration
        RegisterRequest req = new RegisterRequest("noname@example.com", VALID_PASSWORD, null);
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }

    // =========================================================================
    // POST /login
    // =========================================================================

    @Test
    void testLogin_ValidCredentials_Returns200WithToken() throws Exception {
        saveLocalUser("bob@example.com", VALID_PASSWORD);

        AuthRequest req = new AuthRequest("bob@example.com", VALID_PASSWORD);
        mockMvc.perform(post(BASE + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.email").value("bob@example.com"))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testLogin_WrongPassword_Returns401() throws Exception {
        saveLocalUser("carol@example.com", VALID_PASSWORD);

        AuthRequest req = new AuthRequest("carol@example.com", "WrongPwd@9");
        mockMvc.perform(post(BASE + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_UnknownEmail_Returns401() throws Exception {
        AuthRequest req = new AuthRequest("nobody@example.com", VALID_PASSWORD);
        mockMvc.perform(post(BASE + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_BlankEmail_Returns400() throws Exception {
        AuthRequest req = new AuthRequest("", VALID_PASSWORD);
        mockMvc.perform(post(BASE + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_BlankPassword_Returns400() throws Exception {
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
    void testGetMe_WithValidJwt_Returns200WithProfile() throws Exception {
        User user = saveLocalUser("dan@example.com", VALID_PASSWORD);
        String token = generateToken(user);

        mockMvc.perform(get(BASE + "/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("dan@example.com"))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testGetMe_WithoutJwt_Returns401() throws Exception {
        mockMvc.perform(get(BASE + "/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetMe_WithInvalidJwt_Returns401() throws Exception {
        mockMvc.perform(get(BASE + "/me")
                .header("Authorization", "Bearer this.is.not.valid"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetMe_WithExpiredJwt_Returns401() throws Exception {
        mockMvc.perform(get(BASE + "/me")
                .header("Authorization",
                    "Bearer eyJhbGciOiJIUzI1NiJ9" +
                    ".eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiZXhwIjoxfQ.invalid"))
            .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Token round-trip — register then /me, login then /me
    // =========================================================================

    @Test
    void testRegisterToken_CanBeUsedForMe() throws Exception {
        RegisterRequest req = new RegisterRequest("eve@example.com", VALID_PASSWORD, "Eve");

        String body = mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readValue(body, AuthResponse.class).getAccessToken();

        mockMvc.perform(get(BASE + "/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("eve@example.com"));
    }

    @Test
    void testLoginToken_CanBeUsedForMe() throws Exception {
        saveLocalUser("frank@example.com", VALID_PASSWORD);

        String body = mockMvc.perform(post(BASE + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AuthRequest("frank@example.com", VALID_PASSWORD))))
            .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readValue(body, AuthResponse.class).getAccessToken();

        mockMvc.perform(get(BASE + "/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("frank@example.com"));
    }

    // =========================================================================
    // PUT /forgotPassword/{email} (UC19)
    // =========================================================================

    @Test
    void testForgotPassword_ExistingUser_Returns200WithSuccessMessage() throws Exception {
        saveLocalUser("grace@example.com", VALID_PASSWORD);

        ForgotPasswordRequest req = new ForgotPasswordRequest("NewStrong@456");

        mockMvc.perform(put(BASE + "/forgotPassword/grace@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message")
                .value("Password has been changed successfully!"));
    }

    @Test
    void testForgotPassword_UnknownEmail_Returns404() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest("NewStrong@456");

        mockMvc.perform(put(BASE + "/forgotPassword/nobody@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isNotFound());
    }

    @Test
    void testForgotPassword_WeakPassword_Returns400() throws Exception {
        /*
         * The new password must satisfy the same strength @Pattern as
         * registration.  "weakpassword" has no uppercase, no special char,
         * and no digit — all three rules violated.
         */
        ForgotPasswordRequest req = new ForgotPasswordRequest("weakpassword");

        mockMvc.perform(put(BASE + "/forgotPassword/any@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testForgotPassword_BlankPassword_Returns400() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest("");

        mockMvc.perform(put(BASE + "/forgotPassword/any@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testForgotPassword_IsPublicEndpoint_NoJwtRequired() throws Exception {
        /*
         * /forgotPassword must be reachable without a JWT — the user has
         * forgotten their password and is therefore not logged in.
         * No Authorization header is sent here.
         */
        saveLocalUser("helen@example.com", VALID_PASSWORD);

        ForgotPasswordRequest req = new ForgotPasswordRequest("NewStrong@456");

        mockMvc.perform(put(BASE + "/forgotPassword/helen@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk());
    }

    @Test
    void testForgotPassword_NewPasswordCanBeUsedToLogin() throws Exception {
        /*
         * End-to-end verification: after forgotPassword succeeds, the user
         * should be able to log in with the new password and fail with the old one.
         */
        saveLocalUser("ivan@example.com", VALID_PASSWORD);

        String newPassword = "Updated@789";
        ForgotPasswordRequest req = new ForgotPasswordRequest(newPassword);

        mockMvc.perform(put(BASE + "/forgotPassword/ivan@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk());

        // Old password should now be rejected
        mockMvc.perform(post(BASE + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AuthRequest("ivan@example.com", VALID_PASSWORD))))
            .andExpect(status().isUnauthorized());

        // New password should be accepted
        mockMvc.perform(post(BASE + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AuthRequest("ivan@example.com", newPassword))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    // =========================================================================
    // PUT /resetPassword/{email} (UC19)
    // =========================================================================

    @Test
    void testResetPassword_CorrectCurrentPassword_Returns200WithSuccessMessage()
            throws Exception {
        User user = saveLocalUser("judy@example.com", VALID_PASSWORD);
        String token = generateToken(user);

        mockMvc.perform(put(BASE + "/resetPassword/judy@example.com")
                .header("Authorization", "Bearer " + token)
                .param("currentPassword", VALID_PASSWORD)
                .param("newPassword", "NewStrong@456"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Password reset successfully!"));
    }

    @Test
    void testResetPassword_WrongCurrentPassword_Returns400() throws Exception {
        User user = saveLocalUser("kate@example.com", VALID_PASSWORD);
        String token = generateToken(user);

        mockMvc.perform(put(BASE + "/resetPassword/kate@example.com")
                .header("Authorization", "Bearer " + token)
                .param("currentPassword", "WrongPwd@9")
                .param("newPassword", "NewStrong@456"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testResetPassword_UnknownEmail_Returns404() throws Exception {
        /*
         * Even with a valid JWT, if the email in the path does not exist in
         * the database the service should return 404 rather than 400 or 500.
         */
        User user = saveLocalUser("real@example.com", VALID_PASSWORD);
        String token = generateToken(user);

        mockMvc.perform(put(BASE + "/resetPassword/ghost@example.com")
                .header("Authorization", "Bearer " + token)
                .param("currentPassword", VALID_PASSWORD)
                .param("newPassword", "NewStrong@456"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testResetPassword_WeakNewPassword_Returns400() throws Exception {
        User user = saveLocalUser("liam@example.com", VALID_PASSWORD);
        String token = generateToken(user);

        // "newpassword" — no uppercase, no special char, no digit
        mockMvc.perform(put(BASE + "/resetPassword/liam@example.com")
                .header("Authorization", "Bearer " + token)
                .param("currentPassword", VALID_PASSWORD)
                .param("newPassword", "newpassword"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testResetPassword_WithoutJwt_Returns401() throws Exception {
        /*
         * Unlike /forgotPassword, /resetPassword is a protected endpoint.
         * It requires a valid JWT because the user is assumed to be logged in.
         */
        saveLocalUser("mary@example.com", VALID_PASSWORD);

        mockMvc.perform(put(BASE + "/resetPassword/mary@example.com")
                .param("currentPassword", VALID_PASSWORD)
                .param("newPassword", "NewStrong@456"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testResetPassword_NewPasswordCanBeUsedToLogin() throws Exception {
        /*
         * End-to-end verification: after resetPassword succeeds, the user
         * should be able to log in with the new password and fail with the old one.
         */
        User user = saveLocalUser("nick@example.com", VALID_PASSWORD);
        String token = generateToken(user);
        String newPassword = "Changed@789";

        mockMvc.perform(put(BASE + "/resetPassword/nick@example.com")
                .header("Authorization", "Bearer " + token)
                .param("currentPassword", VALID_PASSWORD)
                .param("newPassword", newPassword))
            .andExpect(status().isOk());

        // Old password should now be rejected
        mockMvc.perform(post(BASE + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AuthRequest("nick@example.com", VALID_PASSWORD))))
            .andExpect(status().isUnauthorized());

        // New password should be accepted
        mockMvc.perform(post(BASE + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AuthRequest("nick@example.com", newPassword))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty());
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
            new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        return jwtTokenProvider.generateToken(auth);
    }
}
