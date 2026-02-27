package com.quantitymeasurement;

public final class Quantity<U extends IMeasurable> {

	private final double value;
	private final U unit;
	private static final double EPSILON = 1e-6;

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