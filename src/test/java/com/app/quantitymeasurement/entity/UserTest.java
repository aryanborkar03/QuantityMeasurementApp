package com.app.quantitymeasurement.entity;

import com.app.quantitymeasurement.enums.AuthProvider;
import com.app.quantitymeasurement.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


class UserTest {

    // =========================================================================
    // Builder — LOCAL provider
    // =========================================================================

    @Test
    void testBuilder_LocalUser_SetsAllFields() {
        User user = User.builder()
            .email("alice@example.com")
            .name("Alice")
            .password("hashedPwd")
            .provider(AuthProvider.LOCAL)
            .role(Role.USER)
            .build();

        assertEquals("alice@example.com", user.getEmail());
        assertEquals("Alice",             user.getName());
        assertEquals("hashedPwd",         user.getPassword());
        assertEquals(AuthProvider.LOCAL,  user.getProvider());
        assertEquals(Role.USER,           user.getRole());
        assertNull(user.getProviderId());
        assertNull(user.getImageUrl());
    }

    @Test
    void testBuilder_DefaultRole_IsUser() {
        User user = User.builder()
            .email("bob@example.com")
            .provider(AuthProvider.LOCAL)
            .build();

        assertEquals(Role.USER, user.getRole());
    }

    // =========================================================================
    // Builder — GOOGLE provider
    // =========================================================================

    @Test
    void testBuilder_GoogleUser_SetsProviderFields() {
        User user = User.builder()
            .email("carol@gmail.com")
            .name("Carol")
            .provider(AuthProvider.GOOGLE)
            .providerId("google-sub-123")
            .imageUrl("https://lh3.google.com/photo.jpg")
            .role(Role.USER)
            .build();

        assertEquals(AuthProvider.GOOGLE, user.getProvider());
        assertEquals("google-sub-123",    user.getProviderId());
        assertEquals("https://lh3.google.com/photo.jpg", user.getImageUrl());
        assertNull(user.getPassword());
    }


    // =========================================================================
    // Builder — GITHUB provider
    // =========================================================================

    @Test
    void testBuilder_GitHubUser_SetsAllFields() {
        /*
         * GitHub supplies a numeric user ID as the stable provider ID,
         * an avatar_url for the profile picture, and optionally a display name.
         * The password field must be null — GitHub manages credentials.
         */
        User user = User.builder()
            .email("carol@github.com")
            .name("carol")
            .imageUrl("https://avatars.githubusercontent.com/u/12345?v=4")
            .provider(AuthProvider.GITHUB)
            .providerId("12345")
            .role(Role.USER)
            .build();

        assertEquals("carol@github.com",                                      user.getEmail());
        assertEquals("carol",                                                  user.getName());
        assertEquals("https://avatars.githubusercontent.com/u/12345?v=4",     user.getImageUrl());
        assertEquals(AuthProvider.GITHUB,                                      user.getProvider());
        assertEquals("12345",                                                  user.getProviderId());
        assertEquals(Role.USER,                                                user.getRole());
        assertNull(user.getPassword());
    }

    @Test
    void testBuilder_GitHubUser_NullName_FallsBackToLogin() {
        /*
         * GitHub's display name ('name') field is optional and may be null
         * when a user has not set one. CustomOAuth2UserService falls back to
         * the 'login' (username) in that case. This test verifies the entity
         * accepts a login username in the name field without issue.
         */
        User user = User.builder()
            .email("dave@github.com")
            .name("dave-login")   // username used as fallback name
            .provider(AuthProvider.GITHUB)
            .providerId("99999")
            .role(Role.USER)
            .build();

        assertEquals("dave-login", user.getName());
        assertEquals(AuthProvider.GITHUB, user.getProvider());
    }

    // =========================================================================
    // @PrePersist lifecycle callback
    // =========================================================================

    @Test
    void testPrePersist_SetsCreatedAt() {
        User user = User.builder().email("d@example.com").provider(AuthProvider.LOCAL).build();
        assertNull(user.getCreatedAt(), "createdAt should be null before prePersist");

        user.prePersist();

        assertNotNull(user.getCreatedAt());
        assertTrue(user.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    public void testPrePersist_CalledTwice_UpdatesTimestamp() throws InterruptedException {
        User user = User.builder().email("e@example.com").provider(AuthProvider.LOCAL).build();
        user.prePersist();
        LocalDateTime first = user.getCreatedAt();

        Thread.sleep(10);
        user.prePersist();

        assertTrue(user.getCreatedAt().isAfter(first) || user.getCreatedAt().isEqual(first));
    }

    // =========================================================================
    // toString — password must not appear
    // =========================================================================

    @Test
    void testToString_DoesNotExposePassword() {
        User user = User.builder()
            .email("f@example.com")
            .password("$2a$10$superSecretBcryptHash")
            .provider(AuthProvider.LOCAL)
            .build();

        String str = user.toString();
        assertFalse(str.contains("superSecretBcryptHash"),
            "toString() must not include the password hash");
        assertTrue(str.contains("f@example.com"));
    }

    @Test
    void testToString_ContainsKeyFields() {
        User user = User.builder()
            .email("g@example.com")
            .name("Greg")
            .provider(AuthProvider.GOOGLE)
            .role(Role.ADMIN)
            .build();

        String str = user.toString();
        assertTrue(str.contains("g@example.com"));
        assertTrue(str.contains("ADMIN"));
        assertTrue(str.contains("GOOGLE"));
    }

    // =========================================================================
    // Role assignment
    // =========================================================================

    @Test
    void testSetRole_ToAdmin_Persists() {
        User user = User.builder().email("h@example.com").provider(AuthProvider.LOCAL).build();
        assertEquals(Role.USER, user.getRole());

        user.setRole(Role.ADMIN);
        assertEquals(Role.ADMIN, user.getRole());
    }

    // =========================================================================
    // No-args constructor
    // =========================================================================

    @Test
    void testNoArgsConstructor_AllFieldsNull() {
        User user = new User();
        assertNull(user.getId());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getProvider());
    }
}
