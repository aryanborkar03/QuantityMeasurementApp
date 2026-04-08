package com.app.quantitymeasurement.service;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.DoubleBinaryOperator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.quantitymeasurement.exception.QuantityMeasurementException;
import com.app.quantitymeasurement.dto.response.QuantityDTO;
import com.app.quantitymeasurement.dto.request.QuantityMeasurementDTO;
import com.app.quantitymeasurement.entity.QuantityMeasurementEntity;
import com.app.quantitymeasurement.model.QuantityModel;
import com.app.quantitymeasurement.repository.QuantityMeasurementRepository;
import com.app.quantitymeasurement.unit.IMeasurable;


@Slf4j
@Service
public class QuantityMeasurementServiceImpl implements IQuantityMeasurementService {

    private static final double EPSILON = 1e-6;

    private final QuantityMeasurementRepository repository;

    public QuantityMeasurementServiceImpl(QuantityMeasurementRepository repository) {
        this.repository = repository;
    }

    // -------------------------------------------------------------------------
    // Internal enums
    // -------------------------------------------------------------------------

    /** Operation names used when building entity records. */
    private enum Operation {
        COMPARE, CONVERT, ADD, SUBTRACT, DIVIDE
    }

    /** Arithmetic operations dispatched by {@link #performArithmetic}. */
    private enum ArithmeticOperation {
        ADD, SUBTRACT, DIVIDE
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * Converts both quantities to their base units before comparing.
     * The result is stored in the repository for audit purposes.
     *
     * @throws QuantityMeasurementException if the quantities belong to different categories
     */
    @Override
    public QuantityMeasurementDTO compare(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        QuantityModel<IMeasurable> q1 = convertDtoToModel(thisQuantityDTO);
        QuantityModel<IMeasurable> q2 = convertDtoToModel(thatQuantityDTO);

        try {
            if (!q1.getUnit().getMeasurementType().equals(q2.getUnit().getMeasurementType())) {
                throw new QuantityMeasurementException(
                    "compare Error: Cannot compare different measurement categories: "
                    + q1.getUnit().getMeasurementType() + " and " + q2.getUnit().getMeasurementType());
            }

            boolean result = compareBaseValues(q1, q2);
            QuantityMeasurementEntity entity = buildEntity(q1, q2,
                Operation.COMPARE.name().toLowerCase(),
                String.valueOf(result), null, null, null, false, null);
            repository.save(entity);

            log.debug("COMPARE: " + q1 + " vs " + q2 + " => " + result);
            return QuantityMeasurementDTO.fromEntity(entity);

        } catch (QuantityMeasurementException e) {
            saveErrorEntity(q1, q2, Operation.COMPARE.name().toLowerCase(), e.getMessage());
            throw e;
        } catch (Exception e) {
            saveErrorEntity(q1, q2, Operation.COMPARE.name().toLowerCase(), e.getMessage());
            throw new QuantityMeasurementException("compare Error: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Linear units are converted via a base-unit pivot. Temperature conversions use
     * their own non-linear path through {@link #convertTemperatureUnit}.
     */
    @Override
    public QuantityMeasurementDTO convert(QuantityDTO thisQuantityDTO, QuantityDTO targetUnitDTO) {
        QuantityModel<IMeasurable> source = convertDtoToModel(thisQuantityDTO);
        QuantityModel<IMeasurable> target = convertDtoToModel(targetUnitDTO);

        try {
            double result = (source.getUnit() instanceof com.app.quantitymeasurement.unit.TemperatureUnit)
                ? convertTemperatureUnit(source, target.getUnit())
                : target.getUnit().convertFromBaseUnit(source.getUnit().convertToBaseUnit(source.getValue()));

            QuantityMeasurementEntity entity = buildEntity(source, target,
                Operation.CONVERT.name().toLowerCase(), null, result,
                target.getUnit().getUnitName(), target.getUnit().getMeasurementType(), false, null);
            repository.save(entity);

            log.debug("CONVERT: " + source + " => " + result + " " + target.getUnit().getUnitName());
            return QuantityMeasurementDTO.fromEntity(entity);

        } catch (Exception e) {
            saveErrorEntity(source, target, Operation.CONVERT.name().toLowerCase(), e.getMessage());
            throw new QuantityMeasurementException("convert Error: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Delegates to {@link #add(QuantityDTO, QuantityDTO, QuantityDTO)} using
     * {@code thisQuantityDTO} as the target unit.
     */
    @Override
    public QuantityMeasurementDTO add(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        return add(thisQuantityDTO, thatQuantityDTO, thisQuantityDTO);
    }

    /** {@inheritDoc} */
    @Override
    public QuantityMeasurementDTO add(QuantityDTO thisQuantityDTO,
                                      QuantityDTO thatQuantityDTO,
                                      QuantityDTO targetUnitDTO) {
        QuantityModel<IMeasurable> q1     = convertDtoToModel(thisQuantityDTO);
        QuantityModel<IMeasurable> q2     = convertDtoToModel(thatQuantityDTO);
        QuantityModel<IMeasurable> target = convertDtoToModel(targetUnitDTO);

        try {
            validateArithmeticOperands(q1, q2, target.getUnit(), true);
            double result = target.getUnit().convertFromBaseUnit(
                performArithmetic(q1, q2, ArithmeticOperation.ADD));

            QuantityMeasurementEntity entity = buildEntity(q1, q2,
                Operation.ADD.name().toLowerCase(), null, result,
                target.getUnit().getUnitName(), target.getUnit().getMeasurementType(), false, null);
            repository.save(entity);

            log.debug("ADD: " + q1 + " + " + q2 + " => " + result + " " + target.getUnit().getUnitName());
            return QuantityMeasurementDTO.fromEntity(entity);

        } catch (QuantityMeasurementException e) {
            saveErrorEntity(q1, q2, Operation.ADD.name().toLowerCase(), e.getMessage());
            throw e;
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            saveErrorEntity(q1, q2, Operation.ADD.name().toLowerCase(), e.getMessage());
            throw new QuantityMeasurementException("add Error: " + e.getMessage(), e);
        } catch (Exception e) {
            saveErrorEntity(q1, q2, Operation.ADD.name().toLowerCase(), e.getMessage());
            throw new QuantityMeasurementException("add Error: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Delegates to {@link #subtract(QuantityDTO, QuantityDTO, QuantityDTO)} using
     * {@code thisQuantityDTO} as the target unit.
     */
    @Override
    public QuantityMeasurementDTO subtract(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        return subtract(thisQuantityDTO, thatQuantityDTO, thisQuantityDTO);
    }

    /** {@inheritDoc} */
    @Override
    public QuantityMeasurementDTO subtract(QuantityDTO thisQuantityDTO,
                                           QuantityDTO thatQuantityDTO,
                                           QuantityDTO targetUnitDTO) {
        QuantityModel<IMeasurable> q1     = convertDtoToModel(thisQuantityDTO);
        QuantityModel<IMeasurable> q2     = convertDtoToModel(thatQuantityDTO);
        QuantityModel<IMeasurable> target = convertDtoToModel(targetUnitDTO);

        try {
            validateArithmeticOperands(q1, q2, target.getUnit(), true);
            double result = target.getUnit().convertFromBaseUnit(
                performArithmetic(q1, q2, ArithmeticOperation.SUBTRACT));

            QuantityMeasurementEntity entity = buildEntity(q1, q2,
                Operation.SUBTRACT.name().toLowerCase(), null, result,
                target.getUnit().getUnitName(), target.getUnit().getMeasurementType(), false, null);
            repository.save(entity);

            log.debug("SUBTRACT: " + q1 + " - " + q2 + " => " + result);
            return QuantityMeasurementDTO.fromEntity(entity);

        } catch (QuantityMeasurementException e) {
            saveErrorEntity(q1, q2, Operation.SUBTRACT.name().toLowerCase(), e.getMessage());
            throw e;
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            saveErrorEntity(q1, q2, Operation.SUBTRACT.name().toLowerCase(), e.getMessage());
            throw new QuantityMeasurementException("subtract Error: " + e.getMessage(), e);
        } catch (Exception e) {
            saveErrorEntity(q1, q2, Operation.SUBTRACT.name().toLowerCase(), e.getMessage());
            throw new QuantityMeasurementException("subtract Error: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public QuantityMeasurementDTO divide(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO) {
        QuantityModel<IMeasurable> q1 = convertDtoToModel(thisQuantityDTO);
        QuantityModel<IMeasurable> q2 = convertDtoToModel(thatQuantityDTO);

        try {
            validateArithmeticOperands(q1, q2, null, false);
            double result = performArithmetic(q1, q2, ArithmeticOperation.DIVIDE);

            QuantityMeasurementEntity entity = buildEntity(q1, q2,
                Operation.DIVIDE.name().toLowerCase(), null, result, null, null, false, null);
            repository.save(entity);

            log.debug("DIVIDE: " + q1 + " / " + q2 + " => " + result);
            return QuantityMeasurementDTO.fromEntity(entity);

        } catch (ArithmeticException e) {
            saveErrorEntity(q1, q2, Operation.DIVIDE.name().toLowerCase(), e.getMessage());
            throw e;
        } catch (QuantityMeasurementException e) {
            saveErrorEntity(q1, q2, Operation.DIVIDE.name().toLowerCase(), e.getMessage());
            throw e;
        } catch (Exception e) {
            saveErrorEntity(q1, q2, Operation.DIVIDE.name().toLowerCase(), e.getMessage());
            throw new QuantityMeasurementException("divide Error: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<QuantityMeasurementDTO> getHistoryByOperation(String operation) {
        return QuantityMeasurementDTO.fromEntityList(repository.findByOperation(operation));
    }

    /** {@inheritDoc} */
    @Override
    public List<QuantityMeasurementDTO> getHistoryByMeasurementType(String measurementType) {
        return QuantityMeasurementDTO.fromEntityList(
            repository.findByThisMeasurementType(measurementType));
    }

    /** {@inheritDoc} */
    @Override
    public long getOperationCount(String operation) {
        return repository.countByOperationAndErrorFalse(operation);
    }

    /** {@inheritDoc} */
    @Override
    public List<QuantityMeasurementDTO> getErrorHistory() {
        return QuantityMeasurementDTO.fromEntityList(repository.findByErrorTrue());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Converts a {@link QuantityDTO} to an internal {@link QuantityModel} by resolving
     * the unit enum from the DTO's {@code measurementType} and {@code unit} strings.
     *
     * @param quantity incoming DTO
     * @return populated {@code QuantityModel}
     * @throws IllegalArgumentException if {@code quantity} is {@code null} or the
     *                                  measurement type is unsupported
     */
    private QuantityModel<IMeasurable> convertDtoToModel(QuantityDTO quantity) {
        if (quantity == null)
            throw new IllegalArgumentException("QuantityDTO cannot be null");
        return new QuantityModel<>(quantity.getValue(),
            getModelUnit(quantity.getMeasurementType(), quantity.getUnit()));
    }

    /**
     * Resolves the {@link IMeasurable} unit constant from the given type and name strings.
     *
     * @param measurementType measurement category (e.g., {@code "LengthUnit"})
     * @param unit            unit name (e.g., {@code "FEET"})
     * @return matching unit enum constant
     * @throws IllegalArgumentException if the type is unsupported
     */
    private IMeasurable getModelUnit(String measurementType, String unit) {
        switch (measurementType) {
            case "LengthUnit":      return com.app.quantitymeasurement.unit.LengthUnit.valueOf(unit);
            case "WeightUnit":      return com.app.quantitymeasurement.unit.WeightUnit.valueOf(unit);
            case "VolumeUnit":      return com.app.quantitymeasurement.unit.VolumeUnit.valueOf(unit);
            case "TemperatureUnit": return com.app.quantitymeasurement.unit.TemperatureUnit.valueOf(unit);
            default: throw new IllegalArgumentException("Unsupported measurement type: " + measurementType);
        }
    }

    /**
     * Compares two quantity models by their base-unit values using
     * an epsilon tolerance of {@code 1e-6} to account for floating-point rounding.
     *
     * @param q1 first model
     * @param q2 second model
     * @return {@code true} if the base values are equal within tolerance
     */
    private <U extends IMeasurable> boolean compareBaseValues(
            QuantityModel<U> q1, QuantityModel<U> q2) {
        double base1 = q1.getUnit().convertToBaseUnit(q1.getValue());
        double base2 = q2.getUnit().convertToBaseUnit(q2.getValue());
        return Math.abs(base1 - base2) < EPSILON;
    }

    /**
     * Converts a temperature value to the target unit via the Celsius pivot.
     *
     * @param source     source quantity in some temperature unit
     * @param targetUnit desired target temperature unit
     * @return converted temperature value
     */
    private <U extends IMeasurable> double convertTemperatureUnit(
            QuantityModel<U> source, U targetUnit) {
        return targetUnit.convertFromBaseUnit(source.getUnit().convertToBaseUnit(source.getValue()));
    }

    /**
     * Validates that the two operands are compatible for an arithmetic operation.
     *
     * <p>Checks performed:</p>
     * <ol>
     *   <li>Neither operand is {@code null}.</li>
     *   <li>Both operands belong to the same measurement category.</li>
     *   <li>The category supports arithmetic (temperature is rejected).</li>
     *   <li>When {@code targetRequired} is {@code true}, the target unit is not {@code null}.</li>
     * </ol>
     *
     * @param q1             first operand
     * @param q2             second operand
     * @param targetUnit     target unit; may be {@code null} when not required
     * @param targetRequired {@code true} if the target unit must be non-null
     * @throws IllegalArgumentException      for null operands, category mismatch, or missing target
     * @throws UnsupportedOperationException if the category is {@code TemperatureUnit}
     */
    private <U extends IMeasurable> void validateArithmeticOperands(
            QuantityModel<U> q1, QuantityModel<U> q2, U targetUnit, boolean targetRequired) {

        if (q1 == null || q2 == null)
            throw new IllegalArgumentException("Operands cannot be null");

        String type1 = q1.getUnit().getMeasurementType();
        String type2 = q2.getUnit().getMeasurementType();

        if (!type1.equals(type2)) {
            throw new IllegalArgumentException(
                "Cannot perform arithmetic between different measurement categories: "
                + type1 + " and " + type2);
        }
        if (type1.equals("TemperatureUnit")) {
            throw new UnsupportedOperationException(
                "Arithmetic operations are not supported for temperature units");
        }
        if (targetRequired && targetUnit == null)
            throw new IllegalArgumentException("Target unit is required");
    }

    /**
     * Converts both operands to base units, applies the arithmetic operation,
     * and returns the result in base units.
     *
     * @param q1        first operand
     * @param q2        second operand
     * @param operation operation to apply
     * @return result in base units
     * @throws ArithmeticException if {@code operation} is DIVIDE and the divisor is zero
     */
    private <U extends IMeasurable> double performArithmetic(
            QuantityModel<U> q1, QuantityModel<U> q2, ArithmeticOperation operation) {

        double base1 = q1.getUnit().convertToBaseUnit(q1.getValue());
        double base2 = q2.getUnit().convertToBaseUnit(q2.getValue());

        if (operation == ArithmeticOperation.DIVIDE && base2 == 0)
            throw new ArithmeticException("Division by zero is not allowed");

        DoubleBinaryOperator op;
        switch (operation) {
            case ADD:      op = (a, b) -> a + b; break;
            case SUBTRACT: op = (a, b) -> a - b; break;
            case DIVIDE:   op = (a, b) -> a / b; break;
            default: throw new IllegalArgumentException("Invalid arithmetic operation");
        }
        return op.applyAsDouble(base1, base2);
    }

    /**
     * Constructs a {@link QuantityMeasurementEntity} from the given parameters.
     * Fields that are not applicable for a particular operation may be {@code null}.
     *
     * @param q1           first operand model
     * @param q2           second operand model
     * @param operation    operation name (lowercase)
     * @param resultString string result (for compare); {@code null} otherwise
     * @param resultValue  numeric result; {@code null} for compare
     * @param resultUnit   result unit name; {@code null} for compare and divide
     * @param resultType   result measurement type; {@code null} for compare and divide
     * @param isError      {@code true} for error records
     * @param errorMessage error description; {@code null} for success records
     * @return the constructed entity (not yet persisted)
     */
    private QuantityMeasurementEntity buildEntity(
            QuantityModel<IMeasurable> q1,
            QuantityModel<IMeasurable> q2,
            String operation,
            String resultString,
            Double resultValue,
            String resultUnit,
            String resultType,
            boolean isError,
            String errorMessage) {

        QuantityMeasurementEntity entity = new QuantityMeasurementEntity();
        entity.setThisValue(q1.getValue());
        entity.setThisUnit(q1.getUnit().getUnitName());
        entity.setThisMeasurementType(q1.getUnit().getMeasurementType());
        entity.setThatValue(q2.getValue());
        entity.setThatUnit(q2.getUnit().getUnitName());
        entity.setThatMeasurementType(q2.getUnit().getMeasurementType());
        entity.setOperation(operation);
        entity.setResultString(resultString);
        entity.setResultValue(resultValue);
        entity.setResultUnit(resultUnit);
        entity.setResultMeasurementType(resultType);
        entity.setError(isError);
        entity.setErrorMessage(errorMessage);
        return entity;
    }

    /**
     * Persists an error record to the repository when an operation fails.
     * Called from every catch block so that failed operations always appear in the
     * audit history, regardless of whether the calling method re-throws the exception.
     *
     * <p>Save failures are logged but not re-thrown; the original operation exception
     * must propagate to the caller undisturbed.</p>
     *
     * @param q1           first operand
     * @param q2           second operand
     * @param operation    operation that failed
     * @param errorMessage description of the failure
     */
    private void saveErrorEntity(QuantityModel<IMeasurable> q1,
                                 QuantityModel<IMeasurable> q2,
                                 String operation,
                                 String errorMessage) {
        try {
            repository.save(buildEntity(q1, q2, operation,
                null, null, null, null, true, errorMessage));
        } catch (Exception ex) {
            log.error("Failed to save error entity: " + ex.getMessage());
        }
    }
}
