package com.app.quantitymeasurement.security.jwt;

import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.app.quantitymeasurement.security.UserPrincipal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * JwtTokenProvider
 *
 * Centralised service responsible for the full JWT lifecycle:
 * <ol>
 *   <li><b>Token generation</b> — signs a compact JWT using HMAC-SHA-256 (HS256).</li>
 *   <li><b>Claims extraction</b> — retrieves the subject (email) and role from a
 *       previously issued token.</li>
 *   <li><b>Token validation</b> — verifies the signature, checks the expiry time,
 *       and catches all JJWT-defined exception types gracefully.</li>
 * </ol>
 *
 * <p><b>Token structure (standard JWT claims):</b></p>
 * <ul>
 *   <li>{@code sub}    — the user's email address (principal name)</li>
 *   <li>{@code roles}  — comma-separated authority string (e.g., {@code "ROLE_USER"})</li>
 *   <li>{@code iat}    — issued-at timestamp (epoch milliseconds)</li>
 *   <li>{@code exp}    — expiry timestamp ({@code iat + expirationMs})</li>
 * </ul>
 *
 * <p><b>Key management:</b> the secret is read from {@code app.jwt.secret} in
 * {@code application.properties} as a Base64-encoded string. It must be at least
 * 32 bytes (256 bits) after decoding to satisfy the HS256 minimum key size.
 * Never commit the actual secret to version control; use environment variable
 * substitution ({@code ${JWT_SECRET}}) in production.</p>
 *
 * <p><b>Thread safety:</b> this component is a Spring singleton. The
 * {@link SecretKey} object is immutable and safe to share across threads.</p>
 *
 * @author Abhishek Puri Goswami
 * @version 18.0
 * @since 18.0
 */
@Slf4j
@Component
public class JwtTokenProvider {


    /*
     * -------------------------------------------------------------------------
     * Configuration (injected from application.properties)
     * -------------------------------------------------------------------------
     */

    /**
     * Base64-encoded HMAC-SHA-256 secret key. Injected from
     * {@code app.jwt.secret} in {@code application.properties}.
     */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /**
     * Token validity period in milliseconds. Injected from
     * {@code app.jwt.expiration-ms} in {@code application.properties}.
     * Default: 86 400 000 ms = 24 hours.
     */
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /*
     * -------------------------------------------------------------------------
     * Token generation
     * -------------------------------------------------------------------------
     */

    /**
     * Generates a signed JWT for the authenticated principal.
     *
     * <p>The token payload contains:</p>
     * <ul>
     *   <li>{@code sub}   — the principal's username (email)</li>
     *   <li>{@code roles} — space-separated authority strings</li>
     *   <li>{@code iat}   — current time</li>
     *   <li>{@code exp}   — current time + {@link #jwtExpirationMs}</li>
     * </ul>
     *
     * @param authentication the successfully authenticated principal returned
     *                       by Spring Security's authentication manager
     * @return a compact, URL-safe JWT string (header.payload.signature)
     */
    public String generateToken(Authentication authentication) {
        /*
         * Retrieve the principal. For local logins this is a UserPrincipal;
         * for OAuth2 logins the same type is returned by CustomOAuth2UserService.
         */
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        /*
         * Collect all GrantedAuthority strings into a single space-delimited
         * claim so the JWT filter can reconstruct them on the next request
         * without a database round-trip.
         */
        String roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        Date now    = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername())   // JWT "sub" claim = email
                .claim("roles", roles)                  // custom claim for role(s)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())               // HS256 via derived SecretKey
                .compact();
    }

    /**
     * Overloaded variant that generates a JWT directly from a user email string
     * and a role string. Used by {@code OAuth2AuthenticationSuccessHandler} and
     * {@code AuthController} when an {@link Authentication} object is not
     * available in the current context.
     *
     * @param email the email address that becomes the {@code sub} claim
     * @param role  the authority string (e.g., {@code "ROLE_USER"})
     * @return a compact JWT string
     */
    public String generateTokenFromEmail(String email, String role) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("roles", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /*
     * -------------------------------------------------------------------------
     * Claims extraction
     * -------------------------------------------------------------------------
     */

    /**
     * Extracts the {@code sub} (subject / email) claim from a valid JWT.
     *
     * <p>This method <em>does not</em> validate expiry; call
     * {@link #validateToken(String)} first to ensure the token is still valid
     * before trusting any claims.</p>
     *
     * @param token the compact JWT string
     * @return the email address stored in the {@code sub} claim
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the {@code roles} custom claim from a valid JWT.
     *
     * @param token the compact JWT string
     * @return the roles string (e.g., {@code "ROLE_USER"})
     */
    public String getRolesFromToken(String token) {
        return parseClaims(token).get("roles", String.class);
    }

    /*
     * -------------------------------------------------------------------------
     * Token validation
     * -------------------------------------------------------------------------
     */

    /**
     * Validates a JWT by verifying its signature and checking that it has not
     * expired. All JJWT exception types are caught and logged at WARNING level;
     * the method returns {@code false} rather than propagating the exception,
     * so the filter can simply deny the request without additional try/catch.
     *
     * <p>Exception cases handled:</p>
     * <ul>
     *   <li>{@link io.jsonwebtoken.security.SecurityException} /
     *       {@link MalformedJwtException} — invalid signature or malformed token</li>
     *   <li>{@link ExpiredJwtException}       — token has passed its expiry time</li>
     *   <li>{@link UnsupportedJwtException}   — unexpected JWT format or algorithm</li>
     *   <li>{@link IllegalArgumentException}  — token string is null or empty</li>
     * </ul>
     *
     * @param token the compact JWT string to validate
     * @return {@code true} if the token is well-formed, unexpired, and has a
     *         valid HS256 signature; {@code false} otherwise
     */
    public boolean validateToken(String token) {
        try {
            /*
             * parseSignedClaims() verifies the signature AND checks the exp claim.
             * If either fails, it throws one of the JJWT exceptions listed above.
             */
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;

        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException ex) {
            log.warn("Invalid JWT signature: " + ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token: " + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty: " + ex.getMessage());
        }
        return false;
    }

    /*
     * -------------------------------------------------------------------------
     * Private helpers
     * -------------------------------------------------------------------------
     */

    /**
     * Derives the {@link SecretKey} used for signing and verification from the
     * Base64-encoded {@link #jwtSecret}. The key is re-derived on each call;
     * in a performance-sensitive scenario it could be cached as a field, but
     * the overhead is negligible for token-per-request operations.
     *
     * <p>{@link Keys#hmacShaKeyFor(byte[])} creates an HMAC-SHA key sized to the
     * length of the provided byte array. A 32-byte input produces HS256;
     * 48 bytes → HS384; 64 bytes → HS512.</p>
     *
     * @return the derived {@link SecretKey}
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Parses the JWT and returns its {@link Claims} payload.
     * Callers must ensure {@link #validateToken(String)} returns {@code true}
     * before calling this method; otherwise JJWT exceptions will propagate.
     *
     * @param token the compact JWT string
     * @return the parsed claims payload
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
