package com.quantitymeasurement;

public class Length {

	private final double value;
	private final LengthUnit unit;
	private static final double EPSILON = 1e-6;

	public enum LengthUnit {

		FEET(12.0), 
		INCHES(1.0),
		YARDS(36.0),
		CENTIMETERS(0.393701);

		private final double conversionFactor;

		LengthUnit(double conversionFactor) {
			this.conversionFactor = conversionFactor;
		}

		public double getConversionFactor() {
			return conversionFactor;
		}
	}

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

	private double convertToBaseUnit() {
		return value * unit.getConversionFactor();
	}

	private boolean compare(Length that) {
		double thisValue = this.convertToBaseUnit();
		double thatValue = that.convertToBaseUnit();
		return Math.abs(thisValue - thatValue) < EPSILON;
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

	@Override
	public int hashCode() {
		long normalized = Math.round(convertToBaseUnit() / EPSILON);
		return Long.hashCode(normalized);
	}

	@Override
	public String toString() {
		return String.format("%.2f %s", value, unit);
	}

	public Length convertTo(LengthUnit targetUnit) {
		if (targetUnit == null) {
			throw new IllegalArgumentException("Target unit cannot be null");
		}
		double baseValue = this.convertToBaseUnit();
		double convertedValue = baseValue / targetUnit.getConversionFactor();

		convertedValue = Math.round(convertedValue * 100.0) / 100.0;
		return new Length(convertedValue, targetUnit);
	}
	
	public static double convert(double value, LengthUnit source, LengthUnit target) {
	    if (source == null || target == null) {
	        throw new IllegalArgumentException("Units cannot be null");
	    }
	    if (!Double.isFinite(value)) {
	        throw new IllegalArgumentException("Value must be a finite number");
	    }

	    double baseValue = value * source.getConversionFactor();
	    double convertedValue = baseValue / target.getConversionFactor();
	    return Math.round(convertedValue * 100.0) / 100.0;
	}

	public static void main(String[] args) {
		Length length1 = new Length(1.0, LengthUnit.FEET);
		Length length2 = new Length(12.0, LengthUnit.INCHES);
		System.out.println("Are lengths equal? " + length1.equals(length2));

		Length length3 = new Length(1.0, LengthUnit.YARDS);
		Length length4 = new Length(36.0, LengthUnit.INCHES);
		System.out.println("Are lengths equal? " + length3.equals(length4));

		Length length5 = new Length(100.0, LengthUnit.CENTIMETERS);
		Length length6 = new Length(39.3701, LengthUnit.INCHES);
		System.out.println("Are lengths equal? " + length5.equals(length6));

		System.out.println("Convert 3 Feet to Inches: " + length1.convertTo(LengthUnit.INCHES));
		System.out.println("Convert 2 Yards to Inches: " + length3.convertTo(LengthUnit.INCHES));
		System.out.println("Convert 30.48 cm to Feet: " + new Length(30.48, LengthUnit.CENTIMETERS).convertTo(LengthUnit.FEET));
		System.out.println("Convert 72 Inches to Yards: " + new Length(72.0, LengthUnit.INCHES).convertTo(LengthUnit.YARDS));
		System.out.println("Convert 0 Feet to Inches: " + new Length(0.0, LengthUnit.FEET).convertTo(LengthUnit.INCHES));
		System.out.println("Convert -1 Foot to Inches: " + new Length(-1.0, LengthUnit.FEET).convertTo(LengthUnit.INCHES));
	}
}