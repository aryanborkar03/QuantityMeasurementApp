package com.quantitymeasurement;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LengthTest {

	@Test
	public void testEquality_YardToYard_SameValue() {
		Length yard1 = new Length(1.0, Length.LengthUnit.YARDS);
		Length yard2 = new Length(1.0, Length.LengthUnit.YARDS);
		assertEquals(yard1, yard2);
	}

	@Test
	public void testEquality_YardToYard_DifferentValue() {
		Length yard1 = new Length(1.0, Length.LengthUnit.YARDS);
		Length yard2 = new Length(2.0, Length.LengthUnit.YARDS);
		assertNotEquals(yard1, yard2);
	}

	@Test
	public void testEquality_YardToFeet_EquivalentValue() {
		Length yard1 = new Length(1.0, Length.LengthUnit.YARDS);
		Length feet3 = new Length(3.0, Length.LengthUnit.FEET);
		assertEquals(yard1, feet3);
	}

	@Test
	public void testEquality_FeetToYard_EquivalentValue() {
		Length feet3 = new Length(3.0, Length.LengthUnit.FEET);
		Length yard1 = new Length(1.0, Length.LengthUnit.YARDS);
		assertEquals(feet3, yard1);
	}

	@Test
	public void testEquality_YardToInches_EquivalentValue() {
		Length yard1 = new Length(1.0, Length.LengthUnit.YARDS);
		Length inch36 = new Length(36.0, Length.LengthUnit.INCHES);
		assertEquals(yard1, inch36);
	}

	@Test
	public void testEquality_InchesToYard_EquivalentValue() {
		Length inch36 = new Length(36.0, Length.LengthUnit.INCHES);
		Length yard1 = new Length(1.0, Length.LengthUnit.YARDS);
		assertEquals(inch36, yard1);
	}

	@Test
	public void testEquality_YardToFeet_NonEquivalentValue() {
		Length yard1 = new Length(1.0, Length.LengthUnit.YARDS);
		Length feet2 = new Length(2.0, Length.LengthUnit.FEET);
		assertNotEquals(yard1, feet2);
	}
	
	@Test
	public void testEquality_CentimetersToInches_EquivalentValue() {
		Length cm1 = new Length(1.0, Length.LengthUnit.CENTIMETERS);
		Length inchValue = new Length(0.393701, Length.LengthUnit.INCHES);
		assertEquals(cm1, inchValue);
	}
	
	@Test
	public void testEquality_CentimetersToFeet_NonEquivalentValue() {
		Length cm1 = new Length(1.0, Length.LengthUnit.CENTIMETERS);
		Length feet1 = new Length(1.0, Length.LengthUnit.FEET);
		assertNotEquals(cm1, feet1);
	}
	
	@Test
	public void testEquality_MultiUnit_TransitiveProperty() {
		Length yard = new Length(1.0, Length.LengthUnit.YARDS);
		Length feet = new Length(3.0, Length.LengthUnit.FEET);
		Length inches = new Length(36.0, Length.LengthUnit.INCHES);
		assertEquals(yard, feet);
		assertEquals(feet, inches);
		assertEquals(yard, inches);
	}
	
	@Test
	public void testEquality_NullUnit() {
		assertThrows(
			IllegalArgumentException.class, 
			() -> new Length(1.0, null)
		);
	}
	
	@Test
	public void testEquality_YardSameReference() {
		Length yard = new Length(2.0, Length.LengthUnit.YARDS);
		assertEquals(yard, yard);
	}

	@Test
	public void testEquality_YardNullComparison() {
		Length yard = new Length(2.0, Length.LengthUnit.YARDS);
		assertNotEquals(yard, null);
	}
	
	@Test
	public void testEquality_CentimetersSameReference() {
		Length cm = new Length(2.0, Length.LengthUnit.CENTIMETERS);
		assertEquals(cm, cm);
	}
	
	@Test
	public void testEquality_CentimetersNullComparison() {
		Length cm = new Length(2.0, Length.LengthUnit.CENTIMETERS);
		assertNotEquals(cm, null);
	}
	
	@Test
	public void testEquality_AllUnits_ComplexScenario() {
		Length yards = new Length(2.0, Length.LengthUnit.YARDS);
		Length feet = new Length(6.0, Length.LengthUnit.FEET);
		Length inches = new Length(72.0, Length.LengthUnit.INCHES);
		assertEquals(yards, feet);
		assertEquals(feet, inches);
		assertEquals(yards, inches);
	}
	
	@Test
	public void testEquality_CentimetersToCentimeters_SameValue() {
		Length cm2a = new Length(2.0, Length.LengthUnit.CENTIMETERS);
		Length cm2b = new Length(2.0, Length.LengthUnit.CENTIMETERS);
		assertEquals(cm2a, cm2b);
	}

	@Test
	public void testEquality_CentimetersToCentimeters_DifferentValue() {
		Length cm2 = new Length(2.0, Length.LengthUnit.CENTIMETERS);
		Length cm3 = new Length(3.0, Length.LengthUnit.CENTIMETERS);
		assertNotEquals(cm2, cm3);
	}

	@Test
	public void testEquality_InchesToCentimeters_EquivalentValue() {
		Length inchValue = new Length(0.393701, Length.LengthUnit.INCHES);
		Length cm1 = new Length(1.0, Length.LengthUnit.CENTIMETERS);
		assertEquals(inchValue, cm1);
	}

	@Test
	public void testEquality_DifferentClass() {
		Length yard = new Length(2.0, Length.LengthUnit.YARDS);
		assertFalse(yard.equals("2.0"));
	}

	@Test
	public void testEquality_NaN() {
		assertThrows(
			IllegalArgumentException.class, 
			() -> new Length(Double.NaN, Length.LengthUnit.FEET)
		);
	}

	@Test
	public void testEquality_Infinity() {
		assertThrows(
			IllegalArgumentException.class,
			() -> new Length(Double.POSITIVE_INFINITY, Length.LengthUnit.CENTIMETERS)
		);
	}

	@Test
	public void testEquality_DemonstrateLengthComparisonMethod() {
		boolean result = QuantityMeasurementApp.demonstrateLengthComparison(
			1.0, Length.LengthUnit.YARDS, 
			36.0, Length.LengthUnit.INCHES
		);
		assertTrue(result);
	}
}