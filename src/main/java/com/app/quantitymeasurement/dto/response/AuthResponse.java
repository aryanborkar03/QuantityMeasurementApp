package com.app.quantitymeasurement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    /**
     * The signed JWT access token. Must be sent as {@code Authorization: Bearer <token>}
     * on every protected API call.
     */
    private String accessToken;

    /**
     * Always {@code "Bearer"}. Included for RFC 6750 compliance so clients do not
     * need to hard-code the scheme.
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * The authenticated user's email address. Convenient for the client to
     * display the current user's identity without decoding the JWT.
     */
    private String email;

    /**
     * The authenticated user's display name (may be {@code null} for local
     * accounts that did not supply one at registration).
     */
    private String name;

    /**
     * The authenticated user's role ({@code "USER"} or {@code "ADMIN"}).
     * Allows the client to conditionally show or hide admin-only UI elements
     * without decoding the JWT.
     */
    private String role;
}
