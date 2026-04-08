package com.app.quantitymeasurement.entity;

import com.app.quantitymeasurement.enums.AuthProvider;
import com.app.quantitymeasurement.enums.Role;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "app_user",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "uk_app_user_email")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /*
     * -------------------------------------------------------------------------
     * Primary key
     * -------------------------------------------------------------------------
     */

    /**
     * Auto-generated surrogate primary key.
     * Uses the IDENTITY strategy, which works for both H2 (in-memory) and MySQL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * -------------------------------------------------------------------------
     * Identity fields
     * -------------------------------------------------------------------------
     */

    /**
     * The user's email address. Used as:
     * <ul>
     *   <li>The unique login identifier for local accounts.</li>
     *   <li>The JWT {@code sub} (subject) claim.</li>
     *   <li>The lookup key when processing an incoming Google OAuth2 token.</li>
     * </ul>
     */
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid address")
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * The user's display name. Populated from the Google profile on OAuth2 login,
     * or supplied by the user during local registration.
     */
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(length = 100)
    private String name;

    /*
     * -------------------------------------------------------------------------
     * Authentication fields
     * -------------------------------------------------------------------------
     */

    /**
     * BCrypt-hashed password for local accounts. Set to {@code NULL} for OAuth2
     * accounts because their passwords are managed by the external provider.
     *
     * <p><b>Security note:</b> this field is excluded from {@code toString()} via
     * a custom override to prevent accidental password hash leakage in logs.</p>
     */
    @Column(length = 255)
    private String password;

    /**
     * The authentication provider that was used to create this account.
     * Stored as a STRING enum to make database values self-documenting.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    /**
     * The opaque subject identifier returned by the OAuth2 provider.
     * For Google this is the {@code sub} claim from the ID token — a stable,
     * unique, per-user identifier that never changes even if the user changes
     * their email address.
     *
     * <p>Left {@code NULL} for LOCAL accounts.</p>
     */
    @Column(name = "provider_id", length = 255)
    private String providerId;

    /*
     * -------------------------------------------------------------------------
     * Authorisation field
     * -------------------------------------------------------------------------
     */

    /**
     * The single security role granted to this user.
     * Defaults to {@link Role#USER} at registration time.
     * Must be set to {@link Role#ADMIN} manually in the database to elevate
     * access; it is never granted automatically.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    /*
     * -------------------------------------------------------------------------
     * Profile fields
     * -------------------------------------------------------------------------
     */

    /**
     * URL of the user's profile picture, returned by Google during OAuth2 login.
     * {@code NULL} for local accounts unless the user sets one separately.
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /*
     * -------------------------------------------------------------------------
     * Audit field
     * -------------------------------------------------------------------------
     */

    /**
     * Timestamp at which this record was first persisted.
     * Automatically set by the {@link #prePersist()} lifecycle callback and
     * never changed afterwards. Stored without timezone information (UTC assumed).
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /*
     * -------------------------------------------------------------------------
     * JPA lifecycle callbacks
     * -------------------------------------------------------------------------
     */

    /**
     * Sets {@link #createdAt} to the current UTC wall-clock time immediately
     * before the entity is first inserted into the database.
     *
     * <p>Using a lifecycle callback (rather than a database default) keeps the
     * timestamp consistent between H2 and MySQL and makes it visible to JPA
     * before the flush/commit cycle.</p>
     */
    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    /*
     * -------------------------------------------------------------------------
     * Security override
     * -------------------------------------------------------------------------
     */

    /**
     * Overrides Lombok-generated {@code toString()} to exclude the
     * {@link #password} field, preventing BCrypt hashes from appearing
     * in application logs or stack traces.
     *
     * @return a safe string representation of this user
     */
    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", email='" + email + '\'' +
               ", name='" + name + '\'' +
               ", provider=" + provider +
               ", role=" + role +
               ", createdAt=" + createdAt +
               '}';
    }
}
