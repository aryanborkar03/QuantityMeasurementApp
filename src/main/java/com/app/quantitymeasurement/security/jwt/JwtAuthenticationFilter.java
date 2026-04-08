package com.app.quantitymeasurement.security.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.app.quantitymeasurement.security.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    /*
     * -------------------------------------------------------------------------
     * Dependencies
     * -------------------------------------------------------------------------
     */

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
			CustomUserDetailsService customUserDetailsService) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.customUserDetailsService = customUserDetailsService;
	}

	/*
     * -------------------------------------------------------------------------
     * Core filter logic
     * -------------------------------------------------------------------------
     */

    /**
     * Core filter method invoked once per HTTP request.
     *
     * <p>If a valid JWT is found, the {@link SecurityContextHolder} is populated
     * with a {@link UsernamePasswordAuthenticationToken} before the request
     * continues. If no JWT is present or the JWT is invalid, the context is left
     * empty and the request proceeds unauthenticated (public endpoints will succeed;
     * protected endpoints will be rejected by the access-control rules in
     * {@code SecurityConfig}).</p>
     *
     * @param request     the incoming HTTP request
     * @param response    the outgoing HTTP response
     * @param filterChain the remainder of the filter chain
     * @throws ServletException if a servlet-related error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain)
            throws ServletException, IOException {

        try {
            /*
             * Step 1 — extract token from Authorization header.
             * Returns null if the header is absent or not prefixed with "Bearer ".
             */
            String jwt = extractJwtFromRequest(request);

            /*
             * Step 2 — validate and authenticate if a token was provided.
             * The null check prevents unnecessary validation calls.
             */
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                /*
                 * Step 3 — extract the subject (email) from the validated token.
                 */
                String email = jwtTokenProvider.getEmailFromToken(jwt);

                /*
                 * Step 4 — load the full UserDetails (confirms the user exists
                 * in the database and re-reads current roles/state).
                 */
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                /*
                 * Step 5 — build the Authentication token and attach HTTP request
                 * details (remote address, session ID) for audit/logging purposes.
                 */
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                           // credentials — null after auth
                                userDetails.getAuthorities()    // roles from DB
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                /*
                 * Step 6 — store the Authentication in the SecurityContext.
                 * All downstream components (controllers, service method security,
                 * etc.) can now call SecurityContextHolder.getContext().getAuthentication()
                 * to access the current principal.
                 */
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT authenticated user: " + email +
                            " for " + request.getMethod() + " " + request.getRequestURI());
            }

        } catch (Exception ex) {
            /*
             * Any unexpected exception during JWT processing is caught here to
             * prevent it from propagating and causing a 500 error. The Security
             * context is left empty; the request will proceed unauthenticated
             * and be rejected by the access-control layer if the endpoint requires auth.
             */
            log.warn("Could not set user authentication in security context: "
                           + ex.getMessage());
        }

        /*
         * Step 7 — always continue the filter chain, regardless of whether
         * authentication succeeded. The access-control configuration in
         * SecurityConfig.securityFilterChain() will enforce endpoint-level rules.
         */
        filterChain.doFilter(request, response);
    }

    /*
     * -------------------------------------------------------------------------
     * Private helpers
     * -------------------------------------------------------------------------
     */

    /**
     * Extracts the JWT string from the {@code Authorization} request header.
     *
     * <p>The expected format is: {@code Authorization: Bearer <token>}
     * RFC 6750 specifies this as the standard bearer token format. If the header
     * is absent or does not start with the {@code "Bearer "} prefix, {@code null}
     * is returned and the filter skips authentication for this request.</p>
     *
     * @param request the current HTTP request
     * @return the raw JWT string (without the {@code "Bearer "} prefix),
     *         or {@code null} if the header is absent or malformed
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            /*
             * Trim the "Bearer " prefix (7 characters) to get the raw JWT.
             * String.substring(7) is safe here because we've confirmed the
             * string starts with "Bearer " (7 chars).
             */
            return bearerToken.substring(7);
        }

        return null;
    }
}
