package com.quantitymeasurement;

public class Length {

	private final double value;
	private final LengthUnit unit;
	private static final double EPSILON = 1e-6;

	public Length(double value, LengthUnit unit) {
		if (unit == null) {
			throw new IllegalArgumentException("Unit cannot be null");
		}
		if (!Double.isFinite(value)) {
			throw new IllegalArgumentException("value must be a finite number");
		}

		this.value = value;
		this.unit = unit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Length that = (Length) o;
		return compare(that);
	}

	public Length convertTo(LengthUnit targetUnit) {
		if (targetUnit == null) {
			throw new IllegalArgumentException("Target unit cannot be null");
		}
		double baseValue = this.convertToBaseUnit();
		double convertedValue = targetUnit.convertFromBaseUnit(baseValue);

		convertedValue = Math.round(convertedValue * 1000000.0) / 1000000.0;
		return new Length(convertedValue, targetUnit);
	}

	public Length add(Length thatLength) {
		if (thatLength == null) {
			throw new IllegalArgumentException("Operand cannot be null");
		}
		double sumInBase = this.convertToBaseUnit() + thatLength.convertToBaseUnit();
		double sumInTargetUnit = convertFromBaseToTargetUnit(sumInBase, this.unit);
		return new Length(sumInTargetUnit, this.unit);
	}

	public Length add(Length other, LengthUnit targetUnit) {
		if (other == null) {
			throw new IllegalArgumentException("Operand cannot be null");
		}
		if (targetUnit == null) {
			throw new IllegalArgumentException("Target unit cannot be null");
		}
		if (!Double.isFinite(this.value) || !Double.isFinite(other.value)) {
			throw new IllegalArgumentException("Values must be a finite number");
		}
		return addAndConvert(other, targetUnit);
	}

	private boolean compare(Length that) {
		double thisValue = this.convertToBaseUnit();
		double thatValue = that.convertToBaseUnit();
		return Math.abs(thisValue - thatValue) < EPSILON;
	}

	private Length addAndConvert(Length other, LengthUnit targetUnit) {
		double sumInBase = this.convertToBaseUnit() + other.convertToBaseUnit();
		double sumInTargetUnit = convertFromBaseToTargetUnit(sumInBase, targetUnit);
		return new Length(sumInTargetUnit, targetUnit);
	}

	private double convertToBaseUnit() {
	    return unit.convertToBaseUnit(value);
	}

	private double convertFromBaseToTargetUnit(double lengthInInches, LengthUnit targetUnit) {
		if (targetUnit == null) {
			throw new IllegalArgumentException("Target unit cannot be null");
		}
		double convertedValue = lengthInInches / targetUnit.getConversionFactor();
		return Math.round(convertedValue * 1000000.0) / 1000000.0;
	}

	@Override
	public String toString() {
		return String.format("%s %s", Double.toString(value).replaceAll("\\.0+$", ""), unit);
	}

	@Override
	public int hashCode() {
		long normalized = Math.round(convertToBaseUnit() / EPSILON);
		return Long.hashCode(normalized);
	}

	public static void main(String[] args) {
		Length length1 = new Length(1.0, LengthUnit.FEET);
		Length length2 = new Length(12.0, LengthUnit.INCHES);
		System.out.println("Are lengths equal? " + length1.equals(length2)); // true

		Length length3 = new Length(1.0, LengthUnit.YARDS);
		Length length4 = new Length(36.0, LengthUnit.INCHES);
		System.out.println("Are lengths equal? " + length3.equals(length4)); // true

		Length length5 = new Length(1.0, LengthUnit.CENTIMETERS);
		Length length6 = new Length(1 / 2.54, LengthUnit.INCHES);
		System.out.println("Are lengths equal? " + length5.equals(length6)); // true

		System.out.println("Convert 3 Feet to Inches: " + length1.convertTo(LengthUnit.INCHES));
		System.out.println("Convert 2 Yards to Inches: " + length3.convertTo(LengthUnit.INCHES));
		System.out.println(
				"Convert 30.48 cm to Feet: " + new Length(30.48, LengthUnit.CENTIMETERS).convertTo(LengthUnit.FEET));
		System.out.println(
				"Convert 72 Inches to Yards: " + new Length(72.0, LengthUnit.INCHES).convertTo(LengthUnit.YARDS));
		System.out
				.println("Convert 0 Feet to Inches: " + new Length(0.0, LengthUnit.FEET).convertTo(LengthUnit.INCHES));
		System.out.println(
				"Convert -1 Foot to Inches: " + new Length(-1.0, LengthUnit.FEET).convertTo(LengthUnit.INCHES));

		System.out.println("Add 1 Foot + 12 Inches = " + length1.add(length2));
		System.out.println("Add 12 Inches + 1 Foot = " + length2.add(length1));
		System.out.println("Add 1 Yard + 3 Feet = " + length3.add(new Length(3.0, LengthUnit.FEET)));
		System.out.println("Add 36 Inches + 1 Yard = " + length4.add(length3));
		System.out.println("Add 2.54 cm + 1 Inch = "
				+ new Length(2.54, LengthUnit.CENTIMETERS).add(new Length(1.0, LengthUnit.INCHES)));
		System.out.println(
				"Add 5 Feet + 0 Inches = " + new Length(5.0, LengthUnit.FEET).add(new Length(0.0, LengthUnit.INCHES)));
		System.out.println(
				"Add 5 Feet + (-2 Feet) = " + new Length(5.0, LengthUnit.FEET).add(new Length(-2.0, LengthUnit.FEET)));
		System.out
				.println("Add Large Values: " + new Length(1e6, LengthUnit.FEET).add(new Length(1e6, LengthUnit.FEET)));
		System.out.println(
				"Add Small Values: " + new Length(0.001, LengthUnit.FEET).add(new Length(0.002, LengthUnit.FEET)));

		Length result = new Length(1.0, LengthUnit.FEET).add(new Length(12.0, LengthUnit.INCHES), LengthUnit.FEET);
		System.out.println("Add (1.0 FEET, 12.0 INCHES, FEET) = " + result);

		result = new Length(1.0, LengthUnit.FEET).add(new Length(12.0, LengthUnit.INCHES), LengthUnit.INCHES);
		System.out.println("Add (1.0 FEET, 12.0 INCHES, INCHES) = " + result);

		result = new Length(1.0, LengthUnit.FEET).add(new Length(12.0, LengthUnit.INCHES), LengthUnit.YARDS);
		System.out.println("Add (1.0 FEET, 12.0 INCHES, YARDS) = " + result);

		result = new Length(1.0, LengthUnit.YARDS).add(new Length(3.0, LengthUnit.FEET), LengthUnit.YARDS);
		System.out.println("Add (1.0 YARDS, 3.0 FEET, YARDS) = " + result);

		result = new Length(36.0, LengthUnit.INCHES).add(new Length(1.0, LengthUnit.YARDS), LengthUnit.FEET);
		System.out.println("Add (36.0 INCHES, 1.0 YARDS, FEET) = " + result);

		result = new Length(2.54, LengthUnit.CENTIMETERS).add(new Length(1.0, LengthUnit.INCHES),
				LengthUnit.CENTIMETERS);
		System.out.println("Add (2.54 CM, 1.0 INCH, CM) = " + result);

		result = new Length(5.0, LengthUnit.FEET).add(new Length(0.0, LengthUnit.INCHES), LengthUnit.YARDS);
		System.out.println("Add (5.0 FEET, 0.0 INCHES, YARDS) = " + result);

		result = new Length(5.0, LengthUnit.FEET).add(new Length(-2.0, LengthUnit.FEET), LengthUnit.INCHES);
		System.out.println("Add (5.0 FEET, -2.0 FEET, INCHES) = " + result);
	}
}