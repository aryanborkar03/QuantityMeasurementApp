package com.app.quantitymeasurement.repository;

import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.enums.AuthProvider;
import com.app.quantitymeasurement.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // =========================================================================
    // findByEmail
    // =========================================================================

    @Test
    void testFindByEmail_ExistingEmail_ReturnsUser() {
        User saved = userRepository.save(localUser("alice@example.com", "Alice"));

        Optional<User> found = userRepository.findByEmail("alice@example.com");

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("Alice", found.get().getName());
    }

    @Test
    void testFindByEmail_NonExistentEmail_ReturnsEmpty() {
        Optional<User> found = userRepository.findByEmail("nobody@example.com");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByEmail_IsCaseSensitive() {
        userRepository.save(localUser("alice@example.com", "Alice"));
        // H2 and MySQL treat email as case-sensitive by default
        Optional<User> found = userRepository.findByEmail("ALICE@EXAMPLE.COM");
        assertFalse(found.isPresent());
    }

    // =========================================================================
    // existsByEmail
    // =========================================================================

    @Test
    void testExistsByEmail_ExistingEmail_ReturnsTrue() {
        userRepository.save(localUser("bob@example.com", "Bob"));
        assertTrue(userRepository.existsByEmail("bob@example.com"));
    }

    @Test
    void testExistsByEmail_NonExistentEmail_ReturnsFalse() {
        assertFalse(userRepository.existsByEmail("nobody@example.com"));
    }

    // =========================================================================
    // findByProviderAndProviderId
    // =========================================================================

    @Test
    void testFindByProviderAndProviderId_ExistingPair_ReturnsUser() {
        User google = User.builder()
            .email("carol@gmail.com")
            .name("Carol")
            .provider(AuthProvider.GOOGLE)
            .providerId("google-sub-777")
            .role(Role.USER)
            .build();
        userRepository.save(google);

        Optional<User> found = userRepository
            .findByProviderAndProviderId(AuthProvider.GOOGLE, "google-sub-777");

        assertTrue(found.isPresent());
        assertEquals("carol@gmail.com", found.get().getEmail());
    }

    @Test
    void testFindByProviderAndProviderId_WrongProvider_ReturnsEmpty() {
        User google = User.builder()
            .email("dan@gmail.com")
            .provider(AuthProvider.GOOGLE)
            .providerId("google-sub-999")
            .role(Role.USER)
            .build();
        userRepository.save(google);

        Optional<User> found = userRepository
            .findByProviderAndProviderId(AuthProvider.LOCAL, "google-sub-999");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByProviderAndProviderId_WrongId_ReturnsEmpty() {
        User google = User.builder()
            .email("eve@gmail.com")
            .provider(AuthProvider.GOOGLE)
            .providerId("correct-id")
            .role(Role.USER)
            .build();
        userRepository.save(google);

        Optional<User> found = userRepository
            .findByProviderAndProviderId(AuthProvider.GOOGLE, "wrong-id");

        assertFalse(found.isPresent());
    }

    // =========================================================================
    // Persistence — audit and defaults
    // =========================================================================

    @Test
    void testSave_LocalUser_CreatedAtIsSet() {
        User user = userRepository.save(localUser("f@example.com", "Frank"));
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void testSave_DefaultRole_IsUser() {
        User user = User.builder()
            .email("g@example.com")
            .provider(AuthProvider.LOCAL)
            .build();
        User saved = userRepository.save(user);
        assertEquals(Role.USER, saved.getRole());
    }

    @Test
    void testSave_AdminRole_IsPersisted() {
        User admin = User.builder()
            .email("h@example.com")
            .provider(AuthProvider.LOCAL)
            .role(Role.ADMIN)
            .build();
        User saved = userRepository.save(admin);
        assertEquals(Role.ADMIN, userRepository.findById(saved.getId()).get().getRole());
    }

    @Test
    void testEmailUniqueConstraint_DuplicateThrows() {
        userRepository.save(localUser("dup@example.com", "First"));
        User duplicate = localUser("dup@example.com", "Second");
        assertThrows(Exception.class, () -> userRepository.saveAndFlush(duplicate));
    }


    // =========================================================================
    // GitHub provider — findByProviderAndProviderId
    // =========================================================================

    @Test
    void testFindByProviderAndProviderId_GithubUser_Found() {
        User github = githubUser("carol@github.com", "carol", "gh-id-123");
        userRepository.save(github);

        java.util.Optional<User> found = userRepository
            .findByProviderAndProviderId(AuthProvider.GITHUB, "gh-id-123");

        assertTrue(found.isPresent());
        assertEquals("carol@github.com", found.get().getEmail());
        assertEquals(AuthProvider.GITHUB, found.get().getProvider());
    }

    @Test
    void testFindByProviderAndProviderId_GithubVsGoogle_NotMixed() {
        /*
         * A GitHub user and a Google user with different provider IDs should
         * never be confused even if they share the same numeric string ID.
         */
        userRepository.save(githubUser("github@example.com", "ghuser", "shared-id-99"));
        userRepository.save(googleUser("google@example.com", "gguser", "shared-id-99"));

        java.util.Optional<User> byGithub = userRepository
            .findByProviderAndProviderId(AuthProvider.GITHUB, "shared-id-99");
        java.util.Optional<User> byGoogle = userRepository
            .findByProviderAndProviderId(AuthProvider.GOOGLE, "shared-id-99");

        assertTrue(byGithub.isPresent());
        assertEquals("github@example.com", byGithub.get().getEmail());

        assertTrue(byGoogle.isPresent());
        assertEquals("google@example.com", byGoogle.get().getEmail());
    }

    @Test
    void testFindByProviderAndProviderId_GithubWrongId_ReturnsEmpty() {
        userRepository.save(githubUser("dave@github.com", "dave", "correct-gh-id"));

        java.util.Optional<User> found = userRepository
            .findByProviderAndProviderId(AuthProvider.GITHUB, "wrong-gh-id");

        assertFalse(found.isPresent());
    }

    @Test
    void testSave_GithubUser_PersistsCorrectly() {
        User github = githubUser("eve@github.com", "eve", "gh-eve-999");
        User saved = userRepository.save(github);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals(AuthProvider.GITHUB, saved.getProvider());
        assertEquals("gh-eve-999",        saved.getProviderId());
        assertNull(saved.getPassword());
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private User localUser(String email, String name) {
        return User.builder()
            .email(email)
            .name(name)
            .password("$2a$10$hash")
            .provider(AuthProvider.LOCAL)
            .role(Role.USER)
            .build();
    }

    private User githubUser(String email, String login, String githubId) {
        return User.builder()
            .email(email)
            .name(login)
            .provider(AuthProvider.GITHUB)
            .providerId(githubId)
            .role(Role.USER)
            .build();
    }

    private User googleUser(String email, String name, String googleSub) {
        return User.builder()
            .email(email)
            .name(name)
            .provider(AuthProvider.GOOGLE)
            .providerId(googleSub)
            .role(Role.USER)
            .build();
    }
}
