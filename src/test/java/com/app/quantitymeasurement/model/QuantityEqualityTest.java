package com.app.quantitymeasurement.model;

import com.app.quantitymeasurement.model.Quantity;
import com.app.quantitymeasurement.unit.LengthUnit;
import com.app.quantitymeasurement.unit.WeightUnit;
import com.app.quantitymeasurement.unit.VolumeUnit;
import com.app.quantitymeasurement.unit.TemperatureUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class QuantityEqualityTest {

    private static final double EPSILON = 1e-6;

    // -------------------------------------------------------------------------
    // Length — same unit
    // -------------------------------------------------------------------------

    @Test
    void testEquality_Yards_SameValue() {
        assertEquals(new Quantity<>(1.0, LengthUnit.YARDS), new Quantity<>(1.0, LengthUnit.YARDS));
    }

    @Test
    void testEquality_Yards_DifferentValue() {
        assertNotEquals(new Quantity<>(1.0, LengthUnit.YARDS), new Quantity<>(2.0, LengthUnit.YARDS));
    }

    @Test
    void testEquality_Centimeters_SameValue() {
        assertEquals(new Quantity<>(2.0, LengthUnit.CENTIMETERS), new Quantity<>(2.0, LengthUnit.CENTIMETERS));
    }

    @Test
    void testEquality_Centimeters_DifferentValue() {
        assertNotEquals(new Quantity<>(2.0, LengthUnit.CENTIMETERS), new Quantity<>(3.0, LengthUnit.CENTIMETERS));
    }

    // -------------------------------------------------------------------------
    // Length — cross-unit equivalence
    // -------------------------------------------------------------------------

    @Test
    void testEquality_Yard_Equals_3Feet() {
        assertEquals(new Quantity<>(1.0, LengthUnit.YARDS), new Quantity<>(3.0, LengthUnit.FEET));
    }

    @Test
    void testEquality_3Feet_Equals_Yard() {
        assertEquals(new Quantity<>(3.0, LengthUnit.FEET), new Quantity<>(1.0, LengthUnit.YARDS));
    }

    @Test
    void testEquality_Yard_Equals_36Inches() {
        assertEquals(new Quantity<>(1.0, LengthUnit.YARDS), new Quantity<>(36.0, LengthUnit.INCHES));
    }

    @Test
    void testEquality_36Inches_Equals_Yard() {
        assertEquals(new Quantity<>(36.0, LengthUnit.INCHES), new Quantity<>(1.0, LengthUnit.YARDS));
    }

    @Test
    void testEquality_1Foot_Equals_12Inches() {
        assertEquals(new Quantity<>(1.0, LengthUnit.FEET), new Quantity<>(12.0, LengthUnit.INCHES));
    }

    @Test
    void testEquality_Centimeters_Equals_Inches() {
        assertEquals(new Quantity<>(2.54, LengthUnit.CENTIMETERS), new Quantity<>(1.0, LengthUnit.INCHES));
        assertEquals(new Quantity<>(1.0,  LengthUnit.CENTIMETERS), new Quantity<>(0.393701, LengthUnit.INCHES));
    }

    @Test
    void testEquality_Yard_NotEqual_2Feet() {
        assertNotEquals(new Quantity<>(1.0, LengthUnit.YARDS), new Quantity<>(2.0, LengthUnit.FEET));
    }

    @Test
    void testEquality_Centimeters_NotEqual_Feet() {
        assertNotEquals(new Quantity<>(1.0, LengthUnit.CENTIMETERS), new Quantity<>(1.0, LengthUnit.FEET));
    }

    // -------------------------------------------------------------------------
    // Length — reflexive, symmetric, transitive
    // -------------------------------------------------------------------------

    @Test
    void testEquality_Reflexive() {
        Quantity<LengthUnit> yard = new Quantity<>(2.0, LengthUnit.YARDS);
        assertEquals(yard, yard);
    }

    @Test
    void testEquality_Symmetric() {
        Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.FEET);
        Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
    }

    @Test
    void testEquality_Transitive() {
        Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.YARDS);
        Quantity<LengthUnit> b = new Quantity<>(3.0, LengthUnit.FEET);
        Quantity<LengthUnit> c = new Quantity<>(36.0, LengthUnit.INCHES);
        assertTrue(a.equals(b));
        assertTrue(b.equals(c));
        assertTrue(a.equals(c));
    }

    @Test
    void testEquality_Complex_AllUnits() {
        assertEquals(new Quantity<>(2.0, LengthUnit.YARDS), new Quantity<>(6.0, LengthUnit.FEET));
        assertEquals(new Quantity<>(6.0, LengthUnit.FEET),  new Quantity<>(72.0, LengthUnit.INCHES));
        assertEquals(new Quantity<>(2.0, LengthUnit.YARDS), new Quantity<>(72.0, LengthUnit.INCHES));
    }

    // -------------------------------------------------------------------------
    // Length — null and non-Quantity comparisons
    // -------------------------------------------------------------------------

    @Test
    void testEquality_NullComparison_ReturnsFalse() {
        assertNotEquals(new Quantity<>(2.0, LengthUnit.YARDS), null);
    }

    @Test
    void testEquality_DifferentClass_ReturnsFalse() {
        Quantity<LengthUnit> yard = new Quantity<>(2.0, LengthUnit.YARDS);
        assertFalse(yard.equals("2.0"));
    }

    // -------------------------------------------------------------------------
    // Length — precision (epsilon tolerance)
    // -------------------------------------------------------------------------

    @Test
    void testEquality_SmallDifference_InsideTolerance() {
        assertEquals(new Quantity<>(1.00000008, LengthUnit.FEET), new Quantity<>(1.0, LengthUnit.FEET));
    }

    @Test
    void testEquality_SmallDifference_OutsideTolerance() {
        assertNotEquals(new Quantity<>(1.0000003, LengthUnit.FEET), new Quantity<>(1.0, LengthUnit.FEET));
    }

    @Test
    void testEquality_VerySmallValues_InsideEpsilon() {
        assertEquals(
            new Quantity<>(1e-9,           LengthUnit.INCHES),
            new Quantity<>(1.0000005e-9,   LengthUnit.INCHES)
        );
        assertEquals(
            new Quantity<>(1e-8,  LengthUnit.FEET),
            new Quantity<>(1.2e-7, LengthUnit.INCHES)
        );
    }

    @Test
    void testEquality_VerySmallValues_OutsideEpsilon() {
        assertNotEquals(
            new Quantity<>(1e-9,           LengthUnit.INCHES),
            new Quantity<>(1e-9 + 2e-6,    LengthUnit.INCHES)
        );
    }

    // -------------------------------------------------------------------------
    // Length — constructor validation
    // -------------------------------------------------------------------------

    @Test
    void testConstructor_NullUnit_Throws() {
        assertThrows(IllegalArgumentException.class, () -> new Quantity<>(1.0, (LengthUnit) null));
    }

    @Test
    void testConstructor_NaN_Throws() {
        assertThrows(IllegalArgumentException.class, () -> new Quantity<>(Double.NaN, LengthUnit.FEET));
    }

    @Test
    void testConstructor_Infinity_Throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Quantity<>(Double.POSITIVE_INFINITY, LengthUnit.CENTIMETERS));
    }

    // -------------------------------------------------------------------------
    // Weight — equality
    // -------------------------------------------------------------------------

    @Test
    void testEquality_Kilogram_SameValue() {
        assertEquals(new Quantity<>(1.0, WeightUnit.KILOGRAM), new Quantity<>(1.0, WeightUnit.KILOGRAM));
    }

    @Test
    void testEquality_Kilogram_DifferentValue() {
        assertNotEquals(new Quantity<>(1.0, WeightUnit.KILOGRAM), new Quantity<>(2.0, WeightUnit.KILOGRAM));
    }

    @Test
    void testEquality_Gram_SameValue() {
        assertEquals(new Quantity<>(500.0, WeightUnit.GRAM), new Quantity<>(500.0, WeightUnit.GRAM));
    }

    @Test
    void testEquality_Pound_SameValue() {
        assertEquals(new Quantity<>(2.0, WeightUnit.POUND), new Quantity<>(2.0, WeightUnit.POUND));
    }

    @Test
    void testEquality_Kilogram_Equals_1000Gram() {
        assertEquals(new Quantity<>(1.0,    WeightUnit.KILOGRAM), new Quantity<>(1000.0, WeightUnit.GRAM));
        assertEquals(new Quantity<>(1000.0, WeightUnit.GRAM),     new Quantity<>(1.0,    WeightUnit.KILOGRAM));
    }

    @Test
    void testEquality_Kilogram_Equals_Pound() {
        assertEquals(new Quantity<>(1.0, WeightUnit.KILOGRAM), new Quantity<>(2.204624, WeightUnit.POUND));
    }

    @Test
    void testEquality_Gram_Equals_Pound() {
        assertEquals(new Quantity<>(453.592370, WeightUnit.GRAM), new Quantity<>(1.0, WeightUnit.POUND));
    }

    @Test
    void testEquality_Weight_Symmetric() {
        Quantity<WeightUnit> a = new Quantity<>(1.0, WeightUnit.KILOGRAM);
        Quantity<WeightUnit> b = new Quantity<>(1000.0, WeightUnit.GRAM);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
    }

    @Test
    void testEquality_Weight_Transitive() {
        Quantity<WeightUnit> a = new Quantity<>(1.0,     WeightUnit.KILOGRAM);
        Quantity<WeightUnit> b = new Quantity<>(1000.0,  WeightUnit.GRAM);
        Quantity<WeightUnit> c = new Quantity<>(2.204624, WeightUnit.POUND);
        assertTrue(a.equals(b));
        assertTrue(b.equals(c));
        assertTrue(a.equals(c));
    }

    @Test
    void testEquality_Weight_NullComparison() {
        assertFalse(new Quantity<>(1.0, WeightUnit.KILOGRAM).equals(null));
    }

    @Test
    void testEquality_Weight_Reflexive() {
        Quantity<WeightUnit> w = new Quantity<>(2.0, WeightUnit.KILOGRAM);
        assertEquals(w, w);
    }

    @Test
    void testEquality_Weight_ZeroValue() {
        assertEquals(new Quantity<>(0.0, WeightUnit.KILOGRAM), new Quantity<>(0.0, WeightUnit.GRAM));
    }

    @Test
    void testEquality_Weight_NegativeValue() {
        assertEquals(new Quantity<>(-1.0, WeightUnit.KILOGRAM), new Quantity<>(-1000.0, WeightUnit.GRAM));
    }

    @Test
    void testEquality_Weight_LargeValue() {
        assertEquals(new Quantity<>(1_000_000.0, WeightUnit.GRAM), new Quantity<>(1000.0, WeightUnit.KILOGRAM));
    }

    @Test
    void testEquality_Weight_SmallValue() {
        assertEquals(new Quantity<>(0.001, WeightUnit.KILOGRAM), new Quantity<>(1.0, WeightUnit.GRAM));
    }

    @Test
    void testConstructor_Weight_NullUnit_Throws() {
        assertThrows(IllegalArgumentException.class, () -> new Quantity<>(1.0, (WeightUnit) null));
    }

    // -------------------------------------------------------------------------
    // Cross-category — must not be equal
    // -------------------------------------------------------------------------

    @Test
    void testEquality_CrossCategory_LengthVsWeight() {
        assertFalse(new Quantity<>(1.0, LengthUnit.FEET).equals(new Quantity<>(1.0, WeightUnit.KILOGRAM)));
    }

    @Test
    void testEquality_CrossCategory_WeightVsLength() {
        assertFalse(new Quantity<>(1.0, WeightUnit.KILOGRAM).equals(new Quantity<>(1.0, LengthUnit.FEET)));
    }

    @Test
    void testEquality_CrossCategory_TemperatureVsLength() {
        assertFalse(new Quantity<>(100.0, TemperatureUnit.CELSIUS).equals(new Quantity<>(100.0, LengthUnit.FEET)));
    }

    @Test
    void testEquality_CrossCategory_VolumeVsLength() {
        assertFalse(new Quantity<>(1.0, VolumeUnit.LITRE).equals(new Quantity<>(1.0, LengthUnit.FEET)));
    }

    @Test
    void testEquality_CrossCategory_VolumeVsWeight() {
        assertFalse(new Quantity<>(1.0, VolumeUnit.LITRE).equals(new Quantity<>(1.0, WeightUnit.KILOGRAM)));
    }

    // -------------------------------------------------------------------------
    // HashCode contract
    // -------------------------------------------------------------------------

    @Test
    void testHashCode_EqualLengthObjects_HaveSameHash() {
        Quantity<LengthUnit> a = new Quantity<>(5.0,  LengthUnit.FEET);
        Quantity<LengthUnit> b = new Quantity<>(5.0,  LengthUnit.FEET);
        assertEquals(a.hashCode(), b.hashCode());

        Quantity<LengthUnit> c = new Quantity<>(1.0,  LengthUnit.YARDS);
        Quantity<LengthUnit> d = new Quantity<>(36.0, LengthUnit.INCHES);
        assertEquals(c, d);
        assertEquals(c.hashCode(), d.hashCode());

        Quantity<LengthUnit> e = new Quantity<>(2.54, LengthUnit.CENTIMETERS);
        Quantity<LengthUnit> f = new Quantity<>(1.0,  LengthUnit.INCHES);
        assertEquals(e, f);
        assertEquals(e.hashCode(), f.hashCode());
    }

    @Test
    void testHashCode_UnequalLengthObjects_HaveDifferentHash() {
        Quantity<LengthUnit> a = new Quantity<>(1.0000003, LengthUnit.FEET);
        Quantity<LengthUnit> b = new Quantity<>(1.0,       LengthUnit.FEET);
        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testHashCode_EqualVolumeObjects_HaveSameHash() {
        Quantity<VolumeUnit> a = new Quantity<>(1.0,    VolumeUnit.LITRE);
        Quantity<VolumeUnit> b = new Quantity<>(1000.0, VolumeUnit.MILLILITRE);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testHashCode_Consistency_LengthVsWeight() {
        Quantity<LengthUnit> len = new Quantity<>(3.0, LengthUnit.FEET);
        Quantity<WeightUnit> wt  = new Quantity<>(3.0, WeightUnit.KILOGRAM);
        // They are not equal; no hashCode contract requirement, but must not throw
        assertNotEquals(len, wt);
    }
}
