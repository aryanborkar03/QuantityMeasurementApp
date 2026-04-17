package com.app.auth.security.oauth2;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.app.auth.entity.User;
import com.app.auth.security.UserPrincipal;
import com.app.auth.security.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String DEFAULT_FRONTEND_REDIRECT_URI = "http://localhost:5173";

    private final JwtTokenProvider jwtTokenProvider;
    private final String redirectUri;

    /**
     * FIX: Inject the cookie repository so we can clear the oauth2_auth_request
     * cookie on success. Without this, the cookie persists in the browser after
     * a successful login. Any subsequent navigation that triggers the OAuth2
     * callback endpoint will find the stale cookie, fail state validation, call
     * onAuthenticationFailure, and produce ERR_TOO_MANY_REDIRECTS.
     */
    @Autowired
    private HttpCookieOAuth2AuthorizationRequestRepository cookieRepository;

    public OAuth2AuthenticationSuccessHandler(
            JwtTokenProvider jwtTokenProvider,
            @Value("${app.oauth2.redirect-uri:" + DEFAULT_FRONTEND_REDIRECT_URI + "}")
            String redirectUri) {

        this.jwtTokenProvider = jwtTokenProvider;
        this.redirectUri = redirectUri;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest  request,
                                        HttpServletResponse response,
                                        Authentication      authentication) throws IOException {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();

        String roleAuthority = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        String token = jwtTokenProvider.generateTokenFromEmail(user.getEmail(), roleAuthority);

        String name = user.getName();

        log.info("OAuth2 login successful for: " + user.getEmail() + " — Redirecting to frontend.");

        // Attach BOTH the token and the real name to the URL
        String targetUrl = UriComponentsBuilder.fromUriString(resolveSafeRedirectUri())
                .queryParam("token", token)
                .queryParam("name", name)
                .build()
                .encode()
                .toUriString();

        // FIX: Always clear the oauth2_auth_request cookie on success.
        // This prevents the stale cookie from triggering a spurious
        // authentication failure (and subsequent redirect loop) on
        // any page load after login.
        cookieRepository.removeAuthorizationRequest(request, response);
        super.clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String resolveSafeRedirectUri() {
        if (redirectUri == null || redirectUri.isBlank() || pointsToOAuthEndpoint(redirectUri)) {
            log.warn("Unsafe OAuth2 success redirect URI configured: {}. Falling back to {}",
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
