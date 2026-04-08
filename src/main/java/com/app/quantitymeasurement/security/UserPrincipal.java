package com.app.quantitymeasurement.security;

import com.app.quantitymeasurement.entity.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UserPrincipal implements UserDetails, OAuth2User {

    /*
     * -------------------------------------------------------------------------
     * State
     * -------------------------------------------------------------------------
     */

    /** The underlying persistent user entity. */
    private final User user;

    /**
     * Raw OAuth2 attribute map returned by the Google UserInfo endpoint.
     * {@code null} for principals created via the local authentication path.
     */
    private transient Map<String, Object> attributes;

    /*
     * -------------------------------------------------------------------------
     * Constructors
     * -------------------------------------------------------------------------
     */

    /**
     * Creates a {@code UserPrincipal} for the <em>local authentication</em> path.
     *
     * @param user the loaded user entity; must not be {@code null}
     */
    public UserPrincipal(User user) {
        this.user = user;
    }

    /**
     * Creates a {@code UserPrincipal} for the <em>OAuth2 authentication</em> path.
     *
     * @param user       the loaded or newly created user entity; must not be {@code null}
     * @param attributes the raw OAuth2 attribute map from Google's UserInfo endpoint
     */
    public UserPrincipal(User user, Map<String, Object> attributes) {
        this.user       = user;
        this.attributes = attributes;
    }

    /*
     * -------------------------------------------------------------------------
     * Static factory methods (for readable call-sites)
     * -------------------------------------------------------------------------
     */

    /**
     * Factory method for the local authentication path.
     *
     * @param user the user entity to wrap
     * @return a fully initialised {@code UserPrincipal}
     */
    public static UserPrincipal create(User user) {
        return new UserPrincipal(user);
    }

    /**
     * Factory method for the OAuth2 authentication path.
     *
     * @param user       the user entity to wrap
     * @param attributes Google's raw attribute map
     * @return a fully initialised {@code UserPrincipal} that also satisfies
     *         the {@link OAuth2User} contract
     */
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        return new UserPrincipal(user, attributes);
    }

    /*
     * -------------------------------------------------------------------------
     * Accessors for the underlying entity
     * -------------------------------------------------------------------------
     */

    /**
     * Returns the database primary key of the user.
     * Used internally by services that need to associate data with a user ID.
     *
     * @return the user's {@code id}
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * Returns the email address, which serves as the primary identity of this
     * principal (the JWT {@code sub} claim and the Spring Security principal name).
     *
     * @return the user's email
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * Exposes the full {@link User} entity for cases where the controller or
     * service needs to access user-specific fields (e.g., name, imageUrl).
     *
     * @return the underlying user entity
     */
    public User getUser() {
        return user;
    }

    /*
     * -------------------------------------------------------------------------
     * UserDetails implementation
     * -------------------------------------------------------------------------
     */

    /**
     * Returns the single {@link GrantedAuthority} derived from the user's role.
     * Spring Security's access expressions (e.g., {@code hasRole("ADMIN")}) rely
     * on the {@code ROLE_} prefix being present.
     *
     * @return an immutable singleton list containing the user's role authority
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    /**
     * Returns the BCrypt-hashed password for local accounts, or {@code null}
     * for OAuth2 accounts (Spring Security does not use it for the OAuth2 path).
     *
     * @return the stored password hash, or {@code null}
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Returns the email address as the username (principal name).
     * Spring Security uses this value as the subject when creating its
     * {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}.
     *
     * @return the user's email address
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /** @return {@code true} — account expiry is not implemented in UC-18 */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** @return {@code true} — account locking is not implemented in UC-18 */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** @return {@code true} — credential expiry is not implemented in UC-18 */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** @return {@code true} — soft-delete / disabled flag is not implemented in UC-18 */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /*
     * -------------------------------------------------------------------------
     * OAuth2User implementation
     * -------------------------------------------------------------------------
     */

    /**
     * Returns the raw attribute map provided by Google's UserInfo endpoint.
     * This is the source of {@code email}, {@code name}, {@code picture}, etc.
     *
     * @return the attribute map, or {@code null} for locally authenticated principals
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Sets the OAuth2 attribute map. Called by {@code CustomOAuth2UserService}
     * after the user entity has been resolved.
     *
     * @param attributes the raw Google attribute map
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * Returns the OAuth2 principal name.
     * Spring Security uses the {@code sub} claim (the Google subject ID) as the
     * default name attribute for Google login; this implementation delegates to
     * {@link #getUsername()} (email) for consistency with the local login path.
     *
     * @return the user's email address
     */
    @Override
    public String getName() {
        return user.getEmail();
    }
}
