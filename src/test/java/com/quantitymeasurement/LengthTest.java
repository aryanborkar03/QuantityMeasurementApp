package com.quantitymeasurement;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LengthTest {

	@Test
	public void testEquality_FeetToFeet_SameValue() {
		Length feet1 = new Length(1.0, Length.LengthUnit.FEET);
		Length feet2 = new Length(1.0, Length.LengthUnit.FEET);
		assertEquals(feet1, feet2);
	}

	@Test
	public void testEquality_InchToInch_SameValue() {
		Length inch1 = new Length(1.0, Length.LengthUnit.INCHES);
		Length inch2 = new Length(1.0, Length.LengthUnit.INCHES);
		assertEquals(inch1, inch2);
	}

	@Test
	public void testEquality_InchToFeet_EquivalentValue() {
		Length inch12 = new Length(12.0, Length.LengthUnit.INCHES);
		Length feet1 = new Length(1.0, Length.LengthUnit.FEET);
		assertEquals(inch12, feet1);
	}

	@Test
	public void testEquality_FeetToFeet_DifferentValue() {
		Length feet1 = new Length(1.0, Length.LengthUnit.FEET);
		Length feet2 = new Length(2.0, Length.LengthUnit.FEET);
		assertNotEquals(feet1, feet2);
	}

	@Test
	public void testEquality_InchToInch_DifferentValue() {
		Length inch1 = new Length(1.0, Length.LengthUnit.INCHES);
		Length inch2 = new Length(2.0, Length.LengthUnit.INCHES);
		assertNotEquals(inch1, inch2);
	}

	@Test
	public void testEquality_SameReference() {
		Length feet1 = new Length(1.0, Length.LengthUnit.FEET);
		assertEquals(feet1, feet1);
	}

	@Test
	public void testEquality_NullComparison() {
		Length feet1 = new Length(1.0, Length.LengthUnit.FEET);
		assertNotEquals(feet1, null);
	}

	@Test
	public void testEquality_InvalidUnit() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Length(1.0, null);
		});
	}

	@Test
	public void testEquality_NullUnit() {
		Length feet1 = new Length(1.0, Length.LengthUnit.FEET);
		Length invalid = null;
		assertNotEquals(feet1, invalid);
	}

	@Test
	public void testEquality_DifferentClass() {
		Length l1 = new Length(1.0, Length.LengthUnit.FEET);
		assertFalse(l1.equals("1.0"));
	}

	@Test
	public void testEquality_NaN() {
		assertThrows(IllegalArgumentException.class, () -> new Length(Double.NaN, Length.LengthUnit.FEET));
	}

	@Test
	public void testEquality_Infinity() {
		assertThrows(IllegalArgumentException.class,
				() -> new Length(Double.POSITIVE_INFINITY, Length.LengthUnit.INCHES));
	}
}