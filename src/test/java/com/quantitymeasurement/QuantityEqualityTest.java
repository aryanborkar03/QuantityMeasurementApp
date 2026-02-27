package com.quantitymeasurement;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QuantityEqualityTest {

	@Test
	public void testEquality_YardToYard_SameValue() {
		assertEquals(new Quantity<>(1.0, LengthUnit.YARDS), new Quantity<>(1.0, LengthUnit.YARDS));
	}

	@Test
	public void testEquality_YardToYard_DifferentValue() {
		assertNotEquals(new Quantity<>(1.0, LengthUnit.YARDS), new Quantity<>(2.0, LengthUnit.YARDS));
	}

	@Test
	public void testEquality_YardToFeet_EquivalentValue() {
		assertEquals(new Quantity<>(1.0, LengthUnit.YARDS), new Quantity<>(3.0, LengthUnit.FEET));
	}

	@Test
	public void testEquality_FeetToYard_EquivalentValue() {
		assertEquals(new Quantity<>(3.0, LengthUnit.FEET), new Quantity<>(1.0, LengthUnit.YARDS));
	}

	@Test
	public void testEquality_YardToInches_EquivalentValue() {
		assertEquals(new Quantity<>(1.0, LengthUnit.YARDS), new Quantity<>(36.0, LengthUnit.INCHES));
	}

	@Test
	public void testEquality_InchesToYard_EquivalentValue() {
		assertEquals(new Quantity<>(36.0, LengthUnit.INCHES), new Quantity<>(1.0, LengthUnit.YARDS));
	}

	@Test
	public void testEquality_YardToFeet_NonEquivalentValue() {
		assertNotEquals(new Quantity<>(1.0, LengthUnit.YARDS), new Quantity<>(2.0, LengthUnit.FEET));
	}

	@Test
	public void testEquality_CentimetersToInches_EquivalentValue() {
		assertEquals(new Quantity<>(1.0, LengthUnit.CENTIMETERS), new Quantity<>(0.393701, LengthUnit.INCHES));
	}

	@Test
	public void testEquality_CentimetersToFeet_NonEquivalentValue() {
		assertNotEquals(new Quantity<>(1.0, LengthUnit.CENTIMETERS), new Quantity<>(1.0, LengthUnit.FEET));
	}

	@Test
	public void testEquality_MultiUnit_TransitiveProperty() {
		assertEquals(new Quantity<>(1.0, LengthUnit.YARDS), new Quantity<>(3.0, LengthUnit.FEET));
		assertEquals(new Quantity<>(3.0, LengthUnit.FEET), new Quantity<>(36.0, LengthUnit.INCHES));
		assertEquals(new Quantity<>(1.0, LengthUnit.YARDS), new Quantity<>(36.0, LengthUnit.INCHES));
	}

	@Test
	public void testEquality_NullUnitThrows() {
		assertThrows(IllegalArgumentException.class, () -> new Quantity<>(1.0, null));
	}

	@Test
	public void testEquality_SameReference() {
		Quantity<LengthUnit> yard = new Quantity<>(2.0, LengthUnit.YARDS);
		assertEquals(yard, yard);
	}

	@Test
	public void testEquality_NullComparison() {
		Quantity<LengthUnit> yard = new Quantity<>(2.0, LengthUnit.YARDS);
		assertNotEquals(yard, null);
	}

	@Test
	public void testEquality_AllUnits_ComplexScenario() {
		assertEquals(new Quantity<>(2.0, LengthUnit.YARDS), new Quantity<>(6.0, LengthUnit.FEET));
		assertEquals(new Quantity<>(6.0, LengthUnit.FEET), new Quantity<>(72.0, LengthUnit.INCHES));
		assertEquals(new Quantity<>(2.0, LengthUnit.YARDS), new Quantity<>(72.0, LengthUnit.INCHES));
	}

	@Test
	public void testEquality_CentimetersToCentimeters_SameValue() {
		assertEquals(new Quantity<>(2.0, LengthUnit.CENTIMETERS), new Quantity<>(2.0, LengthUnit.CENTIMETERS));
	}

	@Test
	public void testEquality_CentimetersToCentimeters_DifferentValue() {
		assertNotEquals(new Quantity<>(2.0, LengthUnit.CENTIMETERS), new Quantity<>(3.0, LengthUnit.CENTIMETERS));
	}

	@Test
	public void testEquality_InchesToCentimeters_EquivalentValue() {
		assertEquals(new Quantity<>(0.393701, LengthUnit.INCHES), new Quantity<>(1.0, LengthUnit.CENTIMETERS));
	}

	@Test
	public void testEquality_DifferentClass() {
		Quantity<LengthUnit> yard = new Quantity<>(2.0, LengthUnit.YARDS);
		assertFalse(yard.equals("2.0"));
	}

	@Test
	public void testEquality_NaNThrows() {
		assertThrows(IllegalArgumentException.class, () -> new Quantity<>(Double.NaN, LengthUnit.FEET));
	}

	@Test
	public void testEquality_InfinityThrows() {
		assertThrows(IllegalArgumentException.class,
				() -> new Quantity<>(Double.POSITIVE_INFINITY, LengthUnit.CENTIMETERS));
	}

	@Test
	public void testEquality_VerySmallValues_WithinEpsilon() {
		Quantity<LengthUnit> a = new Quantity<>(1e-9, LengthUnit.INCHES);
		Quantity<LengthUnit> b = new Quantity<>(1.0000005e-9, LengthUnit.INCHES);
		assertEquals(a, b);

		Quantity<LengthUnit> c = new Quantity<>(1e-8, LengthUnit.FEET);
		Quantity<LengthUnit> d = new Quantity<>(1.2e-7, LengthUnit.INCHES);
		assertEquals(c, d);

		Quantity<LengthUnit> e = new Quantity<>(1e-9, LengthUnit.INCHES);
		Quantity<LengthUnit> f = new Quantity<>(1e-9 + 2e-6, LengthUnit.INCHES);
		assertNotEquals(e, f);
	}

	@Test
	public void testEquality_SmallDifference_OutsideTolerance() {
		assertNotEquals(new Quantity<>(1.0000003, LengthUnit.FEET), new Quantity<>(1.0, LengthUnit.FEET));
	}

	@Test
	public void testEquality_SmallDifference_InsideTolerance() {
		assertEquals(new Quantity<>(1.00000008, LengthUnit.FEET), new Quantity<>(1.0, LengthUnit.FEET));
	}

	@Test
	public void testHashCode_EqualObjectsHaveSameHashCode() {
		Quantity<LengthUnit> a1 = new Quantity<>(5.0, LengthUnit.FEET);
		Quantity<LengthUnit> a2 = new Quantity<>(5.0, LengthUnit.FEET);
		assertEquals(a1.hashCode(), a2.hashCode());

		Quantity<LengthUnit> b1 = new Quantity<>(1.0, LengthUnit.YARDS);
		Quantity<LengthUnit> b2 = new Quantity<>(36.0, LengthUnit.INCHES);
		assertEquals(b1, b2);
		assertEquals(b1.hashCode(), b2.hashCode());

		Quantity<LengthUnit> c1 = new Quantity<>(2.54, LengthUnit.CENTIMETERS);
		Quantity<LengthUnit> c2 = new Quantity<>(1.0, LengthUnit.INCHES);
		assertEquals(c1, c2);
		assertEquals(c1.hashCode(), c2.hashCode());

		Quantity<LengthUnit> d1 = new Quantity<>(1.0000003, LengthUnit.FEET);
		Quantity<LengthUnit> d2 = new Quantity<>(1.0, LengthUnit.FEET);
		assertNotEquals(d1, d2);
		assertNotEquals(d1.hashCode(), d2.hashCode());
	}

	// Weight equality related
	@Test
	public void testEquality_KilogramToKilogram_SameValue() {
		assertEquals(new Quantity<>(1.0, WeightUnit.KILOGRAM), new Quantity<>(1.0, WeightUnit.KILOGRAM));
	}

	@Test
	public void testEquality_KilogramToKilogram_DifferentValue() {
		assertNotEquals(new Quantity<>(1.0, WeightUnit.KILOGRAM), new Quantity<>(2.0, WeightUnit.KILOGRAM));
	}

	@Test
	public void testEquality_GramToGram_SameValue() {
		assertEquals(new Quantity<>(500.0, WeightUnit.GRAM), new Quantity<>(500.0, WeightUnit.GRAM));
	}

	@Test
	public void testEquality_PoundToPound_SameValue() {
		assertEquals(new Quantity<>(2.0, WeightUnit.POUND), new Quantity<>(2.0, WeightUnit.POUND));
	}

	@Test
	public void testEquality_KilogramToGram_EquivalentValue() {
		assertEquals(new Quantity<>(1.0, WeightUnit.KILOGRAM), new Quantity<>(1000.0, WeightUnit.GRAM));
	}

	@Test
	public void testEquality_GramToKilogram_EquivalentValue() {
		assertEquals(new Quantity<>(1000.0, WeightUnit.GRAM), new Quantity<>(1.0, WeightUnit.KILOGRAM));
	}

	@Test
	public void testEquality_KilogramToPound_EquivalentValue() {
		assertEquals(new Quantity<>(1.0, WeightUnit.KILOGRAM), new Quantity<>(2.204624, WeightUnit.POUND));
	}

	@Test
	public void testEquality_GramToPound_EquivalentValue() {
		assertEquals(new Quantity<>(453.592370, WeightUnit.GRAM), new Quantity<>(1.0, WeightUnit.POUND));
	}

	@Test
	public void testEquality_Symmetry_Weight() {
		Quantity<WeightUnit> a = new Quantity<>(1.0, WeightUnit.KILOGRAM);
		Quantity<WeightUnit> b = new Quantity<>(1000.0, WeightUnit.GRAM);
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
	}

	@Test
	public void testEquality_Transitive_Weight() {
		Quantity<WeightUnit> a = new Quantity<>(1.0, WeightUnit.KILOGRAM);
		Quantity<WeightUnit> b = new Quantity<>(1000.0, WeightUnit.GRAM);
		Quantity<WeightUnit> c = new Quantity<>(2.204624, WeightUnit.POUND);
		assertTrue(a.equals(b));
		assertTrue(b.equals(c));
		assertTrue(a.equals(c));
	}

	@Test
	public void testEquality_WeightVsLength_Incompatible() {
		assertFalse(new Quantity<>(1.0, WeightUnit.KILOGRAM).equals(new Quantity<>(1.0, LengthUnit.FEET)));
	}

	@Test
	public void testEquality_NullComparison_Weight() {
		assertFalse(new Quantity<>(1.0, WeightUnit.KILOGRAM).equals(null));
	}

	@Test
	public void testEquality_SameReference_Weight() {
		Quantity<WeightUnit> w = new Quantity<>(2.0, WeightUnit.KILOGRAM);
		assertEquals(w, w);
	}

	@Test
	public void testEquality_NullUnit_WeightThrows() {
		assertThrows(IllegalArgumentException.class, () -> new Quantity<>(1.0, (WeightUnit) null));
	}

	@Test
	public void testEquality_ZeroValue_Weight() {
		assertEquals(new Quantity<>(0.0, WeightUnit.KILOGRAM), new Quantity<>(0.0, WeightUnit.GRAM));
	}

	@Test
	public void testEquality_NegativeWeight() {
		assertEquals(new Quantity<>(-1.0, WeightUnit.KILOGRAM), new Quantity<>(-1000.0, WeightUnit.GRAM));
	}

	@Test
	public void testEquality_LargeWeightValue() {
		assertEquals(new Quantity<>(1_000_000.0, WeightUnit.GRAM), new Quantity<>(1000.0, WeightUnit.KILOGRAM));
	}

	@Test
	public void testEquality_SmallWeightValue() {
		assertEquals(new Quantity<>(0.001, WeightUnit.KILOGRAM), new Quantity<>(1.0, WeightUnit.GRAM));
	}
}