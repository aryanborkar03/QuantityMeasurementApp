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

	public static <U extends IMeasurable> Quantity<U> demonstrateAddition(Quantity<U> q1, Quantity<U> q2, U targetUnit) {
		Quantity<U> sum = q1.add(q2, targetUnit);
		System.out.println(q1 + " + " + q2 + " in " + targetUnit.getUnitName() + " = " + sum);
		return sum;
	}
	
	public static <U extends IMeasurable> Quantity<U> demonstrateSubtraction(Quantity<U> a, Quantity<U> b) {
	    Quantity<U> result = a.subtract(b);
	    System.out.println(a + " - " + b + " = " + result);
	    return result;
	}

	public static <U extends IMeasurable> Quantity<U> demonstrateSubtraction(Quantity<U> a, Quantity<U> b, U targetUnit) {
	    Quantity<U> result = a.subtract(b, targetUnit);
	    System.out.println(a + " - " + b + " in " + targetUnit.getUnitName() + " = " + result);
	    return result;
	}

	public static <U extends IMeasurable> double demonstrateDivision(Quantity<U> a, Quantity<U> b) {
	    double result = a.divide(b);
	    System.out.println(a + " / " + b + " = " + result);
	    return result;
	}

	// Main method
	public static void main(String[] args) {
		demonstrateSubtraction(new Quantity<>(10.0, LengthUnit.FEET), new Quantity<>(6.0, LengthUnit.INCHES));
		demonstrateSubtraction(new Quantity<>(10.0, WeightUnit.KILOGRAM), new Quantity<>(5000.0, WeightUnit.GRAM));
		demonstrateSubtraction(new Quantity<>(5.0, VolumeUnit.LITRE), new Quantity<>(500.0, VolumeUnit.MILLILITRE));

		demonstrateSubtraction(new Quantity<>(10.0, LengthUnit.FEET), new Quantity<>(6.0, LengthUnit.INCHES), LengthUnit.INCHES);

		demonstrateDivision(new Quantity<>(10.0, LengthUnit.FEET), new Quantity<>(2.0, LengthUnit.FEET));
		demonstrateDivision(new Quantity<>(12.0, LengthUnit.INCHES), new Quantity<>(1.0, LengthUnit.FEET));
		demonstrateDivision(new Quantity<>(10.0, WeightUnit.KILOGRAM), new Quantity<>(5.0, WeightUnit.KILOGRAM));
		demonstrateDivision(new Quantity<>(5.0, VolumeUnit.LITRE), new Quantity<>(10.0, VolumeUnit.LITRE));

		
        demonstrateEquality(new Quantity<>(1.0, VolumeUnit.LITRE), new Quantity<>(1000.0, VolumeUnit.MILLILITRE));
        demonstrateEquality(new Quantity<>(3.78541, VolumeUnit.LITRE), new Quantity<>(1.0, VolumeUnit.GALLON));
        demonstrateConversion(new Quantity<>(1.0, VolumeUnit.LITRE), VolumeUnit.MILLILITRE);
        demonstrateConversion(new Quantity<>(1.0, VolumeUnit.GALLON), VolumeUnit.LITRE);
        demonstrateConversion(new Quantity<>(1000.0, VolumeUnit.MILLILITRE), VolumeUnit.GALLON);

        demonstrateAddition(new Quantity<>(1.0, VolumeUnit.LITRE), new Quantity<>(1000.0, VolumeUnit.MILLILITRE));
        demonstrateAddition(new Quantity<>(1.0, VolumeUnit.LITRE), new Quantity<>(1.0, VolumeUnit.GALLON), VolumeUnit.MILLILITRE);
        demonstrateAddition(new Quantity<>(1000.0, VolumeUnit.MILLILITRE), new Quantity<>(1.0, VolumeUnit.GALLON), VolumeUnit.GALLON);

        System.out.println("Volume vs Length equality: " + new Quantity<>(1.0, VolumeUnit.LITRE).equals(new Quantity<>(1.0, LengthUnit.FEET)));
        System.out.println("Volume vs Weight equality: " + new Quantity<>(1.0, VolumeUnit.LITRE).equals(new Quantity<>(1.0, WeightUnit.KILOGRAM)));
        System.out.println("Weight vs Length equality: " + new Quantity<>(1.0, WeightUnit.KILOGRAM).equals(new Quantity<>(1.0, LengthUnit.FEET)));

		
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