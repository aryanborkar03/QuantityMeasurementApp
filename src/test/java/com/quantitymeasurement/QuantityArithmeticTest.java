package com.quantitymeasurement;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class QuantityArithmeticTest {

    private static final double EPSILON = 1e-6;

    @Test
    public void testSubtraction_SameUnit_FeetMinusFeet() {
        Quantity<LengthUnit> result = new Quantity<>(10.0, LengthUnit.FEET)
            .subtract(new Quantity<>(5.0, LengthUnit.FEET));
        assertEquals(new Quantity<>(5.0, LengthUnit.FEET), result);
    }

    @Test
    public void testSubtraction_SameUnit_LitreMinusLitre() {
        Quantity<VolumeUnit> result = new Quantity<>(10.0, VolumeUnit.LITRE)
            .subtract(new Quantity<>(3.0, VolumeUnit.LITRE));
        assertEquals(new Quantity<>(7.0, VolumeUnit.LITRE), result);
    }

    @Test
    public void testSubtraction_CrossUnit_FeetMinusInches() {
        Quantity<LengthUnit> result = new Quantity<>(10.0, LengthUnit.FEET)
            .subtract(new Quantity<>(6.0, LengthUnit.INCHES));
        assertEquals(new Quantity<>(9.5, LengthUnit.FEET), result);
    }

    @Test
    public void testSubtraction_CrossUnit_InchesMinusFeet() {
        Quantity<LengthUnit> result = new Quantity<>(120.0, LengthUnit.INCHES)
            .subtract(new Quantity<>(5.0, LengthUnit.FEET));
        assertEquals(new Quantity<>(60.0, LengthUnit.INCHES), result);
    }

    @Test
    public void testSubtraction_ExplicitTargetUnit_Feet() {
        Quantity<LengthUnit> result = new Quantity<>(10.0, LengthUnit.FEET)
            .subtract(new Quantity<>(6.0, LengthUnit.INCHES), LengthUnit.FEET);
        assertEquals(new Quantity<>(9.5, LengthUnit.FEET), result);
    }

    @Test
    public void testSubtraction_ExplicitTargetUnit_Inches() {
        Quantity<LengthUnit> result = new Quantity<>(10.0, LengthUnit.FEET)
            .subtract(new Quantity<>(6.0, LengthUnit.INCHES), LengthUnit.INCHES);
        assertEquals(new Quantity<>(114.0, LengthUnit.INCHES), result);
    }

    @Test
    public void testSubtraction_ExplicitTargetUnit_Millilitre() {
        Quantity<VolumeUnit> result = new Quantity<>(5.0, VolumeUnit.LITRE)
            .subtract(new Quantity<>(2.0, VolumeUnit.LITRE), VolumeUnit.MILLILITRE);
        assertEquals(new Quantity<>(3000.0, VolumeUnit.MILLILITRE), result);
    }

    @Test
    public void testSubtraction_ResultingInNegative() {
        Quantity<LengthUnit> result = new Quantity<>(5.0, LengthUnit.FEET)
            .subtract(new Quantity<>(10.0, LengthUnit.FEET));
        assertEquals(new Quantity<>(-5.0, LengthUnit.FEET), result);
    }

    @Test
    public void testSubtraction_ResultingInZero() {
        Quantity<LengthUnit> result = new Quantity<>(10.0, LengthUnit.FEET)
            .subtract(new Quantity<>(120.0, LengthUnit.INCHES));
        assertEquals(new Quantity<>(0.0, LengthUnit.FEET), result);
    }

    @Test
    public void testSubtraction_WithZeroOperand() {
        Quantity<LengthUnit> result = new Quantity<>(5.0, LengthUnit.FEET)
            .subtract(new Quantity<>(0.0, LengthUnit.INCHES));
        assertEquals(new Quantity<>(5.0, LengthUnit.FEET), result);
    }

    @Test
    public void testSubtraction_WithNegativeOperand() {
        Quantity<LengthUnit> result = new Quantity<>(5.0, LengthUnit.FEET)
            .subtract(new Quantity<>(-2.0, LengthUnit.FEET));
        assertEquals(new Quantity<>(7.0, LengthUnit.FEET), result);
    }

    @Test
    public void testSubtraction_NonCommutative() {
        Quantity<LengthUnit> a = new Quantity<>(10.0, LengthUnit.FEET);
        Quantity<LengthUnit> b = new Quantity<>(5.0, LengthUnit.FEET);
        assertEquals(new Quantity<>(5.0, LengthUnit.FEET), a.subtract(b));
        assertEquals(new Quantity<>(-5.0, LengthUnit.FEET), b.subtract(a));
    }

    @Test
    public void testSubtraction_ChainedOperations() {
        Quantity<LengthUnit> result = new Quantity<>(10.0, LengthUnit.FEET)
            .subtract(new Quantity<>(2.0, LengthUnit.FEET))
            .subtract(new Quantity<>(1.0, LengthUnit.FEET));
        assertEquals(new Quantity<>(7.0, LengthUnit.FEET), result);
    }

    @Test
    public void testSubtraction_WithLargeValues() {
        Quantity<WeightUnit> result = new Quantity<>(1e6, WeightUnit.KILOGRAM)
            .subtract(new Quantity<>(5e5, WeightUnit.KILOGRAM));
        assertEquals(new Quantity<>(5e5, WeightUnit.KILOGRAM), result);
    }

    @Test
    public void testSubtraction_WithSmallValues() {
        Quantity<LengthUnit> result = new Quantity<>(0.001, LengthUnit.FEET)
            .subtract(new Quantity<>(0.0005, LengthUnit.FEET));
        assertEquals(new Quantity<>(0.0005, LengthUnit.FEET), result);
    }

    @Test
    public void testSubtraction_PrecisionAndRounding() {
        Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.FEET);
        Quantity<LengthUnit> b = new Quantity<>(0.333333, LengthUnit.YARDS);
        Quantity<LengthUnit> result = a.subtract(b);
        assertEquals(Math.round(result.getValue() * 1e6) / 1e6, result.getValue(), EPSILON);
    }

    @Test
    public void testSubtraction_NullOperand_Throws() {
        assertThrows(IllegalArgumentException.class, () ->
            new Quantity<>(10.0, LengthUnit.FEET).subtract(null));
    }

    @Test
    public void testSubtraction_NullTargetUnit_Throws() {
        assertThrows(IllegalArgumentException.class, () ->
            new Quantity<>(10.0, LengthUnit.FEET).subtract(new Quantity<>(5.0, LengthUnit.FEET), null));
    }

    @Test
    public void testDivision_SameUnit_FeetDividedByFeet() {
        double ratio = new Quantity<>(10.0, LengthUnit.FEET)
            .divide(new Quantity<>(2.0, LengthUnit.FEET));
        assertEquals(5.0, ratio, EPSILON);
    }

    @Test
    public void testDivision_SameUnit_LitreDividedByLitre() {
        double ratio = new Quantity<>(10.0, VolumeUnit.LITRE)
            .divide(new Quantity<>(5.0, VolumeUnit.LITRE));
        assertEquals(2.0, ratio, EPSILON);
    }

    @Test
    public void testDivision_CrossUnit_FeetDividedByInches() {
        double ratio = new Quantity<>(24.0, LengthUnit.INCHES)
            .divide(new Quantity<>(2.0, LengthUnit.FEET));
        assertEquals(1.0, ratio, EPSILON);
    }

    @Test
    public void testDivision_CrossUnit_KilogramDividedByGram() {
        double ratio = new Quantity<>(2.0, WeightUnit.KILOGRAM)
            .divide(new Quantity<>(2000.0, WeightUnit.GRAM));
        assertEquals(1.0, ratio, EPSILON);
    }

    @Test
    public void testDivision_RatioGreaterThanOne() {
        double ratio = new Quantity<>(10.0, LengthUnit.FEET)
            .divide(new Quantity<>(2.0, LengthUnit.FEET));
        assertTrue(ratio > 1.0);
    }

    @Test
    public void testDivision_RatioLessThanOne() {
        double ratio = new Quantity<>(5.0, LengthUnit.FEET)
            .divide(new Quantity<>(10.0, LengthUnit.FEET));
        assertEquals(0.5, ratio, EPSILON);
    }

    @Test
    public void testDivision_RatioEqualToOne() {
        double ratio = new Quantity<>(10.0, LengthUnit.FEET)
            .divide(new Quantity<>(10.0, LengthUnit.FEET));
        assertEquals(1.0, ratio, EPSILON);
    }

    @Test
    public void testDivision_ByZero_Throws() {
        assertThrows(ArithmeticException.class, () ->
            new Quantity<>(10.0, LengthUnit.FEET).divide(new Quantity<>(0.0, LengthUnit.FEET)));
    }

    @Test
    public void testDivision_NullOperand_Throws() {
        assertThrows(IllegalArgumentException.class, () ->
            new Quantity<>(10.0, LengthUnit.FEET).divide(null));
    }

    @Test
    public void testDivision_WithLargeRatio() {
        double ratio = new Quantity<>(1e6, WeightUnit.KILOGRAM)
            .divide(new Quantity<>(1.0, WeightUnit.KILOGRAM));
        assertEquals(1e6, ratio, EPSILON);
    }

    @Test
    public void testDivision_WithSmallRatio() {
        double ratio = new Quantity<>(1.0, WeightUnit.KILOGRAM)
            .divide(new Quantity<>(1e6, WeightUnit.KILOGRAM));
        assertEquals(1e-6, ratio, 1e-12);
    }

    @Test
    public void testImmutability_AfterSubtraction() {
        Quantity<VolumeUnit> original = new Quantity<>(5.0, VolumeUnit.LITRE);
        Quantity<VolumeUnit> diff = original.subtract(new Quantity<>(500.0, VolumeUnit.MILLILITRE));
        assertEquals(new Quantity<>(5.0, VolumeUnit.LITRE), original);
        assertEquals(new Quantity<>(4.5, VolumeUnit.LITRE), diff);
    }

    @Test
    public void testImmutability_AfterDivision() {
        Quantity<WeightUnit> original = new Quantity<>(10.0, WeightUnit.KILOGRAM);
        double ratio = original.divide(new Quantity<>(5.0, WeightUnit.KILOGRAM));
        assertEquals(new Quantity<>(10.0, WeightUnit.KILOGRAM), original);
        assertEquals(2.0, ratio, EPSILON);
    }

    @Test
    public void testSubtractionAddition_Inverse() {
        Quantity<LengthUnit> a = new Quantity<>(5.0, LengthUnit.FEET);
        Quantity<LengthUnit> b = new Quantity<>(2.0, LengthUnit.FEET);
        Quantity<LengthUnit> result = a.add(b).subtract(b);
        assertEquals(a, result);
    }

    @Test
    public void testDivision_Associativity_NonAssociative() {
        Quantity<LengthUnit> A = new Quantity<>(8.0, LengthUnit.FEET);
        Quantity<LengthUnit> B = new Quantity<>(2.0, LengthUnit.FEET);
        Quantity<LengthUnit> C = new Quantity<>(2.0, LengthUnit.FEET);

        double left = (A.divide(B)) / (C.divide(new Quantity<>(1.0, LengthUnit.FEET)));

        double bDivC = B.divide(C);
        Quantity<LengthUnit> scalarAsQuantity = new Quantity<>(bDivC, LengthUnit.FEET);
        double right = A.divide(scalarAsQuantity);

        assertNotEquals(left, right, EPSILON);
    }

    @Test
    public void testTypeSafetyInCollections_HashSetBehavior() {
        Set<Quantity<VolumeUnit>> set = new HashSet<>();
        set.add(new Quantity<>(1.0, VolumeUnit.LITRE));
        set.add(new Quantity<>(1000.0, VolumeUnit.MILLILITRE));
        assertEquals(1, set.size());
    }

    @Test
    public void testSubtraction_AllMeasurementCategories() {
        assertEquals(new Quantity<>(9.5, LengthUnit.FEET),
            new Quantity<>(10.0, LengthUnit.FEET).subtract(new Quantity<>(6.0, LengthUnit.INCHES)));
        assertEquals(new Quantity<>(5.0, WeightUnit.KILOGRAM),
            new Quantity<>(10.0, WeightUnit.KILOGRAM).subtract(new Quantity<>(5000.0, WeightUnit.GRAM)));
        assertEquals(new Quantity<>(4.5, VolumeUnit.LITRE),
            new Quantity<>(5.0, VolumeUnit.LITRE).subtract(new Quantity<>(500.0, VolumeUnit.MILLILITRE)));
    }

    @Test
    public void testDivision_AllMeasurementCategories() {
        assertEquals(5.0, new Quantity<>(10.0, LengthUnit.FEET).divide(new Quantity<>(2.0, LengthUnit.FEET)), EPSILON);
        assertEquals(2.0, new Quantity<>(10.0, WeightUnit.KILOGRAM).divide(new Quantity<>(5.0, WeightUnit.KILOGRAM)), EPSILON);
        assertEquals(0.5, new Quantity<>(5.0, VolumeUnit.LITRE).divide(new Quantity<>(10.0, VolumeUnit.LITRE)), EPSILON);
    }
}