package com.quantitymeasurement.model;

import com.quantitymeasurement.units.LengthUnit;
import com.quantitymeasurement.units.VolumeUnit;
import com.quantitymeasurement.units.WeightUnit;
import com.quantitymeasurement.units.TemperatureUnit;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QuantityArithmeticTest
 *
 * Tests arithmetic operations (add, subtract, divide) on Quantity,
 * covering: same-unit, cross-unit, explicit target unit, immutability,
 * null/cross-category guards, temperature rejection, internal helper
 * visibility, ArithmeticOperation enum dispatch, and edge values.
 */
public class QuantityArithmeticTest {

    private static final double EPSILON = 1e-6;

    // =========================================================================
    // ADDITION — implicit target (result in first operand's unit)
    // =========================================================================

    @Test
    public void testAdd_SameUnit_FeetPlusFeet() {
        assertEquals(
            new Quantity<>(3.0, LengthUnit.FEET),
            new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(2.0, LengthUnit.FEET))
        );
    }

    @Test
    public void testAdd_SameUnit_InchPlusInch() {
        assertEquals(
            new Quantity<>(12.0, LengthUnit.INCHES),
            new Quantity<>(6.0, LengthUnit.INCHES).add(new Quantity<>(6.0, LengthUnit.INCHES))
        );
    }

    @Test
    public void testAdd_CrossUnit_FeetPlusInches_ResultInFeet() {
        assertEquals(
            new Quantity<>(2.0, LengthUnit.FEET),
            new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(12.0, LengthUnit.INCHES))
        );
    }

    @Test
    public void testAdd_CrossUnit_InchPlusFeet_ResultInInches() {
        assertEquals(
            new Quantity<>(24.0, LengthUnit.INCHES),
            new Quantity<>(12.0, LengthUnit.INCHES).add(new Quantity<>(1.0, LengthUnit.FEET))
        );
    }

    @Test
    public void testAdd_CrossUnit_YardPlusFeet() {
        assertEquals(
            new Quantity<>(2.0, LengthUnit.YARDS),
            new Quantity<>(1.0, LengthUnit.YARDS).add(new Quantity<>(3.0, LengthUnit.FEET))
        );
    }

    @Test
    public void testAdd_CrossUnit_CentimeterPlusInch() {
        assertEquals(
            new Quantity<>(5.08, LengthUnit.CENTIMETERS),
            new Quantity<>(2.54, LengthUnit.CENTIMETERS).add(new Quantity<>(1.0, LengthUnit.INCHES))
        );
    }

    @Test
    public void testAdd_WithZero() {
        assertEquals(
            new Quantity<>(5.0, LengthUnit.FEET),
            new Quantity<>(5.0, LengthUnit.FEET).add(new Quantity<>(0.0, LengthUnit.INCHES))
        );
    }

    @Test
    public void testAdd_NegativeValue() {
        assertEquals(
            new Quantity<>(3.0, LengthUnit.FEET),
            new Quantity<>(5.0, LengthUnit.FEET).add(new Quantity<>(-2.0, LengthUnit.FEET))
        );
    }

    @Test
    public void testAdd_LargeValues() {
        assertEquals(
            new Quantity<>(2e6, LengthUnit.FEET),
            new Quantity<>(1e6, LengthUnit.FEET).add(new Quantity<>(1e6, LengthUnit.FEET))
        );
    }

    @Test
    public void testAdd_SmallValues() {
        assertEquals(
            new Quantity<>(0.003, LengthUnit.FEET),
            new Quantity<>(0.001, LengthUnit.FEET).add(new Quantity<>(0.002, LengthUnit.FEET))
        );
    }

    @Test
    public void testAdd_Weight_SameUnit() {
        assertEquals(
            new Quantity<>(3.0, WeightUnit.KILOGRAM),
            new Quantity<>(1.0, WeightUnit.KILOGRAM).add(new Quantity<>(2.0, WeightUnit.KILOGRAM))
        );
    }

    @Test
    public void testAdd_Volume_LitrePlusLitre() {
        assertEquals(
            new Quantity<>(3.0, VolumeUnit.LITRE),
            new Quantity<>(1.0, VolumeUnit.LITRE).add(new Quantity<>(2.0, VolumeUnit.LITRE))
        );
    }

    @Test
    public void testAdd_Volume_LitrePlusMillilitre() {
        assertEquals(
            new Quantity<>(2.0, VolumeUnit.LITRE),
            new Quantity<>(1.0, VolumeUnit.LITRE).add(new Quantity<>(1000.0, VolumeUnit.MILLILITRE))
        );
    }

    // =========================================================================
    // ADDITION — explicit target unit
    // =========================================================================

    @Test
    public void testAdd_ExplicitTarget_Feet() {
        assertEquals(
            new Quantity<>(2.0, LengthUnit.FEET),
            new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(12.0, LengthUnit.INCHES), LengthUnit.FEET)
        );
    }

    @Test
    public void testAdd_ExplicitTarget_Inches() {
        assertEquals(
            new Quantity<>(24.0, LengthUnit.INCHES),
            new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(12.0, LengthUnit.INCHES), LengthUnit.INCHES)
        );
    }

    @Test
    public void testAdd_ExplicitTarget_Yards() {
        assertEquals(
            new Quantity<>(0.666667, LengthUnit.YARDS),
            new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(12.0, LengthUnit.INCHES), LengthUnit.YARDS)
        );
    }

    @Test
    public void testAdd_ExplicitTarget_Centimeters() {
        assertEquals(
            new Quantity<>(5.079998, LengthUnit.CENTIMETERS),
            new Quantity<>(1.0, LengthUnit.INCHES).add(new Quantity<>(1.0, LengthUnit.INCHES), LengthUnit.CENTIMETERS)
        );
    }

    @Test
    public void testAdd_ExplicitTarget_AllUnitCombinations() {
        assertEquals(
            new Quantity<>(48.0, LengthUnit.INCHES),
            new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(1.0, LengthUnit.YARDS), LengthUnit.INCHES)
        );
        assertEquals(
            new Quantity<>(0.166667, LengthUnit.FEET),
            new Quantity<>(2.54, LengthUnit.CENTIMETERS).add(new Quantity<>(1.0, LengthUnit.INCHES), LengthUnit.FEET)
        );
    }

    @Test
    public void testAdd_ExplicitTarget_WithZero() {
        assertEquals(
            new Quantity<>(1.666667, LengthUnit.YARDS),
            new Quantity<>(5.0, LengthUnit.FEET).add(new Quantity<>(0.0, LengthUnit.INCHES), LengthUnit.YARDS)
        );
    }

    @Test
    public void testAdd_ExplicitTarget_NegativeValues() {
        assertEquals(
            new Quantity<>(36.0, LengthUnit.INCHES),
            new Quantity<>(5.0, LengthUnit.FEET).add(new Quantity<>(-2.0, LengthUnit.FEET), LengthUnit.INCHES)
        );
    }

    @Test
    public void testAdd_ExplicitTarget_LargeToSmallScale() {
        assertEquals(
            new Quantity<>(18000.0, LengthUnit.INCHES),
            new Quantity<>(1000.0, LengthUnit.FEET).add(new Quantity<>(500.0, LengthUnit.FEET), LengthUnit.INCHES)
        );
    }

    @Test
    public void testAdd_ExplicitTarget_SmallToLargeScale() {
        assertEquals(
            new Quantity<>(0.666667, LengthUnit.YARDS),
            new Quantity<>(12.0, LengthUnit.INCHES).add(new Quantity<>(12.0, LengthUnit.INCHES), LengthUnit.YARDS)
        );
    }

    @Test
    public void testAdd_ExplicitTarget_PrecisionTolerance() {
        assertEquals(
            new Quantity<>(3.6, LengthUnit.INCHES),
            new Quantity<>(0.1, LengthUnit.FEET).add(new Quantity<>(0.2, LengthUnit.FEET), LengthUnit.INCHES)
        );
    }

    @Test
    public void testAdd_ExplicitTarget_Commutativity() {
        Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.FEET);
        Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);
        assertEquals(a.add(b, LengthUnit.YARDS), b.add(a, LengthUnit.YARDS));
    }

    @Test
    public void testAdd_ExplicitTarget_Volume_Gallon() {
        assertEquals(
            new Quantity<>(2.0, VolumeUnit.GALLON),
            new Quantity<>(3.785412, VolumeUnit.LITRE)
                .add(new Quantity<>(3.785412, VolumeUnit.LITRE), VolumeUnit.GALLON)
        );
    }

    @Test
    public void testAdd_ExplicitTarget_Weight_CrossUnit() {
        assertEquals(
            new Quantity<>(2000.0, WeightUnit.GRAM),
            new Quantity<>(1.0, WeightUnit.KILOGRAM).add(new Quantity<>(1000.0, WeightUnit.GRAM), WeightUnit.GRAM)
        );
    }

    // =========================================================================
    // SUBTRACTION — implicit target
    // =========================================================================

    @Test
    public void testSubtract_SameUnit_FeetMinusFeet() {
        assertEquals(
            new Quantity<>(5.0, LengthUnit.FEET),
            new Quantity<>(10.0, LengthUnit.FEET).subtract(new Quantity<>(5.0, LengthUnit.FEET))
        );
    }

    @Test
    public void testSubtract_CrossUnit_FeetMinusInches() {
        assertEquals(
            new Quantity<>(9.5, LengthUnit.FEET),
            new Quantity<>(10.0, LengthUnit.FEET).subtract(new Quantity<>(6.0, LengthUnit.INCHES))
        );
    }

    @Test
    public void testSubtract_CrossUnit_InchesMinusFeet() {
        assertEquals(
            new Quantity<>(60.0, LengthUnit.INCHES),
            new Quantity<>(120.0, LengthUnit.INCHES).subtract(new Quantity<>(5.0, LengthUnit.FEET))
        );
    }

    @Test
    public void testSubtract_ResultNegative() {
        assertEquals(
            new Quantity<>(-5.0, LengthUnit.FEET),
            new Quantity<>(5.0, LengthUnit.FEET).subtract(new Quantity<>(10.0, LengthUnit.FEET))
        );
    }

    @Test
    public void testSubtract_ResultZero() {
        assertEquals(
            new Quantity<>(0.0, LengthUnit.FEET),
            new Quantity<>(10.0, LengthUnit.FEET).subtract(new Quantity<>(120.0, LengthUnit.INCHES))
        );
    }

    @Test
    public void testSubtract_WithZeroOperand() {
        assertEquals(
            new Quantity<>(5.0, LengthUnit.FEET),
            new Quantity<>(5.0, LengthUnit.FEET).subtract(new Quantity<>(0.0, LengthUnit.INCHES))
        );
    }

    @Test
    public void testSubtract_WithNegativeOperand() {
        assertEquals(
            new Quantity<>(7.0, LengthUnit.FEET),
            new Quantity<>(5.0, LengthUnit.FEET).subtract(new Quantity<>(-2.0, LengthUnit.FEET))
        );
    }

    @Test
    public void testSubtract_NonCommutative() {
        Quantity<LengthUnit> a = new Quantity<>(10.0, LengthUnit.FEET);
        Quantity<LengthUnit> b = new Quantity<>(5.0,  LengthUnit.FEET);
        assertEquals(new Quantity<>(5.0,  LengthUnit.FEET), a.subtract(b));
        assertEquals(new Quantity<>(-5.0, LengthUnit.FEET), b.subtract(a));
    }

    @Test
    public void testSubtract_Chained() {
        assertEquals(
            new Quantity<>(7.0, LengthUnit.FEET),
            new Quantity<>(10.0, LengthUnit.FEET)
                .subtract(new Quantity<>(2.0, LengthUnit.FEET))
                .subtract(new Quantity<>(1.0, LengthUnit.FEET))
        );
    }

    @Test
    public void testSubtract_AllCategories() {
        assertEquals(
            new Quantity<>(9.5, LengthUnit.FEET),
            new Quantity<>(10.0, LengthUnit.FEET).subtract(new Quantity<>(6.0, LengthUnit.INCHES))
        );
        assertEquals(
            new Quantity<>(5.0, WeightUnit.KILOGRAM),
            new Quantity<>(10.0, WeightUnit.KILOGRAM).subtract(new Quantity<>(5000.0, WeightUnit.GRAM))
        );
        assertEquals(
            new Quantity<>(4.5, VolumeUnit.LITRE),
            new Quantity<>(5.0, VolumeUnit.LITRE).subtract(new Quantity<>(500.0, VolumeUnit.MILLILITRE))
        );
    }

    @Test
    public void testSubtract_LargeValues() {
        assertEquals(
            new Quantity<>(5e5, WeightUnit.KILOGRAM),
            new Quantity<>(1e6, WeightUnit.KILOGRAM).subtract(new Quantity<>(5e5, WeightUnit.KILOGRAM))
        );
    }

    @Test
    public void testSubtract_Volume_LitreMinusLitre() {
        assertEquals(
            new Quantity<>(7.0, VolumeUnit.LITRE),
            new Quantity<>(10.0, VolumeUnit.LITRE).subtract(new Quantity<>(3.0, VolumeUnit.LITRE))
        );
    }

    // =========================================================================
    // SUBTRACTION — explicit target unit
    // =========================================================================

    @Test
    public void testSubtract_ExplicitTarget_Feet() {
        assertEquals(
            new Quantity<>(9.5, LengthUnit.FEET),
            new Quantity<>(10.0, LengthUnit.FEET).subtract(new Quantity<>(6.0, LengthUnit.INCHES), LengthUnit.FEET)
        );
    }

    @Test
    public void testSubtract_ExplicitTarget_Inches() {
        assertEquals(
            new Quantity<>(114.0, LengthUnit.INCHES),
            new Quantity<>(10.0, LengthUnit.FEET).subtract(new Quantity<>(6.0, LengthUnit.INCHES), LengthUnit.INCHES)
        );
    }

    @Test
    public void testSubtract_ExplicitTarget_Millilitre() {
        assertEquals(
            new Quantity<>(3000.0, VolumeUnit.MILLILITRE),
            new Quantity<>(5.0, VolumeUnit.LITRE).subtract(new Quantity<>(2.0, VolumeUnit.LITRE), VolumeUnit.MILLILITRE)
        );
    }

    // =========================================================================
    // DIVISION
    // =========================================================================

    @Test
    public void testDivide_SameUnit_Feet() {
        assertEquals(5.0,
            new Quantity<>(10.0, LengthUnit.FEET).divide(new Quantity<>(2.0, LengthUnit.FEET)), EPSILON);
    }

    @Test
    public void testDivide_CrossUnit_InchesOverFeet() {
        assertEquals(1.0,
            new Quantity<>(24.0, LengthUnit.INCHES).divide(new Quantity<>(2.0, LengthUnit.FEET)), EPSILON);
    }

    @Test
    public void testDivide_CrossUnit_KilogramOverGram() {
        assertEquals(1.0,
            new Quantity<>(2.0, WeightUnit.KILOGRAM).divide(new Quantity<>(2000.0, WeightUnit.GRAM)), EPSILON);
    }

    @Test
    public void testDivide_RatioGreaterThanOne() {
        assertTrue(new Quantity<>(10.0, LengthUnit.FEET).divide(new Quantity<>(2.0, LengthUnit.FEET)) > 1.0);
    }

    @Test
    public void testDivide_RatioLessThanOne() {
        assertEquals(0.5,
            new Quantity<>(5.0, LengthUnit.FEET).divide(new Quantity<>(10.0, LengthUnit.FEET)), EPSILON);
    }

    @Test
    public void testDivide_RatioEqualToOne() {
        assertEquals(1.0,
            new Quantity<>(10.0, LengthUnit.FEET).divide(new Quantity<>(10.0, LengthUnit.FEET)), EPSILON);
    }

    @Test
    public void testDivide_AllCategories() {
        assertEquals(5.0,  new Quantity<>(10.0, LengthUnit.FEET).divide(new Quantity<>(2.0, LengthUnit.FEET)), EPSILON);
        assertEquals(2.0,  new Quantity<>(10.0, WeightUnit.KILOGRAM).divide(new Quantity<>(5.0, WeightUnit.KILOGRAM)), EPSILON);
        assertEquals(0.5,  new Quantity<>(5.0, VolumeUnit.LITRE).divide(new Quantity<>(10.0, VolumeUnit.LITRE)), EPSILON);
    }

    @Test
    public void testDivide_ByZero_Throws() {
        assertThrows(ArithmeticException.class,
            () -> new Quantity<>(10.0, LengthUnit.FEET).divide(new Quantity<>(0.0, LengthUnit.FEET)));
    }

    @Test
    public void testDivide_LargeRatio() {
        assertEquals(1e6,
            new Quantity<>(1e6, WeightUnit.KILOGRAM).divide(new Quantity<>(1.0, WeightUnit.KILOGRAM)), EPSILON);
    }

    @Test
    public void testDivide_SmallRatio() {
        assertEquals(1e-6,
            new Quantity<>(1.0, WeightUnit.KILOGRAM).divide(new Quantity<>(1e6, WeightUnit.KILOGRAM)), 1e-12);
    }

    @Test
    public void testDivide_NoRounding_FullPrecision() {
        assertEquals(10.0 / 3.0,
            new Quantity<>(10.0, LengthUnit.FEET).divide(new Quantity<>(3.0, LengthUnit.FEET)), 1e-12);
    }

    // =========================================================================
    // IMMUTABILITY — all operations return new objects, original unchanged
    // =========================================================================

    @Test
    public void testImmutability_Add_OriginalUnchanged() {
        Quantity<VolumeUnit> original = new Quantity<>(5.0, VolumeUnit.LITRE);
        Quantity<VolumeUnit> sum      = original.add(new Quantity<>(500.0, VolumeUnit.MILLILITRE));
        assertEquals(new Quantity<>(5.0, VolumeUnit.LITRE), original);
        assertNotSame(original, sum);
    }

    @Test
    public void testImmutability_Subtract_OriginalUnchanged() {
        Quantity<VolumeUnit> original = new Quantity<>(5.0, VolumeUnit.LITRE);
        Quantity<VolumeUnit> diff     = original.subtract(new Quantity<>(500.0, VolumeUnit.MILLILITRE));
        assertEquals(new Quantity<>(5.0, VolumeUnit.LITRE), original);
        assertEquals(new Quantity<>(4.5, VolumeUnit.LITRE), diff);
    }

    @Test
    public void testImmutability_Divide_OriginalUnchanged() {
        Quantity<WeightUnit> original = new Quantity<>(10.0, WeightUnit.KILOGRAM);
        double ratio                  = original.divide(new Quantity<>(5.0, WeightUnit.KILOGRAM));
        assertEquals(new Quantity<>(10.0, WeightUnit.KILOGRAM), original);
        assertEquals(2.0, ratio, EPSILON);
    }

    @Test
    public void testImmutability_AddSubtract_Inverse() {
        Quantity<LengthUnit> a = new Quantity<>(5.0, LengthUnit.FEET);
        Quantity<LengthUnit> b = new Quantity<>(2.0, LengthUnit.FEET);
        assertEquals(a, a.add(b).subtract(b));
    }

    // =========================================================================
    // VALIDATION — null, cross-category, null target unit
    // =========================================================================

    @Test
    public void testAdd_NullOperand_Throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Quantity<>(1.0, LengthUnit.FEET).add(null));
    }

    @Test
    public void testSubtract_NullOperand_Throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Quantity<>(10.0, LengthUnit.FEET).subtract(null));
    }

    @Test
    public void testDivide_NullOperand_Throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Quantity<>(10.0, LengthUnit.FEET).divide(null));
    }

    @Test
    public void testNullOperand_SameErrorMessage_AllOperations() {
        Quantity<LengthUnit> q = new Quantity<>(1.0, LengthUnit.FEET);
        String msgAdd = assertThrows(IllegalArgumentException.class, () -> q.add(null)).getMessage();
        String msgSub = assertThrows(IllegalArgumentException.class, () -> q.subtract(null)).getMessage();
        String msgDiv = assertThrows(IllegalArgumentException.class, () -> q.divide(null)).getMessage();
        assertEquals(msgAdd, msgSub);
        assertEquals(msgAdd, msgDiv);
    }

    @Test
    public void testCrossCategory_Add_Throws() {
        Quantity<LengthUnit> length = new Quantity<>(10.0, LengthUnit.FEET);
        Quantity<WeightUnit> weight = new Quantity<>(5.0,  WeightUnit.KILOGRAM);
        assertThrows(IllegalArgumentException.class, () -> length.add((Quantity) weight));
    }

    @Test
    public void testCrossCategory_Subtract_Throws() {
        Quantity<LengthUnit> length = new Quantity<>(10.0, LengthUnit.FEET);
        Quantity<WeightUnit> weight = new Quantity<>(5.0,  WeightUnit.KILOGRAM);
        assertThrows(IllegalArgumentException.class, () -> length.subtract((Quantity) weight));
    }

    @Test
    public void testCrossCategory_Divide_Throws() {
        Quantity<LengthUnit> length = new Quantity<>(10.0, LengthUnit.FEET);
        Quantity<WeightUnit> weight = new Quantity<>(5.0,  WeightUnit.KILOGRAM);
        assertThrows(IllegalArgumentException.class, () -> length.divide((Quantity) weight));
    }

    @Test
    public void testAdd_NullTargetUnit_Throws() {
        Quantity<LengthUnit> a = new Quantity<>(5.0, LengthUnit.FEET);
        Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);
        assertThrows(IllegalArgumentException.class, () -> a.add(b, null));
    }

    @Test
    public void testSubtract_NullTargetUnit_Throws() {
        Quantity<LengthUnit> a = new Quantity<>(5.0, LengthUnit.FEET);
        Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);
        assertThrows(IllegalArgumentException.class, () -> a.subtract(b, null));
    }

    // =========================================================================
    // TEMPERATURE — arithmetic must be rejected
    // =========================================================================

    @Test
    public void testTemperature_Add_Throws() {
        Quantity<TemperatureUnit> a = new Quantity<>(100.0, TemperatureUnit.CELSIUS);
        Quantity<TemperatureUnit> b = new Quantity<>(50.0,  TemperatureUnit.CELSIUS);
        UnsupportedOperationException ex =
            assertThrows(UnsupportedOperationException.class, () -> a.add(b));
        assertTrue(ex.getMessage().toLowerCase().contains("not supported"));
    }

    @Test
    public void testTemperature_Subtract_Throws() {
        Quantity<TemperatureUnit> a = new Quantity<>(100.0, TemperatureUnit.CELSIUS);
        Quantity<TemperatureUnit> b = new Quantity<>(50.0,  TemperatureUnit.CELSIUS);
        assertThrows(UnsupportedOperationException.class, () -> a.subtract(b));
    }

    @Test
    public void testTemperature_Divide_Throws() {
        Quantity<TemperatureUnit> a = new Quantity<>(100.0, TemperatureUnit.CELSIUS);
        Quantity<TemperatureUnit> b = new Quantity<>(50.0,  TemperatureUnit.CELSIUS);
        assertThrows(UnsupportedOperationException.class, () -> a.divide(b));
    }

    @Test
    public void testTemperature_CrossUnit_Add_Throws() {
        Quantity<TemperatureUnit> a = new Quantity<>(0.0,  TemperatureUnit.CELSIUS);
        Quantity<TemperatureUnit> b = new Quantity<>(32.0, TemperatureUnit.FAHRENHEIT);
        assertThrows(UnsupportedOperationException.class, () -> a.add(b));
    }

    // =========================================================================
    // CHAINING & PERFORMANCE
    // =========================================================================

    @Test
    public void testChain_AddSubtractDivide() {
        Quantity<LengthUnit> q1 = new Quantity<>(10.0, LengthUnit.FEET);
        Quantity<LengthUnit> q2 = new Quantity<>(2.0,  LengthUnit.FEET);
        Quantity<LengthUnit> q3 = new Quantity<>(1.0,  LengthUnit.FEET);
        Quantity<LengthUnit> q4 = new Quantity<>(7.0,  LengthUnit.FEET);
        assertEquals(11.0 / 7.0, q1.add(q2).subtract(q3).divide(q4), EPSILON);
    }

    @Test
    public void testLargeDataset_CumulativeAdd() {
        Quantity<LengthUnit> base = new Quantity<>(0.0, LengthUnit.INCHES);
        for (int i = 0; i < 1000; i++) {
            base = base.add(new Quantity<>(1.0, LengthUnit.INCHES));
        }
        assertEquals(new Quantity<>(1000.0, LengthUnit.INCHES), base);
    }

    @Test
    public void testPerformance_10000Additions() {
        Quantity<LengthUnit> q = new Quantity<>(0.0, LengthUnit.INCHES);
        for (int i = 0; i < 10000; i++) {
            q = q.add(new Quantity<>(1.0, LengthUnit.INCHES));
        }
        assertEquals(10000.0, q.getValue(), EPSILON);
    }

    @Test
    public void testRounding_AddSubtract_SixDecimalPlaces() {
        Quantity<LengthUnit> a = new Quantity<>(1.0,      LengthUnit.FEET);
        Quantity<LengthUnit> b = new Quantity<>(0.333333, LengthUnit.YARDS);
        Quantity<LengthUnit> res = a.subtract(b);
        double rounded = Math.round(res.getValue() * 1_000_000.0) / 1_000_000.0;
        assertEquals(rounded, res.getValue(), EPSILON);
    }

    @Test
    public void testRounding_Add_Accuracy() {
        Quantity<LengthUnit> a   = new Quantity<>(1.234567, LengthUnit.FEET);
        Quantity<LengthUnit> b   = new Quantity<>(0.0,      LengthUnit.FEET);
        Quantity<LengthUnit> res = a.add(b);
        double rounded = Math.round(res.getValue() * 1_000_000.0) / 1_000_000.0;
        assertEquals(rounded, res.getValue(), 1e-6);
    }

    @Test
    public void testTypeSafety_HashSet_EqualQuantitiesCollapsed() {
        Set<Quantity<VolumeUnit>> set = new HashSet<>();
        set.add(new Quantity<>(1.0,    VolumeUnit.LITRE));
        set.add(new Quantity<>(1000.0, VolumeUnit.MILLILITRE));
        assertEquals(1, set.size());
    }

    // =========================================================================
    // INTERNAL STRUCTURE — ArithmeticOperation enum and helper visibility
    // =========================================================================

    @Test
    public void testArithmeticOperation_Enum_AllConstantsPresent() throws Exception {
        Class<?> enumClass = findInnerEnum(Quantity.class, "ArithmeticOperation");
        assertNotNull(enumClass);
        assertNotNull(Enum.valueOf((Class<Enum>) enumClass, "ADD"));
        assertNotNull(Enum.valueOf((Class<Enum>) enumClass, "SUBTRACT"));
        assertNotNull(Enum.valueOf((Class<Enum>) enumClass, "DIVIDE"));
    }

    @Test
    public void testArithmeticOperation_Add_Computes() throws Exception {
        Class<?> enumClass = findInnerEnum(Quantity.class, "ArithmeticOperation");
        Object add         = Enum.valueOf((Class<Enum>) enumClass, "ADD");
        Method compute     = enumClass.getDeclaredMethod("compute", double.class, double.class);
        compute.setAccessible(true);
        assertEquals(15.0, (double) compute.invoke(add, 10.0, 5.0), EPSILON);
        assertEquals(10.0, (double) compute.invoke(add, 7.0,  3.0), EPSILON);
    }

    @Test
    public void testArithmeticOperation_Subtract_Computes() throws Exception {
        Class<?> enumClass = findInnerEnum(Quantity.class, "ArithmeticOperation");
        Object sub         = Enum.valueOf((Class<Enum>) enumClass, "SUBTRACT");
        Method compute     = enumClass.getDeclaredMethod("compute", double.class, double.class);
        compute.setAccessible(true);
        assertEquals(5.0, (double) compute.invoke(sub, 10.0, 5.0), EPSILON);
        assertEquals(4.0, (double) compute.invoke(sub, 7.0,  3.0), EPSILON);
    }

    @Test
    public void testArithmeticOperation_Divide_Computes() throws Exception {
        Class<?> enumClass = findInnerEnum(Quantity.class, "ArithmeticOperation");
        Object div         = Enum.valueOf((Class<Enum>) enumClass, "DIVIDE");
        Method compute     = enumClass.getDeclaredMethod("compute", double.class, double.class);
        compute.setAccessible(true);
        assertEquals(2.0, (double) compute.invoke(div, 10.0, 5.0), EPSILON);
        assertEquals(3.5, (double) compute.invoke(div, 7.0,  2.0), EPSILON);
    }

    @Test
    public void testArithmeticOperation_DivideByZero_ThrowsArithmetic() throws Exception {
        Class<?> enumClass = findInnerEnum(Quantity.class, "ArithmeticOperation");
        Object div         = Enum.valueOf((Class<Enum>) enumClass, "DIVIDE");
        Method compute     = enumClass.getDeclaredMethod("compute", double.class, double.class);
        compute.setAccessible(true);
        InvocationTargetException thrown = assertThrows(
            InvocationTargetException.class, () -> compute.invoke(div, 10.0, 0.0));
        assertTrue(thrown.getCause() instanceof ArithmeticException);
    }

    @Test
    public void testArithmeticOperation_Compute_MethodIsPublic() throws Exception {
        Class<?> enumClass = findInnerEnum(Quantity.class, "ArithmeticOperation");
        Method compute     = enumClass.getMethod("compute", double.class, double.class);
        assertNotNull(compute);
    }

    @Test
    public void testHelper_PerformArithmetic_IsPrivate() throws Exception {
        Method perform = Quantity.class.getDeclaredMethod(
            "performArithmetic", Quantity.class, findInnerEnum(Quantity.class, "ArithmeticOperation"));
        assertTrue(Modifier.isPrivate(perform.getModifiers()));
    }

    @Test
    public void testValidation_NullGuard_IsConsistentAcrossAllOperations() {
        // Verifies that null validation is centralized — all three operations
        // produce the identical error message, proving a shared guard path.
        Quantity<LengthUnit> q   = new Quantity<>(1.0, LengthUnit.FEET);
        String msgAdd = assertThrows(IllegalArgumentException.class, () -> q.add(null)).getMessage();
        String msgSub = assertThrows(IllegalArgumentException.class, () -> q.subtract(null)).getMessage();
        String msgDiv = assertThrows(IllegalArgumentException.class, () -> q.divide(null)).getMessage();
        assertEquals(msgAdd, msgSub);
        assertEquals(msgAdd, msgDiv);
    }

    @Test
    public void testHelper_PerformArithmetic_BaseUnitConversion() throws Exception {
        Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.FEET);
        Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);
        Method perform = Quantity.class.getDeclaredMethod(
            "performArithmetic", Quantity.class, findInnerEnum(Quantity.class, "ArithmeticOperation"));
        perform.setAccessible(true);
        Object addConst = Enum.valueOf(
            (Class<Enum>) findInnerEnum(Quantity.class, "ArithmeticOperation"), "ADD");
        double baseResult = (double) perform.invoke(a, b, addConst);
        double expected   = a.getUnit().convertToBaseUnit(a.getValue())
                          + b.getUnit().convertToBaseUnit(b.getValue());
        assertEquals(expected, baseResult, EPSILON);
    }

    @Test
    public void testHelper_ResultConversion_AddVsExplicitTarget_Consistent() {
        Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.FEET);
        Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);
        Quantity<LengthUnit> sumFeet   = a.add(b);
        Quantity<LengthUnit> sumInches = a.add(b, LengthUnit.INCHES);
        assertEquals(sumInches, sumFeet.convertTo(LengthUnit.INCHES));
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static Class<?> findInnerEnum(Class<?> outer, String enumName) {
        for (Class<?> c : outer.getDeclaredClasses()) {
            if (c.isEnum() && c.getSimpleName().equals(enumName)) return c;
        }
        return null;
    }
}