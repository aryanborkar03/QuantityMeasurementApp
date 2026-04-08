package com.app.quantitymeasurement.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.enums.AuthProvider;
import com.app.quantitymeasurement.enums.Role;
import com.app.quantitymeasurement.security.jwt.JwtTokenProvider;


@SpringBootTest
@ActiveProfiles("test")
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Authentication authentication;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        User user = User.builder()
            .id(1L)
            .email("alice@example.com")
            .name("Alice")
            .password("$2a$10$hash")
            .provider(AuthProvider.LOCAL)
            .role(Role.USER)
            .build();

        userPrincipal = UserPrincipal.create(user);
        authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities());
    }

    // =========================================================================
    // generateToken(Authentication)
    // =========================================================================

    @Test
    void testGenerateToken_ReturnsNonBlankString() {
        String token = jwtTokenProvider.generateToken(authentication);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void testGenerateToken_HasThreeParts() {
        String token = jwtTokenProvider.generateToken(authentication);
        assertEquals(3, token.split("\\.").length,
            "JWT must have header.payload.signature format");
    }

    // =========================================================================
    // generateTokenFromEmail(String, String)
    // =========================================================================

    @Test
    void testGenerateTokenFromEmail_ReturnsValidToken() {
        String token = jwtTokenProvider.generateTokenFromEmail("bob@example.com", "ROLE_USER");
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testGenerateTokenFromEmail_SubjectIsEmail() {
        String token = jwtTokenProvider.generateTokenFromEmail("carol@example.com", "ROLE_ADMIN");
        assertEquals("carol@example.com", jwtTokenProvider.getEmailFromToken(token));
    }

    // =========================================================================
    // getEmailFromToken
    // =========================================================================

    @Test
    void testGetEmailFromToken_ReturnsCorrectEmail() {
        String token = jwtTokenProvider.generateToken(authentication);
        assertEquals("alice@example.com", jwtTokenProvider.getEmailFromToken(token));
    }

    // =========================================================================
    // getRolesFromToken
    // =========================================================================

    @Test
    void testGetRolesFromToken_ContainsRoleUser() {
        String token = jwtTokenProvider.generateToken(authentication);
        String roles = jwtTokenProvider.getRolesFromToken(token);
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_USER"));
    }

    @Test
    void testGetRolesFromToken_FromEmail_ContainsSuppliedRole() {
        String token = jwtTokenProvider.generateTokenFromEmail("d@example.com", "ROLE_ADMIN");
        assertEquals("ROLE_ADMIN", jwtTokenProvider.getRolesFromToken(token));
    }

    // =========================================================================
    // validateToken — valid cases
    // =========================================================================

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        String token = jwtTokenProvider.generateToken(authentication);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testValidateToken_FromEmailVariant_ReturnsTrue() {
        String token = jwtTokenProvider.generateTokenFromEmail("e@example.com", "ROLE_USER");
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    // =========================================================================
    // validateToken — invalid cases
    // =========================================================================

    @Test
    void testValidateToken_TamperedSignature_ReturnsFalse() {
        String token = jwtTokenProvider.generateToken(authentication);
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "tampered";
        assertFalse(jwtTokenProvider.validateToken(tampered));
    }

    @Test
    void testValidateToken_RandomString_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken("not.a.jwt"));
    }

    @Test
    void testValidateToken_EmptyString_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    void testValidateToken_NullThrowsOrReturnsFalse() {
        try {
            boolean result = jwtTokenProvider.validateToken(null);
            assertFalse(result);
        } catch (Exception e) {
            // acceptable — null is not a valid token
        }
    }

    @Test
    void testValidateToken_TwoPartToken_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken("header.payload"));
    }

    // =========================================================================
    // Two tokens for same user are both valid
    // =========================================================================

    @Test
    void testTwoTokensForSameUser_BothValid() {
        String t1 = jwtTokenProvider.generateToken(authentication);
        String t2 = jwtTokenProvider.generateToken(authentication);
        assertTrue(jwtTokenProvider.validateToken(t1));
        assertTrue(jwtTokenProvider.validateToken(t2));
    }
}
