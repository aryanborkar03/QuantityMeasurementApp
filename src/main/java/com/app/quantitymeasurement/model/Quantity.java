package com.app.quantitymeasurement.model;

import java.util.function.DoubleBinaryOperator;
import com.app.quantitymeasurement.unit.IMeasurable;
import com.app.quantitymeasurement.unit.SupportsArithmetic;


public final class Quantity<U extends IMeasurable> {

    private final double value;
    private final U unit;

    /** Precision tolerance used when comparing quantities for equality. */
    private static final double EPSILON = 1e-6;

    /** Scale factor for rounding arithmetic results to 6 decimal places. */
    private static final double ROUND_SCALE = 1e6;

    // -------------------------------------------------------------------------
    // Internal arithmetic operation enum
    // -------------------------------------------------------------------------

    /**
     * Supported arithmetic operations, each backed by a {@link DoubleBinaryOperator}.
     */
    private enum ArithmeticOperation {

        ADD((a, b) -> a + b),
        SUBTRACT((a, b) -> a - b),
        DIVIDE((a, b) -> {
            if (b == 0.0) throw new ArithmeticException("Division by zero");
            return a / b;
        });

        private final DoubleBinaryOperator operator;

        ArithmeticOperation(DoubleBinaryOperator operator) {
            this.operator = operator;
        }

        double compute(double a, double b) {
            return operator.applyAsDouble(a, b);
        }
    }

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructs a {@code Quantity} with the given value and unit.
     *
     * @param value numeric quantity value; must be finite
     * @param unit  measurable unit; must not be {@code null}
     * @throws IllegalArgumentException if {@code unit} is {@code null} or
     *                                  {@code value} is not finite
     */
    public Quantity(double value, U unit) {
        if (unit == null)             throw new IllegalArgumentException("Unit cannot be null");
        if (!Double.isFinite(value))  throw new IllegalArgumentException("Value must be a finite number");
        this.value = value;
        this.unit  = unit;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /** @return the numeric value of this quantity */
    public double getValue() { return value; }

    /** @return the unit associated with this quantity */
    public U getUnit() { return unit; }

    // -------------------------------------------------------------------------
    // Comparison and conversion
    // -------------------------------------------------------------------------

    /**
     * Compares this quantity with another for equality within a tolerance of {@code 1e-6}
     * in base units. Cross-category pairs always return {@code false}.
     *
     * @param o object to compare
     * @return {@code true} if both quantities represent the same measurement
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
     * Converts this quantity to the specified target unit.
     *
     * @param targetUnit unit to convert to; must be in the same measurement category
     * @return new {@code Quantity} with the converted value
     * @throws IllegalArgumentException if {@code targetUnit} is {@code null} or
     *                                  belongs to a different measurement category
     */
    public Quantity<U> convertTo(U targetUnit) {
        validateTargetUnit(targetUnit);
        double baseValue = unit.convertToBaseUnit(value);
        double converted = targetUnit.convertFromBaseUnit(baseValue);
        return new Quantity<>(converted, targetUnit);
    }

    // -------------------------------------------------------------------------
    // Arithmetic
    // -------------------------------------------------------------------------

    /**
     * Adds another quantity to this one and returns the result in the current unit.
     *
     * @param other quantity to add; must be in the same measurement category
     * @return sum expressed in this quantity's unit
     */
    public Quantity<U> add(Quantity<? extends IMeasurable> other) {
        validateQuantity(other);
        return new Quantity<>(round(unit.convertFromBaseUnit(
            performArithmetic(other, ArithmeticOperation.ADD))), unit);
    }

    /**
     * Adds another quantity to this one and expresses the result in the target unit.
     *
     * @param other      quantity to add
     * @param targetUnit unit for the result
     * @return sum expressed in {@code targetUnit}
     */
    public Quantity<U> add(Quantity<? extends IMeasurable> other, U targetUnit) {
        validateQuantity(other);
        validateTargetUnit(targetUnit);
        validateArithmeticSupport(targetUnit);
        return new Quantity<>(round(targetUnit.convertFromBaseUnit(
            performArithmetic(other, ArithmeticOperation.ADD))), targetUnit);
    }

    /**
     * Subtracts another quantity from this one and returns the result in the current unit.
     *
     * @param other quantity to subtract; must be in the same measurement category
     * @return difference expressed in this quantity's unit
     */
    public Quantity<U> subtract(Quantity<? extends IMeasurable> other) {
        validateQuantity(other);
        return new Quantity<>(round(unit.convertFromBaseUnit(
            performArithmetic(other, ArithmeticOperation.SUBTRACT))), unit);
    }

    /**
     * Subtracts another quantity from this one and expresses the result in the target unit.
     *
     * @param other      quantity to subtract
     * @param targetUnit unit for the result
     * @return difference expressed in {@code targetUnit}
     */
    public Quantity<U> subtract(Quantity<? extends IMeasurable> other, U targetUnit) {
        validateQuantity(other);
        validateTargetUnit(targetUnit);
        validateArithmeticSupport(targetUnit);
        return new Quantity<>(round(targetUnit.convertFromBaseUnit(
            performArithmetic(other, ArithmeticOperation.SUBTRACT))), targetUnit);
    }

    /**
     * Divides this quantity by another and returns the dimensionless numeric ratio.
     *
     * @param other divisor quantity; must be in the same measurement category
     * @return ratio as a plain {@code double}
     * @throws ArithmeticException if the divisor converts to zero in base units
     */
    public double divide(Quantity<? extends IMeasurable> other) {
        validateQuantity(other);
        return performArithmetic(other, ArithmeticOperation.DIVIDE);
    }

    // -------------------------------------------------------------------------
    // hashCode / toString
    // -------------------------------------------------------------------------

    /**
     * Hash code consistent with {@link #equals}: based on the base-unit value
     * rounded to the same {@code EPSILON} precision used for equality.
     */
    @Override
    public int hashCode() {
        long normalized = Math.round(unit.convertToBaseUnit(value) / EPSILON);
        return Long.hashCode(normalized);
    }

    /**
     * Returns a human-readable representation such as {@code "2 FEET"}.
     */
    @Override
    public String toString() {
        return String.format("%s %s",
            Double.toString(value).replaceAll("\\.0+$", ""),
            unit.getUnitName());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /** Validates that {@code other} is non-null, same category, and has a finite value. */
    private void validateQuantity(Quantity<? extends IMeasurable> other) {
        if (other == null)
            throw new IllegalArgumentException("Other quantity must not be null");
        if (!Double.isFinite(this.value) || !Double.isFinite(other.getValue()))
            throw new IllegalArgumentException("Values must be finite numbers");
        if (this.unit.getClass() != other.getUnit().getClass())
            throw new IllegalArgumentException("Cannot operate across different measurement categories");
    }

    /** Validates that {@code targetUnit} is non-null and in the same category as this unit. */
    private void validateTargetUnit(IMeasurable targetUnit) {
        if (targetUnit == null)
            throw new IllegalArgumentException("Target unit must not be null");
        if (targetUnit.getClass() != this.unit.getClass())
            throw new IllegalArgumentException("Target unit must belong to same measurement category");
    }

    /** Validates that {@code unit} supports arithmetic (implements {@link SupportsArithmetic}). */
    private void validateArithmeticSupport(IMeasurable unit) {
        if (!(unit instanceof SupportsArithmetic))
            throw new UnsupportedOperationException(
                "Arithmetic operations not supported for unit type: " + unit.getClass().getSimpleName());
    }

    /**
     * Converts both operands to base units, applies the operation, and returns the
     * result in base units.
     */
    private double performArithmetic(Quantity<? extends IMeasurable> other,
                                     ArithmeticOperation operation) {
        validateArithmeticSupport(this.unit);
        validateArithmeticSupport(other.getUnit());
        double baseThis  = unit.convertToBaseUnit(value);
        double baseOther = other.getUnit().convertToBaseUnit(other.getValue());
        return operation.compute(baseThis, baseOther);
    }

    /** Rounds a value to 6 decimal places. */
    private double round(double v) {
        return Math.round(v * ROUND_SCALE) / ROUND_SCALE;
    }
}
