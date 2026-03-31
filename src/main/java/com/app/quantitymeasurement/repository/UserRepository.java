package com.app.quantitymeasurement.repository;

import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.enums.AuthProvider;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository
 *
 * Spring Data JPA repository for the {@link User} entity. Provides standard CRUD
 * operations inherited from {@link JpaRepository}, plus application-specific
 * finder methods used by the authentication and user-management flows.
 *
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Looks up a user by their email address.
     * @param email the email address to search for
     * @return an {@link Optional} containing the matching user, or empty if none found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given email address already exists.
     * @param email the email address to check
     * @return {@code true} if a user with this email exists, {@code false} otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Looks up a user by their OAuth2 provider and the provider-assigned subject ID.
     * @param provider   the authentication provider (e.g., {@link AuthProvider#GOOGLE},
     *                   {@link AuthProvider#GITHUB})
     * @param providerId the provider-assigned subject identifier
     * @return an {@link Optional} containing the matching user, or empty if none found
     */
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
}