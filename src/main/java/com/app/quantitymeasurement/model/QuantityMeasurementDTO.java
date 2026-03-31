package com.app.quantitymeasurement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * QuantityMeasurementDTO
 *
 * API response DTO that represents the outcome of a single quantity measurement
 * operation. It mirrors the fields of {@link QuantityMeasurementEntity} but is
 * designed for API communication rather than database persistence.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuantityMeasurementDTO {

    // -------------------------------------------------------------------------
    // First operand
    // -------------------------------------------------------------------------

    /** Numeric value of the first operand. */
    private Double thisValue;

    /** Unit name of the first operand (e.g., {@code "FEET"}, {@code "KILOGRAM"}). */
    private String thisUnit;

    /** Measurement category of the first operand (e.g., {@code "LengthUnit"}). */
    private String thisMeasurementType;

    // -------------------------------------------------------------------------
    // Second operand
    // -------------------------------------------------------------------------

    /** Numeric value of the second operand. */
    private Double thatValue;

    /** Unit name of the second operand (e.g., {@code "INCHES"}, {@code "GRAM"}). */
    private String thatUnit;

    /** Measurement category of the second operand. */
    private String thatMeasurementType;

    // -------------------------------------------------------------------------
    // Operation
    // -------------------------------------------------------------------------

    /**
     * Name of the operation performed — {@code "compare"}, {@code "convert"},
     * {@code "add"}, {@code "subtract"}, or {@code "divide"}.
     */
    private String operation;

    // -------------------------------------------------------------------------
    // Result
    // -------------------------------------------------------------------------

    /**
     * String result for {@code compare} operations ({@code "true"} or {@code "false"}).
     * {@code null} for all other operations.
     */
    private String resultString;

    /**
     * Numeric result for arithmetic and conversion operations.
     * {@code 0.0} for comparison operations.
     */
    private Double resultValue;

    /**
     * Unit of the result quantity (e.g., {@code "FEET"}).
     * {@code null} for compare and divide operations.
     */
    private String resultUnit;

    /**
     * Measurement category of the result quantity.
     * {@code null} for compare and divide operations.
     */
    private String resultMeasurementType;

    // -------------------------------------------------------------------------
    // Error
    // -------------------------------------------------------------------------

    /**
     * Error message when the operation failed; {@code null} on success.
     */
    private String errorMessage;

    /**
     * {@code true} when the operation produced an error and was not completed
     * successfully.
     */
    private boolean error;

    // -------------------------------------------------------------------------
    // Static factory methods
    // -------------------------------------------------------------------------

    /**
     * Creates a {@code QuantityMeasurementDTO} from a {@link QuantityMeasurementEntity}.
     *
     * @param entity the entity to convert; returns {@code null} if {@code entity} is {@code null}
     * @return the corresponding DTO
     */
    public static QuantityMeasurementDTO fromEntity(QuantityMeasurementEntity entity) {
        if (entity == null) return null;
        return QuantityMeasurementDTO.builder()
            .thisValue(entity.getThisValue())
            .thisUnit(entity.getThisUnit())
            .thisMeasurementType(entity.getThisMeasurementType())
            .thatValue(entity.getThatValue())
            .thatUnit(entity.getThatUnit())
            .thatMeasurementType(entity.getThatMeasurementType())
            .operation(entity.getOperation())
            .resultString(entity.getResultString())
            .resultValue(entity.getResultValue() != null ? entity.getResultValue() : 0.0)
            .resultUnit(entity.getResultUnit())
            .resultMeasurementType(entity.getResultMeasurementType())
            .errorMessage(entity.getErrorMessage())
            .error(entity.isError())
            .build();
    }

    /**
     * Converts this DTO to a {@link QuantityMeasurementEntity} suitable for persistence.
     *
     * @return the corresponding entity
     */
    public QuantityMeasurementEntity toEntity() {
        QuantityMeasurementEntity entity = new QuantityMeasurementEntity();
        entity.setThisValue(this.thisValue);
        entity.setThisUnit(this.thisUnit);
        entity.setThisMeasurementType(this.thisMeasurementType);
        entity.setThatValue(this.thatValue);
        entity.setThatUnit(this.thatUnit);
        entity.setThatMeasurementType(this.thatMeasurementType);
        entity.setOperation(this.operation);
        entity.setResultString(this.resultString);
        entity.setResultValue(this.resultValue);
        entity.setResultUnit(this.resultUnit);
        entity.setResultMeasurementType(this.resultMeasurementType);
        entity.setErrorMessage(this.errorMessage);
        entity.setError(this.error);
        return entity;
    }

    /**
     * Converts a list of entities to a list of DTOs using the Stream API.
     *
     * @param entities list of entities; returns an empty list if {@code null}
     * @return list of corresponding DTOs
     */
    public static List<QuantityMeasurementDTO> fromEntityList(List<QuantityMeasurementEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream()
            .map(QuantityMeasurementDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Converts a list of DTOs to a list of entities using the Stream API.
     *
     * @param dtos list of DTOs; returns an empty list if {@code null}
     * @return list of corresponding entities
     */
    public static List<QuantityMeasurementEntity> toEntityList(List<QuantityMeasurementDTO> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream()
            .map(QuantityMeasurementDTO::toEntity)
            .collect(Collectors.toList());
    }
}
