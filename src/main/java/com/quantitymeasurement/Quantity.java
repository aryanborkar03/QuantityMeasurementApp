package com.quantitymeasurement;

import java.util.function.DoubleBinaryOperator;

public final class Quantity<U extends IMeasurable> {

    private final double value;
    private final U unit;
    private static final double EPSILON = 1e-6;
    private static final double ROUND_SCALE = 1e6;

    private enum ArithmeticOperation {

        ADD((a, b) -> a + b), SUBTRACT((a, b) -> a - b), DIVIDE((a, b) -> {
            if (b == 0.0)
                throw new ArithmeticException("Division by zero");
            return a / b;
        });

        private final DoubleBinaryOperator op;

        ArithmeticOperation(DoubleBinaryOperator op) {
            this.op = op;
        }

        public double compute(double a, double b) {
            return op.applyAsDouble(a, b);
        }
    }

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

    public double getValue() {
        return value;
    }

    public U getUnit() {
        return unit;
    }

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

    public Quantity<U> convertTo(U targetUnit) {
        if (targetUnit == null) {
            throw new IllegalArgumentException("Target unit cannot be null");
        }

        double baseValue = unit.convertToBaseUnit(value);
        double converted = targetUnit.convertFromBaseUnit(baseValue);

        return new Quantity<>(converted, targetUnit);
    }

    private void validateArithmeticOperands(Quantity<? extends IMeasurable> other, IMeasurable targetUnit,
            boolean targetUnitRequired) {
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
            throw new IllegalArgumentException("Cannot operate across different measurement categories");
        }
        if (targetUnitRequired && targetUnit == null) {
            throw new IllegalArgumentException("Target unit must not be null");
        }
        if (targetUnit != null && targetUnit.getClass() != this.unit.getClass()) {
            throw new IllegalArgumentException("Target unit must belong to same measurement category");
        }
    }

    private double performArithmetic(Quantity<? extends IMeasurable> other, ArithmeticOperation operation) {
        // Validate operation support on both units before any conversion
        this.unit.validateOperationSupport(operation.name());
        other.getUnit().validateOperationSupport(operation.name());

        // For division, require both units to explicitly support arithmetic
        if (operation == ArithmeticOperation.DIVIDE) {
            if (!this.unit.supportsArithmetic() || !other.getUnit().supportsArithmetic()) {
                throw new UnsupportedOperationException(
                        "Division not supported for unit type: " + this.unit.getClass().getSimpleName());
            }
        }

        double baseThis = this.unit.convertToBaseUnit(this.value);
        double baseOther = other.getUnit().convertToBaseUnit(other.getValue());
        return operation.compute(baseThis, baseOther);
    }

    public Quantity<U> add(Quantity<? extends IMeasurable> other) {
        validateArithmeticOperands(other, this.unit, false);
        double baseResult = performArithmetic(other, ArithmeticOperation.ADD);
        double resultInThisUnit = this.unit.convertFromBaseUnit(baseResult);
        double rounded = Math.round(resultInThisUnit * ROUND_SCALE) / ROUND_SCALE;
        return new Quantity<>(rounded, this.unit);
    }

    public Quantity<U> add(Quantity<? extends IMeasurable> other, U targetUnit) {
        validateArithmeticOperands(other, targetUnit, true);
        // validate target unit supports the operation
        targetUnit.validateOperationSupport(ArithmeticOperation.ADD.name());

        double baseResult = performArithmetic(other, ArithmeticOperation.ADD);
        double resultInTarget = targetUnit.convertFromBaseUnit(baseResult);
        double rounded = Math.round(resultInTarget * ROUND_SCALE) / ROUND_SCALE;
        return new Quantity<>(rounded, targetUnit);
    }

    public Quantity<U> subtract(Quantity<? extends IMeasurable> other) {
        validateArithmeticOperands(other, this.unit, false);
        double baseResult = performArithmetic(other, ArithmeticOperation.SUBTRACT);
        double resultInThisUnit = this.unit.convertFromBaseUnit(baseResult);
        double rounded = Math.round(resultInThisUnit * ROUND_SCALE) / ROUND_SCALE;
        return new Quantity<>(rounded, this.unit);
    }

    public Quantity<U> subtract(Quantity<? extends IMeasurable> other, U targetUnit) {
        validateArithmeticOperands(other, targetUnit, true);
        targetUnit.validateOperationSupport(ArithmeticOperation.SUBTRACT.name());

        double baseResult = performArithmetic(other, ArithmeticOperation.SUBTRACT);
        double resultInTarget = targetUnit.convertFromBaseUnit(baseResult);
        double rounded = Math.round(resultInTarget * ROUND_SCALE) / ROUND_SCALE;
        return new Quantity<>(rounded, targetUnit);
    }

    public double divide(Quantity<? extends IMeasurable> other) {
        validateArithmeticOperands(other, null, false);
        // performArithmetic will validate operation support and check division support
        return performArithmetic(other, ArithmeticOperation.DIVIDE);
    }

    @Override
    public int hashCode() {
        long normalized = Math.round(unit.convertToBaseUnit(value) / EPSILON);
        return Long.hashCode(normalized);
    }

    @Override
    public String toString() {
        return String.format("%s %s", Double.toString(value).replace("\\.0+$", ""), unit.getUnitName());
    }
}