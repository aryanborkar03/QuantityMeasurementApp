package com.app.auth.security.oauth2;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final String DEFAULT_FRONTEND_REDIRECT_URI = "http://localhost:5173";

    /**
     * Frontend URL to redirect to on failure. The {@code ?error=} query parameter
     * is appended dynamically.
     */
    @Value("${app.oauth2.redirect-uri:" + DEFAULT_FRONTEND_REDIRECT_URI + "}")
    private String redirectUri;

    @Autowired
    private HttpCookieOAuth2AuthorizationRequestRepository cookieRepository;

    /**
     * Handles a failed OAuth2 authentication event.
     *
     * <p>Clears the {@code oauth2_auth_request} cookie first to prevent a
     * stale cookie from causing an infinite redirect loop on the next attempt.
     * The error message from the {@link AuthenticationException} is URL-encoded
     * (UTF-8) and appended to the redirect URI.</p>
     *
     * @param request   the current HTTP request
     * @param response  the current HTTP response
     * @param exception the exception that caused the authentication failure
     * @throws IOException if the redirect fails
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest  request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String errorMessage = exception.getLocalizedMessage();
        log.warn("OAuth2 authentication failed: {}", errorMessage);

        // FIX: Always remove the stale oauth2_auth_request cookie on failure.
        // Without this, the cookie persists in the browser and the next OAuth2
        // attempt picks up a mismatched state value, throws again, redirects
        // to the failure handler, and the cycle produces ERR_TOO_MANY_REDIRECTS.
        cookieRepository.removeAuthorizationRequest(request, response);

        String safeRedirectUri = resolveSafeRedirectUri();

        String targetUrl = UriComponentsBuilder.fromUriString(safeRedirectUri)
                .queryParam("error", errorMessage != null ? errorMessage : "Authentication failed")
                .build()
                .encode()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String resolveSafeRedirectUri() {
        if (redirectUri == null || redirectUri.isBlank() || pointsToOAuthEndpoint(redirectUri)) {
            log.warn("Unsafe OAuth2 failure redirect URI configured: {}. Falling back to {}",
                    redirectUri, DEFAULT_FRONTEND_REDIRECT_URI);
            return DEFAULT_FRONTEND_REDIRECT_URI;
        }
        return redirectUri;
    }

    private boolean pointsToOAuthEndpoint(String uri) {
        return uri.contains("/login/oauth2/")
                || uri.contains("/oauth2/authorization/")
                || uri.endsWith("/login")
                || uri.contains("/login?");
    }
}
