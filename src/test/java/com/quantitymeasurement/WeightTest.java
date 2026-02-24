package com.quantitymeasurement;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class WeightTest {
	
	private static final double EPSILON = 1e-6;

	 @Test
	    void testEquality_KilogramToKilogram_SameValue() {
	        assertEquals(
	            new Weight(1.0, WeightUnit.KILOGRAM),
	            new Weight(1.0, WeightUnit.KILOGRAM)
	        );
	    }

	    @Test
	    void testEquality_KilogramToKilogram_DifferentValue() {
	        assertNotEquals(
	            new Weight(1.0, WeightUnit.KILOGRAM),
	            new Weight(2.0, WeightUnit.KILOGRAM)
	        );
	    }

	    @Test
	    void testEquality_GramToGram_SameValue() {
	        assertEquals(
	            new Weight(500.0, WeightUnit.GRAM),
	            new Weight(500.0, WeightUnit.GRAM)
	        );
	    }

	    @Test
	    void testEquality_PoundToPound_SameValue() {
	        assertEquals(
	            new Weight(2.0, WeightUnit.POUND),
	            new Weight(2.0, WeightUnit.POUND)
	        );
	    }

	    @Test
	    void testEquality_KilogramToGram_EquivalentValue() {
	        assertEquals(
	            new Weight(1.0, WeightUnit.KILOGRAM),
	            new Weight(1000.0, WeightUnit.GRAM)
	        );
	    }

	    @Test
	    void testEquality_GramToKilogram_EquivalentValue() {
	        assertEquals(
	            new Weight(1000.0, WeightUnit.GRAM),
	            new Weight(1.0, WeightUnit.KILOGRAM)
	        );
	    }

	    @Test
	    void testEquality_KilogramToPound_EquivalentValue() {
	        assertTrue(
	            new Weight(1.0, WeightUnit.KILOGRAM)
	            .equals(new Weight(2.204624, WeightUnit.POUND))
	        );
	    }

	    @Test
	    void testEquality_GramToPound_EquivalentValue() {
	        assertTrue(
	            new Weight(453.592370, WeightUnit.GRAM)
	            .equals(new Weight(1.0, WeightUnit.POUND))
	        );
	    }

	    @Test
	    void testEquality_Symmetry() {
	        Weight a = new Weight(1.0, WeightUnit.KILOGRAM);
	        Weight b = new Weight(1000.0, WeightUnit.GRAM);
	        assertTrue(a.equals(b));
	        assertTrue(b.equals(a));
	    }

	    @Test
	    void testEquality_Transitive() {
	        Weight a = new Weight(1.0, WeightUnit.KILOGRAM);
	        Weight b = new Weight(1000.0, WeightUnit.GRAM);
	        Weight c = new Weight(2.204624, WeightUnit.POUND);
	        assertTrue(a.equals(b));
	        assertTrue(b.equals(c));
	        assertTrue(a.equals(c));
	    }

	    @Test
	    void testEquality_WeightVsLength_Incompatible() {
	        Weight w = new Weight(1.0, WeightUnit.KILOGRAM);
	        Length l = new Length(1.0, LengthUnit.FEET);
	        assertFalse(w.equals(l));
	    }

	    @Test
	    void testEquality_NullComparison() {
	        assertFalse(new Weight(1.0, WeightUnit.KILOGRAM).equals(null));
	    }

	    @Test
	    void testEquality_SameReference() {
	        Weight w = new Weight(2.0, WeightUnit.KILOGRAM);
	        assertEquals(w, w);
	    }

	    @Test
	    void testEquality_NullUnit_Weight() {
	        assertThrows(
	            IllegalArgumentException.class, 
	            () -> new Weight(1.0, null)
	        );
	    }

	    @Test
	    void testEquality_ZeroValue() {
	        assertEquals(
	            new Weight(0.0, WeightUnit.KILOGRAM),
	            new Weight(0.0, WeightUnit.GRAM)
	        );
	    }

	    @Test
	    void testEquality_NegativeWeight() {
	        assertEquals(
	            new Weight(-1.0, WeightUnit.KILOGRAM),
	            new Weight(-1000.0, WeightUnit.GRAM)
	        );
	    }

	    @Test
	    void testEquality_LargeWeightValue() {
	        assertEquals(
	            new Weight(1_000_000.0, WeightUnit.GRAM),
	            new Weight(1000.0, WeightUnit.KILOGRAM)
	        );
	    }

	    @Test
	    void testEquality_SmallWeightValue() {
	        assertEquals(
	            new Weight(0.001, WeightUnit.KILOGRAM),
	            new Weight(1.0, WeightUnit.GRAM)
	        );
	    }

	    @Test
	    void testConversion_PoundToKilogram() {
	        Weight converted = new Weight(2.204624, WeightUnit.POUND).convertTo(WeightUnit.KILOGRAM);
	        assertEquals(1.0, converted.getValue(), EPSILON);
	        assertEquals(WeightUnit.KILOGRAM, converted.getUnit());
	    }

	    @Test
	    void testConversion_KilogramToPound() {
	        Weight converted = new Weight(1.0, WeightUnit.KILOGRAM).convertTo(WeightUnit.POUND);
	        assertEquals(2.204624, converted.getValue(), EPSILON);
	        assertEquals(WeightUnit.POUND, converted.getUnit());
	    }

	    @Test
	    void testConversion_SameUnit_Weight() {
	        Weight converted = new Weight(5.0, WeightUnit.KILOGRAM).convertTo(WeightUnit.KILOGRAM);
	        assertEquals(new Weight(5.0, WeightUnit.KILOGRAM), converted);
	    }

	    @Test
	    void testConversion_ZeroValue_Weight() {
	        Weight converted = new Weight(0.0, WeightUnit.KILOGRAM).convertTo(WeightUnit.GRAM);
	        assertEquals(new Weight(0.0, WeightUnit.GRAM), converted);
	    }

	    @Test
	    void testConversion_NegativeValue_Weight() {
	        Weight converted = new Weight(-1.0, WeightUnit.KILOGRAM).convertTo(WeightUnit.GRAM);
	        assertEquals(new Weight(-1000.0, WeightUnit.GRAM), converted);
	    }

	    @Test
	    void testConversion_RoundTrip() {
	        Weight original = new Weight(1.5, WeightUnit.KILOGRAM);
	        Weight roundTrip = original
	            .convertTo(WeightUnit.GRAM)
	            .convertTo(WeightUnit.KILOGRAM);
	        assertEquals(original, roundTrip);
	    }

	    @Test
	    void testAddition_SameUnit_KilogramPlusKilogram() {
	        Weight sum = new Weight(1.0, WeightUnit.KILOGRAM)
	            .add(new Weight(2.0, WeightUnit.KILOGRAM));
	        assertEquals(new Weight(3.0, WeightUnit.KILOGRAM), sum);
	    }

	    @Test
	    void testAddition_CrossUnit_KilogramPlusGram() {
	        Weight sum = new Weight(1.0, WeightUnit.KILOGRAM)
	            .add(new Weight(1000.0, WeightUnit.GRAM));
	        assertEquals(new Weight(2.0, WeightUnit.KILOGRAM), sum);
	    }

	    @Test
	    void testAddition_CrossUnit_PoundPlusKilogram() {
	        Weight sum = new Weight(2.204624, WeightUnit.POUND)
	            .add(new Weight(1.0, WeightUnit.KILOGRAM));
	        assertEquals(4.409248, sum.getValue(), 1e6);
	        assertEquals(WeightUnit.POUND, sum.getUnit());
	    }

	    @Test
	    void testAddition_WithZero_Weight() {
	        Weight sum = new Weight(5.0, WeightUnit.KILOGRAM)
	            .add(new Weight(0.0, WeightUnit.GRAM));
	        assertEquals(new Weight(5.0, WeightUnit.KILOGRAM), sum);
	    }

	    @Test
	    void testAddition_NegativeValues_Weight() {
	        Weight sum = new Weight(5.0, WeightUnit.KILOGRAM)
	            .add(new Weight(-2000.0, WeightUnit.GRAM));
	        assertEquals(new Weight(3.0, WeightUnit.KILOGRAM), sum);
	    }

	    @Test
	    void testAddition_LargeValues_Weight() {
	        Weight sum = new Weight(1e6, WeightUnit.KILOGRAM)
	            .add(new Weight(1e6, WeightUnit.KILOGRAM));
	        assertEquals(new Weight(2e6, WeightUnit.KILOGRAM), sum);
	    }

	    @Test
	    void testAddition_ExplicitTargetUnit_Kilogram() {
	        Weight sum = new Weight(1.0, WeightUnit.KILOGRAM)
	            .add(new Weight(1000.0, WeightUnit.GRAM), WeightUnit.GRAM);
	        assertEquals(new Weight(2000.0, WeightUnit.GRAM), sum);
	    }

	    @Test
	    void testAddition_ExplicitTargetUnit_Pound() {
	        Weight sum = new Weight(1.0, WeightUnit.POUND)
	            .add(new Weight(453.592, WeightUnit.GRAM), WeightUnit.POUND);
	        assertEquals(2.0, sum.getValue(), EPSILON);
	        assertEquals(WeightUnit.POUND, sum.getUnit());
	    }

	    @Test
	    void testAddition_ExplicitTargetUnit_Kilogram_FromKgAndPound() {
	        Weight sum = new Weight(2.0, WeightUnit.KILOGRAM)
	            .add(new Weight(4.0, WeightUnit.POUND), WeightUnit.KILOGRAM);
	        assertEquals(3.814368, sum.getValue(), 1e-5);
	        assertEquals(WeightUnit.KILOGRAM, sum.getUnit());
	    }

	    @Test
	    void testAddition_Commutativity_Weight() {
	        Weight a = new Weight(1.0, WeightUnit.KILOGRAM)
	            .add(new Weight(1000.0, WeightUnit.GRAM));
	        Weight b = new Weight(1000.0, WeightUnit.GRAM)
	            .add(new Weight(1.0, WeightUnit.KILOGRAM));
	        assertEquals(a.convertTo(WeightUnit.KILOGRAM), b.convertTo(WeightUnit.KILOGRAM));
	    }

	    @Test
	    void testHashCode_ConsistencyWithEquals_InSet() {
	        Set<Weight> set = new HashSet<>();
	        set.add(new Weight(1.0, WeightUnit.KILOGRAM));
	        set.add(new Weight(1000.0, WeightUnit.GRAM));
	        assertEquals(1, set.size());
	    }

	    @Test
	    void testHashCode_MapKeyBehavior() {
	        Map<Weight, String> map = new HashMap<>();
	        map.put(new Weight(1.0, WeightUnit.KILOGRAM), "oneKg");
	        map.put(new Weight(1000.0, WeightUnit.GRAM), "oneKgAgain");
	        assertEquals(1, map.size());
	        assertTrue(map.containsKey(new Weight(1.0, WeightUnit.KILOGRAM)));
	    }

	    @Test
	    void testInvalidValue_NaN() {
	        assertThrows(
	            IllegalArgumentException.class, 
	            () -> new Weight(Double.NaN, WeightUnit.KILOGRAM)
	        );
	    }

	    @Test
	    void testInvalidValue_Infinite() {
	        assertThrows(
	            IllegalArgumentException.class, 
	            () -> new Weight(Double.POSITIVE_INFINITY, WeightUnit.KILOGRAM)
	        );
	    }

	    @Test
	    void testFromBaseUnit_InequalityDetection() {
	        assertNotEquals(
	            new Weight(1.0, WeightUnit.KILOGRAM),
	            new Weight(2.0, WeightUnit.KILOGRAM)
	        );
	    }
	}