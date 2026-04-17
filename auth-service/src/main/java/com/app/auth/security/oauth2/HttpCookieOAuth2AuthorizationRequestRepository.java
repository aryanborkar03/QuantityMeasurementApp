package com.app.auth.security.oauth2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Cookie-backed OAuth2 authorization request repository.
 *
 * FIX — "Authorization request not found" behind a gateway
 * ─────────────────────────────────────────────────────────
 * The error happens because:
 *
 * 1. The browser hits the gateway on port 8080.
 * 2. The gateway forwards /oauth2/authorization/google → auth-service (8081).
 * 3. Auth-service sets the oauth2_auth_request cookie on its response.
 * 4. Spring Cloud Gateway must forward the Set-Cookie response header and
 *    preserve the browser-facing host while proxying to auth-service.
 * 5. Even if the cookie makes it to the browser, it may be scoped to the
 *    wrong domain/path because the response came from 8081 internally.
 * 6. On the callback (/login/oauth2/code/google), the gateway forwards the
 *    request to auth-service — but the browser sends the cookie to 8080,
 *    and whether the gateway forwards it depends on its filter config.
 *
 * The complete fix has THREE parts (see also application.properties and
 * api-gateway application.properties for the other two parts):
 *
 * Part 1 (this file): Make the cookie robust:
 *   - Increase Max-Age to 300s (5 min) to survive slow OAuth2 flows.
 *   - Remove SameSite=Lax — replaced by SameSite=None; Secure for
 *     cross-origin redirect flows. For localhost (http) we use SameSite=Lax
 *     and detect via the redirectUri.
 *   - Log cookie set/load/remove so issues are visible in logs.
 *
 * Part 2 (api-gateway application.properties): Add PreserveHostHeader to the
 *   auth-service route so the cookie is scoped to localhost:8080 in the browser.
 *
 * Part 3 (auth-service application.properties): Keep
 *   server.forward-headers-strategy=FRAMEWORK so {baseUrl} resolves to
 *   the gateway port (8080), not the auth-service port (8081).
 */
@Slf4j
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";

    // 5 minutes — enough for any OAuth2 flow including slow networks
    private static final int COOKIE_EXPIRE_SECONDS = 300;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── AuthorizationRequestRepository contract ────────────────────────────

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Optional<String> cookieValue = getCookieValue(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        if (cookieValue.isEmpty()) {
            log.warn("oauth2_auth_request cookie NOT found. Cookies present: {}",
                    request.getCookies() == null ? "none" :
                    Arrays.stream(request.getCookies())
                          .map(Cookie::getName)
                          .toList());
            return null;
        }
        log.debug("oauth2_auth_request cookie found, deserializing...");
        return cookieValue.map(this::deserialize).orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            return;
        }
        String cookieValue = serialize(authorizationRequest);
        if (cookieValue == null) {
            log.error("Failed to serialize OAuth2AuthorizationRequest — OAuth2 login will fail");
            return;
        }

        // Determine if we're on HTTPS (production) or HTTP (localhost dev).
        // SameSite=None requires Secure flag and only works on HTTPS.
        // SameSite=Lax works on HTTP (localhost) and allows top-level cross-site GETs
        // (which is exactly what the OAuth2 callback redirect is).
        boolean isSecure = request.isSecure()
                || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));

        String sameSiteAndSecure = isSecure
                ? "; SameSite=None; Secure"
                : "; SameSite=Lax";

        String headerValue = OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME + "=" + cookieValue
                + "; Path=/; HttpOnly; Max-Age=" + COOKIE_EXPIRE_SECONDS
                + sameSiteAndSecure;

        response.addHeader("Set-Cookie", headerValue);
        log.debug("Saved oauth2_auth_request cookie ({} chars, secure={}, sameSite={})",
                cookieValue.length(), isSecure, isSecure ? "None" : "Lax");
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                  HttpServletResponse response) {
        OAuth2AuthorizationRequest stored = loadAuthorizationRequest(request);
        if (stored != null) {
            deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            log.debug("Removed oauth2_auth_request cookie after callback");
        }
        return stored;
    }

    // ── Serialization ──────────────────────────────────────────────────────

    private String serialize(OAuth2AuthorizationRequest request) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("au", request.getAuthorizationUri());
            map.put("c",  request.getClientId());
            map.put("ru", request.getRedirectUri());
            map.put("s",  request.getScopes());
            map.put("st", request.getState());
            map.put("rt", request.getResponseType().getValue());
            map.put("ap", request.getAdditionalParameters());
            map.put("a",  request.getAttributes());

            byte[] json = objectMapper.writeValueAsBytes(map);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (Exception e) {
            log.error("Could not serialize OAuth2AuthorizationRequest: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private OAuth2AuthorizationRequest deserialize(String value) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(value);
            Map<String, Object> map = objectMapper.readValue(json,
                    new TypeReference<Map<String, Object>>() {});

            String responseTypeValue = (String) getValue(map, "rt", "responseType");
            OAuth2AuthorizationResponseType responseType =
                    "code".equals(responseTypeValue)
                            ? OAuth2AuthorizationRequest.authorizationCode().build().getResponseType()
                            : new OAuth2AuthorizationResponseType(responseTypeValue);

            Object scopesRaw = getValue(map, "s", "scopes");
            Set<String> scopes = scopesRaw instanceof Set
                    ? (Set<String>) scopesRaw
                    : new LinkedHashSet<>((List<String>) scopesRaw);

            Map<String, Object> additionalParams =
                    (Map<String, Object>) getValueOrDefault(map, "ap", "additionalParameters", new HashMap<>());
            Map<String, Object> attributes =
                    (Map<String, Object>) getValueOrDefault(map, "a", "attributes", new HashMap<>());

            OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.authorizationCode()
                    .authorizationUri((String) getValue(map, "au", "authorizationUri"))
                    .clientId((String) getValue(map, "c", "clientId"))
                    .redirectUri((String) getValue(map, "ru", "redirectUri"))
                    .scopes(scopes)
                    .state((String) getValue(map, "st", "state"))
                    .additionalParameters(additionalParams)
                    .attributes(attributes);

            String authorizationRequestUri =
                    (String) getValueOrDefault(map, "aru", "authorizationRequestUri", null);
            if (authorizationRequestUri != null) {
                builder.authorizationRequestUri(authorizationRequestUri);
            }

            return builder.build();

        } catch (Exception e) {
            log.warn("Could not deserialize OAuth2AuthorizationRequest from cookie: {}", e.getMessage());
            return null;
        }
    }

    // ── Cookie helpers ────────────────────────────────────────────────────

    private Optional<String> getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        return Arrays.stream(cookies)
                .filter(c -> c.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst();
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        response.addHeader("Set-Cookie",
                name + "=; Path=/; HttpOnly; Max-Age=0; SameSite=Lax");
    }

    private Object getValue(Map<String, Object> map, String compactKey, String legacyKey) {
        return map.containsKey(compactKey) ? map.get(compactKey) : map.get(legacyKey);
    }

    private Object getValueOrDefault(Map<String, Object> map,
                                     String compactKey,
                                     String legacyKey,
                                     Object defaultValue) {
        Object value = getValue(map, compactKey, legacyKey);
        return value != null ? value : defaultValue;
    }
}
