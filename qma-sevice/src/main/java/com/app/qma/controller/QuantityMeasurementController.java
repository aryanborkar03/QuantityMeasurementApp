package com.app.qma.controller;

import com.app.qma.dto.request.QuantityInputDTO;
import com.app.qma.dto.request.QuantityMeasurementDTO;
import com.app.qma.service.IQuantityMeasurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/quantities")
@Tag(name = "Quantity Measurements", description = "Quantity measurement operations and history")
public class QuantityMeasurementController {

    private final IQuantityMeasurementService service;

    public QuantityMeasurementController(IQuantityMeasurementService service) {
        this.service = service;
    }

    // ── Write operations (public — no token required) ─────────────────────────
    // userEmail is resolved from SecurityContext inside the service;
    // null for unauthenticated callers, email string for authenticated ones.

    @PostMapping("/compare")
    @Operation(summary = "Compare two quantities")
    public ResponseEntity<QuantityMeasurementDTO> compare(@Valid @RequestBody QuantityInputDTO dto) {
        log.info("POST /compare");
        return ResponseEntity.ok(service.compare(dto.getThisQuantityDTO(), dto.getThatQuantityDTO()));
    }

    @PostMapping("/convert")
    @Operation(summary = "Convert a quantity to a different unit")
    public ResponseEntity<QuantityMeasurementDTO> convert(@Valid @RequestBody QuantityInputDTO dto) {
        log.info("POST /convert");
        return ResponseEntity.ok(service.convert(dto.getThisQuantityDTO(), dto.getThatQuantityDTO()));
    }

    @PostMapping("/add")
    @Operation(summary = "Add two quantities")
    public ResponseEntity<QuantityMeasurementDTO> add(@Valid @RequestBody QuantityInputDTO dto) {
        log.info("POST /add");
        QuantityMeasurementDTO result = dto.getTargetUnitDTO() != null
                ? service.add(dto.getThisQuantityDTO(), dto.getThatQuantityDTO(), dto.getTargetUnitDTO())
                : service.add(dto.getThisQuantityDTO(), dto.getThatQuantityDTO());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/subtract")
    @Operation(summary = "Subtract two quantities")
    public ResponseEntity<QuantityMeasurementDTO> subtract(@Valid @RequestBody QuantityInputDTO dto) {
        log.info("POST /subtract");
        QuantityMeasurementDTO result = dto.getTargetUnitDTO() != null
                ? service.subtract(dto.getThisQuantityDTO(), dto.getThatQuantityDTO(), dto.getTargetUnitDTO())
                : service.subtract(dto.getThisQuantityDTO(), dto.getThatQuantityDTO());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/divide")
    @Operation(summary = "Divide two quantities")
    public ResponseEntity<QuantityMeasurementDTO> divide(@Valid @RequestBody QuantityInputDTO dto) {
        log.info("POST /divide");
        return ResponseEntity.ok(service.divide(dto.getThisQuantityDTO(), dto.getThatQuantityDTO()));
    }

    // ── User-scoped history routes (/me/) — requires authentication ───────────

    /**
     * Returns the authenticated user's full history across all operations.
     * Route: GET /api/v1/quantities/me/history
     */
    @GetMapping("/me/history")
    @Operation(summary = "My full history",
               description = "Returns all operations performed by the authenticated user",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<QuantityMeasurementDTO>> getMyHistory() {
        String email = requireAuthenticatedEmail();
        log.info("GET /me/history [user={}]", email);
        return ResponseEntity.ok(service.getMyHistory(email));
    }

    /**
     * Returns the authenticated user's history filtered by operation type.
     * Route: GET /api/v1/quantities/me/history/operation/{operation}
     */
    @GetMapping("/me/history/operation/{operation}")
    @Operation(summary = "My history by operation type",
               description = "Returns the current user's records for a specific operation",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<QuantityMeasurementDTO>> getMyHistoryByOperation(
            @Parameter(description = "compare | convert | add | subtract | divide")
            @PathVariable String operation) {
        String email = requireAuthenticatedEmail();
        log.info("GET /me/history/operation/{} [user={}]", operation, email);
        return ResponseEntity.ok(service.getMyHistoryByOperation(email, operation));
    }

    /**
     * Returns the authenticated user's history filtered by measurement type.
     * Route: GET /api/v1/quantities/me/history/type/{measurementType}
     */
    @GetMapping("/me/history/type/{measurementType}")
    @Operation(summary = "My history by measurement type",
               description = "Returns the current user's records for a specific measurement type",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<QuantityMeasurementDTO>> getMyHistoryByType(
            @Parameter(description = "LengthUnit | WeightUnit | VolumeUnit | TemperatureUnit")
            @PathVariable String measurementType) {
        String email = requireAuthenticatedEmail();
        log.info("GET /me/history/type/{} [user={}]", measurementType, email);
        return ResponseEntity.ok(service.getMyHistoryByMeasurementType(email, measurementType));
    }

    /**
     * Returns the authenticated user's error records.
     * Route: GET /api/v1/quantities/me/history/errored
     */
    @GetMapping("/me/history/errored")
    @Operation(summary = "My error history",
               description = "Returns the current user's failed operations",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<QuantityMeasurementDTO>> getMyErrorHistory() {
        String email = requireAuthenticatedEmail();
        log.info("GET /me/history/errored [user={}]", email);
        return ResponseEntity.ok(service.getMyErrorHistory(email));
    }

    /**
     * Returns the count of the authenticated user's successful operations for a given type.
     * Route: GET /api/v1/quantities/me/count/{operation}
     */
    @GetMapping("/me/count/{operation}")
    @Operation(summary = "My operation count",
               description = "Returns the count of the current user's successful operations",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Long> getMyOperationCount(
            @Parameter(description = "COMPARE | CONVERT | ADD | SUBTRACT | DIVIDE")
            @PathVariable String operation) {
        String email = requireAuthenticatedEmail();
        log.info("GET /me/count/{} [user={}]", operation, email);
        return ResponseEntity.ok(service.getMyOperationCount(email, operation));
    }

    // ── Admin / global history routes — requires authentication ───────────────

    @GetMapping("/history/operation/{operation}")
    @Operation(summary = "Global history by operation (admin)")
    public ResponseEntity<List<QuantityMeasurementDTO>> getOperationHistory(
            @PathVariable String operation) {
        log.info("GET /history/operation/{}", operation);
        return ResponseEntity.ok(service.getHistoryByOperation(operation));
    }

    @GetMapping("/history/type/{measurementType}")
    @Operation(summary = "Global history by measurement type (admin)")
    public ResponseEntity<List<QuantityMeasurementDTO>> getMeasurementHistory(
            @PathVariable String measurementType) {
        log.info("GET /history/type/{}", measurementType);
        return ResponseEntity.ok(service.getHistoryByMeasurementType(measurementType));
    }

    @GetMapping("/history/errored")
    @Operation(summary = "Global error history (admin only)")
    public ResponseEntity<List<QuantityMeasurementDTO>> getErrorHistory() {
        log.info("GET /history/errored");
        return ResponseEntity.ok(service.getErrorHistory());
    }

    @GetMapping("/count/{operation}")
    @Operation(summary = "Global operation count (admin)")
    public ResponseEntity<Long> getOperationCount(@PathVariable String operation) {
        log.info("GET /count/{}", operation);
        return ResponseEntity.ok(service.getOperationCount(operation));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String requireAuthenticatedEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return auth.getName();
    }
}
