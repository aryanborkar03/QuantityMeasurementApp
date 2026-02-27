package com.quantitymeasurement;

public class QuantityMeasurementApp {

	public static <U extends IMeasurable> boolean demonstrateEquality(Quantity<U> q1, Quantity<U> q2) {
		boolean result = q1.equals(q2);
		System.out.println(q1 + " == " + q2 + " ? " + result);
		return result;
	}

	public static <U extends IMeasurable> Quantity<U> demonstrateConversion(Quantity<U> source, U targetUnit) {
		Quantity<U> converted = source.convertTo(targetUnit);
		System.out.println(source + " -> " + converted);
		return converted;
	}

	public static <U extends IMeasurable> Quantity<U> demonstrateAddition(Quantity<U> q1, Quantity<U> q2) {
		Quantity<U> sum = q1.add(q2);
		System.out.println(q1 + " + " + q2 + " = " + sum);
		return sum;
	}

	public static <U extends IMeasurable> Quantity<U> demonstrateAddition(Quantity<U> q1, Quantity<U> q2,
			U targetUnit) {
		Quantity<U> sum = q1.add(q2, targetUnit);
		System.out.println(q1 + " + " + q2 + " in " + targetUnit.getUnitName() + " = " + sum);
		return sum;
	}

	// Main method
	public static void main(String[] args) {
		demonstrateEquality(new Quantity<>(1.0, WeightUnit.KILOGRAM), new Quantity<>(1000.0, WeightUnit.GRAM));
		demonstrateEquality(new Quantity<>(2.204624, WeightUnit.POUND), new Quantity<>(1.0, WeightUnit.KILOGRAM));
		demonstrateEquality(new Quantity<>(453.592, WeightUnit.GRAM), new Quantity<>(1.0, WeightUnit.POUND));
		demonstrateEquality(new Quantity<>(1.0, WeightUnit.KILOGRAM), new Quantity<>(1.0, WeightUnit.KILOGRAM));
		demonstrateEquality(new Quantity<>(2.0, WeightUnit.POUND), new Quantity<>(2.0, WeightUnit.POUND));
		demonstrateEquality(new Quantity<>(500.0, WeightUnit.GRAM), new Quantity<>(0.5, WeightUnit.KILOGRAM));

		demonstrateConversion(new Quantity<>(1.0, WeightUnit.KILOGRAM), WeightUnit.GRAM);
		demonstrateConversion(new Quantity<>(2.0, WeightUnit.POUND), WeightUnit.KILOGRAM);
		demonstrateConversion(new Quantity<>(500.0, WeightUnit.GRAM), WeightUnit.POUND);
		demonstrateConversion(new Quantity<>(0.0, WeightUnit.KILOGRAM), WeightUnit.GRAM);
		demonstrateConversion(new Quantity<>(-1.0, WeightUnit.KILOGRAM), WeightUnit.GRAM);

		demonstrateAddition(new Quantity<>(1.0, WeightUnit.KILOGRAM), new Quantity<>(2.0, WeightUnit.KILOGRAM));
		demonstrateAddition(new Quantity<>(1.0, WeightUnit.KILOGRAM), new Quantity<>(1000.0, WeightUnit.GRAM));
		demonstrateAddition(new Quantity<>(500.0, WeightUnit.GRAM), new Quantity<>(0.5, WeightUnit.KILOGRAM));
		demonstrateAddition(new Quantity<>(1.0, WeightUnit.KILOGRAM), new Quantity<>(1000.0, WeightUnit.GRAM), WeightUnit.GRAM);
		demonstrateAddition(new Quantity<>(1.0, WeightUnit.POUND), new Quantity<>(453.592, WeightUnit.GRAM), WeightUnit.POUND);
		demonstrateAddition(new Quantity<>(2.0, WeightUnit.KILOGRAM), new Quantity<>(4.0, WeightUnit.POUND), WeightUnit.KILOGRAM);

		System.out.println("Weight vs Length equality: " + new Quantity<>(1.0, WeightUnit.KILOGRAM).equals(new Quantity<>(1.0, LengthUnit.FEET)));
		demonstrateEquality(new Quantity<>(1.0, LengthUnit.FEET), new Quantity<>(1.0, WeightUnit.KILOGRAM));

		demonstrateEquality(new Quantity<>(1.0, LengthUnit.FEET), new Quantity<>(12.0, LengthUnit.INCHES));
		demonstrateEquality(new Quantity<>(1.0, LengthUnit.YARDS), new Quantity<>(36.0, LengthUnit.INCHES));
		demonstrateEquality(new Quantity<>(100.0, LengthUnit.CENTIMETERS), new Quantity<>(39.370078, LengthUnit.INCHES));
		demonstrateEquality(new Quantity<>(3.0, LengthUnit.FEET), new Quantity<>(1.0, LengthUnit.YARDS));
		demonstrateEquality(new Quantity<>(30.48, LengthUnit.CENTIMETERS), new Quantity<>(1.0, LengthUnit.FEET));

		demonstrateConversion(new Quantity<>(1.0, LengthUnit.FEET), LengthUnit.INCHES);
		demonstrateConversion(new Quantity<>(3.0, LengthUnit.YARDS), LengthUnit.FEET);
		demonstrateConversion(new Quantity<>(36.0, LengthUnit.INCHES), LengthUnit.YARDS);
		demonstrateConversion(new Quantity<>(30.48, LengthUnit.CENTIMETERS), LengthUnit.FEET);
		demonstrateConversion(new Quantity<>(-1.0, LengthUnit.FEET), LengthUnit.INCHES);

		demonstrateAddition(new Quantity<>(1.0, LengthUnit.FEET), new Quantity<>(12.0, LengthUnit.INCHES));
		demonstrateAddition(new Quantity<>(12.0, LengthUnit.INCHES), new Quantity<>(1.0, LengthUnit.FEET));
		demonstrateAddition(new Quantity<>(1.0, LengthUnit.YARDS), new Quantity<>(3.0, LengthUnit.FEET));
		demonstrateAddition(new Quantity<>(2.54, LengthUnit.CENTIMETERS), new Quantity<>(1.0, LengthUnit.INCHES));
		demonstrateAddition(new Quantity<>(5.0, LengthUnit.FEET), new Quantity<>(0.0, LengthUnit.INCHES));
		demonstrateAddition(new Quantity<>(5.0, LengthUnit.FEET), new Quantity<>(-2.0, LengthUnit.FEET));
		
		demonstrateAddition(new Quantity<>(1.0, LengthUnit.FEET), new Quantity<>(12.0, LengthUnit.INCHES), LengthUnit.FEET);
		demonstrateAddition(new Quantity<>(1.0, LengthUnit.FEET), new Quantity<>(12.0, LengthUnit.INCHES), LengthUnit.INCHES);
		demonstrateAddition(new Quantity<>(1.0, LengthUnit.FEET), new Quantity<>(12.0, LengthUnit.INCHES), LengthUnit.YARDS);
		demonstrateAddition(new Quantity<>(1.0, LengthUnit.YARDS), new Quantity<>(3.0, LengthUnit.FEET), LengthUnit.YARDS);
		demonstrateAddition(new Quantity<>(36.0, LengthUnit.INCHES), new Quantity<>(1.0, LengthUnit.YARDS), LengthUnit.FEET);
		demonstrateAddition(new Quantity<>(2.54, LengthUnit.CENTIMETERS), new Quantity<>(1.0, LengthUnit.INCHES), LengthUnit.CENTIMETERS);
		demonstrateAddition(new Quantity<>(5.0, LengthUnit.FEET), new Quantity<>(0.0, LengthUnit.INCHES), LengthUnit.YARDS);
		demonstrateAddition(new Quantity<>(5.0, LengthUnit.FEET), new Quantity<>(-2.0, LengthUnit.FEET), LengthUnit.INCHES);
	}
}