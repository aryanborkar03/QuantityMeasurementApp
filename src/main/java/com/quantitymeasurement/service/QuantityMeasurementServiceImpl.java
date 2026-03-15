
package com.quantitymeasurement.service;

import java.util.function.DoubleBinaryOperator;

import com.quantitymeasurement.dto.QuantityDTO;
import com.quantitymeasurement.dto.QuantityDTO.IMeasurableUnit;
import com.quantitymeasurement.entity.QuantityMeasurementEntity;
import com.quantitymeasurement.units.IMeasurable;
import com.quantitymeasurement.model.QuantityModel;
import com.quantitymeasurement.repository.IQuantityMeasurementRepository;

/**
 * QuantityMeasurementServiceImpl
 *
 * Service layer implementation responsible for executing
 * quantity measurement business operations.
 *
 * This class implements the {@link IQuantityMeasurementService}
 * interface and contains the core logic for:
 * <ul>
 * <li>Quantity comparison</li>
 * <li>Unit conversion</li>
 * <li>Addition</li>
 * <li>Subtraction</li>
 * <li>Division</li>
 * </ul>
 *
 * The service layer acts as the business logic layer in the
 * N-Tier architecture and interacts with the repository layer
 * to store operation results.
 *
 * Internally the service converts {@link QuantityDTO} objects
 * into {@link QuantityModel} objects to perform calculations
 * using measurable unit implementations.
 *
 */
public class QuantityMeasurementServiceImpl implements IQuantityMeasurementService {

	private IQuantityMeasurementRepository repository;

	/**
	 * Constructs the service implementation with the specified repository.
	 *
	 * @param repository repository responsible for storing measurement entities
	 */
	public QuantityMeasurementServiceImpl(IQuantityMeasurementRepository repository) {
		this.repository = repository;
	}

	/**
	 * Enumeration representing supported operation types.
	 */
	private enum Operation {
	    COMPARE,
	    CONVERT,
	    ADD,
	    SUBTRACT,
	    DIVIDE
	}

	/**
	 * Enumeration representing arithmetic operations.
	 */
	private enum ArithmeticOperation {
		ADD,
		SUBTRACT,
		DIVIDE
	}

	/**
	 * Compares two quantities.
	 *
	 * @param thisQuantityDTO first quantity
	 * @param thatQuantityDTO second quantity
	 * @return true if quantities are equal
	 */
	@Override
	public boolean compare(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {

		QuantityModel<IMeasurable> q1 = getQuantityModel(thisQuantityDTO);
		QuantityModel<IMeasurable> q2 = getQuantityModel(thatQuantityDTO);

		// Cross category validation
	    if (!q1.getUnit().getMeasurementType()
	            .equals(q2.getUnit().getMeasurementType())) {

	        throw new IllegalArgumentException(
	                "Cannot compare different measurement categories");
	    }
		
		boolean result = compare(q1, q2);

		repository.save(
				new QuantityMeasurementEntity(
						q1,
						q2,
						Operation.COMPARE.name(),
						result ? "Equal" : "Not Equal"));

		return result;
	}

	/**
	 * Performs comparison using base unit values.
	 *
	 * @param q1 first quantity
	 * @param q2 second quantity
	 * @param <U> measurable unit type
	 * @return comparison result
	 */
	private <U extends IMeasurable> boolean compare(
			QuantityModel<U> q1,
			QuantityModel<U> q2) {

		double base1 = q1.getUnit().convertToBaseUnit(q1.getValue());
		double base2 = q2.getUnit().convertToBaseUnit(q2.getValue());

		return Double.compare(base1, base2) == 0;
	}

	/**
	 * Converts a quantity into a target unit.
	 *
	 * @param thisQuantityDTO source quantity
	 * @param targetUnitDTO target unit DTO
	 * @return converted quantity DTO
	 */
	@Override
	public QuantityDTO convert(QuantityDTO thisQuantityDTO, QuantityDTO targetUnitDTO) {

		QuantityModel<IMeasurable> source = getQuantityModel(thisQuantityDTO);
		QuantityModel<IMeasurable> target = getQuantityModel(targetUnitDTO);

		double baseValue = source.getUnit().convertToBaseUnit(source.getValue());
		double result;

		if (source.getUnit() instanceof com.quantitymeasurement.units.TemperatureUnit) {
			result = convertTemperatureUnit(source, target.getUnit());
		} else {
			result = target.getUnit().convertFromBaseUnit(baseValue);
		}

		QuantityModel<IMeasurable> resultModel =
				new QuantityModel<>(result, target.getUnit());

		repository.save(
				new QuantityMeasurementEntity(
						source,
						source,
						Operation.CONVERT.name(),
						resultModel));

		return getQuantityDTO(resultModel);
	}

	/**
	 * Handles temperature conversions which require special logic.
	 *
	 * @param source source quantity
	 * @param targetUnit target unit
	 * @param <U> measurable unit type
	 * @return converted value
	 */
	private <U extends IMeasurable> double convertTemperatureUnit(
			QuantityModel<U> source,
			U targetUnit) {

		double base = source.getUnit().convertToBaseUnit(source.getValue());
		return targetUnit.convertFromBaseUnit(base);
	}

	/**
	 * Adds two quantities.
	 *
	 * @param thisQuantityDTO first quantity
	 * @param thatQuantityDTO second quantity
	 * @return result quantity DTO
	 */
	@Override
	public QuantityDTO add(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {

		return add(thisQuantityDTO, thatQuantityDTO, thisQuantityDTO);
	}

	/**
	 * Adds two quantities and converts the result into a target unit.
	 *
	 * @param thisQuantityDTO first quantity
	 * @param thatQuantityDTO second quantity
	 * @param targetUnitDTO target unit
	 * @return result quantity DTO
	 */
	@Override
	public QuantityDTO add(
			QuantityDTO thisQuantityDTO,
			QuantityDTO thatQuantityDTO,
			QuantityDTO targetUnitDTO) {

		QuantityModel<IMeasurable> q1 = getQuantityModel(thisQuantityDTO);
		QuantityModel<IMeasurable> q2 = getQuantityModel(thatQuantityDTO);
		QuantityModel<IMeasurable> target = getQuantityModel(targetUnitDTO);

		validateArithmeticOperands(q1, q2, target.getUnit(), true);

		double resultBase = performArithmetic(q1, q2, ArithmeticOperation.ADD);
		double result = target.getUnit().convertFromBaseUnit(resultBase);

		QuantityModel<IMeasurable> resultModel =
				new QuantityModel<>(result, target.getUnit());

		repository.save(
				new QuantityMeasurementEntity(
						q1,
						q2,
						Operation.ADD.name(),
						resultModel));

		return getQuantityDTO(resultModel);
	}

	/**
	 * Subtracts two quantities.
	 *
	 * @param thisQuantityDTO first quantity
	 * @param thatQuantityDTO second quantity
	 * @return result quantity DTO
	 */
	@Override
	public QuantityDTO subtract(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {

		return subtract(thisQuantityDTO, thatQuantityDTO, thisQuantityDTO);
	}

	/**
	 * Subtracts two quantities and converts the result
	 * into a target unit.
	 *
	 * @param thisQuantityDTO first quantity
	 * @param thatQuantityDTO second quantity
	 * @param targetUnitDTO target unit
	 * @return result quantity DTO
	 */
	@Override
	public QuantityDTO subtract(
			QuantityDTO thisQuantityDTO,
			QuantityDTO thatQuantityDTO,
			QuantityDTO targetUnitDTO) {

		QuantityModel<IMeasurable> q1 = getQuantityModel(thisQuantityDTO);
		QuantityModel<IMeasurable> q2 = getQuantityModel(thatQuantityDTO);
		QuantityModel<IMeasurable> target = getQuantityModel(targetUnitDTO);

		validateArithmeticOperands(q1, q2, target.getUnit(), true);

		double resultBase = performArithmetic(q1, q2, ArithmeticOperation.SUBTRACT);
		double result = target.getUnit().convertFromBaseUnit(resultBase);

		QuantityModel<IMeasurable> resultModel =
				new QuantityModel<>(result, target.getUnit());

		repository.save(
				new QuantityMeasurementEntity(
						q1,
						q2,
						Operation.SUBTRACT.name(),
						resultModel));

		return getQuantityDTO(resultModel);
	}

	/**
	 * Divides two quantities and returns the ratio.
	 *
	 * @param thisQuantityDTO first quantity
	 * @param thatQuantityDTO second quantity
	 * @return division result
	 */
	@Override
	public double divide(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {

		QuantityModel<IMeasurable> q1 = getQuantityModel(thisQuantityDTO);
		QuantityModel<IMeasurable> q2 = getQuantityModel(thatQuantityDTO);

		validateArithmeticOperands(q1, q2, null, false);

		double result = performArithmetic(q1, q2, ArithmeticOperation.DIVIDE);

		repository.save(
				new QuantityMeasurementEntity(
						q1,
						q2,
						Operation.DIVIDE.name(),
						String.valueOf(result)));

		return result;
	}

	/**
	 * Converts DTO into model representation.
	 *
	 * @param quantity DTO quantity
	 * @return quantity model
	 */
	private QuantityModel<IMeasurable> getQuantityModel(QuantityDTO quantity) {

	    if (quantity == null)
	        throw new IllegalArgumentException("QuantityDTO cannot be null");

	    IMeasurable unit =
	            getModelUnit(quantity.getMeasurementType(), quantity.getUnit());

	    return new QuantityModel<>(
	            quantity.getValue(),
	            unit
	    );
	}

	/**
	 * Converts a model object into DTO representation.
	 *
	 * @param quantity model quantity
	 * @return DTO quantity
	 */
	private QuantityDTO getQuantityDTO(QuantityModel<IMeasurable> quantity) {

	    if (quantity == null)
	        throw new IllegalArgumentException("QuantityModel cannot be null");

	    IMeasurableUnit dtoUnit =
	            getDTOUnit(
	                    quantity.getUnit().getMeasurementType(),
	                    quantity.getUnit().getUnitName()
	            );

	    return new QuantityDTO(
	            quantity.getValue(),
	            dtoUnit
	    );
	}

	/**
	 * Retrieves the model unit implementation
	 * based on measurement type.
	 */
	private IMeasurable getModelUnit(String measurementType, String unit) {

	    switch (measurementType) {

	        case "LengthUnit":
	            return com.quantitymeasurement.units.LengthUnit.valueOf(unit);

	        case "WeightUnit":
	            return com.quantitymeasurement.units.WeightUnit.valueOf(unit);

	        case "VolumeUnit":
	            return com.quantitymeasurement.units.VolumeUnit.valueOf(unit);

	        case "TemperatureUnit":
	            return com.quantitymeasurement.units.TemperatureUnit.valueOf(unit);

	        default:
	            throw new IllegalArgumentException(
	                    "Unsupported measurement type: " + measurementType);
	    }
	}

	/**
	 * Retrieves the DTO unit enumeration
	 * based on measurement type.
	 */
	private IMeasurableUnit getDTOUnit(String measurementType, String unit) {

	    switch (measurementType) {

	        case "LengthUnit":
	            return QuantityDTO.LengthUnit.valueOf(unit);

	        case "WeightUnit":
	            return QuantityDTO.WeightUnit.valueOf(unit);

	        case "VolumeUnit":
	            return QuantityDTO.VolumeUnit.valueOf(unit);

	        case "TemperatureUnit":
	            return QuantityDTO.TemperatureUnit.valueOf(unit);

	        default:
	            throw new IllegalArgumentException(
	                    "Unsupported measurement type: " + measurementType);
	    }
	}

	/**
	 * Validates arithmetic operands before performing
	 * arithmetic operations.
	 */
	private <U extends IMeasurable> void validateArithmeticOperands(
	        QuantityModel<U> q1,
	        QuantityModel<U> q2,
	        U targetUnit,
	        boolean targetRequired) {

	    if (q1 == null || q2 == null)
	        throw new IllegalArgumentException("Operands cannot be null");

	    String type1 = q1.getUnit().getMeasurementType();
	    String type2 = q2.getUnit().getMeasurementType();

	    // Cross category check
	    if (!type1.equals(type2)) {
	        throw new IllegalArgumentException(
	                "Cannot perform arithmetic on different measurement categories");
	    }

	    // Temperature arithmetic NOT allowed
	    if (type1.equals("TemperatureUnit")) {
	        throw new UnsupportedOperationException(
	                "Arithmetic operations are not supported for temperature units");
	    }

	    if (targetRequired && targetUnit == null)
	        throw new IllegalArgumentException("Target unit required");
	}

	/**
	 * Performs arithmetic operations after converting
	 * operands into base units.
	 */
	private <U extends IMeasurable> double performArithmetic(
			QuantityModel<U> q1,
			QuantityModel<U> q2,
			ArithmeticOperation operation) {

		double base1 = q1.getUnit().convertToBaseUnit(q1.getValue());
		double base2 = q2.getUnit().convertToBaseUnit(q2.getValue());

		if (operation == ArithmeticOperation.DIVIDE && base2 == 0) {
	        throw new ArithmeticException("Division by zero is not allowed");
	    }

		DoubleBinaryOperator op;

		switch (operation) {

		case ADD:
			op = (a, b) -> a + b;
			break;

		case SUBTRACT:
			op = (a, b) -> a - b;
			break;

		case DIVIDE:
			op = (a, b) -> a / b;
			break;

		default:
			throw new IllegalArgumentException("Invalid arithmetic operation");
		}

		return op.applyAsDouble(base1, base2);
	}

	/**
	 * Entry point for manual testing of the
	 * QuantityMeasurementServiceImpl functionality.
	 *
	 * This method demonstrates how the service layer
	 * performs different quantity measurement operations
	 * including comparison, conversion, arithmetic
	 * calculations, and repository storage.
	 *
	 * The following operations are tested:
	 * <ul>
	 * <li>Quantity comparison</li>
	 * <li>Unit conversion</li>
	 * <li>Addition</li>
	 * <li>Addition with target unit</li>
	 * <li>Subtraction</li>
	 * <li>Subtraction with target unit</li>
	 * <li>Division</li>
	 * </ul>
	 *
	 * It also prints all stored measurement entities
	 * saved in the repository during execution.
	 *
	 * @param args command line arguments
	 */
	public static void main(String[] args) {

	    System.out.println("---- Testing QuantityMeasurementServiceImpl ----");

	    /*
	     * Initialize repository using Singleton pattern.
	     * The repository stores all measurement entities
	     * generated by service operations.
	     */
	    IQuantityMeasurementRepository repository =
	            com.quantitymeasurement.repository
	                    .QuantityMeasurementCacheRepository.getInstance();

	    /*
	     * Create the service instance by injecting the repository.
	     * The service layer performs all business logic operations.
	     */
	    QuantityMeasurementServiceImpl service =
	            new QuantityMeasurementServiceImpl(repository);

	    /*
	     * Test data used for service operations.
	     * q1 and q2 represent two measurable quantities.
	     * target represents the desired result unit.
	     */
	    QuantityDTO q1 =
	            new QuantityDTO(2, QuantityDTO.LengthUnit.FEET);

	    QuantityDTO q2 =
	            new QuantityDTO(24, QuantityDTO.LengthUnit.INCHES);

	    QuantityDTO target =
	            new QuantityDTO(0, QuantityDTO.LengthUnit.YARDS);

	    /*
	     * Perform comparison operation between two quantities.
	     */
	    boolean comparison =
	            service.compare(q1, q2);

	    System.out.println("Comparison Result: " + comparison);

	    /*
	     * Perform unit conversion.
	     */
	    QuantityDTO converted =
	            service.convert(q2, target);

	    System.out.println("Conversion Result: " + converted);

	    /*
	     * Perform addition of two quantities.
	     */
	    QuantityDTO added =
	            service.add(q1, q2);

	    System.out.println("Addition Result: " + added);

	    /*
	     * Perform addition with result converted to a target unit.
	     */
	    QuantityDTO addedTarget =
	            service.add(q1, q2, target);

	    System.out.println("Addition with Target Unit Result: " + addedTarget);

	    /*
	     * Perform subtraction operation.
	     */
	    QuantityDTO subtracted =
	            service.subtract(q1, q2);

	    System.out.println("Subtraction Result: " + subtracted);

	    /*
	     * Perform subtraction with result converted to a target unit.
	     */
	    QuantityDTO subtractedTarget =
	            service.subtract(q1, q2, target);

	    System.out.println("Subtraction with Target Unit Result: " + subtractedTarget);

	    /*
	     * Perform division operation between quantities.
	     */
	    double division =
	            service.divide(q1, q2);

	    System.out.println("Division Result: " + division);

	    /*
	     * Print all stored measurement records from the repository.
	     */
	    System.out.println("\n---- Stored Measurements ----");

	    repository.getAllMeasurements()
	              .forEach(System.out::println);

	    System.out.println("\n---- Service Testing Complete ----");
	}
}
