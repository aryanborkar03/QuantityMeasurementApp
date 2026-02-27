package com.quantitymeasurement;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BackwardCompatibilityTest {

	@Test
	public void testBackwardCompatibility_UC1EqualityTests() {
		assertEquals(new Quantity<>(3.0, LengthUnit.FEET), new Quantity<>(36.0, LengthUnit.INCHES));
	}

	@Test
	public void testBackwardCompatibility_UC5ConversionTests() {
		Quantity<LengthUnit> result = new Quantity<>(1.0, LengthUnit.YARDS).convertTo(LengthUnit.FEET);
		assertEquals(new Quantity<>(3.0, LengthUnit.FEET), result);
	}

	@Test
	public void testBackwardCompatibility_UC6AdditionTests() {
		Quantity<LengthUnit> result = new Quantity<>(2.0, LengthUnit.FEET).add(new Quantity<>(24.0, LengthUnit.INCHES));
		assertEquals(new Quantity<>(4.0, LengthUnit.FEET), result);
	}

	@Test
	public void testBackwardCompatibility_UC7AdditionWithTargetUnitTests() {
		Quantity<LengthUnit> result = new Quantity<>(2.0, LengthUnit.FEET).add(new Quantity<>(24.0, LengthUnit.INCHES),
				LengthUnit.INCHES);
		assertEquals(new Quantity<>(48.0, LengthUnit.INCHES), result);
	}

	@Test
	public void testQuantityLengthRefactored_Equality() {
		assertTrue(new Quantity<>(1.0, LengthUnit.FEET).equals(new Quantity<>(12.0, LengthUnit.INCHES)));
	}

	@Test
	public void testQuantityLengthRefactored_ConvertTo() {
		Quantity<LengthUnit> result = new Quantity<>(1.0, LengthUnit.FEET).convertTo(LengthUnit.INCHES);
		assertEquals(new Quantity<>(12.0, LengthUnit.INCHES), result);
	}

	@Test
	public void testQuantityLengthRefactored_Add() {
		Quantity<LengthUnit> result = new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(12.0, LengthUnit.INCHES),
				LengthUnit.FEET);
		assertEquals(new Quantity<>(2.0, LengthUnit.FEET), result);
	}

	@Test
	public void testQuantityLengthRefactored_AddWithTargetUnit() {
		Quantity<LengthUnit> result = new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(12.0, LengthUnit.INCHES),
				LengthUnit.YARDS);
		assertEquals(new Quantity<>(0.666667, LengthUnit.YARDS), result);
	}

	@Test
	public void testQuantityLengthRefactored_NullUnitThrows() {
		assertThrows(IllegalArgumentException.class, () -> new Quantity<>(1.0, (LengthUnit) null));
	}

	@Test
	public void testQuantityLengthRefactored_InvalidValueThrows() {
		assertThrows(IllegalArgumentException.class, () -> new Quantity<>(Double.NaN, LengthUnit.FEET));
	}

	@Test
	public void testBackwardCompatibility_UC1EqualityTests_Alternate() {
		assertEquals(new Quantity<>(3.0, LengthUnit.FEET), new Quantity<>(36.0, LengthUnit.INCHES));
	}

	@Test
	public void testBackwardCompatibility_UC5ConversionTests_Alternate() {
		Quantity<LengthUnit> result = new Quantity<>(1.0, LengthUnit.YARDS).convertTo(LengthUnit.FEET);
		assertEquals(new Quantity<>(3.0, LengthUnit.FEET), result);
	}

	@Test
	public void testBackwardCompatibility_UC6AdditionTests_Alternate() {
		Quantity<LengthUnit> result = new Quantity<>(2.0, LengthUnit.FEET).add(new Quantity<>(24.0, LengthUnit.INCHES));
		assertEquals(new Quantity<>(4.0, LengthUnit.FEET), result);
	}

	@Test
	public void testBackwardCompatibility_UC7AdditionWithTargetUnitTests_Alternate() {
		Quantity<LengthUnit> result = new Quantity<>(2.0, LengthUnit.FEET).add(new Quantity<>(24.0, LengthUnit.INCHES), LengthUnit.INCHES);  
		assertEquals(new Quantity<>(48.0, LengthUnit.INCHES), result);
	}
}