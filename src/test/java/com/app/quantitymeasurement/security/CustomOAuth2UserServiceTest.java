package com.app.quantitymeasurement.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.enums.AuthProvider;
import com.app.quantitymeasurement.enums.Role;
import com.app.quantitymeasurement.repository.UserRepository;
import com.app.quantitymeasurement.security.oauth2.CustomOAuth2UserService;


@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private TestableOAuth2UserService service;

    @BeforeEach
    void setUp() {
        service = new TestableOAuth2UserService(userRepository);
    }

    // =========================================================================
    // Google — first-time registration
    // =========================================================================

    @Test
    void testGoogle_NewUser_CreatesAndReturnsUserPrincipal() {
        Map<String, Object> attrs = googleAttrs("alice@gmail.com", "Alice",
                "https://pic.google.com/alice.jpg", "google-sub-111");

        when(userRepository.findByEmail("alice@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
            .thenReturn(savedUser("alice@gmail.com", AuthProvider.GOOGLE, "google-sub-111"));

        OAuth2User result = service.process(userRequest("google"), oAuth2User(attrs, "sub"));

        assertInstanceOf(UserPrincipal.class, result);
        assertEquals("alice@gmail.com", ((UserPrincipal) result).getEmail());
        verify(userRepository).save(argThat(u ->
            u.getProvider() == AuthProvider.GOOGLE &&
            "google-sub-111".equals(u.getProviderId()) &&
            u.getPassword() == null
        ));
    }

    // =========================================================================
    // Google — returning user (profile refresh)
    // =========================================================================

    @Test
    void testGoogle_ReturningUser_UpdatesNameAndImage() {
        Map<String, Object> attrs = googleAttrs("bob@gmail.com", "Bob Updated",
                "https://pic.google.com/bob-new.jpg", "google-sub-222");

        User existing = User.builder()
            .id(2L).email("bob@gmail.com").name("Bob Old")
            .imageUrl("https://pic.google.com/bob-old.jpg")
            .provider(AuthProvider.GOOGLE).providerId("google-sub-222")
            .role(Role.USER).build();

        when(userRepository.findByEmail("bob@gmail.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.process(userRequest("google"), oAuth2User(attrs, "sub"));

        verify(userRepository).save(argThat(u ->
            "Bob Updated".equals(u.getName()) &&
            "https://pic.google.com/bob-new.jpg".equals(u.getImageUrl())
        ));
    }

    // =========================================================================
    // Google — provider conflict: email already registered locally
    // =========================================================================

    @Test
    void testGoogle_EmailAlreadyLocal_ThrowsConflict() {
        Map<String, Object> attrs = googleAttrs("carol@example.com", "Carol",
                null, "google-sub-333");

        User local = User.builder()
            .email("carol@example.com").provider(AuthProvider.LOCAL)
            .password("$2a$10$hash").role(Role.USER).build();

        when(userRepository.findByEmail("carol@example.com")).thenReturn(Optional.of(local));

        OAuth2UserRequest request = userRequest("google");
        OAuth2User user = oAuth2User(attrs, "sub");

        OAuth2AuthenticationException ex =
                assertThrows(OAuth2AuthenticationException.class,
                        () -> service.process(request, user));
        assertTrue(ex.getMessage().contains("email and password"),
            "Should tell the user to log in with their local password");
    }

    // =========================================================================
    // GitHub — first-time registration
    // =========================================================================

    @Test
    void testGitHub_NewUser_CreatesAndReturnsUserPrincipal() {
        Map<String, Object> attrs = githubAttrs(99999, "dave-gh", "Dave",
                "dave@github.com", "https://avatars.githubusercontent.com/u/99999?v=4");

        when(userRepository.findByEmail("dave@github.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
            .thenReturn(savedUser("dave@github.com", AuthProvider.GITHUB, "99999"));

        OAuth2User result = service.process(userRequest("github"), oAuth2User(attrs, "id"));

        assertInstanceOf(UserPrincipal.class, result);
        assertEquals("dave@github.com", ((UserPrincipal) result).getEmail());
        verify(userRepository).save(argThat(u ->
            u.getProvider() == AuthProvider.GITHUB &&
            "99999".equals(u.getProviderId()) &&   // numeric id stored as String
            u.getPassword() == null
        ));
    }

    // =========================================================================
    // GitHub — null display name falls back to login username
    // =========================================================================

    @Test
    void testGitHub_NullDisplayName_FallsBackToLogin() {
        Map<String, Object> attrs = githubAttrs(77777, "eve-login", null,
                "eve@github.com", "https://avatars.githubusercontent.com/u/77777?v=4");

        when(userRepository.findByEmail("eve@github.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.process(userRequest("github"), oAuth2User(attrs, "id"));

        /*
         * When GitHub's 'name' attribute is null (user has not set a display name),
         * the service falls back to the 'login' (username). The persisted name must
         * be the login string, not null.
         */
        verify(userRepository).save(argThat(u -> "eve-login".equals(u.getName())));
    }

    // =========================================================================
    // GitHub — null email (private account) is rejected with a clear message
    // =========================================================================

    @Test
    void testGitHub_NullEmail_ThrowsDescriptiveError() {
        /*
         * GitHub only returns the email when it is not marked as private.
         * The service must reject this case with a clear error rather than
         * propagating a NullPointerException.
         */
        Map<String, Object> attrs = githubAttrs(55555, "frank-private", "Frank",
                null, "https://avatars.githubusercontent.com/u/55555?v=4");

        OAuth2UserRequest request = userRequest("github");
        OAuth2User user = oAuth2User(attrs, "id");

        OAuth2AuthenticationException ex =
                assertThrows(OAuth2AuthenticationException.class,
                        () -> service.process(request, user));
        String msg = ex.getMessage().toLowerCase();
        assertTrue(msg.contains("email") || msg.contains("github"),
            "Error must reference email or github so the user knows how to resolve it");
    }

    // =========================================================================
    // GitHub — provider conflict: email already registered with Google
    // =========================================================================

    @Test
    void testGitHub_EmailAlreadyGoogle_ThrowsConflictMentioningGoogle() {
        Map<String, Object> attrs = githubAttrs(33333, "grace-gh", "Grace",
                "grace@example.com", null);

        User googleExisting = User.builder()
            .email("grace@example.com").provider(AuthProvider.GOOGLE)
            .providerId("google-sub-grace").role(Role.USER).build();

        when(userRepository.findByEmail("grace@example.com"))
            .thenReturn(Optional.of(googleExisting));

        OAuth2UserRequest request = userRequest("github");
        OAuth2User user = oAuth2User(attrs, "id");

        OAuth2AuthenticationException ex =
                assertThrows(OAuth2AuthenticationException.class,
                        () -> service.process(request, user));
        assertTrue(ex.getMessage().toLowerCase().contains("google"),
            "Error must tell the user they registered with Google, not GitHub");
    }

    // =========================================================================
    // GitHub — provider conflict: email already registered locally
    // =========================================================================

    @Test
    void testGitHub_EmailAlreadyLocal_ThrowsConflict() {
        Map<String, Object> attrs = githubAttrs(22222, "henry-gh", "Henry",
                "henry@example.com", null);

        User localExisting = User.builder()
            .email("henry@example.com").provider(AuthProvider.LOCAL)
            .password("$2a$10$hash").role(Role.USER).build();

        when(userRepository.findByEmail("henry@example.com"))
            .thenReturn(Optional.of(localExisting));

        OAuth2UserRequest request = userRequest("github");
        OAuth2User user = oAuth2User(attrs, "id");

        OAuth2AuthenticationException ex =
                assertThrows(OAuth2AuthenticationException.class,
                        () -> service.process(request, user));
        assertTrue(ex.getMessage().contains("email and password"));
    }

    // =========================================================================
    // GitHub — returning user refreshes name and avatar
    // =========================================================================

    @Test
    void testGitHub_ReturningUser_UpdatesNameAndAvatar() {
        Map<String, Object> attrs = githubAttrs(11111, "hank-gh", "Hank Updated",
                "hank@github.com",
                "https://avatars.githubusercontent.com/u/11111-new?v=4");

        User existing = User.builder()
            .id(5L).email("hank@github.com").name("Hank Old")
            .imageUrl("https://avatars.githubusercontent.com/u/11111?v=4")
            .provider(AuthProvider.GITHUB).providerId("11111")
            .role(Role.USER).build();

        when(userRepository.findByEmail("hank@github.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.process(userRequest("github"), oAuth2User(attrs, "id"));

        verify(userRepository).save(argThat(u ->
            "Hank Updated".equals(u.getName()) &&
            "https://avatars.githubusercontent.com/u/11111-new?v=4".equals(u.getImageUrl())
        ));
    }

    // =========================================================================
    // Unsupported provider
    // =========================================================================

    @Test
    void testUnsupportedProvider_ThrowsDescriptiveError() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("email", "test@example.com");
        attrs.put("id",    "any-id");

        OAuth2UserRequest request = userRequest("facebook");
        OAuth2User user = oAuth2User(attrs, "id");

        OAuth2AuthenticationException ex =
                assertThrows(OAuth2AuthenticationException.class,
                        () -> service.process(request, user));
        
        String msg = ex.getMessage().toLowerCase();
        assertTrue(msg.contains("unsupported") || msg.contains("facebook"),
            "Error must identify the unsupported provider");
    }

    // =========================================================================
    // Test infrastructure
    // =========================================================================

    /**
     * Subclass of {@link CustomOAuth2UserService} that exposes the private
     * {@code processOAuth2User()} method as {@link #process(OAuth2UserRequest, OAuth2User)}
     * so tests can call it directly without going through the HTTP-dependent
     * {@code DefaultOAuth2UserService.loadUser()}.
     */
    static class TestableOAuth2UserService extends CustomOAuth2UserService {

        TestableOAuth2UserService(UserRepository repo) {
        	super(repo);
            try {
                java.lang.reflect.Field f =
                    CustomOAuth2UserService.class.getDeclaredField("userRepository");
                f.setAccessible(true);
                f.set(this, repo);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Calls the real {@code processOAuth2User()} via reflection.
         */
        OAuth2User process(OAuth2UserRequest req, OAuth2User rawUser) {
            try {
                java.lang.reflect.Method m = CustomOAuth2UserService.class
                    .getDeclaredMethod("processOAuth2User",
                                       OAuth2UserRequest.class, OAuth2User.class);
                m.setAccessible(true);
                return (OAuth2User) m.invoke(this, req, rawUser);
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof OAuth2AuthenticationException oae) throw oae;
                if (cause instanceof RuntimeException re) throw re;
                throw new RuntimeException(cause);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private ClientRegistration clientRegistration(String registrationId) {
        return ClientRegistration.withRegistrationId(registrationId)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .clientId("test-client-id")
            .clientSecret("test-client-secret")
            .redirectUri("http://localhost:8080/login/oauth2/code/" + registrationId)
            .authorizationUri("https://example.com/oauth2/authorize")
            .tokenUri("https://example.com/oauth2/token")
            .userInfoUri("https://example.com/userinfo")
            .userNameAttributeName("sub")
            .build();
    }

    private OAuth2UserRequest userRequest(String registrationId) {
        OAuth2AccessToken token = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER, "test-token",
            Instant.now(), Instant.now().plusSeconds(3600)
        );
        return new OAuth2UserRequest(clientRegistration(registrationId), token);
    }

    private OAuth2User oAuth2User(Map<String, Object> attributes, String nameKey) {
        return new DefaultOAuth2User(Set.of(() -> "ROLE_USER"), attributes, nameKey);
    }

    private User savedUser(String email, AuthProvider provider, String providerId) {
        return User.builder()
            .id(1L).email(email).name("Test User")
            .provider(provider).providerId(providerId)
            .role(Role.USER).build();
    }

    private Map<String, Object> googleAttrs(String email, String name,
                                             String picture, String sub) {
        Map<String, Object> m = new HashMap<>();
        m.put("email",   email);
        m.put("name",    name);
        m.put("picture", picture);
        m.put("sub",     sub);
        return m;
    }

    private Map<String, Object> githubAttrs(int id, String login, String name,
                                             String email, String avatarUrl) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",         id);
        m.put("login",      login);
        m.put("name",       name);
        m.put("email",      email);
        m.put("avatar_url", avatarUrl);
        return m;
    }
}
