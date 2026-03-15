package com.quantitymeasurement.model;

import java.util.function.DoubleBinaryOperator;

import com.quantitymeasurement.units.IMeasurable;
import com.quantitymeasurement.units.SupportsArithmetic;

/**
 * Quantity
 *
 * Immutable domain model representing a measurable quantity
 * consisting of a numeric value and its associated unit.
 *
 * The generic type parameter ensures that the unit used
 * implements the {@link IMeasurable} interface, enabling
 * uniform handling of different measurement categories
 * such as length, weight, volume, and temperature.
 *
 * This class supports:
 * <ul>
 * <li>Equality comparison across compatible units</li>
 * <li>Unit conversion</li>
 * <li>Addition</li>
 * <li>Subtraction</li>
 * <li>Division</li>
 * </ul>
 *
 * Arithmetic operations are allowed only for units that
 * implement the {@link SupportsArithmetic} marker interface.
 *
 * All operations internally convert values to their base
 * measurement unit to ensure consistency across calculations.
 *
 * The class is designed to be immutable, meaning once a
 * Quantity object is created, its value and unit cannot
 * be modified.
 *
 * @param <U> unit type implementing IMeasurable
 */
public final class Quantity<U extends IMeasurable> {

    /**
     * Numeric value of the quantity.
     */
    private final double value;

    /**
     * Unit associated with the quantity.
     */
    private final U unit;

    /**
     * Precision tolerance used when comparing quantities.
     */
    private static final double EPSILON = 1e-6;

    /**
     * Scale factor used for rounding arithmetic results.
     */
    private static final double ROUND_SCALE = 1e6;

    /**
     * Enumeration representing supported arithmetic operations.
     *
     * Each operation defines a functional implementation
     * using {@link DoubleBinaryOperator}.
     */
    private enum ArithmeticOperation {

        ADD((a, b) -> a + b),

        SUBTRACT((a, b) -> a - b),

        DIVIDE((a, b) -> {
            if (b == 0.0) {
                throw new ArithmeticException("Division by zero");
            }
            return a / b;
        });

        /**
         * Functional operator used to perform arithmetic.
         */
        private final DoubleBinaryOperator operator;

        /**
         * Constructs the arithmetic operation with
         * the specified operator.
         *
         * @param operator functional arithmetic implementation
         */
        ArithmeticOperation(DoubleBinaryOperator operator) {
            this.operator = operator;
        }

        /**
         * Executes the arithmetic operation.
         *
         * @param a first operand
         * @param b second operand
         * @return result of the operation
         */
        public double compute(double a, double b) {
            return operator.applyAsDouble(a, b);
        }
    }

    /**
     * Constructs a Quantity object with the given value and unit.
     *
     * @param value numeric quantity value
     * @param unit measurable unit
     */
    public Quantity(double value, U unit) {

        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null");
        }

        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Value must be a finite number");
        }

        this.value = value;
        this.unit = unit;
    }

    /**
     * Returns the numeric value of the quantity.
     *
     * @return quantity value
     */
    public double getValue() {
        return value;
    }

    /**
     * Returns the unit associated with the quantity.
     *
     * @return measurable unit
     */
    public U getUnit() {
        return unit;
    }

    /**
     * Compares this quantity with another object.
     *
     * Two quantities are considered equal if their
     * base unit values differ by less than EPSILON.
     *
     * @param o object to compare
     * @return true if quantities are equivalent
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Quantity<?> other)) {
            return false;
        }

        if (this.unit.getClass() != other.unit.getClass()) {
            return false;
        }

        double thisBase = unit.convertToBaseUnit(value);
        double otherBase = other.unit.convertToBaseUnit(other.value);

        return Math.abs(thisBase - otherBase) < EPSILON;
    }

    /**
     * Converts the quantity to a specified target unit.
     *
     * @param targetUnit target unit
     * @return converted quantity
     */
    public Quantity<U> convertTo(U targetUnit) {

        validateTargetUnit(targetUnit);

        double baseValue = unit.convertToBaseUnit(value);
        double converted = targetUnit.convertFromBaseUnit(baseValue);

        return new Quantity<>(converted, targetUnit);
    }

    /**
     * Validates the compatibility of two quantities
     * before performing operations.
     *
     * @param other other quantity
     */
    private void validateQuantity(Quantity<? extends IMeasurable> other) {

        if (other == null) {
            throw new IllegalArgumentException("Other quantity must not be null");
        }

        if (this.unit == null || other.getUnit() == null) {
            throw new IllegalArgumentException("Unit must not be null");
        }

        if (!Double.isFinite(this.value) || !Double.isFinite(other.getValue())) {
            throw new IllegalArgumentException("Values must be finite numbers");
        }

        if (this.unit.getClass() != other.getUnit().getClass()) {
            throw new IllegalArgumentException(
                "Cannot operate across different measurement categories"
            );
        }
    }

    /**
     * Validates that the target unit belongs to the
     * same measurement category.
     *
     * @param targetUnit target unit
     */
    private void validateTargetUnit(IMeasurable targetUnit) {

        if (targetUnit == null) {
            throw new IllegalArgumentException("Target unit must not be null");
        }

        if (targetUnit.getClass() != this.unit.getClass()) {
            throw new IllegalArgumentException(
                "Target unit must belong to same measurement category"
            );
        }
    }

    /**
     * Validates that the given unit supports arithmetic
     * operations by checking the {@link SupportsArithmetic}
     * marker interface.
     *
     * @param unit measurable unit
     */
    private void validateArithmeticSupport(IMeasurable unit) {
       
    	if (!(unit instanceof SupportsArithmetic)) {
            throw new UnsupportedOperationException(
                "Arithmetic operations not supported for unit type: "
                + unit.getClass().getSimpleName()
            );
        }
    }

    /**
     * Performs arithmetic operations by converting
     * both operands to base units.
     *
     * @param other second quantity
     * @param operation arithmetic operation
     * @return base unit result
     */
    private double performArithmetic(
        Quantity<? extends IMeasurable> other,
        ArithmeticOperation operation) {
        validateArithmeticSupport(this.unit);
        validateArithmeticSupport(other.getUnit());

        double baseThis = unit.convertToBaseUnit(value);
        double baseOther = other.getUnit().convertToBaseUnit(other.getValue());

        return operation.compute(baseThis, baseOther);
    }

    /**
     * Adds two quantities and returns result in the
     * current unit.
     *
     * @param other quantity to add
     * @return resulting quantity
     */
    public Quantity<U> add(Quantity<? extends IMeasurable> other) {
        validateQuantity(other);

        double baseResult = performArithmetic(other, ArithmeticOperation.ADD);
        double result = unit.convertFromBaseUnit(baseResult);
        double rounded = Math.round(result * ROUND_SCALE) / ROUND_SCALE;

        return new Quantity<>(rounded, unit);
    }

    /**
     * Adds two quantities and converts result
     * to the specified target unit.
     *
     * @param other quantity to add
     * @param targetUnit target unit
     * @return resulting quantity
     */
    public Quantity<U> add(Quantity<? extends IMeasurable> other, U targetUnit) {
        validateQuantity(other);
        validateTargetUnit(targetUnit);
        validateArithmeticSupport(targetUnit);

        double baseResult = performArithmetic(other, ArithmeticOperation.ADD);
        double result = targetUnit.convertFromBaseUnit(baseResult);
        double rounded = Math.round(result * ROUND_SCALE) / ROUND_SCALE;

        return new Quantity<>(rounded, targetUnit);
    }

    /**
     * Subtracts two quantities and returns result
     * in the current unit.
     *
     * @param other quantity to subtract
     * @return resulting quantity
     */
    public Quantity<U> subtract(Quantity<? extends IMeasurable> other) {
        validateQuantity(other);

        double baseResult = performArithmetic(other, ArithmeticOperation.SUBTRACT);
        double result = unit.convertFromBaseUnit(baseResult);
        double rounded = Math.round(result * ROUND_SCALE) / ROUND_SCALE;

        return new Quantity<>(rounded, unit);
    }

    /**
     * Subtracts two quantities and converts result
     * to the specified target unit.
     *
     * @param other quantity to subtract
     * @param targetUnit target unit
     * @return resulting quantity
     */
    public Quantity<U> subtract(Quantity<? extends IMeasurable> other, U targetUnit) {
        validateQuantity(other);
        validateTargetUnit(targetUnit);
        validateArithmeticSupport(targetUnit);

        double baseResult = performArithmetic(other, ArithmeticOperation.SUBTRACT);
        double result = targetUnit.convertFromBaseUnit(baseResult);
        double rounded = Math.round(result * ROUND_SCALE) / ROUND_SCALE;
        
        return new Quantity<>(rounded, targetUnit);
    }

    /**
     * Divides two quantities and returns a numeric ratio.
     *
     * @param other divisor quantity
     * @return division result
     */
    public double divide(Quantity<? extends IMeasurable> other) {
        validateQuantity(other);
        
    	return performArithmetic(other, ArithmeticOperation.DIVIDE);
    }

    /**
     * Generates hash code based on normalized base unit value.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {

        long normalized = Math.round(unit.convertToBaseUnit(value) / EPSILON);

        return Long.hashCode(normalized);
    }

    /**
     * Returns formatted string representation
     * of the quantity.
     *
     * @return formatted quantity string
     */
    @Override
    public String toString() {

        return String.format("%s %s", Double.toString(value).replace("\\.0+$", ""), unit.getUnitName());
    }
}