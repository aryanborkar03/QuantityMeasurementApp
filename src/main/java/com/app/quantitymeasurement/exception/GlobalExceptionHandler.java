package com.app.quantitymeasurement.exception;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler
 *
 * Centralised exception handler for all REST controllers in the application.
 * {@code @ControllerAdvice} intercepts exceptions thrown by any controller and
 * returns consistent, structured JSON error responses instead of raw stack traces.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {


    /**
     * Handles Bean Validation failures that arise when a {@code @Valid}-annotated
     * request body fails its constraints.
     *
     * <p>All field-level error messages are collected and joined into a single
     * {@code message} string so the client receives full feedback in one response.</p>
     *
     * @param ex      the validation exception
     * @param request the current HTTP request (used for the {@code path} field)
     * @return {@code 400 Bad Request} with a structured validation error body
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));

        log.warn("Validation failed: " + errorMessage);

        return ResponseEntity.badRequest().body(buildErrorBody(
            HttpStatus.BAD_REQUEST.value(),
            "Quantity Measurement Error",
            errorMessage,
            ex.getBindingResult().getObjectName()
        ));
    }

    /**
     * Handles {@link QuantityMeasurementException} thrown by the service layer,
     * for example when two quantities of incompatible types are compared or an
     * unsupported arithmetic operation is attempted.
     *
     * @param ex      the quantity measurement exception
     * @param request the current HTTP request
     * @return {@code 400 Bad Request} with a structured error body
     */
    @ExceptionHandler(QuantityMeasurementException.class)
    public ResponseEntity<Map<String, Object>> handleQuantityException(
            QuantityMeasurementException ex,
            HttpServletRequest request) {

        log.warn("QuantityMeasurementException: " + ex.getMessage());

        return ResponseEntity.badRequest().body(buildErrorBody(
            HttpStatus.BAD_REQUEST.value(),
            "Quantity Measurement Error",
            ex.getMessage(),
            request.getRequestURI()
        ));
    }

    /**
     * Handles {@link IllegalArgumentException} thrown when an invalid argument
     * is passed to a service or utility method (e.g., an unrecognised unit name).
     *
     * @param ex      the exception
     * @param request the current HTTP request
     * @return {@code 400 Bad Request} with a structured error body
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("IllegalArgumentException: " + ex.getMessage());

        return ResponseEntity.badRequest().body(buildErrorBody(
            HttpStatus.BAD_REQUEST.value(),
            "Quantity Measurement Error",
            ex.getMessage(),
            request.getRequestURI()
        ));
    }

    /**
     * Catch-all handler for any exception not covered by a more specific handler above.
     * Ensures that unhandled errors always produce a structured response rather than
     * an empty body or raw stack trace.
     *
     * @param ex      the unhandled exception
     * @param request the current HTTP request
     * @return {@code 500 Internal Server Error} with a structured error body
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception: " + ex.getMessage());

        return ResponseEntity.internalServerError().body(buildErrorBody(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            ex.getMessage(),
            request.getRequestURI()
        ));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Builds the standardised error response map used by all handlers.
     *
     * @param status  HTTP status code
     * @param error   short error category label
     * @param message detailed error description
     * @param path    request path that triggered the error
     * @return map ready to be serialised as the JSON response body
     */
    private Map<String, Object> buildErrorBody(int status, String error,
                                               String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    status);
        body.put("error",     error);
        body.put("message",   message);
        body.put("path",      path);
        return body;
    }
}