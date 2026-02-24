package com.quantitymeasurement;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LengthTest {
	
	private static final double EPSILON = 1e-6;

	@Test
	public void testEquality_YardToYard_SameValue() {
		Length yard1 = new Length(1.0, LengthUnit.YARDS);
		Length yard2 = new Length(1.0, LengthUnit.YARDS);
		assertEquals(yard1, yard2);
	}

	@Test
	public void testEquality_YardToYard_DifferentValue() {
		Length yard1 = new Length(1.0, LengthUnit.YARDS);
		Length yard2 = new Length(2.0, LengthUnit.YARDS);
		assertNotEquals(yard1, yard2);
	}

	@Test
	public void testEquality_YardToFeet_EquivalentValue() {
		Length yard1 = new Length(1.0, LengthUnit.YARDS);
		Length feet3 = new Length(3.0, LengthUnit.FEET);
		assertEquals(yard1, feet3);
	}

	@Test
	public void testEquality_FeetToYard_EquivalentValue() {
		Length feet3 = new Length(3.0, LengthUnit.FEET);
		Length yard1 = new Length(1.0, LengthUnit.YARDS);
		assertEquals(feet3, yard1);
	}

	@Test
	public void testEquality_YardToInches_EquivalentValue() {
		Length yard1 = new Length(1.0, LengthUnit.YARDS);
		Length inch36 = new Length(36.0, LengthUnit.INCHES);
		assertEquals(yard1, inch36);
	}

	@Test
	public void testEquality_InchesToYard_EquivalentValue() {
		Length inch36 = new Length(36.0, LengthUnit.INCHES);
		Length yard1 = new Length(1.0, LengthUnit.YARDS);
		assertEquals(inch36, yard1);
	}

	@Test
	public void testEquality_YardToFeet_NonEquivalentValue() {
		Length yard1 = new Length(1.0, LengthUnit.YARDS);
		Length feet2 = new Length(2.0, LengthUnit.FEET);
		assertNotEquals(yard1, feet2);
	}
	
	@Test
	public void testEquality_CentimetersToInches_EquivalentValue() {
		Length cm1 = new Length(1.0, LengthUnit.CENTIMETERS);
		Length inchValue = new Length(0.393701, LengthUnit.INCHES);
		assertEquals(cm1, inchValue);
	}
	
	@Test
	public void testEquality_CentimetersToFeet_NonEquivalentValue() {
		Length cm1 = new Length(1.0, LengthUnit.CENTIMETERS);
		Length feet1 = new Length(1.0, LengthUnit.FEET);
		assertNotEquals(cm1, feet1);
	}
	
	@Test
	public void testEquality_MultiUnit_TransitiveProperty() {
		Length yard = new Length(1.0, LengthUnit.YARDS);
		Length feet = new Length(3.0, LengthUnit.FEET);
		Length inches = new Length(36.0, LengthUnit.INCHES);
		assertEquals(yard, feet);
		assertEquals(feet, inches);
		assertEquals(yard, inches);
	}
	
	@Test
	public void testEquality_NullUnit() {
		assertThrows(
			IllegalArgumentException.class, 
			() -> new Length(1.0, null)
		);
	}
	
	@Test
	public void testEquality_YardSameReference() {
		Length yard = new Length(2.0, LengthUnit.YARDS);
		assertEquals(yard, yard);
	}

	@Test
	public void testEquality_YardNullComparison() {
		Length yard = new Length(2.0, LengthUnit.YARDS);
		assertNotEquals(yard, null);
	}
	
	@Test
	public void testEquality_CentimetersSameReference() {
		Length cm = new Length(2.0, LengthUnit.CENTIMETERS);
		assertEquals(cm, cm);
	}
	
	@Test
	public void testEquality_CentimetersNullComparison() {
		Length cm = new Length(2.0, LengthUnit.CENTIMETERS);
		assertNotEquals(cm, null);
	}
	
	@Test
	public void testEquality_AllUnits_ComplexScenario() {
		Length yards = new Length(2.0, LengthUnit.YARDS);
		Length feet = new Length(6.0, LengthUnit.FEET);
		Length inches = new Length(72.0, LengthUnit.INCHES);
		assertEquals(yards, feet);
		assertEquals(feet, inches);
		assertEquals(yards, inches);
	}
	
	@Test
	public void testEquality_CentimetersToCentimeters_SameValue() {
		Length cm2a = new Length(2.0, LengthUnit.CENTIMETERS);
		Length cm2b = new Length(2.0, LengthUnit.CENTIMETERS);
		assertEquals(cm2a, cm2b);
	}

	@Test
	public void testEquality_CentimetersToCentimeters_DifferentValue() {
		Length cm2 = new Length(2.0, LengthUnit.CENTIMETERS);
		Length cm3 = new Length(3.0, LengthUnit.CENTIMETERS);
		assertNotEquals(cm2, cm3);
	}

	@Test
	public void testEquality_InchesToCentimeters_EquivalentValue() {
		Length inchValue = new Length(0.393701, LengthUnit.INCHES);
		Length cm1 = new Length(1.0, LengthUnit.CENTIMETERS);
		assertEquals(inchValue, cm1);
	}

	@Test
	public void testEquality_DifferentClass() {
		Length yard = new Length(2.0, LengthUnit.YARDS);
		assertFalse(yard.equals("2.0"));
	}

	@Test
	public void testEquality_NaN() {
		assertThrows(
			IllegalArgumentException.class, 
			() -> new Length(Double.NaN, LengthUnit.FEET)
		);
	}

	@Test
	public void testEquality_Infinity() {
		assertThrows(
			IllegalArgumentException.class,
			() -> new Length(Double.POSITIVE_INFINITY, LengthUnit.CENTIMETERS)
		);
	}

	@Test
	public void testEquality_DemonstrateLengthComparisonMethod() {
		boolean result = QuantityMeasurementApp.demonstrateLengthComparison(
			1.0, LengthUnit.YARDS, 
			36.0, LengthUnit.INCHES
		);
		assertTrue(result);
	}
	
	@Test
    public void testConversion_FeetToInches() {
        Length result = QuantityMeasurementApp.demonstrateLengthConversion(1.0, LengthUnit.FEET, LengthUnit.INCHES);
        Length expected = new Length(12.0, LengthUnit.INCHES);
        assertEquals(expected, result);
    }

    @Test
    public void testConversion_InchesToFeet() {
        Length result = QuantityMeasurementApp.demonstrateLengthConversion(24.0, LengthUnit.INCHES, LengthUnit.FEET);
        Length expected = new Length(2.0, LengthUnit.FEET);
        assertEquals(expected, result);
    }

    @Test
    public void testConversion_YardsToInches() {
        Length result = QuantityMeasurementApp.demonstrateLengthConversion(1.0, LengthUnit.YARDS, LengthUnit.INCHES);
        Length expected = new Length(36.0, LengthUnit.INCHES);
        assertEquals(expected, result);
    }

    @Test
    public void testConversion_InchesToYards() {
        Length result = QuantityMeasurementApp.demonstrateLengthConversion(72.0, LengthUnit.INCHES, LengthUnit.YARDS);
        Length expected = new Length(2.0, LengthUnit.YARDS);
        assertEquals(expected, result);
    }

    @Test
    public void testConversion_CentimetersToInches() {
        Length result = QuantityMeasurementApp.demonstrateLengthConversion(2.54, LengthUnit.CENTIMETERS, LengthUnit.INCHES);
        Length expected = new Length(1.0, LengthUnit.INCHES);
        assertTrue(result.equals(expected));
    }

    @Test
    public void testConversion_FeetToYards() {
        Length result = QuantityMeasurementApp.demonstrateLengthConversion(6.0, LengthUnit.FEET, LengthUnit.YARDS);
        Length expected = new Length(2.0, LengthUnit.YARDS);
        assertEquals(expected, result);
    }

    @Test
    public void testConversion_RoundTrip_PreservesValue() {
        Length original = new Length(3.0, LengthUnit.FEET);
        Length converted = original.convertTo(LengthUnit.INCHES).convertTo(LengthUnit.FEET);
        assertTrue(original.equals(converted));
    }

    @Test
    public void testConversion_ZeroValue() {
        Length result = QuantityMeasurementApp.demonstrateLengthConversion(0.0, LengthUnit.FEET, LengthUnit.INCHES);
        Length expected = new Length(0.0, LengthUnit.INCHES);
        assertEquals(expected, result);
    }

    @Test
    public void testConversion_NegativeValue() {
        Length result = QuantityMeasurementApp.demonstrateLengthConversion(-1.0, LengthUnit.FEET, LengthUnit.INCHES);
        Length expected = new Length(-12.0, LengthUnit.INCHES);
        assertEquals(expected, result);
    }

    @Test
    public void testConversion_InvalidUnit_Throws() {
        assertThrows(
    		IllegalArgumentException.class, 
    		() -> QuantityMeasurementApp.demonstrateLengthConversion(1.0, null, LengthUnit.INCHES)
    	);
    }

    @Test
    public void testConversion_NaNOrInfinite_Throws() {
        assertThrows(
    		IllegalArgumentException.class, 
    		() -> new Length(Double.NaN, LengthUnit.FEET)
    	);
        assertThrows(
    		IllegalArgumentException.class, 
    		() -> new Length(Double.POSITIVE_INFINITY, LengthUnit.INCHES)
		);
    }

    @Test
    public void testConversion_SameUnit() {
        Length result = QuantityMeasurementApp.demonstrateLengthConversion(5.0, LengthUnit.FEET, LengthUnit.FEET);
        Length expected = new Length(5.0, LengthUnit.FEET);
        assertEquals(expected, result);
    }
    
    @Test
    public void testAddition_SameUnit_FeetPlusFeet() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(1.0, LengthUnit.FEET),
            new Length(2.0, LengthUnit.FEET)
        );
        assertEquals(new Length(3.0, LengthUnit.FEET), result);
    }

    @Test
    public void testAddition_SameUnit_InchPlusInch() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(6.0, LengthUnit.INCHES),
            new Length(6.0, LengthUnit.INCHES)
        );
        assertEquals(new Length(12.0, LengthUnit.INCHES), result);
    }

    @Test
    public void testAddition_CrossUnit_FeetPlusInches() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(1.0, LengthUnit.FEET),
            new Length(12.0, LengthUnit.INCHES)
        );
        assertEquals(new Length(2.0, LengthUnit.FEET), result);
    }

    @Test
    public void testAddition_CrossUnit_InchPlusFeet() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(12.0, LengthUnit.INCHES),
            new Length(1.0, LengthUnit.FEET)
        );
        assertEquals(new Length(24.0, LengthUnit.INCHES), result);
    }

    @Test
    public void testAddition_CrossUnit_YardPlusFeet() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(1.0, LengthUnit.YARDS),
            new Length(3.0, LengthUnit.FEET)
        );
        assertEquals(new Length(2.0, LengthUnit.YARDS), result);
    }

    @Test
    public void testAddition_CrossUnit_CentimeterPlusInch() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(2.54, LengthUnit.CENTIMETERS),
            new Length(1.0, LengthUnit.INCHES)
        );
        assertTrue(result.equals(new Length(5.08, LengthUnit.CENTIMETERS)));
    }

    @Test
    public void testAddition_Commutativity() {
        Length a = new Length(1.0, LengthUnit.FEET);
        Length b = new Length(12.0, LengthUnit.INCHES);
        assertEquals(a.add(b), b.add(a).convertTo(LengthUnit.FEET));
    }

    @Test
    public void testAddition_WithZero() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(5.0, LengthUnit.FEET),
            new Length(0.0, LengthUnit.INCHES)
        );
        assertEquals(new Length(5.0, LengthUnit.FEET), result);
    }

    @Test
    public void testAddition_NegativeValues() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(5.0, LengthUnit.FEET),
            new Length(-2.0, LengthUnit.FEET)
        );
        assertEquals(new Length(3.0, LengthUnit.FEET), result);
    }

    @Test
    public void testAddition_NullSecondOperand() {
        assertThrows(
    		IllegalArgumentException.class, 
    		() -> QuantityMeasurementApp.demonstrateLengthAddition(new Length(1.0, LengthUnit.FEET), null)
		);
    }

    @Test
    public void testAddition_LargeValues() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(1e6, LengthUnit.FEET),
            new Length(1e6, LengthUnit.FEET)
        );
        assertEquals(new Length(2e6, LengthUnit.FEET), result);
    }

    @Test
    public void testAddition_SmallValues() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(0.001, LengthUnit.FEET),
            new Length(0.002, LengthUnit.FEET)
        );
        assertTrue(result.equals(new Length(0.003, LengthUnit.FEET)));
    }
    
    @Test
    public void testAddition_ExplicitTargetUnit_Feet() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
	        new Length(1.0, LengthUnit.FEET),
	        new Length(12.0, LengthUnit.INCHES),
	        LengthUnit.FEET
        );
        assertEquals(new Length(2.0, LengthUnit.FEET), result);
    }

    @Test
    public void testAddition_ExplicitTargetUnit_Inches() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(1.0, LengthUnit.FEET),
            new Length(12.0, LengthUnit.INCHES),
            LengthUnit.INCHES
        );
        assertEquals(new Length(24.0, LengthUnit.INCHES), result);
    }

    @Test
    public void testAddition_ExplicitTargetUnit_Yards() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(1.0, LengthUnit.FEET),
            new Length(12.0, LengthUnit.INCHES),
            LengthUnit.YARDS
        );
        assertTrue(result.equals(new Length(0.666667, LengthUnit.YARDS)));
    }

    @Test
    public void testAddition_ExplicitTargetUnit_Centimeters() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(1.0, LengthUnit.INCHES),
            new Length(1.0, LengthUnit.INCHES),
            LengthUnit.CENTIMETERS
        );
        assertTrue(result.equals(new Length(5.079998, LengthUnit.CENTIMETERS)));
    }

    @Test
    public void testAddition_ExplicitTargetUnit_SameAsFirstOperand() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(2.0, LengthUnit.YARDS),
            new Length(3.0, LengthUnit.FEET),
            LengthUnit.YARDS
        );
        assertEquals(new Length(3.0, LengthUnit.YARDS), result);
    }

    @Test
    public void testAddition_ExplicitTargetUnit_SameAsSecondOperand() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(2.0, LengthUnit.YARDS),
            new Length(3.0, LengthUnit.FEET),
            LengthUnit.FEET
        );
        assertEquals(new Length(9.0, LengthUnit.FEET), result);
    }

    @Test
    public void testAddition_ExplicitTargetUnit_Commutativity() {
        Length a = new Length(1.0, LengthUnit.FEET);
        Length b = new Length(12.0, LengthUnit.INCHES);
        Length result1 = QuantityMeasurementApp.demonstrateLengthAddition(a, b, LengthUnit.YARDS);
        Length result2 = QuantityMeasurementApp.demonstrateLengthAddition(b, a, LengthUnit.YARDS);
        assertTrue(result1.equals(result2));
    }

    @Test
    public void testAddition_ExplicitTargetUnit_WithZero() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(5.0, LengthUnit.FEET),
            new Length(0.0, LengthUnit.INCHES),
            LengthUnit.YARDS
        );
        assertTrue(result.equals(new Length(1.666667, LengthUnit.YARDS)));
    }

    @Test
    public void testAddition_ExplicitTargetUnit_NegativeValues() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(5.0, LengthUnit.FEET),
            new Length(-2.0, LengthUnit.FEET),
            LengthUnit.INCHES
        );
        assertEquals(new Length(36.0, LengthUnit.INCHES), result);
    }

    @Test
    public void testAddition_ExplicitTargetUnit_NullTargetUnit() {
        assertThrows(
    		IllegalArgumentException.class, 
    		() -> QuantityMeasurementApp.demonstrateLengthAddition(
                new Length(1.0, LengthUnit.FEET),
                new Length(12.0, LengthUnit.INCHES),
                null
            )
		);
    }

    @Test
    public void testAddition_ExplicitTargetUnit_LargeToSmallScale() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(1000.0, LengthUnit.FEET),
            new Length(500.0, LengthUnit.FEET),
            LengthUnit.INCHES
        );
        assertEquals(new Length(18000.0, LengthUnit.INCHES), result);
    }

    @Test
    public void testAddition_ExplicitTargetUnit_SmallToLargeScale() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(12.0, LengthUnit.INCHES),
            new Length(12.0, LengthUnit.INCHES),
            LengthUnit.YARDS
        );
        assertTrue(result.equals(new Length(0.666667, LengthUnit.YARDS)));
    }

    @Test
    public void testAddition_ExplicitTargetUnit_AllUnitCombinations() {
        Length result1 = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(1.0, LengthUnit.FEET),
            new Length(1.0, LengthUnit.YARDS),
            LengthUnit.INCHES
        );
        assertTrue(new Length(48.0, LengthUnit.INCHES).equals(result1));

        Length result2 = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(2.54, LengthUnit.CENTIMETERS),
            new Length(1.0, LengthUnit.INCHES),
            LengthUnit.FEET
        );
        assertTrue(result2.equals(new Length(0.166667, LengthUnit.FEET)));
    }

    @Test
    public void testAddition_ExplicitTargetUnit_PrecisionTolerance() {
        Length result = QuantityMeasurementApp.demonstrateLengthAddition(
            new Length(0.1, LengthUnit.FEET),
            new Length(0.2, LengthUnit.FEET),
            LengthUnit.INCHES
        );
        assertTrue(result.equals(new Length(3.6, LengthUnit.INCHES)));
    }
    
    @Test
    public void testEquality_VerySmallValues_WithinEpsilon() {
        Length a = new Length(1e-9, LengthUnit.INCHES);
        Length b = new Length(1.0000005e-9, LengthUnit.INCHES);
        assertEquals(a, b);

        Length c = new Length(1e-8, LengthUnit.FEET);
        Length d = new Length(1.2e-7, LengthUnit.INCHES);
        assertEquals(c, d);

        Length e = new Length(1e-9, LengthUnit.INCHES);
        Length f = new Length(1e-9 + 2e-6, LengthUnit.INCHES);
        assertNotEquals(e, f);
    }

    @Test
    public void testHashCode_EqualObjectsHaveSameHashCode() {
        Length a1 = new Length(5.0, LengthUnit.FEET);
        Length a2 = new Length(5.0, LengthUnit.FEET);
        assertEquals(a1.hashCode(), a2.hashCode());

        Length b1 = new Length(1.0, LengthUnit.YARDS);
        Length b2 = new Length(36.0, LengthUnit.INCHES);
        assertEquals(b1, b2);           // just to confirm equality
        assertEquals(b1.hashCode(), b2.hashCode());

        Length c1 = new Length(2.54, LengthUnit.CENTIMETERS);
        Length c2 = new Length(1.0, LengthUnit.INCHES);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        Length d1 = new Length(1.0000003, LengthUnit.FEET);
        Length d2 = new Length(1.0, LengthUnit.FEET);
        assertNotEquals(d1, d2);
        assertNotEquals(d1.hashCode(), d2.hashCode());
    }
    
    @Test
    public void testEquality_SmallDifference_OutsideTolerance() {
        Length a = new Length(1.0000003, LengthUnit.FEET);
        Length b = new Length(1.0, LengthUnit.FEET);
        assertNotEquals(a, b);
    }

    @Test
    public void testEquality_SmallDifference_InsideTolerance() {
        Length a = new Length(1.00000008, LengthUnit.FEET);
        Length b = new Length(1.0, LengthUnit.FEET);
        assertEquals(a, b);
    }
    
    @Test
    public void testLengthUnitEnum_FeetConstant() {
        assertEquals(
			12.0, 
			LengthUnit.FEET.getConversionFactor(),
			EPSILON
		);
    }

    @Test
    public void testLengthUnitEnum_InchesConstant() {
        assertEquals(
    		1.0,
            LengthUnit.INCHES.getConversionFactor(),
            EPSILON
        );
    }

    @Test
    public void testLengthUnitEnum_YardsConstant() {
        assertEquals(
    		36.0,
            LengthUnit.YARDS.getConversionFactor(),
            EPSILON
        );
    }

    @Test
    public void testLengthUnitEnum_CentimetersConstant() {
        assertEquals(
        	1.0 / 2.54,
            LengthUnit.CENTIMETERS.getConversionFactor(),
            EPSILON
        );
    }

    @Test
    public void testConvertToBaseUnit_FeetToInches() {
        assertEquals(
    		60.0,
            LengthUnit.FEET.convertToBaseUnit(5.0),
            EPSILON
        );
    }

    @Test
    public void testConvertToBaseUnit_InchesToInches() {
        assertEquals(
    		12.0,
            LengthUnit.INCHES.convertToBaseUnit(12.0),
            EPSILON
        );
    }

    @Test
    public void testConvertToBaseUnit_YardsToInches() {
        assertEquals(
    		36.0,
            LengthUnit.YARDS.convertToBaseUnit(1.0),
            EPSILON
        );
    }

    @Test
    public void testConvertToBaseUnit_CentimetersToInches() {
        assertEquals(
    		12.0,
            LengthUnit.CENTIMETERS.convertToBaseUnit(30.48),
            EPSILON
        );
    }

    @Test
    public void testConvertFromBaseUnit_InchesToFeet() {
        assertEquals(
    		1.0,
            LengthUnit.FEET.convertFromBaseUnit(12.0),
            EPSILON
        );
    }

    @Test
    public void testConvertFromBaseUnit_InchesToInches() {
        assertEquals(
    		12.0,
            LengthUnit.INCHES.convertFromBaseUnit(12.0),
            EPSILON
        );
    }

    @Test
    public void testConvertFromBaseUnit_InchesToYards() {
        assertEquals(
    		1.0,
            LengthUnit.YARDS.convertFromBaseUnit(36.0),
            EPSILON
        );
    }

    @Test
    public void testConvertFromBaseUnit_InchesToCentimeters() {
        assertEquals(
    		30.48,
            LengthUnit.CENTIMETERS.convertFromBaseUnit(12.0),
            EPSILON
        );
    }

    @Test
    public void testQuantityLengthRefactored_Equality() {
        Length a = new Length(1.0, LengthUnit.FEET);
        Length b = new Length(12.0, LengthUnit.INCHES);

        assertTrue(a.equals(b));
    }

    @Test
    public void testQuantityLengthRefactored_ConvertTo() {
        Length result = new Length(1.0, LengthUnit.FEET).convertTo(LengthUnit.INCHES);

        assertEquals(new Length(12.0, LengthUnit.INCHES), result);
    }

    @Test
    public void testQuantityLengthRefactored_Add() {
        Length result = new Length(1.0, LengthUnit.FEET)
    		.add(
	    		new Length(12.0, LengthUnit.INCHES),
	            LengthUnit.FEET
	        );

        assertEquals(new Length(2.0, LengthUnit.FEET), result);
    }

    @Test
    public void testQuantityLengthRefactored_AddWithTargetUnit() {
        Length result = new Length(1.0, LengthUnit.FEET)
    		.add(
				new Length(12.0, LengthUnit.INCHES),
				LengthUnit.YARDS
			);

        assertEquals(new Length(0.666667, LengthUnit.YARDS), result);
    }

    @Test
    public void testQuantityLengthRefactored_NullUnit() {
        assertThrows(
    		IllegalArgumentException.class,
            () -> new Length(1.0, null)
        );
    }

    @Test
    public void testQuantityLengthRefactored_InvalidValue() {
        assertThrows(
    		IllegalArgumentException.class,
            () -> new Length(Double.NaN, LengthUnit.FEET)
        );
    }

    @Test
    public void testBackwardCompatibility_UC1EqualityTests() {
        assertEquals(
    		new Length(3.0, LengthUnit.FEET),
            new Length(36.0, LengthUnit.INCHES)
        );
    }

    @Test
    public void testBackwardCompatibility_UC5ConversionTests() {
        Length result = new Length(1.0, LengthUnit.YARDS)
            .convertTo(LengthUnit.FEET);

        assertEquals(new Length(3.0, LengthUnit.FEET), result);
    }

    @Test
    public void testBackwardCompatibility_UC6AdditionTests() {
        Length result = new Length(2.0, LengthUnit.FEET)
            .add(new Length(24.0, LengthUnit.INCHES));

        assertEquals(new Length(4.0, LengthUnit.FEET), result);
    }

    @Test
    public void testBackwardCompatibility_UC7AdditionWithTargetUnitTests() {
        Length result = new Length(2.0, LengthUnit.FEET)
            .add(
        		new Length(24.0, LengthUnit.INCHES),
                LengthUnit.INCHES
            );

        assertEquals(new Length(48.0, LengthUnit.INCHES), result);
    }

    @Test
    public void testArchitecturalScalability_MultipleCategories() {
        assertDoesNotThrow(() -> LengthUnit.valueOf("FEET"));
    }

    @Test
    public void testRoundTripConversion_RefactoredDesign() {
        Length original = new Length(5.0, LengthUnit.FEET);
        Length converted = original.convertTo(LengthUnit.CENTIMETERS)
            .convertTo(LengthUnit.FEET);

        assertEquals(original, converted);
    }

    @Test
    public void testUnitImmutability() {
        assertTrue(LengthUnit.FEET instanceof Enum);
        assertTrue(LengthUnit.INCHES instanceof Enum);
        assertTrue(LengthUnit.YARDS instanceof Enum);
        assertTrue(LengthUnit.CENTIMETERS instanceof Enum);
    }
}