package com.app.quantitymeasurement.security.oauth2;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    /**
     * Frontend URL to redirect to on failure. The {@code ?error=} query parameter
     * is appended dynamically. Defaults to Swagger UI for local development.
     */
    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/callback}")
    private String redirectUri;

    /**
     * Handles a failed OAuth2 authentication event.
     *
     * <p>The error message from the {@link AuthenticationException} is URL-encoded
     * (UTF-8) and appended to the redirect URI so it can be safely included in a
     * query parameter without breaking the URL structure.</p>
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

        /*
         * Log the failure at WARNING level so it appears in application logs
         * without generating a full stack trace (the message is usually descriptive
         * enough for diagnosis).
         */
        String errorMessage = exception.getLocalizedMessage();
        log.warn("OAuth2 authentication failed: " + errorMessage);

        /*
         * Build the redirect URL:  <redirectUri>?error=<urlEncodedMessage>
         *
         * UriComponentsBuilder.queryParam() does NOT automatically URL-encode the
         * value in all Spring versions, so we encode it explicitly here to prevent
         * injection of additional query parameters through a crafted error message.
         */
        String encodedError = URLEncoder.encode(
                errorMessage != null ? errorMessage : "Authentication failed",
                StandardCharsets.UTF_8);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", encodedError)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
