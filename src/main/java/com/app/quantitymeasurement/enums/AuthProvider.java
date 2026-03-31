package com.app.quantitymeasurement.enums;

/**
 * AuthProvider
 *
 * Identifies the authentication provider that was used to create or last
 * authenticate a user account.
 */
public enum AuthProvider {

    /**
     * Local authentication — email + BCrypt-hashed password stored in the database.
     */
    LOCAL,

    /**
     * Google OAuth2 authentication — identity verified by Google's OpenID Connect layer.
     * The {@code sub} claim is used as the stable {@code providerId}.
     */
    GOOGLE,

    /**
     * GitHub OAuth2 authentication — identity verified by GitHub's OAuth2 flow.
     * The GitHub numeric user ID is used as the stable {@code providerId}.
     * Email may be {@code null} if the user's GitHub email is set to private;
     * in that case the login is rejected with a descriptive error.
     */
    GITHUB
}