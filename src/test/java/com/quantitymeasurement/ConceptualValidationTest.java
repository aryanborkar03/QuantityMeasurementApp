package com.quantitymeasurement;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConceptualValidationTest {

	private static final double EPSILON = 1e-6;

	@Test
	public void testIMeasurableInterface_LengthUnitImplementation() {
		IMeasurable unit = LengthUnit.FEET;
		assertEquals("FEET", unit.getUnitName());
		assertEquals(12.0, unit.getConversionFactor(), EPSILON);
		assertEquals(12.0, unit.convertToBaseUnit(1.0), EPSILON);
		assertEquals(1.0, unit.convertFromBaseUnit(12.0), EPSILON);
	}

	@Test
	public void testIMeasurableInterface_WeightUnitImplementation() {
		IMeasurable unit = WeightUnit.KILOGRAM;
		assertEquals("KILOGRAM", unit.getUnitName());
		assertEquals(1.0, unit.getConversionFactor(), EPSILON);
		assertEquals(1.0, unit.convertToBaseUnit(1.0), EPSILON);
		assertEquals(1.0, unit.convertFromBaseUnit(1.0), EPSILON);
	}

	@Test
	public void testIMeasurableInterface_ConsistentBehavior() {
		IMeasurable length = LengthUnit.INCHES;
		IMeasurable weight = WeightUnit.GRAM;
		assertNotNull(length.getUnitName());
		assertNotNull(weight.getUnitName());
	}

	@Test
	public void testGenericQuantity_LengthOperations_Equality() {
		assertTrue(new Quantity<>(1.0, LengthUnit.FEET).equals(new Quantity<>(12.0, LengthUnit.INCHES)));
	}

	@Test
	public void testGenericQuantity_WeightOperations_Equality() {
		assertTrue(new Quantity<>(1.0, WeightUnit.KILOGRAM).equals(new Quantity<>(1000.0, WeightUnit.GRAM)));
	}

	@Test
	public void testGenericQuantity_LengthOperations_Conversion() {
		Quantity<LengthUnit> result = new Quantity<>(1.0, LengthUnit.FEET).convertTo(LengthUnit.INCHES);
		assertEquals(new Quantity<>(12.0, LengthUnit.INCHES), result);
	}

	@Test
	public void testGenericQuantity_WeightOperations_Conversion() {
		Quantity<WeightUnit> result = new Quantity<>(1.0, WeightUnit.KILOGRAM).convertTo(WeightUnit.GRAM);
		assertEquals(new Quantity<>(1000.0, WeightUnit.GRAM), result);
	}

	@Test
	public void testGenericQuantity_LengthOperations_Addition() {
		Quantity<LengthUnit> result = new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(12.0, LengthUnit.INCHES),
				LengthUnit.FEET);
		assertEquals(new Quantity<>(2.0, LengthUnit.FEET), result);
	}

	@Test
	public void testGenericQuantity_WeightOperations_Addition() {
		Quantity<WeightUnit> result = new Quantity<>(1.0, WeightUnit.KILOGRAM)
				.add(new Quantity<>(1000.0, WeightUnit.GRAM), WeightUnit.KILOGRAM);
		assertEquals(new Quantity<>(2.0, WeightUnit.KILOGRAM), result);
	}
	
	@Test
	public void testCrossCategoryPrevention_LengthVsWeight() {
		assertFalse(new Quantity<>(1.0, LengthUnit.FEET).equals(new Quantity<>(1.0, WeightUnit.KILOGRAM)));
	}

	@Test
	public void testGenericQuantity_ConstructorValidation_NullUnit() {
		assertThrows(IllegalArgumentException.class, () -> new Quantity<>(1.0, null));
	}

	@Test
	public void testGenericQuantity_ConstructorValidation_InvalidValue() {
		assertThrows(IllegalArgumentException.class, () -> new Quantity<>(Double.NaN, LengthUnit.FEET));
	}

	@Test
	public void testHashCode_GenericQuantity_Consistency() {
		Quantity<LengthUnit> a = new Quantity<>(3.0, LengthUnit.FEET);
		Quantity<LengthUnit> b = new Quantity<>(36.0, LengthUnit.INCHES);
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void testEquals_GenericQuantity_ContractPreservation() {
		Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.FEET);
		Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);
		Quantity<LengthUnit> c = new Quantity<>(1.0 / 3.0, LengthUnit.YARDS);
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
		assertTrue(a.equals(c) && b.equals(c));
	}

	@Test
	public void testImmutability_GenericQuantity() {
		Quantity<LengthUnit> q = new Quantity<>(1.0, LengthUnit.FEET);
		assertEquals(1.0, q.getValue(), EPSILON);
		assertEquals(LengthUnit.FEET, q.getUnit());
	}
}