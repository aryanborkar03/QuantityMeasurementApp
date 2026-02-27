package com.quantitymeasurement;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WeightQuantityTest {

	private static final double EPSILON = 1e-6;

	@Test
	public void testConversion_PoundToKilogram() {
		Quantity<WeightUnit> converted = new Quantity<>(2.204624, WeightUnit.POUND).convertTo(WeightUnit.KILOGRAM);
		assertEquals(1.0, converted.getValue(), EPSILON);
		assertEquals(WeightUnit.KILOGRAM, converted.getUnit());
	}

	@Test
	public void testConversion_KilogramToPound() {
		Quantity<WeightUnit> converted = new Quantity<>(1.0, WeightUnit.KILOGRAM).convertTo(WeightUnit.POUND);
		assertEquals(2.204624, converted.getValue(), EPSILON);
		assertEquals(WeightUnit.POUND, converted.getUnit());
	}

	@Test
	public void testAddition_WeightSameUnit() {
		Quantity<WeightUnit> sum = new Quantity<>(1.0, WeightUnit.KILOGRAM)
				.add(new Quantity<>(2.0, WeightUnit.KILOGRAM));
		assertEquals(new Quantity<>(3.0, WeightUnit.KILOGRAM), sum);
	}

	@Test
	public void testAddition_WeightCrossUnit() {
		Quantity<WeightUnit> sum = new Quantity<>(1.0, WeightUnit.KILOGRAM).add(new Quantity<>(1000.0, WeightUnit.GRAM),
				WeightUnit.GRAM);
		assertEquals(new Quantity<>(2000.0, WeightUnit.GRAM), sum);
	}

	@Test
	public void testAddition_WeightNegativeValues() {
		Quantity<WeightUnit> sum = new Quantity<>(5.0, WeightUnit.KILOGRAM)
				.add(new Quantity<>(-2.0, WeightUnit.KILOGRAM));
		assertEquals(new Quantity<>(3.0, WeightUnit.KILOGRAM), sum);
	}

	@Test
	public void testEquality_KilogramToPound() {
		assertEquals(new Quantity<>(1.0, WeightUnit.KILOGRAM), new Quantity<>(2.204624, WeightUnit.POUND));
	}

	@Test
	public void testEquality_GramToPound_EquivalentValue() {
		assertEquals(new Quantity<>(453.592370, WeightUnit.GRAM), new Quantity<>(1.0, WeightUnit.POUND));
	}

	@Test
	public void testEquality_ZeroValue() {
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

	@Test
	public void testEquality_NullUnit_WeightThrows() {
		assertThrows(IllegalArgumentException.class, () -> new Quantity<>(1.0, (WeightUnit) null));
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
}