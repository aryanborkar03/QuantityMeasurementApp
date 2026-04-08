package com.app.quantitymeasurement.security.jwt;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;


@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    /** Jackson mapper for serialising the error response to JSON. */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Called by Spring Security when an authenticated user attempts to access
     * a resource they are not authorised to use.
     *
     * <p>Writes a {@code 403 Forbidden} JSON response directly to the HTTP
     * response output stream, bypassing MVC dispatch.</p>
     *
     * @param request               the request that triggered the access denial
     * @param response              the response to write the 403 body to
     * @param accessDeniedException the exception that describes why access was denied
     * @throws IOException if writing to the response output stream fails
     */
    @Override
    public void handle(HttpServletRequest    request,
                       HttpServletResponse   response,
                       AccessDeniedException accessDeniedException) throws IOException {

        log.warn("Access denied for " + request.getRequestURI()
                       + " — " + accessDeniedException.getMessage());

        /*
         * Build the structured error response body.
         * LinkedHashMap preserves insertion order for a predictable JSON key sequence.
         */
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    HttpServletResponse.SC_FORBIDDEN);
        body.put("error",     "Forbidden");
        body.put("message",   accessDeniedException.getMessage());
        body.put("path",      request.getRequestURI());

        /*
         * Set response headers and status before writing the body.
         */
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
