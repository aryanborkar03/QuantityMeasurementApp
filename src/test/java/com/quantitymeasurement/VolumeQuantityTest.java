package com.quantitymeasurement;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class VolumeQuantityTest {

	private static final double EPSILON = 1e-6;

	@Test
	public void testEquality_LitreToLitre_SameValue() {
		assertEquals(new Quantity<>(1.0, VolumeUnit.LITRE), new Quantity<>(1.0, VolumeUnit.LITRE));
	}

	@Test
	public void testEquality_LitreToLitre_DifferentValue() {
		assertNotEquals(new Quantity<>(1.0, VolumeUnit.LITRE), new Quantity<>(2.0, VolumeUnit.LITRE));
	}

	@Test
	public void testEquality_MillilitreToMillilitre_SameValue() {
		assertEquals(new Quantity<>(500.0, VolumeUnit.MILLILITRE), new Quantity<>(500.0, VolumeUnit.MILLILITRE));
	}

	@Test
	public void testEquality_GallonToGallon_SameValue() {
		assertEquals(new Quantity<>(2.0, VolumeUnit.GALLON), new Quantity<>(2.0, VolumeUnit.GALLON));
	}

	@Test
	public void testEquality_LitreToMillilitre_EquivalentValue() {
		assertEquals(new Quantity<>(1.0, VolumeUnit.LITRE), new Quantity<>(1000.0, VolumeUnit.MILLILITRE));
	}

	@Test
	public void testEquality_MillilitreToLitre_Symmetric() {
		assertTrue(new Quantity<>(1000.0, VolumeUnit.MILLILITRE).equals(new Quantity<>(1.0, VolumeUnit.LITRE)));
	}

	@Test
	public void testEquality_LitreToGallon_WithinEpsilon() {
		Quantity<VolumeUnit> litres = new Quantity<>(3.785412, VolumeUnit.LITRE);
		Quantity<VolumeUnit> gallon = new Quantity<>(1.0, VolumeUnit.GALLON);
		assertTrue(litres.equals(gallon));
		assertTrue(gallon.equals(litres));
	}

	@Test
	public void testEquality_TransitiveProperty() {
		Quantity<VolumeUnit> a = new Quantity<>(3.785412, VolumeUnit.LITRE);
		Quantity<VolumeUnit> b = new Quantity<>(1.0, VolumeUnit.GALLON);
		Quantity<VolumeUnit> c = new Quantity<>(3785.412, VolumeUnit.MILLILITRE);
		assertTrue(a.equals(b));
		assertTrue(b.equals(c));
		assertTrue(a.equals(c));
	}

	@Test
	public void testConversion_LitreToMillilitre() {
		Quantity<VolumeUnit> converted = new Quantity<>(1.0, VolumeUnit.LITRE).convertTo(VolumeUnit.MILLILITRE);
		assertEquals(1000.0, converted.getValue(), EPSILON);
		assertEquals(VolumeUnit.MILLILITRE, converted.getUnit());
	}

	@Test
	public void testConversion_MillilitreToLitre() {
		Quantity<VolumeUnit> converted = new Quantity<>(1000.0, VolumeUnit.MILLILITRE).convertTo(VolumeUnit.LITRE);
		assertEquals(1.0, converted.getValue(), EPSILON);
		assertEquals(VolumeUnit.LITRE, converted.getUnit());
	}

	@Test
	public void testConversion_GallonToLitre() {
		Quantity<VolumeUnit> converted = new Quantity<>(1.0, VolumeUnit.GALLON).convertTo(VolumeUnit.LITRE);
		assertEquals(3.785412, converted.getValue(), EPSILON);
	}

	@Test
	public void testConversion_LitreToGallon() {
		Quantity<VolumeUnit> converted = new Quantity<>(3.785412, VolumeUnit.LITRE).convertTo(VolumeUnit.GALLON);
		assertEquals(1.0, converted.getValue(), EPSILON);
	}

	@Test
	public void testConversion_RoundTrip_PreservesValue() {
		Quantity<VolumeUnit> original = new Quantity<>(1.5, VolumeUnit.LITRE);
		Quantity<VolumeUnit> roundTrip = original.convertTo(VolumeUnit.MILLILITRE).convertTo(VolumeUnit.LITRE);
		assertEquals(original, roundTrip);
	}

	@Test
	public void testConversion_SameUnit_NoChange() {
		Quantity<VolumeUnit> converted = new Quantity<>(5.0, VolumeUnit.LITRE).convertTo(VolumeUnit.LITRE);
		assertEquals(new Quantity<>(5.0, VolumeUnit.LITRE), converted);
	}

	@Test
	public void testConversion_ZeroAndNegativeValues() {
		assertEquals(
			new Quantity<>(0.0, VolumeUnit.MILLILITRE),
			new Quantity<>(0.0, VolumeUnit.LITRE).convertTo(VolumeUnit.MILLILITRE)
		);
		assertEquals(
			new Quantity<>(-1000.0, VolumeUnit.MILLILITRE),
			new Quantity<>(-1.0, VolumeUnit.LITRE).convertTo(VolumeUnit.MILLILITRE)
		);
	}

	@Test
	public void testAddition_SameUnit_LitrePlusLitre() {
		Quantity<VolumeUnit> sum = new Quantity<>(1.0, VolumeUnit.LITRE).add(new Quantity<>(2.0, VolumeUnit.LITRE));
		assertEquals(new Quantity<>(3.0, VolumeUnit.LITRE), sum);
	}

	@Test
	public void testAddition_SameUnit_MillilitrePlusMillilitre() {
		Quantity<VolumeUnit> sum = new Quantity<>(500.0, VolumeUnit.MILLILITRE)
			.add(new Quantity<>(500.0, VolumeUnit.MILLILITRE));
		assertEquals(new Quantity<>(1000.0, VolumeUnit.MILLILITRE), sum);
	}

	@Test
	public void testAddition_CrossUnit_LitrePlusMillilitre_Implicit() {
		Quantity<VolumeUnit> sum = new Quantity<>(1.0, VolumeUnit.LITRE)
			.add(new Quantity<>(1000.0, VolumeUnit.MILLILITRE));
		assertEquals(new Quantity<>(2.0, VolumeUnit.LITRE), sum);
	}

	@Test
	public void testAddition_CrossUnit_MillilitrePlusLitre_Implicit() {
		Quantity<VolumeUnit> sum = new Quantity<>(1000.0, VolumeUnit.MILLILITRE)
			.add(new Quantity<>(1.0, VolumeUnit.LITRE));
		assertEquals(new Quantity<>(2000.0, VolumeUnit.MILLILITRE), sum);
	}

	@Test
	public void testAddition_ExplicitTargetUnit_Litre() {
		Quantity<VolumeUnit> sum = new Quantity<>(1.0, VolumeUnit.LITRE)
			.add(new Quantity<>(1000.0, VolumeUnit.MILLILITRE), VolumeUnit.LITRE);
		assertEquals(new Quantity<>(2.0, VolumeUnit.LITRE), sum);
	}

	@Test
	public void testAddition_ExplicitTargetUnit_Millilitre() {
		Quantity<VolumeUnit> sum = new Quantity<>(1.0, VolumeUnit.LITRE)
			.add(new Quantity<>(1000.0, VolumeUnit.MILLILITRE), VolumeUnit.MILLILITRE);
		assertEquals(new Quantity<>(2000.0, VolumeUnit.MILLILITRE), sum);
	}

	@Test
	public void testAddition_ExplicitTargetUnit_Gallon() {
		Quantity<VolumeUnit> sum = new Quantity<>(3.785412, VolumeUnit.LITRE)
			.add(new Quantity<>(3.785412, VolumeUnit.LITRE), VolumeUnit.GALLON);
		assertEquals(new Quantity<>(2.0, VolumeUnit.GALLON), sum);
	}

	@Test
	public void testAddition_Commutativity_WithTargetUnit() {
		Quantity<VolumeUnit> a = new Quantity<>(1.0, VolumeUnit.LITRE);
		Quantity<VolumeUnit> b = new Quantity<>(1000.0, VolumeUnit.MILLILITRE);
		Quantity<VolumeUnit> r1 = a.add(b, VolumeUnit.GALLON);
		Quantity<VolumeUnit> r2 = b.add(a, VolumeUnit.GALLON);
		assertEquals(r1, r2);
	}

	@Test
	public void testAddition_WithZeroAndNegative() {
		Quantity<VolumeUnit> base = new Quantity<>(5.0, VolumeUnit.LITRE);
		assertEquals(base, base.add(new Quantity<>(0.0, VolumeUnit.MILLILITRE)));
		assertEquals(new Quantity<>(3.0, VolumeUnit.LITRE), base.add(new Quantity<>(-2000.0, VolumeUnit.MILLILITRE)));
	}

	@Test
	public void testAddition_LargeAndSmallValues() {
		Quantity<VolumeUnit> large = new Quantity<>(1e6, VolumeUnit.LITRE);
		Quantity<VolumeUnit> sum = large.add(new Quantity<>(1e6, VolumeUnit.LITRE));
		assertEquals(new Quantity<>(2e6, VolumeUnit.LITRE), sum);

		Quantity<VolumeUnit> small = new Quantity<>(0.001, VolumeUnit.LITRE);
		Quantity<VolumeUnit> smallSum = small.add(new Quantity<>(0.002, VolumeUnit.LITRE));
		assertEquals(new Quantity<>(0.003, VolumeUnit.LITRE), smallSum);
	}

	@Test
	public void testCrossCategoryPrevention_VolumeVsLength() {
		assertFalse(new Quantity<>(1.0, VolumeUnit.LITRE).equals(new Quantity<>(1.0, LengthUnit.FEET)));
	}

	@Test
	public void testCrossCategoryPrevention_VolumeVsWeight() {
		assertFalse(new Quantity<>(1.0, VolumeUnit.LITRE).equals(new Quantity<>(1.0, WeightUnit.KILOGRAM)));
	}

	@Test
	public void testNullUnitConstructor_Throws() {
		assertThrows(IllegalArgumentException.class, () -> new Quantity<>(1.0, (VolumeUnit) null));
	}

	@Test
	public void testEquals_NullComparison_ReturnsFalse() {
		assertFalse(new Quantity<>(1.0, VolumeUnit.LITRE).equals(null));
	}

	@Test
	public void testSameReferenceEquality() {
		Quantity<VolumeUnit> q = new Quantity<>(2.0, VolumeUnit.LITRE);
		assertEquals(q, q);
	}

	@Test
	public void testVolumeUnitEnum_ConstantsAndConversionFactors() {
		assertEquals(1.0, VolumeUnit.LITRE.getConversionFactor(), EPSILON);
		assertEquals(0.001, VolumeUnit.MILLILITRE.getConversionFactor(), EPSILON);
		assertEquals(3.785412, VolumeUnit.GALLON.getConversionFactor(), EPSILON);
	}

	@Test
	public void testConvertToBaseUnitAndFromBaseUnit() {
		assertEquals(5.0, VolumeUnit.LITRE.convertToBaseUnit(5.0), EPSILON);
		assertEquals(1.0, VolumeUnit.MILLILITRE.convertToBaseUnit(1000.0), EPSILON);
		assertEquals(3.785412, VolumeUnit.GALLON.convertToBaseUnit(1.0), EPSILON);

		assertEquals(2.0, VolumeUnit.LITRE.convertFromBaseUnit(2.0), EPSILON);
		assertEquals(1000.0, VolumeUnit.MILLILITRE.convertFromBaseUnit(1.0), EPSILON);
		assertEquals(1.0, VolumeUnit.GALLON.convertFromBaseUnit(3.785412), EPSILON);
	}

	@Test
	public void testHashCode_EqualObjectsHaveSameHashCode() {
		Quantity<VolumeUnit> a = new Quantity<>(1.0, VolumeUnit.LITRE);
		Quantity<VolumeUnit> b = new Quantity<>(1000.0, VolumeUnit.MILLILITRE);
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());

		Quantity<VolumeUnit> c = new Quantity<>(3.785412, VolumeUnit.LITRE);
		Quantity<VolumeUnit> d = new Quantity<>(1.0, VolumeUnit.GALLON);
		assertEquals(c, d);
		assertEquals(c.hashCode(), d.hashCode());
	}

	@Test
	public void testTypeSafetyInCollections_HashSetBehavior() {
		Set<Quantity<VolumeUnit>> set = new HashSet<>();
		set.add(new Quantity<>(1.0, VolumeUnit.LITRE));
		set.add(new Quantity<>(1000.0, VolumeUnit.MILLILITRE));
		assertEquals(1, set.size());
	}

	@Test
	public void testPrecision_SmallDifferencesWithinEpsilon() {
		Quantity<VolumeUnit> a = new Quantity<>(1.00000005, VolumeUnit.LITRE);
		Quantity<VolumeUnit> b = new Quantity<>(1.0, VolumeUnit.LITRE);
		assertEquals(a, b);
	}

	@Test
	public void testPrecision_SmallDifferencesOutsideEpsilon() {
		Quantity<VolumeUnit> a = new Quantity<>(1.00001, VolumeUnit.LITRE);
		Quantity<VolumeUnit> b = new Quantity<>(1.0, VolumeUnit.LITRE);
		assertNotEquals(a, b);
	}

	@Test
	public void testConstructor_InvalidValue_NaNOrInfinite_Throws() {
		assertThrows(IllegalArgumentException.class, () -> new Quantity<>(Double.NaN, VolumeUnit.LITRE));
		assertThrows(IllegalArgumentException.class, () -> new Quantity<>(Double.POSITIVE_INFINITY, VolumeUnit.MILLILITRE));
	}

	@Test
	public void testIMeasurableInterface_VolumeUnitImplementation() {
		IMeasurable unit = VolumeUnit.LITRE;
		assertEquals("LITRE", unit.getUnitName());
		assertEquals(1.0, unit.getConversionFactor(), EPSILON);
		assertEquals(1.0, unit.convertToBaseUnit(1.0), EPSILON);
		assertEquals(1.0, unit.convertFromBaseUnit(1.0), EPSILON);
	}

	@Test
	public void testGenericQuantity_VolumeOperations_EqualityAndConversion() {
		Quantity<VolumeUnit> v1 = new Quantity<>(1.0, VolumeUnit.LITRE);
		Quantity<VolumeUnit> v2 = new Quantity<>(1000.0, VolumeUnit.MILLILITRE);
		assertTrue(v1.equals(v2));
		Quantity<VolumeUnit> converted = v1.convertTo(VolumeUnit.MILLILITRE);
		assertEquals(new Quantity<>(1000.0, VolumeUnit.MILLILITRE), converted);
	}

	@Test
	public void testImmutability_GenericQuantity() {
		Quantity<VolumeUnit> q = new Quantity<>(1.0, VolumeUnit.LITRE);
		Quantity<VolumeUnit> added = q.add(new Quantity<>(1.0, VolumeUnit.LITRE));
		assertEquals(new Quantity<>(1.0, VolumeUnit.LITRE), q);
		assertEquals(new Quantity<>(2.0, VolumeUnit.LITRE), added);
	}

	@Test
	public void testScalability_NewCategoryIntegration() {
		Quantity<VolumeUnit> q = new Quantity<>(2.0, VolumeUnit.LITRE);
		assertEquals(new Quantity<>(2000.0, VolumeUnit.MILLILITRE), q.convertTo(VolumeUnit.MILLILITRE));
	}

	@Test
	public void testDemonstrationMethods_HandleVolume() {
		boolean eq = QuantityMeasurementApp.demonstrateEquality(
			new Quantity<>(1.0, VolumeUnit.LITRE),
			new Quantity<>(1000.0, VolumeUnit.MILLILITRE)
		);
		assertTrue(eq);

		Quantity<VolumeUnit> converted = QuantityMeasurementApp.demonstrateConversion(
			new Quantity<>(1.0, VolumeUnit.GALLON),
			VolumeUnit.LITRE
		);
		assertEquals(new Quantity<>(3.785412, VolumeUnit.LITRE), converted);

		Quantity<VolumeUnit> sum = QuantityMeasurementApp.demonstrateAddition(
			new Quantity<>(1.0, VolumeUnit.LITRE),
			new Quantity<>(1000.0, VolumeUnit.MILLILITRE)
		);
		assertEquals(new Quantity<>(2.0, VolumeUnit.LITRE), sum);
	}
}