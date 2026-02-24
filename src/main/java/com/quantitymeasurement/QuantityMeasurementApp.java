package com.quantitymeasurement;

public class QuantityMeasurementApp {

	public static boolean demonstrateLengthEquality(Length l1, Length l2) {
		return l1.equals(l2);
	}

	public static boolean demonstrateLengthComparison(double value1, LengthUnit unit1, 
													  double value2, LengthUnit unit2) {
		Length l1 = new Length(value1, unit1);
		Length l2 = new Length(value2, unit2);
		boolean result = l1.equals(l2);
		System.out.println(l1 + " == " + l2 + " ? " + result);
		return result;
	}

	public static Length demonstrateLengthConversion(double value, LengthUnit fromUnit, 
																   LengthUnit toUnit) { 
		Length source = new Length(value, fromUnit); 
		Length converted = source.convertTo(toUnit); 
		System.out.println(source + " -> " + converted); 
		return converted; 
	}
	
	
	public static Length demonstrateLengthConversion(Length length, LengthUnit toUnit) { 
		Length converted = length.convertTo(toUnit); 
		System.out.println(length + " -> " + converted); 
		return converted; 
	}
	
	
	public static Length demonstrateLengthAddition(Length length1, Length length2) {
	    Length sum = length1.add(length2); 
	    System.out.println(length1 + " + " + length2 + " = " + sum);
	    return sum;
	}

    public static Length demonstrateLengthAddition(Length length1, Length length2, LengthUnit targetUnit) {
        Length sum = length1.add(length2, targetUnit);
        System.out.println(length1 + " + " + length2 + " in " + targetUnit + " = " + sum);
        return sum;
    }
	
	public static void main(String[] args) {
		demonstrateLengthComparison(1.0, LengthUnit.FEET, 12.0, LengthUnit.INCHES);
		demonstrateLengthComparison(1.0, LengthUnit.YARDS, 36.0, LengthUnit.INCHES);
		demonstrateLengthComparison(100.0, LengthUnit.CENTIMETERS, 39.3701, LengthUnit.INCHES);
		demonstrateLengthComparison(3.0, LengthUnit.FEET, 1.0, LengthUnit.YARDS);
		demonstrateLengthComparison(30.48, LengthUnit.CENTIMETERS, 1.0, LengthUnit.FEET);
		
		demonstrateLengthConversion(1.0, LengthUnit.FEET, LengthUnit.INCHES); 
		demonstrateLengthConversion(3.0, LengthUnit.YARDS, LengthUnit.FEET); 
		demonstrateLengthConversion(36.0, LengthUnit.INCHES, LengthUnit.YARDS); 
		demonstrateLengthConversion(30.48, LengthUnit.CENTIMETERS,LengthUnit.FEET); 
		
		demonstrateLengthConversion(new Length(-1.0, LengthUnit.FEET), LengthUnit.INCHES);

		demonstrateLengthAddition(
			new Length(1.0, LengthUnit.FEET),
			new Length(12.0, LengthUnit.INCHES)
		);
		
		demonstrateLengthAddition(
			new Length(12.0, LengthUnit.INCHES),
			new Length(1.0, LengthUnit.FEET)
		);
		
		demonstrateLengthAddition(
			new Length(1.0, LengthUnit.YARDS),
			new Length(3.0, LengthUnit.FEET)
		);
		
		demonstrateLengthAddition(
			new Length(2.54, LengthUnit.CENTIMETERS),
			new Length(1.0, LengthUnit.INCHES)
		);
		
		demonstrateLengthAddition(
			new Length(5.0, LengthUnit.FEET),
			new Length(0.0, LengthUnit.INCHES)
		);
		
		demonstrateLengthAddition(
			new Length(5.0, LengthUnit.FEET),
			new Length(-2.0, LengthUnit.FEET)
		);
		
		demonstrateLengthAddition(
		    new Length(1.0, LengthUnit.FEET),
		    new Length(12.0, LengthUnit.INCHES),
		    LengthUnit.FEET
		);
		
		demonstrateLengthAddition(
			new Length(1.0, LengthUnit.FEET),                                  
			new Length(12.0, LengthUnit.INCHES),
			LengthUnit.INCHES
		);
		
		demonstrateLengthAddition(
			new Length(1.0, LengthUnit.FEET),
	        new Length(12.0, LengthUnit.INCHES),
		    LengthUnit.YARDS
		);
		
		demonstrateLengthAddition(
		    new Length(1.0, LengthUnit.YARDS),
			new Length(3.0, LengthUnit.FEET),
		    LengthUnit.YARDS
		); 
		
		demonstrateLengthAddition(
			new Length(36.0, LengthUnit.INCHES),
			new Length(1.0, LengthUnit.YARDS),
			LengthUnit.FEET
		);
		
		demonstrateLengthAddition(
			new Length(2.54, LengthUnit.CENTIMETERS),
			new Length(1.0, LengthUnit.INCHES),
			LengthUnit.CENTIMETERS
		);
		
		demonstrateLengthAddition(
			new Length(5.0, LengthUnit.FEET),
			new Length(0.0, LengthUnit.INCHES),
			LengthUnit.YARDS
		);
		
		demonstrateLengthAddition(
			new Length(5.0, LengthUnit.FEET),
			new Length(-2.0, LengthUnit.FEET),
			LengthUnit.INCHES
		);
	}
}