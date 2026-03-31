package com.app.quantitymeasurement.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.app.quantitymeasurement.dto.request.AuthRequest;
import com.app.quantitymeasurement.dto.request.RegisterRequest;
import com.app.quantitymeasurement.dto.response.AuthResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * AuthDTOTest
 *
 * Bean Validation tests for {@link AuthRequest} and {@link RegisterRequest}.
 * Uses the Jakarta Validation API directly (no Spring context needed) to verify
 * that constraint violations are triggered for invalid inputs.
 */
public class AuthDTOTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // =========================================================================
    // AuthRequest — valid
    // =========================================================================

    @Test
    public void testAuthRequest_ValidPayload_NoViolations() {
        AuthRequest req = new AuthRequest("user@example.com", "password123");
        assertTrue(validator.validate(req).isEmpty());
    }

    // =========================================================================
    // AuthRequest — email
    // =========================================================================

    @Test
    public void testAuthRequest_BlankEmail_Violation() {
        AuthRequest req = new AuthRequest("", "password123");
        Set<String> messages = messages(validator.validate(req));
        assertTrue(messages.stream().anyMatch(m -> m.contains("blank")));
    }

    @Test
    public void testAuthRequest_InvalidEmailFormat_Violation() {
        AuthRequest req = new AuthRequest("not-an-email", "password123");
        Set<String> messages = messages(validator.validate(req));
        assertTrue(messages.stream().anyMatch(m -> m.toLowerCase().contains("valid")));
    }

    @Test
    public void testAuthRequest_NullEmail_Violation() {
        AuthRequest req = new AuthRequest(null, "password123");
        assertFalse(validator.validate(req).isEmpty());
    }

    // =========================================================================
    // AuthRequest — password
    // =========================================================================

    @Test
    public void testAuthRequest_BlankPassword_Violation() {
        AuthRequest req = new AuthRequest("user@example.com", "");
        Set<String> messages = messages(validator.validate(req));
        assertTrue(messages.stream().anyMatch(m -> m.contains("blank")));
    }

    @Test
    public void testAuthRequest_NullPassword_Violation() {
        AuthRequest req = new AuthRequest("user@example.com", null);
        assertFalse(validator.validate(req).isEmpty());
    }

    // =========================================================================
    // RegisterRequest — valid
    // =========================================================================

    @Test
    public void testRegisterRequest_ValidPayload_NoViolations() {
        RegisterRequest req = new RegisterRequest("new@example.com", "strongPass1", "Jane Doe");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    public void testRegisterRequest_NullName_NoViolation() {
        // name is optional
        RegisterRequest req = new RegisterRequest("new@example.com", "strongPass1", null);
        assertTrue(validator.validate(req).isEmpty());
    }

    // =========================================================================
    // RegisterRequest — email
    // =========================================================================

    @Test
    public void testRegisterRequest_BlankEmail_Violation() {
        RegisterRequest req = new RegisterRequest("", "strongPass1", "Jane");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    public void testRegisterRequest_InvalidEmail_Violation() {
        RegisterRequest req = new RegisterRequest("bad-email", "strongPass1", "Jane");
        assertFalse(validator.validate(req).isEmpty());
    }

    // =========================================================================
    // RegisterRequest — password length
    // =========================================================================

    @Test
    public void testRegisterRequest_PasswordTooShort_Violation() {
        RegisterRequest req = new RegisterRequest("x@example.com", "short", "X");
        Set<String> messages = messages(validator.validate(req));
        assertTrue(messages.stream().anyMatch(m -> m.contains("8")));
    }

    @Test
    public void testRegisterRequest_PasswordTooLong_Violation() {
        String tooLong = "a".repeat(101);
        RegisterRequest req = new RegisterRequest("x@example.com", tooLong, "X");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    public void testRegisterRequest_Password8Chars_Valid() {
        RegisterRequest req = new RegisterRequest("x@example.com", "exactly8", "X");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    public void testRegisterRequest_Password100Chars_Valid() {
        String exactly100 = "a".repeat(100);
        RegisterRequest req = new RegisterRequest("x@example.com", exactly100, "X");
        assertTrue(validator.validate(req).isEmpty());
    }

    // =========================================================================
    // RegisterRequest — name length
    // =========================================================================

    @Test
    public void testRegisterRequest_NameTooLong_Violation() {
        String tooLongName = "a".repeat(101);
        RegisterRequest req = new RegisterRequest("x@example.com", "password123", tooLongName);
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    public void testRegisterRequest_Name100Chars_Valid() {
        String exactly100 = "a".repeat(100);
        RegisterRequest req = new RegisterRequest("x@example.com", "password123", exactly100);
        assertTrue(validator.validate(req).isEmpty());
    }

    // =========================================================================
    // AuthResponse — builder
    // =========================================================================

    @Test
    public void testAuthResponse_Builder_SetsAllFields() {
        AuthResponse resp = AuthResponse.builder()
            .accessToken("token123")
            .tokenType("Bearer")
            .email("user@example.com")
            .name("User")
            .role("USER")
            .build();

        assertEquals("token123",          resp.getAccessToken());
        assertEquals("Bearer",            resp.getTokenType());
        assertEquals("user@example.com",  resp.getEmail());
        assertEquals("User",              resp.getName());
        assertEquals("USER",              resp.getRole());
    }

    @Test
    public void testAuthResponse_DefaultTokenType_IsBearer() {
        AuthResponse resp = AuthResponse.builder().build();
        assertEquals("Bearer", resp.getTokenType());
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private Set<String> messages(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toSet());
    }
}
