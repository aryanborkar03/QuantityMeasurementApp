package com.app.quantitymeasurement.controller;

import lombok.extern.slf4j.Slf4j;

import com.app.quantitymeasurement.dto.request.QuantityInputDTO;
import com.app.quantitymeasurement.dto.request.QuantityMeasurementDTO;
import com.app.quantitymeasurement.service.IQuantityMeasurementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/v1/quantities")
@Tag(name = "Quantity Measurements", 
     description = "REST API for quantity measurement operations")
public class QuantityMeasurementController {

    private final IQuantityMeasurementService quantityMeasurementService;
    
    public QuantityMeasurementController(IQuantityMeasurementService quantityMeasurementService) {
		this.quantityMeasurementService = quantityMeasurementService;
	}

	// -------------------------------------------------------------------------
    // POST — operation endpoints
    // -------------------------------------------------------------------------

    /**
     * Compares two quantities for equality after converting both to their base units.
     *
     * @param quantityInputDTO request body containing the two operands
     * @return {@code 200 OK} with a {@link QuantityMeasurementDTO} whose
     *         {@code resultString} field is {@code "true"} or {@code "false"}
     */
    @PostMapping("/compare")
    @Operation(summary = "Compare two quantities",
               description = "Compares two quantities for equality after converting to base units")
    public ResponseEntity<QuantityMeasurementDTO> compareQuantities(
            @Valid @RequestBody QuantityInputDTO quantityInputDTO) {
    	
        log.info("POST /compare");
        
        return ResponseEntity.ok(
        		quantityMeasurementService.compare(
            quantityInputDTO.getThisQuantityDTO(),
            quantityInputDTO.getThatQuantityDTO()
        ));
    }

    /**
     * Converts a quantity from its current unit to the unit specified by
     * {@code thatQuantityDTO}. The value of {@code thatQuantityDTO} is ignored;
     * only its {@code unit} and {@code measurementType} fields are used.
     *
     * @param quantityInputDTO request body containing the source quantity and target unit
     * @return {@code 200 OK} with a {@link QuantityMeasurementDTO} containing
     *         the converted value in {@code resultValue}
     */
    @PostMapping("/convert")
    @Operation(summary = "Convert a quantity to a different unit",
               description = "Converts a quantity from its current unit to the specified target unit")
    public ResponseEntity<QuantityMeasurementDTO> convertQuantity(
            @Valid @RequestBody QuantityInputDTO quantityInputDTO) {
        log.info("POST /convert");
        return ResponseEntity.ok(quantityMeasurementService.convert(
            quantityInputDTO.getThisQuantityDTO(),
            quantityInputDTO.getThatQuantityDTO()
        ));
    }

    /**
     * Adds two quantities. If {@code targetUnitDTO} is provided, the result is
     * expressed in that unit; otherwise, the unit of the first operand is used.
     *
     * @param quantityInputDTO request body containing the two operands and optional target unit
     * @return {@code 200 OK} with a {@link QuantityMeasurementDTO} containing the sum
     */
    @PostMapping("/add")
    @Operation(summary = "Add two quantities",
               description = "Adds two quantities, with an optional target unit for the result")
    public ResponseEntity<QuantityMeasurementDTO> addQuantities(
            @Valid @RequestBody QuantityInputDTO quantityInputDTO) {
        log.info("POST /add");
        QuantityMeasurementDTO result = (quantityInputDTO.getTargetUnitDTO() != null)
            ? quantityMeasurementService.add(
                quantityInputDTO.getThisQuantityDTO(),
                quantityInputDTO.getThatQuantityDTO(),
                quantityInputDTO.getTargetUnitDTO())
            : quantityMeasurementService.add(
                quantityInputDTO.getThisQuantityDTO(),
                quantityInputDTO.getThatQuantityDTO());
        return ResponseEntity.ok(result);
    }

    /**
     * Subtracts the second quantity from the first. If {@code targetUnitDTO} is
     * provided, the result is expressed in that unit; otherwise, the unit of the
     * first operand is used.
     *
     * @param quantityInputDTO request body containing the two operands and optional target unit
     * @return {@code 200 OK} with a {@link QuantityMeasurementDTO} containing the difference
     */
    @PostMapping("/subtract")
    @Operation(summary = "Subtract two quantities",
               description = "Subtracts the second quantity from the first, with an optional target unit")
    public ResponseEntity<QuantityMeasurementDTO> subtractQuantities(
            @Valid @RequestBody QuantityInputDTO quantityInputDTO) {
        log.info("POST /subtract");
        QuantityMeasurementDTO result = (quantityInputDTO.getTargetUnitDTO() != null)
            ? quantityMeasurementService.subtract(
                quantityInputDTO.getThisQuantityDTO(),
                quantityInputDTO.getThatQuantityDTO(),
                quantityInputDTO.getTargetUnitDTO())
            : quantityMeasurementService.subtract(
                quantityInputDTO.getThisQuantityDTO(),
                quantityInputDTO.getThatQuantityDTO());
        return ResponseEntity.ok(result);
    }

    /**
     * Divides the first quantity by the second and returns the dimensionless numeric ratio.
     *
     * @param quantityInputDTO request body containing the dividend and divisor
     * @return {@code 200 OK} with a {@link QuantityMeasurementDTO} whose
     *         {@code resultValue} holds the ratio
     */
    @PostMapping("/divide")
    @Operation(summary = "Divide two quantities",
               description = "Divides the first quantity by the second and returns the numeric ratio")
    public ResponseEntity<QuantityMeasurementDTO> divideQuantities(
            @Valid @RequestBody QuantityInputDTO quantityInputDTO) {
        log.info("POST /divide");
        return ResponseEntity.ok(quantityMeasurementService.divide(
            quantityInputDTO.getThisQuantityDTO(),
            quantityInputDTO.getThatQuantityDTO()
        ));
    }

    // -------------------------------------------------------------------------
    // GET — history and count endpoints
    // -------------------------------------------------------------------------

    /**
     * Returns all persisted measurement records for the given operation type.
     *
     * @param operation operation name to filter by (e.g., {@code compare}, {@code add})
     * @return {@code 200 OK} with a list of matching {@link QuantityMeasurementDTO} records
     */
    @GetMapping("/history/operation/{operation}")
    @Operation(summary = "Get operation history by type",
               description = "Returns all measurement records for the specified operation type")
    public ResponseEntity<List<QuantityMeasurementDTO>> getOperationHistory(
            @Parameter(description = "Operation type — compare, convert, add, subtract, divide")
            @PathVariable String operation) {
        log.info("GET /history/operation/" + operation);
        return ResponseEntity.ok(quantityMeasurementService.getHistoryByOperation(operation));
    }

    /**
     * Returns all persisted measurement records whose first operand belongs to
     * the given measurement type.
     *
     * @param measurementType measurement category to filter by
     *                        (e.g., {@code LengthUnit}, {@code WeightUnit})
     * @return {@code 200 OK} with a list of matching {@link QuantityMeasurementDTO} records
     */
    @GetMapping("/history/type/{measurementType}")
    @Operation(summary = "Get measurement history by type",
               description = "Returns all measurement records for the specified measurement type")
    public ResponseEntity<List<QuantityMeasurementDTO>> getMeasurementHistory(
            @Parameter(description = "Measurement type — LengthUnit, WeightUnit, VolumeUnit, TemperatureUnit")
            @PathVariable String measurementType) {
        log.info("GET /history/type/" + measurementType);
        return ResponseEntity.ok(quantityMeasurementService.getHistoryByMeasurementType(measurementType));
    }

    /**
     * Returns all persisted records that represent failed (error) operations.
     *
     * @return {@code 200 OK} with a list of error {@link QuantityMeasurementDTO} records
     */
    @GetMapping("/history/errored")
    @Operation(summary = "Get error history",
               description = "Returns all measurement records that resulted in an error")
    public ResponseEntity<List<QuantityMeasurementDTO>> getErrorHistory() {
        log.info("GET /history/errored");
        return ResponseEntity.ok(quantityMeasurementService.getErrorHistory());
    }

    /**
     * Returns the count of successful (non-error) operations for the given type.
     *
     * @param operation operation type to count (e.g., {@code COMPARE}, {@code ADD})
     * @return {@code 200 OK} with the count as a {@code Long}
     */
    @GetMapping("/count/{operation}")
    @Operation(summary = "Get operation count",
               description = "Returns the count of successful operations for the specified operation type")
    public ResponseEntity<Long> getOperationCount(
            @Parameter(description = "Operation type to count — COMPARE, CONVERT, ADD, SUBTRACT, DIVIDE")
            @PathVariable String operation) {
        log.info("GET /count/" + operation);
        return ResponseEntity.ok(quantityMeasurementService.getOperationCount(operation));
    }
}
