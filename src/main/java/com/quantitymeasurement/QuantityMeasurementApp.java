package com.quantitymeasurement;

public class QuantityMeasurementApp {

	public static boolean demonstrateLengthEquality(Length l1, Length l2) {
		return l1.equals(l2);
	}

	public static void demonstrateFeetEquality() {
		Length feet1 = new Length(1.0, Length.LengthUnit.FEET);
		Length feet2 = new Length(1.0, Length.LengthUnit.FEET);
		System.out.println("Feet equality: " + demonstrateLengthEquality(feet1, feet2));
	}

	public static void demonstrateInchesEquality() {
		Length inch1 = new Length(1.0, Length.LengthUnit.INCHES);
		Length inch2 = new Length(1.0, Length.LengthUnit.INCHES);
		System.out.println("Inches equality: " + demonstrateLengthEquality(inch1, inch2));
	}

	public static void demonstrateFeetInchesComparison() {
		Length feet1 = new Length(1.0, Length.LengthUnit.FEET);
		Length inch12 = new Length(12.0, Length.LengthUnit.INCHES);
		System.out.println("Feet vs Inches equality: " + demonstrateLengthEquality(feet1, inch12));
	}

	public static void main(String[] args) {
		demonstrateFeetEquality();
		demonstrateInchesEquality();
		demonstrateFeetInchesComparison();
	}
}