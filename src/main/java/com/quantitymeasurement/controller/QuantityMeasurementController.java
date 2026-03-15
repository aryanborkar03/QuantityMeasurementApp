package com.quantitymeasurement.controller;

import com.quantitymeasurement.dto.QuantityDTO;
import com.quantitymeasurement.repository.IQuantityMeasurementRepository;
import com.quantitymeasurement.repository.QuantityMeasurementCacheRepository;
import com.quantitymeasurement.service.IQuantityMeasurementService;
import com.quantitymeasurement.service.QuantityMeasurementServiceImpl;

/**
 * UC15: QuantityMeasurementController acts as the entry point
 * between user interaction and the service layer.
 *
 * This class belongs to the Controller Layer in the N-Tier
 * architecture of the Quantity Measurement Application.
 *
 * The controller is responsible for receiving input requests
 * from the Application Layer, performing minimal validation,
 * and delegating all business logic to the Service Layer.
 *
 * <p>Responsibilities of this controller include:</p>
 * <ul>
 * <li>Accepting {@link QuantityDTO} objects from the application layer</li>
 * <li>Delegating quantity operations to the service layer</li>
 * <li>Returning standardized DTO responses</li>
 * <li>Acting as an intermediary between the UI/Application and business logic</li>
 * </ul>
 *
 * <p>Architectural Role:</p>
 * Controller Layer in the N-Tier architecture.
 *
 * <p>Important Design Rule:</p>
 * This class must contain <b>NO business logic</b>.
 * All calculations, validations, and processing are delegated
 * to the Service Layer implementation.
 *
 * The controller simply orchestrates the flow between
 * the application layer and the service layer.
 *
 * <p>Operations supported by this controller:</p>
 * <ul>
 * <li>Quantity comparison</li>
 * <li>Unit conversion</li>
 * <li>Addition</li>
 * <li>Subtraction</li>
 * <li>Division</li>
 * </ul>
 *
 */
public class QuantityMeasurementController {

	/**
	 * Reference to the service layer implementation.
	 *
	 * The controller delegates all quantity measurement
	 * operations to the service layer through this interface.
	 */
	private IQuantityMeasurementService quantityMeasurementService;

	/**
	 * Constructor for initializing the controller
	 * with a service layer dependency.
	 *
	 * This enables dependency injection and keeps the
	 * controller loosely coupled from the service
	 * implementation.
	 *
	 * @param quantityMeasurementService service layer instance
	 */
	public QuantityMeasurementController(IQuantityMeasurementService quantityMeasurementService) {
		this.quantityMeasurementService = quantityMeasurementService;
	}

	/**
	 * Performs equality comparison between two quantities.
	 *
	 * The controller forwards the request to the service layer
	 * which performs the actual comparison logic.
	 *
	 * @param thisQuantityDTO first quantity
	 * @param thatQuantityDTO second quantity
	 * @return true if both quantities are equal, otherwise false
	 */
	public boolean performComparison(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
		return quantityMeasurementService.compare(thisQuantityDTO, thatQuantityDTO);
	}

	/**
	 * Performs unit conversion.
	 *
	 * The controller delegates the conversion request
	 * to the service layer.
	 *
	 * @param thisQuantityDTO source quantity
	 * @param thatQuantityDTO target unit DTO
	 * @return converted quantity DTO
	 */
	public QuantityDTO performConversion(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
		return quantityMeasurementService.convert(thisQuantityDTO, thatQuantityDTO);
	}

	/**
	 * Performs addition operation between two quantities.
	 *
	 * The addition result will be returned in the base
	 * measurement unit by default.
	 *
	 * @param thisQuantityDTO first quantity
	 * @param thatQuantityDTO second quantity
	 * @return resulting quantity after addition
	 */
	public QuantityDTO performAddition(
			QuantityDTO thisQuantityDTO,
			QuantityDTO thatQuantityDTO
	) {
		return quantityMeasurementService.add(thisQuantityDTO, thatQuantityDTO);
	}

	/**
	 * Performs addition operation between two quantities
	 * and converts the result into a specified target unit.
	 *
	 * @param thisQuantityDTO first quantity
	 * @param thatQuantityDTO second quantity
	 * @param targetUnitDTO target unit for result
	 * @return resulting quantity in target unit
	 */
	public QuantityDTO performAddition(
			QuantityDTO thisQuantityDTO,
			QuantityDTO thatQuantityDTO,
			QuantityDTO targetUnitDTO
			) {
		return quantityMeasurementService.add(thisQuantityDTO, thatQuantityDTO, targetUnitDTO);
	}
	
	/**
	 * Performs subtraction operation between two quantities.
	 *
	 * The subtraction result will be returned in the base
	 * measurement unit by default.
	 *
	 * @param thisQuantityDTO first quantity
	 * @param thatQuantityDTO second quantity
	 * @return resulting quantity after subtraction
	 */
	public QuantityDTO performSubtraction(
			QuantityDTO thisQuantityDTO,
			QuantityDTO thatQuantityDTO
			) {
		return quantityMeasurementService.subtract(thisQuantityDTO, thatQuantityDTO);
	}
	
	/**
	 * Performs subtraction operation between two quantities
	 * and converts the result into a specified target unit.
	 *
	 * @param thisQuantityDTO first quantity
	 * @param thatQuantityDTO second quantity
	 * @param targetUnitDTO target unit for result
	 * @return resulting quantity in target unit
	 */
	public QuantityDTO performSubtraction(
			QuantityDTO thisQuantityDTO,
			QuantityDTO thatQuantityDTO,
			QuantityDTO targetUnitDTO
	) {
		return quantityMeasurementService.subtract(thisQuantityDTO, thatQuantityDTO, targetUnitDTO);
	}

	/**
	 * Performs division operation between two quantities.
	 *
	 * The controller forwards the division request
	 * to the service layer which performs the actual
	 * calculation.
	 *
	 * @param thisQuantityDTO first quantity
	 * @param thatQuantityDTO second quantity
	 * @return division result as a numeric value
	 */
	public double performDivision(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
		return quantityMeasurementService.divide(thisQuantityDTO, thatQuantityDTO);
	}

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
	 * @param thisQuantityDTO source quantity
	 * @param targetQuantityDTO target unit DTO
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
	 * Demonstrates an addition operation with explicit target unit
	 * and formatted console output.
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
			System.out.println("Error: Cannot perform arithmetic between different measurement categories: "
					+ thisQuantityDTO.getMeasurementType() + " and " + thatQuantityDTO.getMeasurementType());
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
	
	/**
	 * Main method used for testing the functionality
	 * of the QuantityMeasurementController.
	 *
	 * This method demonstrates how the controller interacts
	 * with the repository and service layers to perform
	 * various quantity measurement operations.
	 *
	 * The following operations are demonstrated:
	 * 1. Quantity comparison
	 * 2. Unit conversion
	 * 3. Addition
	 * 4. Addition with target unit
	 * 5. Subtraction
	 * 6. Subtraction with target unit
	 * 7. Division
	 *
	 * This method serves as a simple test harness
	 * for validating the controller functionality.
	 *
	 * @param args command line arguments
	 */
	// Main method for testing purposes
	// Main method for testing purposes
	public static void main(String[] args) {

	    System.out.println("---- Quantity Measurement Controller Test ----");

	    // Create repository
	    IQuantityMeasurementRepository repository =
	            QuantityMeasurementCacheRepository.getInstance();

	    // Create service
	    IQuantityMeasurementService service =
	            new QuantityMeasurementServiceImpl(repository);

	    // Create controller
	    QuantityMeasurementController controller =
	            new QuantityMeasurementController(service);

	    // -----------------------------
	    // Test Data
	    // -----------------------------

	    QuantityDTO q1 =
	            new QuantityDTO(2, QuantityDTO.LengthUnit.FEET);

	    QuantityDTO q2 =
	            new QuantityDTO(24, QuantityDTO.LengthUnit.INCHES);

	    QuantityDTO target =
	            new QuantityDTO(0, QuantityDTO.LengthUnit.YARDS);

	    // -----------------------------
	    // Comparison
	    // -----------------------------

	    boolean result =
	            controller.performComparison(q1, q2);

	    System.out.println("Comparison Result: " + result);

	    // -----------------------------
	    // Conversion
	    // -----------------------------

	    QuantityDTO converted =
	            controller.performConversion(q2, target);

	    System.out.println("Conversion Result: " + converted);

	    // -----------------------------
	    // Addition (default unit)
	    // -----------------------------

	    QuantityDTO added =
	            controller.performAddition(q1, q2);

	    System.out.println("Addition Result: " + added);

	    // -----------------------------
	    // Addition (target unit)
	    // -----------------------------

	    QuantityDTO addedTarget =
	            controller.performAddition(q1, q2, target);

	    System.out.println("Addition with Target Unit Result: " + addedTarget);

	    // -----------------------------
	    // Subtraction (default unit)
	    // -----------------------------

	    QuantityDTO subtracted =
	            controller.performSubtraction(q1, q2);

	    System.out.println("Subtraction Result: " + subtracted);

	    // -----------------------------
	    // Subtraction (target unit)
	    // -----------------------------

	    QuantityDTO subtractedTarget =
	            controller.performSubtraction(q1, q2, target);

	    System.out.println("Subtraction with Target Unit Result: " + subtractedTarget);

	    // -----------------------------
	    // Division
	    // -----------------------------

	    double division =
	            controller.performDivision(q1, q2);

	    System.out.println("Division Result: " + division);

	    System.out.println("---- Test Completed ----");
	}
}