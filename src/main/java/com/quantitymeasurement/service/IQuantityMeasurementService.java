package com.quantitymeasurement.service;

import com.quantitymeasurement.dto.QuantityDTO;

/**
 * IQuantityMeasurementService
 *
 * Service interface defining the business operations for
 * quantity measurement processing.
 *
 * This interface acts as the Service Layer contract in the
 * N-Tier architecture of the Quantity Measurement system.
 *
 * The service layer is responsible for implementing the
 * business logic for quantity operations while keeping
 * the controller layer independent of implementation details.
 *
 * Operations supported by this service include:
 * <ul>
 * <li>Quantity comparison</li>
 * <li>Unit conversion</li>
 * <li>Addition</li>
 * <li>Subtraction</li>
 * <li>Division</li>
 * </ul>
 *
 * The service methods operate on {@link QuantityDTO} objects
 * which act as data transfer objects between application layers.
 */
public interface IQuantityMeasurementService {

    /**
     * Compares two quantities for equality.
     *
     * The comparison is performed after converting both
     * quantities to their respective base units.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @return true if quantities are equal, otherwise false
     */
    boolean compare(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO);

    /**
     * Converts a quantity from its current unit
     * to the specified target unit.
     *
     * @param thisQuantityDTO source quantity
     * @param thatQuantityDTO target unit DTO
     * @return converted quantity DTO
     */
    QuantityDTO convert(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO);

    /**
     * Adds two quantities and returns the result
     * in the unit of the first quantity.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @return resulting quantity DTO
     */
    QuantityDTO add(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO);

    /**
     * Adds two quantities and converts the result
     * into a specified target unit.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @param targetUnitDTO target unit DTO
     * @return resulting quantity DTO
     */
    QuantityDTO add(
            QuantityDTO thisQuantityDTO,
            QuantityDTO thatQuantityDTO,
            QuantityDTO targetUnitDTO
    );

    /**
     * Subtracts one quantity from another and returns
     * the result in the unit of the first quantity.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @return resulting quantity DTO
     */
    QuantityDTO subtract(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO);

    /**
     * Subtracts quantities and converts the result
     * into a specified target unit.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @param targetUnitDTO target unit DTO
     * @return resulting quantity DTO
     */
    QuantityDTO subtract(
            QuantityDTO thisQuantityDTO,
            QuantityDTO thatQuantityDTO,
            QuantityDTO targetUnitDTO
    );

    /**
     * Divides one quantity by another and returns
     * the resulting numeric ratio.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @return division result
     */
    double divide(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO);

}