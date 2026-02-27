package com.quantitymeasurement;

public final class Quantity<U extends IMeasurable> {

	private final double value;
	private final U unit;
	private static final double EPSILON = 1e-6;
	private static final double ROUND_SCALE = 1e6;

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

	public Quantity<U> add(Quantity<U> other) {
		if (other == null) {
			throw new IllegalArgumentException("Operand cannot be null");
		}

		double sumBase = unit.convertToBaseUnit(value) + other.unit.convertToBaseUnit(other.value);
		double result = unit.convertFromBaseUnit(sumBase);
		return new Quantity<>(result, unit);
	}

	public Quantity<U> add(Quantity<U> other, U targetUnit) {
		if (other == null) {
			throw new IllegalArgumentException("Operand cannot be null");
		}

		if (targetUnit == null) {
			throw new IllegalArgumentException("Target unit cannot be null");
		}

		double sumBase = unit.convertToBaseUnit(value) + other.unit.convertToBaseUnit(other.value);
		double result = targetUnit.convertFromBaseUnit(sumBase);
		return new Quantity<>(result, targetUnit);
	}

	private void validateOperand(Quantity<U> other) {
		if (other == null) {
			throw new IllegalArgumentException("Other quantity must not be null");
		}
		if (this.unit == null || other.unit == null) {
			throw new IllegalArgumentException("Unit must not be null");
		}
		if (Double.isNaN(this.value) || Double.isInfinite(this.value) || 
			Double.isNaN(other.value) || Double.isInfinite(other.value)) {
			throw new IllegalArgumentException("Values must be finite numbers");
		}
		if (this.unit.getClass() != other.unit.getClass()) {
			throw new IllegalArgumentException("Cannot operate across different measurement categories");
		}
	}

	public Quantity<U> subtract(Quantity<U> other) {
		validateOperand(other);
		
		double baseThis = this.unit.convertToBaseUnit(this.value);
		double baseOther = other.unit.convertToBaseUnit(other.value);
		double baseResult = baseThis - baseOther;
		double resultInThisUnit = this.unit.convertFromBaseUnit(baseResult);
		
		double rounded = Math.round(resultInThisUnit * ROUND_SCALE) / ROUND_SCALE;
		return new Quantity<>(rounded, this.unit);
	}

	public Quantity<U> subtract(Quantity<U> other, U targetUnit) {
		validateOperand(other);
		
		if (targetUnit == null) {
			throw new IllegalArgumentException("Target unit cannot be null");
		}
		
		if (targetUnit.getClass() != this.unit.getClass()) {
			throw new IllegalArgumentException("Target unit must belong to same measurement category");
		}
		
		double baseThis = this.unit.convertToBaseUnit(this.value);
		double baseOther = other.unit.convertToBaseUnit(other.value);
		double baseResult = baseThis - baseOther;
		double resultInTarget = targetUnit.convertFromBaseUnit(baseResult);
		
		double rounded = Math.round(resultInTarget * ROUND_SCALE) / ROUND_SCALE;
		return new Quantity<>(rounded, targetUnit);
	}

	public double divide(Quantity<U> other) {
		validateOperand(other);
		
		double baseThis = this.unit.convertToBaseUnit(this.value);
		double baseOther = other.unit.convertToBaseUnit(other.value);
		
		if (baseOther == 0.0) {
			throw new ArithmeticException("Division by zero quantity");
		}
		
		return baseThis / baseOther;
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