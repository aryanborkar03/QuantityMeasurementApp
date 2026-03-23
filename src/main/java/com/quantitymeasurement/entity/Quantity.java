package com.quantitymeasurement.entity;

import java.util.function.DoubleBinaryOperator;
import com.quantitymeasurement.units.IMeasurable;
import com.quantitymeasurement.units.SupportsArithmetic;

/**
 * Quantity
 *
 * Immutable domain model representing a measurable quantity consisting of a numeric
 * value and its associated unit. Used by UC1-UC14 tests and the generic measurement
 * architecture built before the N-Tier refactoring in UC15/UC16.
 *
 * Supports equality comparison, unit conversion, addition, subtraction, and division.
 * Arithmetic operations are allowed only for units that implement SupportsArithmetic.
 *
 * All operations internally convert values to their base measurement unit to ensure
 * consistency across calculations. This class is immutable — operations return new
 * Quantity objects rather than modifying the original.
 *
 * @param <U> unit type implementing IMeasurable
 *
 * @author Developer
 * @version 16.0
 * @since 1.0
 */
public final class Quantity<U extends IMeasurable> {

    /** Numeric value of the quantity */
    private final double value;

    /** Unit associated with the quantity */
    private final U unit;

    /** Precision tolerance used when comparing quantities for equality */
    private static final double EPSILON = 1e-6;

    /** Scale factor used for rounding arithmetic results to 6 decimal places */
    private static final double ROUND_SCALE = 1e6;

    /**
     * Enumeration representing supported arithmetic operations.
     * Each operation defines a functional implementation using DoubleBinaryOperator.
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

        private final DoubleBinaryOperator operator;

        ArithmeticOperation(DoubleBinaryOperator operator) {
            this.operator = operator;
        }

        /**
         * Executes the arithmetic operation on two values.
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
     * @param unit  measurable unit
     * @throws IllegalArgumentException if unit is null or value is not finite
     */
    public Quantity(double value, U unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null");
        }
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Value must be a finite number");
        }
        this.value = value;
        this.unit  = unit;
    }

    /**
     * Returns the numeric value of this quantity.
     *
     * @return quantity value
     */
    public double getValue() {
        return value;
    }

    /**
     * Returns the unit associated with this quantity.
     *
     * @return measurable unit
     */
    public U getUnit() {
        return unit;
    }

    /**
     * Compares this quantity with another for equality.
     *
     * Two quantities are equal if their base unit values differ by less than EPSILON (1e-6).
     * Cross-category comparisons (e.g., length vs weight) always return false.
     *
     * @param o object to compare
     * @return true if quantities represent the same measurement
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quantity<?> other)) return false;
        if (this.unit.getClass() != other.unit.getClass()) return false;
        double thisBase  = unit.convertToBaseUnit(value);
        double otherBase = other.unit.convertToBaseUnit(other.value);
        return Math.abs(thisBase - otherBase) < EPSILON;
    }

    /**
     * Converts this quantity to a specified target unit.
     *
     * @param targetUnit target unit
     * @return new Quantity with the converted value in the target unit
     * @throws IllegalArgumentException if targetUnit is null or from a different category
     */
    public Quantity<U> convertTo(U targetUnit) {
        validateTargetUnit(targetUnit);
        double baseValue  = unit.convertToBaseUnit(value);
        double converted  = targetUnit.convertFromBaseUnit(baseValue);
        return new Quantity<>(converted, targetUnit);
    }

    /**
     * Adds this quantity to another and returns the result in the current unit.
     *
     * @param other quantity to add
     * @return result in the same unit as this quantity
     */
    public Quantity<U> add(Quantity<? extends IMeasurable> other) {
        validateQuantity(other);
        double baseResult = performArithmetic(other, ArithmeticOperation.ADD);
        double result     = unit.convertFromBaseUnit(baseResult);
        double rounded    = Math.round(result * ROUND_SCALE) / ROUND_SCALE;
        return new Quantity<>(rounded, unit);
    }

    /**
     * Adds this quantity to another and converts the result to the specified target unit.
     *
     * @param other      quantity to add
     * @param targetUnit target unit for the result
     * @return result in the target unit
     */
    public Quantity<U> add(Quantity<? extends IMeasurable> other, U targetUnit) {
        validateQuantity(other);
        validateTargetUnit(targetUnit);
        validateArithmeticSupport(targetUnit);
        double baseResult = performArithmetic(other, ArithmeticOperation.ADD);
        double result     = targetUnit.convertFromBaseUnit(baseResult);
        double rounded    = Math.round(result * ROUND_SCALE) / ROUND_SCALE;
        return new Quantity<>(rounded, targetUnit);
    }

    /**
     * Subtracts another quantity from this one and returns the result in the current unit.
     *
     * @param other quantity to subtract
     * @return result in the same unit as this quantity
     */
    public Quantity<U> subtract(Quantity<? extends IMeasurable> other) {
        validateQuantity(other);
        double baseResult = performArithmetic(other, ArithmeticOperation.SUBTRACT);
        double result     = unit.convertFromBaseUnit(baseResult);
        double rounded    = Math.round(result * ROUND_SCALE) / ROUND_SCALE;
        return new Quantity<>(rounded, unit);
    }

    /**
     * Subtracts another quantity from this one and converts the result to the target unit.
     *
     * @param other      quantity to subtract
     * @param targetUnit target unit for the result
     * @return result in the target unit
     */
    public Quantity<U> subtract(Quantity<? extends IMeasurable> other, U targetUnit) {
        validateQuantity(other);
        validateTargetUnit(targetUnit);
        validateArithmeticSupport(targetUnit);
        double baseResult = performArithmetic(other, ArithmeticOperation.SUBTRACT);
        double result     = targetUnit.convertFromBaseUnit(baseResult);
        double rounded    = Math.round(result * ROUND_SCALE) / ROUND_SCALE;
        return new Quantity<>(rounded, targetUnit);
    }

    /**
     * Divides this quantity by another and returns the dimensionless numeric ratio.
     *
     * @param other divisor quantity
     * @return division result
     * @throws ArithmeticException if the divisor converts to zero in base units
     */
    public double divide(Quantity<? extends IMeasurable> other) {
        validateQuantity(other);
        return performArithmetic(other, ArithmeticOperation.DIVIDE);
    }

    /**
     * Validates that two quantities are compatible for operations.
     * Checks for null, cross-category mismatch, and non-finite values.
     */
    private void validateQuantity(Quantity<? extends IMeasurable> other) {
        if (other == null) throw new IllegalArgumentException("Other quantity must not be null");
        if (this.unit == null || other.getUnit() == null)
            throw new IllegalArgumentException("Unit must not be null");
        if (!Double.isFinite(this.value) || !Double.isFinite(other.getValue()))
            throw new IllegalArgumentException("Values must be finite numbers");
        if (this.unit.getClass() != other.getUnit().getClass())
            throw new IllegalArgumentException("Cannot operate across different measurement categories");
    }

    /**
     * Validates that the target unit belongs to the same measurement category.
     */
    private void validateTargetUnit(IMeasurable targetUnit) {
        if (targetUnit == null) throw new IllegalArgumentException("Target unit must not be null");
        if (targetUnit.getClass() != this.unit.getClass())
            throw new IllegalArgumentException("Target unit must belong to same measurement category");
    }

    /**
     * Validates that the given unit supports arithmetic operations (implements SupportsArithmetic).
     */
    private void validateArithmeticSupport(IMeasurable unit) {
        if (!(unit instanceof SupportsArithmetic)) {
            throw new UnsupportedOperationException(
                "Arithmetic operations not supported for unit type: " + unit.getClass().getSimpleName());
        }
    }

    /**
     * Performs the specified arithmetic operation after converting both operands to base units.
     */
    private double performArithmetic(
            Quantity<? extends IMeasurable> other, ArithmeticOperation operation) {
        validateArithmeticSupport(this.unit);
        validateArithmeticSupport(other.getUnit());
        double baseThis  = unit.convertToBaseUnit(value);
        double baseOther = other.getUnit().convertToBaseUnit(other.getValue());
        return operation.compute(baseThis, baseOther);
    }

    /**
     * Generates hash code based on the normalized base unit value.
     */
    @Override
    public int hashCode() {
        long normalized = Math.round(unit.convertToBaseUnit(value) / EPSILON);
        return Long.hashCode(normalized);
    }

    /**
     * Returns a formatted string representation of this quantity.
     */
    @Override
    public String toString() {
        return String.format("%s %s",
            Double.toString(value).replace("\\.0+$", ""),
            unit.getUnitName());
    }
}