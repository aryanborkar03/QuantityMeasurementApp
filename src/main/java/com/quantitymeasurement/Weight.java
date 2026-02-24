package com.quantitymeasurement;

public class Weight {

	private final double value;
	private final WeightUnit unit;
	private static final double EPSILON = 1e-6;

	public Weight(double value, WeightUnit unit) {
		if (unit == null) {
			throw new IllegalArgumentException("Unit cannot be null");
		}
		if (!Double.isFinite(value)) {
			throw new IllegalArgumentException("value must be a finite number");
		}

		this.value = value;
		this.unit = unit;
	}
	
	public double getValue() {
		return value;
	}
	
	public WeightUnit getUnit() {
		return unit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Weight that = (Weight) o;
		return compare(that);
	}

	public Weight convertTo(WeightUnit targetUnit) {
		if (targetUnit == null) {
			throw new IllegalArgumentException("Target unit cannot be null");
		}
		double baseValue = this.convertToBaseUnit();
		double convertedValue = targetUnit.convertFromBaseUnit(baseValue);

		convertedValue = Math.round(convertedValue * 1_000_000.0) / 1_000_000.0;
		return new Weight(convertedValue, targetUnit);
	}

	public Weight add(Weight thatWeight) {
		if (thatWeight == null) {
			throw new IllegalArgumentException("Operand cannot be null");
		}
		double sumInBase = this.convertToBaseUnit() + thatWeight.convertToBaseUnit();
		double sumInTargetUnit = convertFromBaseToTargetUnit(sumInBase, this.unit);
		return new Weight(sumInTargetUnit, this.unit);
	}

	public Weight add(Weight other, WeightUnit targetUnit) {
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

	private boolean compare(Weight that) {
		double thisValue = this.convertToBaseUnit();
		double thatValue = that.convertToBaseUnit();
		return Math.abs(thisValue - thatValue) < EPSILON;
	}

	private Weight addAndConvert(Weight other, WeightUnit targetUnit) {
		double sumInBase = this.convertToBaseUnit() + other.convertToBaseUnit();
		double sumInTargetUnit = convertFromBaseToTargetUnit(sumInBase, targetUnit);
		return new Weight(sumInTargetUnit, targetUnit);
	}

	private double convertToBaseUnit() {
		return unit.convertToBaseUnit(value);
	}

	private double convertFromBaseToTargetUnit(double weightKilograms,
            								   WeightUnit targetUnit) {
		if (targetUnit == null) {
			throw new IllegalArgumentException("Target unit cannot be null");
		}
		return targetUnit.convertFromBaseUnit(weightKilograms);
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
		Weight weight1 = new Weight(1.0, WeightUnit.KILOGRAM);
		Weight weight2 = new Weight(1000.0, WeightUnit.GRAM);
		System.out.println("Are weights equal? " + weight1.equals(weight2)); // true

		Weight weight3 = new Weight(2.204624, WeightUnit.POUND);
		Weight weight4 = new Weight(1.0, WeightUnit.KILOGRAM);
		System.out.println("Are weights equal? " + weight3.equals(weight4)); // true

		Weight weight5 = new Weight(453.592, WeightUnit.GRAM);
		Weight weight6 = new Weight(1.0, WeightUnit.POUND);
		System.out.println("Are weights equal? " + weight5.equals(weight6)); // true

		System.out.println("Convert 1 kg to grams: " + weight1.convertTo(WeightUnit.GRAM));
		System.out.println("Convert 1000 g to kg: " + weight2.convertTo(WeightUnit.KILOGRAM));

		System.out.println("Convert 2 pounds to kg: " + new Weight(2.0, WeightUnit.POUND).convertTo(WeightUnit.KILOGRAM));
		System.out.println("Convert 500 g to pounds: " + new Weight(500.0, WeightUnit.GRAM).convertTo(WeightUnit.POUND));
		System.out.println("Convert 0 kg to grams: " + new Weight(0.0, WeightUnit.KILOGRAM).convertTo(WeightUnit.GRAM));
		System.out.println("Convert -1 kg to grams: " + new Weight(-1.0, WeightUnit.KILOGRAM).convertTo(WeightUnit.GRAM));

		System.out.println("Add 1 kg + 1000 g = " + weight1.add(weight2));
		System.out.println("Add 1000 g + 1 kg = " + weight2.add(weight1));
		System.out.println("Add 1 pound + 1 kg = " + weight6.add(weight1));
		
		System.out.println("Add 500 g + 0.5 kg = " + new Weight(500.0, WeightUnit.GRAM).add(new Weight(0.5, WeightUnit.KILOGRAM)));
		System.out.println("Add 5 kg + 0 g = " + new Weight(5.0, WeightUnit.KILOGRAM).add(new Weight(0.0, WeightUnit.GRAM)));
		System.out.println("Add 5 kg + (-2 kg) = " + new Weight(5.0, WeightUnit.KILOGRAM).add(new Weight(-2.0, WeightUnit.KILOGRAM)));
		System.out.println("Add Large Values: " + new Weight(1e6, WeightUnit.KILOGRAM).add(new Weight(1e6, WeightUnit.KILOGRAM)));

		Weight result;

		result = new Weight(1.0, WeightUnit.KILOGRAM).add(new Weight(1000.0, WeightUnit.GRAM), WeightUnit.KILOGRAM);
		System.out.println("Add (1 kg, 1000 g, KG) = " + result);

		result = new Weight(1.0, WeightUnit.KILOGRAM).add(new Weight(1000.0, WeightUnit.GRAM), WeightUnit.GRAM);
		System.out.println("Add (1 kg, 1000 g, GRAM) = " + result);

		result = new Weight(1.0, WeightUnit.KILOGRAM).add(new Weight(1000.0, WeightUnit.GRAM), WeightUnit.POUND);
		System.out.println("Add (1 kg, 1000 g, POUND) = " + result);

		result = new Weight(1.0, WeightUnit.POUND).add(new Weight(453.592, WeightUnit.GRAM), WeightUnit.POUND);
		System.out.println("Add (1 lb, 453.592 g, POUND) = " + result);

		result = new Weight(5.0, WeightUnit.KILOGRAM).add(new Weight(0.0, WeightUnit.GRAM), WeightUnit.POUND);
		System.out.println("Add (5 kg, 0 g, POUND) = " + result);

		result = new Weight(5.0, WeightUnit.KILOGRAM).add(new Weight(-2.0, WeightUnit.KILOGRAM), WeightUnit.GRAM);
		System.out.println("Add (5 kg, -2 kg, GRAM) = " + result);
	}
}