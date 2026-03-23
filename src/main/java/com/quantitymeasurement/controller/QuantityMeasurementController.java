package com.quantitymeasurement.controller;

import java.util.logging.Logger;

import com.quantitymeasurement.entity.QuantityDTO;
import com.quantitymeasurement.service.IQuantityMeasurementService;

/**
 * QuantityMeasurementController
 *
 * Acts as the entry point between user interaction and the service layer.
 * Belongs to the Controller Layer in the N-Tier architecture.
 *
 * The controller is responsible for receiving input requests from the Application Layer,
 * performing minimal validation, and delegating all business logic to the Service Layer.
 * It must contain NO business logic of its own.
 *
 * Operations supported by this controller:
 * - Quantity comparison
 * - Unit conversion
 * - Addition (with and without target unit)
 * - Subtraction (with and without target unit)
 * - Division
 *
 * Each operation has both a raw "perform" method (returns result directly) and a
 * "demonstrate" method (prints formatted output to the console for demo purposes).
 *
 * @author Developer
 * @version 16.0
 * @since 1.0
 */
public class QuantityMeasurementController {

    /**
     * Logger for recording controller-level events.
     * Uses SLF4J-compatible java.util.logging as configured by Logback in pom.xml.
     */
    private static final Logger logger = Logger.getLogger(
        QuantityMeasurementController.class.getName()
    );

    /**
     * Reference to the service layer — all business logic is delegated here.
     * Injected via constructor to maintain loose coupling.
     */
    private IQuantityMeasurementService quantityMeasurementService;

    /**
     * Constructs the controller with the given service layer dependency.
     *
     * @param quantityMeasurementService service layer instance
     */
    public QuantityMeasurementController(IQuantityMeasurementService quantityMeasurementService) {
        this.quantityMeasurementService = quantityMeasurementService;
        logger.info("QuantityMeasurementController initialized.");
    }

    /**
     * Delegates a comparison request to the service layer.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @return true if both quantities are equal
     */
    public boolean performComparison(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        return quantityMeasurementService.compare(thisQuantityDTO, thatQuantityDTO);
    }

    /**
     * Delegates a unit conversion request to the service layer.
     *
     * @param thisQuantityDTO source quantity
     * @param thatQuantityDTO target unit DTO
     * @return converted quantity DTO
     */
    public QuantityDTO performConversion(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        return quantityMeasurementService.convert(thisQuantityDTO, thatQuantityDTO);
    }

    /**
     * Delegates an addition request to the service layer.
     * Result is returned in the same unit as the first operand.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @return addition result
     */
    public QuantityDTO performAddition(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        return quantityMeasurementService.add(thisQuantityDTO, thatQuantityDTO);
    }

    /**
     * Delegates an addition request with a target unit to the service layer.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @param targetUnitDTO   target unit for result
     * @return addition result in target unit
     */
    public QuantityDTO performAddition(
            QuantityDTO thisQuantityDTO,
            QuantityDTO thatQuantityDTO,
            QuantityDTO targetUnitDTO) {
        return quantityMeasurementService.add(thisQuantityDTO, thatQuantityDTO, targetUnitDTO);
    }

    /**
     * Delegates a subtraction request to the service layer.
     * Result is returned in the same unit as the first operand.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @return subtraction result
     */
    public QuantityDTO performSubtraction(
            QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        return quantityMeasurementService.subtract(thisQuantityDTO, thatQuantityDTO);
    }

    /**
     * Delegates a subtraction request with a target unit to the service layer.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @param targetUnitDTO   target unit for result
     * @return subtraction result in target unit
     */
    public QuantityDTO performSubtraction(
            QuantityDTO thisQuantityDTO,
            QuantityDTO thatQuantityDTO,
            QuantityDTO targetUnitDTO) {
        return quantityMeasurementService.subtract(thisQuantityDTO, thatQuantityDTO, targetUnitDTO);
    }

    /**
     * Delegates a division request to the service layer.
     *
     * @param thisQuantityDTO dividend quantity
     * @param thatQuantityDTO divisor quantity
     * @return numeric division result
     */
    public double performDivision(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        return quantityMeasurementService.divide(thisQuantityDTO, thatQuantityDTO);
    }

    /* --------------------------------------------------------------------- 
     * Demonstrate methods — formatted console output for demo/testing
     * -------------------------------------------------------------------- */

    /**
     * Demonstrates a comparison operation with formatted console output.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     */
    public void demonstrateComparison(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        System.out.println("--- Equality Demonstration ---");
        System.out.println("Operation: COMPARISON");
        System.out.println("This Quantity: " + thisQuantityDTO.getValue() + " " + thisQuantityDTO.getUnit());
        System.out.println("That Quantity: " + thatQuantityDTO.getValue() + " " + thatQuantityDTO.getUnit());
        try {
            boolean result = performComparison(thisQuantityDTO, thatQuantityDTO);
            System.out.println("Comparison Result: " + result);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Demonstrates a conversion operation with formatted console output.
     *
     * @param thisQuantityDTO     source quantity
     * @param targetQuantityDTO   target unit DTO
     */
    public void demonstrateConversion(QuantityDTO thisQuantityDTO, QuantityDTO targetQuantityDTO) {
        System.out.println("--- Conversion Demonstration ---");
        System.out.println("Operation: CONVERT");
        System.out.println("This Quantity: " + thisQuantityDTO.getValue() + " " + thisQuantityDTO.getUnit());
        System.out.println("Target Unit:   " + targetQuantityDTO.getUnit());
        try {
            QuantityDTO result = performConversion(thisQuantityDTO, targetQuantityDTO);
            System.out.println("Conversion Result: " + result.getValue() + " " + result.getUnit());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Demonstrates an addition operation with formatted console output.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     */
    public void demonstrateAddition(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        System.out.println("--- Addition Demonstration ---");
        System.out.println("Operation: ADD");
        System.out.println("This Quantity: " + thisQuantityDTO.getValue() + " " + thisQuantityDTO.getUnit());
        System.out.println("That Quantity: " + thatQuantityDTO.getValue() + " " + thatQuantityDTO.getUnit());
        try {
            QuantityDTO result = performAddition(thisQuantityDTO, thatQuantityDTO);
            System.out.println("Addition Result: " + result.getValue() + " " + result.getUnit());
        } catch (UnsupportedOperationException e) {
            System.out.println("Error: " + thisQuantityDTO.getUnit() + " does not support ADD operations.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Cannot perform arithmetic between different measurement categories: "
                + thisQuantityDTO.getMeasurementType() + " and " + thatQuantityDTO.getMeasurementType());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Demonstrates an addition operation with an explicit target unit.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @param targetUnitDTO   target unit for result
     */
    public void demonstrateAddition(
            QuantityDTO thisQuantityDTO,
            QuantityDTO thatQuantityDTO,
            QuantityDTO targetUnitDTO) {
        System.out.println("--- Addition Demonstration (with Target Unit) ---");
        System.out.println("Operation: ADD");
        System.out.println("This Quantity: " + thisQuantityDTO.getValue() + " " + thisQuantityDTO.getUnit());
        System.out.println("That Quantity: " + thatQuantityDTO.getValue() + " " + thatQuantityDTO.getUnit());
        System.out.println("Target Unit:   " + targetUnitDTO.getUnit());
        try {
            QuantityDTO result = performAddition(thisQuantityDTO, thatQuantityDTO, targetUnitDTO);
            System.out.println("Addition Result: " + result.getValue() + " " + result.getUnit());
        } catch (UnsupportedOperationException e) {
            System.out.println("Error: " + thisQuantityDTO.getUnit() + " does not support ADD operations.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Cannot perform arithmetic between different measurement categories: "
                + thisQuantityDTO.getMeasurementType() + " and " + thatQuantityDTO.getMeasurementType());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Demonstrates a subtraction operation with formatted console output.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     */
    public void demonstrateSubtraction(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        System.out.println("--- Subtraction Demonstration ---");
        System.out.println("Operation: SUBTRACT");
        System.out.println("This Quantity: " + thisQuantityDTO.getValue() + " " + thisQuantityDTO.getUnit());
        System.out.println("That Quantity: " + thatQuantityDTO.getValue() + " " + thatQuantityDTO.getUnit());
        try {
            QuantityDTO result = performSubtraction(thisQuantityDTO, thatQuantityDTO);
            System.out.println("Subtraction Result: " + result.getValue() + " " + result.getUnit());
        } catch (UnsupportedOperationException e) {
            System.out.println("Error: " + thisQuantityDTO.getUnit() + " does not support SUBTRACT operations.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Cannot perform arithmetic between different measurement categories.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Demonstrates a division operation with formatted console output.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     */
    public void demonstrateDivision(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        System.out.println("--- Division Demonstration ---");
        System.out.println("Operation: DIVIDE");
        System.out.println("This Quantity: " + thisQuantityDTO.getValue() + " " + thisQuantityDTO.getUnit());
        System.out.println("That Quantity: " + thatQuantityDTO.getValue() + " " + thatQuantityDTO.getUnit());
        try {
            double result = performDivision(thisQuantityDTO, thatQuantityDTO);
            System.out.println("Division Result: " + result);
        } catch (ArithmeticException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (UnsupportedOperationException e) {
            System.out.println("Error: " + thisQuantityDTO.getUnit() + " does not support DIVIDE operations.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}