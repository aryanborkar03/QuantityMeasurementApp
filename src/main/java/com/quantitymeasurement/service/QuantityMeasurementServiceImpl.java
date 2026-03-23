package com.quantitymeasurement.service;

import java.util.function.DoubleBinaryOperator;
import java.util.logging.Logger;

import com.quantitymeasurement.entity.QuantityDTO;
import com.quantitymeasurement.entity.QuantityDTO.IMeasurableUnit;
import com.quantitymeasurement.entity.QuantityMeasurementEntity;
import com.quantitymeasurement.entity.QuantityModel;
import com.quantitymeasurement.units.IMeasurable;
import com.quantitymeasurement.repository.IQuantityMeasurementRepository;

/**
 * QuantityMeasurementServiceImpl
 *
 * Service layer implementation responsible for executing quantity measurement business
 * operations. Implements IQuantityMeasurementService and contains the core logic for:
 * - Quantity comparison
 * - Unit conversion
 * - Addition
 * - Subtraction
 * - Division
 *
 * The service layer acts as the business logic layer in the N-Tier architecture and
 * interacts with the repository layer (injected via constructor) to store operation
 * results. Since the repository is injected through the interface, the service can
 * work seamlessly with both QuantityMeasurementCacheRepository and
 * QuantityMeasurementDatabaseRepository — no changes to service logic are needed.
 *
 * Internally the service converts QuantityDTO objects into QuantityModel objects to
 * perform calculations using measurable unit implementations.
 *
 * @author Developer
 * @version 16.0
 * @since 1.0
 */
public class QuantityMeasurementServiceImpl implements IQuantityMeasurementService {

    /**
     * Logger for recording service operations and errors.
     */
    private static final Logger logger = Logger.getLogger(
        QuantityMeasurementServiceImpl.class.getName()
    );

    /**
     * Repository used for persisting measurement operation results.
     * Injected at construction time — supports both cache and database repositories.
     */
    private IQuantityMeasurementRepository repository;

    /**
     * Constructs the service implementation with the specified repository.
     *
     * Dependency Injection pattern: the caller supplies the repository implementation,
     * so the service never creates its own persistence layer.
     *
     * @param repository repository responsible for storing measurement entities
     */
    public QuantityMeasurementServiceImpl(IQuantityMeasurementRepository repository) {
        this.repository = repository;
        logger.info("QuantityMeasurementServiceImpl initialized with repository: "
            + repository.getClass().getSimpleName());
    }

    /**
     * Enumeration representing supported operation types for entity recording.
     */
    private enum Operation {
        COMPARE, CONVERT, ADD, SUBTRACT, DIVIDE
    }

    /**
     * Enumeration representing arithmetic operations for internal dispatch.
     */
    private enum ArithmeticOperation {
        ADD, SUBTRACT, DIVIDE
    }

    /**
     * Compares two quantities for equality by converting both to their base units
     * and comparing within a floating-point tolerance.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @return true if both quantities represent the same value
     * @throws IllegalArgumentException if the quantities belong to different categories
     */
    @Override
    public boolean compare(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        QuantityModel<IMeasurable> q1 = getQuantityModel(thisQuantityDTO);
        QuantityModel<IMeasurable> q2 = getQuantityModel(thatQuantityDTO);

        /*
         * Cross-category check — cannot compare, e.g., length against weight.
         */
        if (!q1.getUnit().getMeasurementType().equals(q2.getUnit().getMeasurementType())) {
            throw new IllegalArgumentException(
                "Cannot compare different measurement categories");
        }

        boolean result = compareBaseValues(q1, q2);

        /*
         * Persist the operation result so it becomes part of the measurement history.
         */
        repository.save(new QuantityMeasurementEntity(
            q1, q2, Operation.COMPARE.name(), result ? "Equal" : "Not Equal"
        ));

        logger.fine("COMPARE: " + q1 + " vs " + q2 + " => " + result);
        return result;
    }

    /**
     * Compares two quantities using their base unit values.
     *
     * @param q1 first quantity
     * @param q2 second quantity
     * @return true if base values are equal within 1e-6 tolerance
     */
    private <U extends IMeasurable> boolean compareBaseValues(
            QuantityModel<U> q1, QuantityModel<U> q2) {
        double base1 = q1.getUnit().convertToBaseUnit(q1.getValue());
        double base2 = q2.getUnit().convertToBaseUnit(q2.getValue());
        return Double.compare(base1, base2) == 0;
    }

    /**
     * Converts a quantity from its current unit to the specified target unit.
     *
     * Temperature conversions use non-linear formulas; all other conversions use
     * the base-unit pivot approach.
     *
     * @param thisQuantityDTO source quantity
     * @param targetUnitDTO   DTO whose unit specifies the conversion target
     * @return new QuantityDTO with the converted value in the target unit
     */
    @Override
    public QuantityDTO convert(QuantityDTO thisQuantityDTO, QuantityDTO targetUnitDTO) {
        QuantityModel<IMeasurable> source = getQuantityModel(thisQuantityDTO);
        QuantityModel<IMeasurable> target = getQuantityModel(targetUnitDTO);

        double result;
        if (source.getUnit() instanceof com.quantitymeasurement.units.TemperatureUnit) {
            /*
             * Temperature requires its own conversion path because the formula is
             * non-linear (e.g., Fahrenheit → Celsius is not a simple multiply).
             */
            result = convertTemperatureUnit(source, target.getUnit());
        } else {
            double baseValue = source.getUnit().convertToBaseUnit(source.getValue());
            result = target.getUnit().convertFromBaseUnit(baseValue);
        }

        QuantityModel<IMeasurable> resultModel = new QuantityModel<>(result, target.getUnit());

        repository.save(new QuantityMeasurementEntity(
            source, source, Operation.CONVERT.name(), resultModel
        ));

        logger.fine("CONVERT: " + source + " => " + resultModel);
        return getQuantityDTO(resultModel);
    }

    /**
     * Handles temperature conversions via base-unit (Celsius) pivot.
     *
     * @param source     source quantity in some temperature unit
     * @param targetUnit the desired target temperature unit
     * @return converted temperature value
     */
    private <U extends IMeasurable> double convertTemperatureUnit(
            QuantityModel<U> source, U targetUnit) {
        double base = source.getUnit().convertToBaseUnit(source.getValue());
        return targetUnit.convertFromBaseUnit(base);
    }

    /**
     * Adds two quantities and returns the result in the unit of the first quantity.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @return result in the same unit as the first operand
     */
    @Override
    public QuantityDTO add(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        return add(thisQuantityDTO, thatQuantityDTO, thisQuantityDTO);
    }

    /**
     * Adds two quantities and converts the result to the specified target unit.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @param targetUnitDTO   target unit for the result
     * @return result in the target unit
     * @throws IllegalArgumentException       if the operands are from different categories
     * @throws UnsupportedOperationException  if the measurement type does not support arithmetic
     */
    @Override
    public QuantityDTO add(
            QuantityDTO thisQuantityDTO,
            QuantityDTO thatQuantityDTO,
            QuantityDTO targetUnitDTO) {

        QuantityModel<IMeasurable> q1     = getQuantityModel(thisQuantityDTO);
        QuantityModel<IMeasurable> q2     = getQuantityModel(thatQuantityDTO);
        QuantityModel<IMeasurable> target = getQuantityModel(targetUnitDTO);

        validateArithmeticOperands(q1, q2, target.getUnit(), true);

        double resultBase = performArithmetic(q1, q2, ArithmeticOperation.ADD);
        double result     = target.getUnit().convertFromBaseUnit(resultBase);

        QuantityModel<IMeasurable> resultModel = new QuantityModel<>(result, target.getUnit());

        repository.save(new QuantityMeasurementEntity(
            q1, q2, Operation.ADD.name(), resultModel
        ));

        logger.fine("ADD: " + q1 + " + " + q2 + " => " + resultModel);
        return getQuantityDTO(resultModel);
    }

    /**
     * Subtracts the second quantity from the first and returns the result in the unit
     * of the first quantity.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @return result in the same unit as the first operand
     */
    @Override
    public QuantityDTO subtract(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        return subtract(thisQuantityDTO, thatQuantityDTO, thisQuantityDTO);
    }

    /**
     * Subtracts the second quantity from the first and converts the result to the target unit.
     *
     * @param thisQuantityDTO first quantity
     * @param thatQuantityDTO second quantity
     * @param targetUnitDTO   target unit for the result
     * @return result in the target unit
     */
    @Override
    public QuantityDTO subtract(
            QuantityDTO thisQuantityDTO,
            QuantityDTO thatQuantityDTO,
            QuantityDTO targetUnitDTO) {

        QuantityModel<IMeasurable> q1     = getQuantityModel(thisQuantityDTO);
        QuantityModel<IMeasurable> q2     = getQuantityModel(thatQuantityDTO);
        QuantityModel<IMeasurable> target = getQuantityModel(targetUnitDTO);

        validateArithmeticOperands(q1, q2, target.getUnit(), true);

        double resultBase = performArithmetic(q1, q2, ArithmeticOperation.SUBTRACT);
        double result     = target.getUnit().convertFromBaseUnit(resultBase);

        QuantityModel<IMeasurable> resultModel = new QuantityModel<>(result, target.getUnit());

        repository.save(new QuantityMeasurementEntity(
            q1, q2, Operation.SUBTRACT.name(), resultModel
        ));

        logger.fine("SUBTRACT: " + q1 + " - " + q2 + " => " + resultModel);
        return getQuantityDTO(resultModel);
    }

    /**
     * Divides the first quantity by the second and returns the numeric ratio.
     *
     * @param thisQuantityDTO dividend quantity
     * @param thatQuantityDTO divisor quantity
     * @return division result as a double
     * @throws ArithmeticException if the divisor base value is zero
     */
    @Override
    public double divide(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        QuantityModel<IMeasurable> q1 = getQuantityModel(thisQuantityDTO);
        QuantityModel<IMeasurable> q2 = getQuantityModel(thatQuantityDTO);

        validateArithmeticOperands(q1, q2, null, false);

        double result = performArithmetic(q1, q2, ArithmeticOperation.DIVIDE);

        repository.save(new QuantityMeasurementEntity(
            q1, q2, Operation.DIVIDE.name(), String.valueOf(result)
        ));

        logger.fine("DIVIDE: " + q1 + " / " + q2 + " => " + result);
        return result;
    }

    /* -----------------------------------------------------------------------
     * Internal helpers
     * --------------------------------------------------------------------- */

    /**
     * Converts a QuantityDTO into an internal QuantityModel with the appropriate unit.
     *
     * @param quantity incoming DTO
     * @return QuantityModel populated with value and unit
     * @throws IllegalArgumentException if quantity is null or unit type is unsupported
     */
    private QuantityModel<IMeasurable> getQuantityModel(QuantityDTO quantity) {
        if (quantity == null)
            throw new IllegalArgumentException("QuantityDTO cannot be null");

        IMeasurable unit = getModelUnit(quantity.getMeasurementType(), quantity.getUnit());
        return new QuantityModel<>(quantity.getValue(), unit);
    }

    /**
     * Converts an internal QuantityModel back into a QuantityDTO for the caller.
     *
     * @param quantity internal model
     * @return DTO with the same value and unit information
     */
    private QuantityDTO getQuantityDTO(QuantityModel<IMeasurable> quantity) {
        if (quantity == null)
            throw new IllegalArgumentException("QuantityModel cannot be null");

        IMeasurableUnit dtoUnit = getDTOUnit(
            quantity.getUnit().getMeasurementType(),
            quantity.getUnit().getUnitName()
        );
        return new QuantityDTO(quantity.getValue(), dtoUnit);
    }

    /**
     * Resolves the IMeasurable unit instance from measurement type and unit name strings.
     *
     * Routes the lookup to the appropriate unit enum based on measurement type.
     *
     * @param measurementType category (e.g., "LengthUnit")
     * @param unit            unit name (e.g., "FEET")
     * @return matching IMeasurable enum constant
     * @throws IllegalArgumentException if the measurement type is not supported
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
                throw new IllegalArgumentException("Unsupported measurement type: " + measurementType);
        }
    }

    /**
     * Resolves the QuantityDTO.IMeasurableUnit enum from measurement type and unit name.
     *
     * @param measurementType category (e.g., "LengthUnit")
     * @param unit            unit name (e.g., "FEET")
     * @return matching QuantityDTO unit enum constant
     * @throws IllegalArgumentException if the measurement type is not supported
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
                throw new IllegalArgumentException("Unsupported measurement type: " + measurementType);
        }
    }

    /**
     * Validates that two operands are compatible for arithmetic operations.
     *
     * Checks:
     * 1. Neither operand is null.
     * 2. Both operands belong to the same measurement category.
     * 3. The measurement category supports arithmetic (not temperature).
     * 4. If a target unit is required, it is not null.
     *
     * @param q1             first operand
     * @param q2             second operand
     * @param targetUnit     optional target unit (may be null when not required)
     * @param targetRequired true if a non-null target unit is mandatory
     * @throws IllegalArgumentException      for null operands or category mismatch
     * @throws UnsupportedOperationException for temperature arithmetic
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

        if (!type1.equals(type2)) {
            throw new IllegalArgumentException(
                "Cannot perform arithmetic on different measurement categories");
        }

        /*
         * Temperature arithmetic is not physically meaningful (you cannot "add" temperatures),
         * so it is explicitly blocked here.
         */
        if (type1.equals("TemperatureUnit")) {
            throw new UnsupportedOperationException(
                "Arithmetic operations are not supported for temperature units");
        }

        if (targetRequired && targetUnit == null)
            throw new IllegalArgumentException("Target unit is required");
    }

    /**
     * Performs the specified arithmetic operation on two quantity models.
     *
     * Both operands are first converted to their base units before the operation
     * is applied. This ensures accuracy regardless of the input units.
     *
     * @param q1        first operand
     * @param q2        second operand
     * @param operation arithmetic operation to perform
     * @return result in base units
     * @throws ArithmeticException if dividing by zero
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
            case ADD:      op = (a, b) -> a + b; break;
            case SUBTRACT: op = (a, b) -> a - b; break;
            case DIVIDE:   op = (a, b) -> a / b; break;
            default: throw new IllegalArgumentException("Invalid arithmetic operation");
        }
        return op.applyAsDouble(base1, base2);
    }
}