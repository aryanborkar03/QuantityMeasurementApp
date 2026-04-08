package com.app.quantitymeasurement.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.app.quantitymeasurement.dto.request.AuthRequest;
import com.app.quantitymeasurement.dto.request.ForgotPasswordRequest;
import com.app.quantitymeasurement.dto.request.RegisterRequest;
import com.app.quantitymeasurement.dto.response.AuthResponse;
import com.app.quantitymeasurement.dto.response.MessageResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;


class AuthDTOTest {

    private static Validator validator;

    /** A password that satisfies every constraint in {@link RegisterRequest}. */
    private static final String VALID_PASSWORD = "Strong@123";

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // =========================================================================
    // AuthRequest — valid
    // =========================================================================

    @Test
    void testAuthRequest_ValidPayload_NoViolations() {
        AuthRequest req = new AuthRequest("user@example.com", "anyPassword");
        assertTrue(validator.validate(req).isEmpty());
    }

    // =========================================================================
    // AuthRequest — email
    // =========================================================================

    @Test
    void testAuthRequest_BlankEmail_Violation() {
        AuthRequest req = new AuthRequest("", "anyPassword");
        Set<String> messages = messages(validator.validate(req));
        assertTrue(messages.stream().anyMatch(m -> m.contains("blank")));
    }

    @Test
    void testAuthRequest_InvalidEmailFormat_Violation() {
        AuthRequest req = new AuthRequest("not-an-email", "anyPassword");
        Set<String> messages = messages(validator.validate(req));
        assertTrue(messages.stream().anyMatch(m -> m.toLowerCase().contains("valid")));
    }

    @Test
    void testAuthRequest_NullEmail_Violation() {
        AuthRequest req = new AuthRequest(null, "anyPassword");
        assertFalse(validator.validate(req).isEmpty());
    }

    // =========================================================================
    // AuthRequest — password
    // =========================================================================

    @Test
    void testAuthRequest_BlankPassword_Violation() {
        AuthRequest req = new AuthRequest("user@example.com", "");
        Set<String> messages = messages(validator.validate(req));
        assertTrue(messages.stream().anyMatch(m -> m.contains("blank")));
    }

    @Test
    void testAuthRequest_NullPassword_Violation() {
        AuthRequest req = new AuthRequest("user@example.com", null);
        assertFalse(validator.validate(req).isEmpty());
    }

    // =========================================================================
    // RegisterRequest — valid
    // =========================================================================

    @Test
    void testRegisterRequest_ValidPayload_NoViolations() {
        // UC19: password now requires uppercase, special char, and digit
        RegisterRequest req = new RegisterRequest("new@example.com", VALID_PASSWORD, "Jane Doe");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void testRegisterRequest_NullName_NoViolation() {
        // name is optional — null must not produce a violation
        RegisterRequest req = new RegisterRequest("new@example.com", VALID_PASSWORD, null);
        assertTrue(validator.validate(req).isEmpty());
    }
    
	 // =========================================================================
	 // RegisterRequest — invalid password cases (Parameterized)
	 // =========================================================================
	
	 @ParameterizedTest
	 @ValueSource(strings = {
	         "short",        // too short (fails @Size and @Pattern)
	         "strong@123",   // no uppercase
	         "StrongPass1",  // no special char
	         "Strong@pass"   // no digit
	 })
	 void testRegisterRequest_InvalidPasswords_Violation(String password) {
	
	     RegisterRequest req =
	             new RegisterRequest("x@example.com", password, "X");
	
	     assertFalse(validator.validate(req).isEmpty());
	 }

    // =========================================================================
    // RegisterRequest — email
    // =========================================================================

    @Test
    void testRegisterRequest_BlankEmail_Violation() {
        RegisterRequest req = new RegisterRequest("", VALID_PASSWORD, "Jane");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testRegisterRequest_InvalidEmail_Violation() {
        RegisterRequest req = new RegisterRequest("bad-email", VALID_PASSWORD, "Jane");
        assertFalse(validator.validate(req).isEmpty());
    }

    // =========================================================================
    // RegisterRequest — password: length boundary
    // =========================================================================

    @Test
    void testRegisterRequest_PasswordTooLong_Violation() {
        // 101 chars — fails @Size(max=100)
        String tooLong = "A@1" + "a".repeat(98);  // 101 chars, otherwise strong
        RegisterRequest req = new RegisterRequest("x@example.com", tooLong, "X");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testRegisterRequest_PasswordExactly8Chars_Valid() {
        // "Strong@1" — exactly 8 chars, meets all strength requirements
        RegisterRequest req = new RegisterRequest("x@example.com", "Strong@1", "X");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void testRegisterRequest_PasswordExactly100Chars_Valid() {
        // 100 chars: starts with required chars, filled to boundary
        String exactly100 = "Aa1@" + "a".repeat(96);  // 4 + 96 = 100 chars
        RegisterRequest req = new RegisterRequest("x@example.com", exactly100, "X");
        assertTrue(validator.validate(req).isEmpty());
    }

    // =========================================================================
    // RegisterRequest — password: strength (@Pattern, UC19)
    // =========================================================================


    @Test
    void testRegisterRequest_PasswordAllRequirementsMet_Valid() {
        // Passes every constraint: uppercase S, special @, digit 1, 9 chars
        RegisterRequest req = new RegisterRequest("x@example.com", "Strong@1x", "X");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void testRegisterRequest_PasswordStrengthMessage_ContainsKeyTerms() {
        // The @Pattern message must guide the user toward what is required
        RegisterRequest req = new RegisterRequest("x@example.com", "allowercase1@", "X");
        Set<String> messages = messages(validator.validate(req));
        assertTrue(
            messages.stream().anyMatch(m ->
                m.toLowerCase().contains("uppercase") ||
                m.toLowerCase().contains("special")  ||
                m.toLowerCase().contains("number")
            ),
            "Violation message must mention at least one unmet strength requirement"
        );
    }

    // =========================================================================
    // RegisterRequest — name length
    // =========================================================================

    @Test
    void testRegisterRequest_NameTooLong_Violation() {
        String tooLongName = "a".repeat(101);
        RegisterRequest req = new RegisterRequest("x@example.com", VALID_PASSWORD, tooLongName);
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testRegisterRequest_NameExactly100Chars_Valid() {
        String exactly100 = "a".repeat(100);
        RegisterRequest req = new RegisterRequest("x@example.com", VALID_PASSWORD, exactly100);
        assertTrue(validator.validate(req).isEmpty());
    }

    // =========================================================================
    // ForgotPasswordRequest — valid (UC19)
    // =========================================================================

    @Test
    void testForgotPasswordRequest_ValidPassword_NoViolations() {
        ForgotPasswordRequest req = new ForgotPasswordRequest(VALID_PASSWORD);
        assertTrue(validator.validate(req).isEmpty());
    }

	 // =========================================================================
	 // ForgotPasswordRequest — invalid password cases (Parameterized)
	 // =========================================================================
	
	 @ParameterizedTest
	 @MethodSource("invalidPasswords")
	 void testForgotPasswordRequest_InvalidPasswords_Violation(String password) {
	
	     ForgotPasswordRequest req = new ForgotPasswordRequest(password);
	
	     assertFalse(validator.validate(req).isEmpty());
	 }
	
	 private static Stream<String> invalidPasswords() {
	     return Stream.of(
	             null,        // null
	             "",          // blank
	             "weak@123",  // no uppercase
	             "WeakPass1", // no special char
	             "Weak@pass", // no digit
	             "Str@ng1"    // too short (7 chars)
	     );
	 }

    @Test
    void testForgotPasswordRequest_StrongPassword_NoViolations() {
        ForgotPasswordRequest req = new ForgotPasswordRequest("NewStr@ng1");
        assertTrue(validator.validate(req).isEmpty());
    }

    // =========================================================================
    // AuthResponse — builder
    // =========================================================================

    @Test
    void testAuthResponse_Builder_SetsAllFields() {
        AuthResponse resp = AuthResponse.builder()
            .accessToken("token123")
            .tokenType("Bearer")
            .email("user@example.com")
            .name("User")
            .role("USER")
            .build();

        assertEquals("token123",         resp.getAccessToken());
        assertEquals("Bearer",           resp.getTokenType());
        assertEquals("user@example.com", resp.getEmail());
        assertEquals("User",             resp.getName());
        assertEquals("USER",             resp.getRole());
    }

    @Test
    void testAuthResponse_DefaultTokenType_IsBearer() {
        AuthResponse resp = AuthResponse.builder().build();
        assertEquals("Bearer", resp.getTokenType());
    }

    // =========================================================================
    // MessageResponse (UC19)
    // =========================================================================

    @Test
    void testMessageResponse_AllArgsConstructor_SetsMessage() {
        MessageResponse resp = new MessageResponse("Password reset successfully!");
        assertEquals("Password reset successfully!", resp.getMessage());
    }

    @Test
    void testMessageResponse_NoArgsConstructor_MessageIsNull() {
        MessageResponse resp = new MessageResponse();
        assertNull(resp.getMessage());
    }

    @Test
    void testMessageResponse_Setter_UpdatesMessage() {
        MessageResponse resp = new MessageResponse();
        resp.setMessage("Password has been changed successfully!");
        assertEquals("Password has been changed successfully!", resp.getMessage());
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
