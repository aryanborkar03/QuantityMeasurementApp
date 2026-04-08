package com.app.quantitymeasurement.security;

import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.enums.AuthProvider;
import com.app.quantitymeasurement.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class UserPrincipalTest {

    private User localUser;
    private User googleUser;
    private User githubUser;

    @BeforeEach
    void setUp() {
        localUser = User.builder()
            .id(1L)
            .email("alice@example.com")
            .name("Alice")
            .password("$2a$10$hashedPassword")
            .provider(AuthProvider.LOCAL)
            .role(Role.USER)
            .build();

        googleUser = User.builder()
            .id(2L)
            .email("bob@gmail.com")
            .name("Bob")
            .provider(AuthProvider.GOOGLE)
            .providerId("google-sub-456")
            .imageUrl("https://pic.google.com/bob.jpg")
            .role(Role.ADMIN)
            .build();

        githubUser = User.builder()
            .id(3L)
            .email("carol@github.com")
            .name("carol")
            .provider(AuthProvider.GITHUB)
            .providerId("12345")
            .imageUrl("https://avatars.githubusercontent.com/u/12345?v=4")
            .role(Role.USER)
            .build();
    }

    // =========================================================================
    // Factory methods
    // =========================================================================

    @Test
    void testCreate_LocalUser_ReturnsPrincipal() {
        UserPrincipal principal = UserPrincipal.create(localUser);
        assertNotNull(principal);
        assertEquals("alice@example.com", principal.getUsername());
    }

    @Test
    void testCreate_WithAttributes_StoresAttributes() {
        Map<String, Object> attrs = Map.of("email", "bob@gmail.com", "name", "Bob");
        UserPrincipal principal = UserPrincipal.create(googleUser, attrs);
        assertNotNull(principal.getAttributes());
        assertEquals("bob@gmail.com", principal.getAttributes().get("email"));
    }

    // =========================================================================
    // Identity
    // =========================================================================

    @Test
    void testGetUsername_ReturnsEmail() {
        UserPrincipal p = UserPrincipal.create(localUser);
        assertEquals("alice@example.com", p.getUsername());
    }

    @Test
    void testGetEmail_ReturnsEmail() {
        UserPrincipal p = UserPrincipal.create(localUser);
        assertEquals("alice@example.com", p.getEmail());
    }

    @Test
    void testGetId_ReturnsDatabaseId() {
        UserPrincipal p = UserPrincipal.create(localUser);
        assertEquals(1L, p.getId());
    }

    @Test
    void testGetUser_ReturnsUnderlyingEntity() {
        UserPrincipal p = UserPrincipal.create(localUser);
        assertSame(localUser, p.getUser());
    }

    // =========================================================================
    // Authorities
    // =========================================================================

    @Test
    void testGetAuthorities_UserRole_ReturnsRoleUser() {
        UserPrincipal p = UserPrincipal.create(localUser);
        Collection<? extends GrantedAuthority> authorities = p.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }

    @Test
    void testGetAuthorities_AdminRole_ReturnsRoleAdmin() {
        UserPrincipal p = UserPrincipal.create(googleUser);
        Collection<? extends GrantedAuthority> authorities = p.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
    }

    // =========================================================================
    // Password
    // =========================================================================

    @Test
    void testGetPassword_LocalUser_ReturnsBcryptHash() {
        UserPrincipal p = UserPrincipal.create(localUser);
        assertEquals("$2a$10$hashedPassword", p.getPassword());
    }

    @Test
    void testGetPassword_GoogleUser_ReturnsNull() {
        UserPrincipal p = UserPrincipal.create(googleUser);
        assertNull(p.getPassword());
    }

    // =========================================================================
    // UserDetails boolean flags
    // =========================================================================

    @Test
    void testIsAccountNonExpired_ReturnsTrue() {
        assertTrue(UserPrincipal.create(localUser).isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked_ReturnsTrue() {
        assertTrue(UserPrincipal.create(localUser).isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired_ReturnsTrue() {
        assertTrue(UserPrincipal.create(localUser).isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled_ReturnsTrue() {
        assertTrue(UserPrincipal.create(localUser).isEnabled());
    }

    // =========================================================================
    // OAuth2User — getName
    // =========================================================================

    @Test
    void testGetName_ReturnsEmail() {
        UserPrincipal p = UserPrincipal.create(googleUser, Map.of("email", "bob@gmail.com"));
        assertEquals("bob@gmail.com", p.getName());
    }

    // =========================================================================
    // GitHub principal
    // =========================================================================

    @Test
    void testCreate_GitHubUser_WithAttributes_StoresAttributes() {
        /*
         * GitHub's attribute map uses different keys than Google.
         * 'id' is the numeric user ID (stable providerId), 'login' is the
         * username, and 'avatar_url' is the profile picture URL.
         */
        Map<String, Object> attrs = Map.of(
            "id",         12345,
            "login",      "carol",
            "email",      "carol@github.com",
            "avatar_url", "https://avatars.githubusercontent.com/u/12345?v=4"
        );
        UserPrincipal principal = UserPrincipal.create(githubUser, attrs);

        assertNotNull(principal);
        assertEquals("carol@github.com", principal.getUsername());
        assertEquals("carol@github.com", principal.getEmail());
        assertEquals(AuthProvider.GITHUB, principal.getUser().getProvider());
        assertEquals("12345",            principal.getUser().getProviderId());
        assertNotNull(principal.getAttributes());
        assertEquals(12345, principal.getAttributes().get("id"));
    }

    @Test
    void testGetPassword_GitHubUser_ReturnsNull() {
        /*
         * GitHub OAuth2 accounts have no locally stored password — GitHub
         * manages credentials. The password field must be null.
         */
        UserPrincipal p = UserPrincipal.create(githubUser);
        assertNull(p.getPassword());
    }

    @Test
    void testGetAuthorities_GitHubUser_ReturnsRoleUser() {
        UserPrincipal p = UserPrincipal.create(githubUser);
        Collection<? extends GrantedAuthority> authorities = p.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }

    @Test
    void testGetName_GitHubUser_ReturnsEmail() {
        /*
         * OAuth2User.getName() returns the email address for consistency
         * across all providers — it does not return the GitHub login/username.
         */
        Map<String, Object> attrs = Map.of("email", "carol@github.com", "login", "carol");
        UserPrincipal p = UserPrincipal.create(githubUser, attrs);
        assertEquals("carol@github.com", p.getName());
    }


    @Test
    void testSetAttributes_UpdatesAttributes() {
        UserPrincipal p = UserPrincipal.create(googleUser);
        assertNull(p.getAttributes());

        Map<String, Object> attrs = Map.of("sub", "google-sub-456");
        p.setAttributes(attrs);
        assertEquals("google-sub-456", p.getAttributes().get("sub"));
    }
}
