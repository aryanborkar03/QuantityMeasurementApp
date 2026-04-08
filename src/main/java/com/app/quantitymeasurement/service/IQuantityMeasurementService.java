package com.app.quantitymeasurement.service;

import com.app.quantitymeasurement.dto.response.QuantityDTO;
import com.app.quantitymeasurement.dto.request.QuantityMeasurementDTO;

import java.util.List;


public interface IQuantityMeasurementService {

    /**
     * Compares two quantities for equality after converting both to their base unit.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @return DTO whose {@code resultString} is {@code "true"} or {@code "false"}
     */
    QuantityMeasurementDTO compare(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO);

    /**
     * Converts {@code thisQuantityDTO} to the unit specified by {@code thatQuantityDTO}.
     * The value of the second argument is ignored; only its unit and measurement type
     * are used as the conversion target.
     *
     * @param thisQuantityDTO source quantity
     * @param thatQuantityDTO target unit descriptor
     * @return DTO with the converted value in {@code resultValue}
     */
    QuantityMeasurementDTO convert(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO);

    /**
     * Adds two quantities and returns the result in the unit of the first operand.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @return DTO with the sum in {@code resultValue}
     */
    QuantityMeasurementDTO add(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO);

    /**
     * Adds two quantities and converts the result to the specified target unit.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @param targetUnitDTO   target unit for the result
     * @return DTO with the sum expressed in the target unit
     */
    QuantityMeasurementDTO add(
            QuantityDTO thisQuantityDTO,
            QuantityDTO thatQuantityDTO,
            QuantityDTO targetUnitDTO);

    /**
     * Subtracts the second quantity from the first and returns the result in
     * the unit of the first operand.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO quantity to subtract
     * @return DTO with the difference in {@code resultValue}
     */
    QuantityMeasurementDTO subtract(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO);

    /**
     * Subtracts the second quantity from the first and converts the result to
     * the specified target unit.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO quantity to subtract
     * @param targetUnitDTO   target unit for the result
     * @return DTO with the difference expressed in the target unit
     */
    QuantityMeasurementDTO subtract(
            QuantityDTO thisQuantityDTO,
            QuantityDTO thatQuantityDTO,
            QuantityDTO targetUnitDTO);

    /**
     * Divides the first quantity by the second and returns the dimensionless ratio.
     *
     * @param thisQuantityDTO dividend
     * @param thatQuantityDTO divisor
     * @return DTO with the ratio in {@code resultValue}
     */
    QuantityMeasurementDTO divide(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO);

    /**
     * Returns all persisted operation records that match the given operation type.
     *
     * @param operation operation name to filter by (e.g., {@code "compare"}, {@code "add"})
     * @return list of matching DTOs
     */
    List<QuantityMeasurementDTO> getHistoryByOperation(String operation);

    /**
     * Returns all persisted operation records whose first operand belongs to
     * the given measurement type.
     *
     * @param measurementType measurement category to filter by (e.g., {@code "LengthUnit"})
     * @return list of matching DTOs
     */
    List<QuantityMeasurementDTO> getHistoryByMeasurementType(String measurementType);

    /**
     * Returns the count of successful (non-error) operations for the given type.
     *
     * @param operation operation name to count
     * @return count of successful records
     */
    long getOperationCount(String operation);

    /**
     * Returns all persisted records that represent failed operations.
     *
     * @return list of error DTOs
     */
    List<QuantityMeasurementDTO> getErrorHistory();
}
