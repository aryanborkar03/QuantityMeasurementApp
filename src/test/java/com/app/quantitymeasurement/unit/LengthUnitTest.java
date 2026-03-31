package com.app.quantitymeasurement.unit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LengthUnitTest
 *
 * Tests the LengthUnit enum's conversion factors, base unit conversions,
 * unit name, measurement type, arithmetic support, and enum identity.
 *
 * Base unit for length is INCHES.
 * Conversion factor for each unit equals convertToBaseUnit(1.0).

 */
public class LengthUnitTest {

    private static final double EPSILON = 1e-6;

    // =========================================================================
    // CONVERSION FACTOR  (factor = convertToBaseUnit(1.0) for linear units)
    // =========================================================================

    @Test
    public void testConversionFactor_Feet() {
        assertEquals(12.0, LengthUnit.FEET.convertToBaseUnit(1.0), EPSILON);
    }

    @Test
    public void testConversionFactor_Inches() {
        assertEquals(1.0, LengthUnit.INCHES.convertToBaseUnit(1.0), EPSILON);
    }

    @Test
    public void testConversionFactor_Yards() {
        assertEquals(36.0, LengthUnit.YARDS.convertToBaseUnit(1.0), EPSILON);
    }

    @Test
    public void testConversionFactor_Centimeters() {
        assertEquals(1.0 / 2.54, LengthUnit.CENTIMETERS.convertToBaseUnit(1.0), EPSILON);
    }

    // =========================================================================
    // convertToBaseUnit  (result in INCHES)
    // =========================================================================

    @Test
    public void testConvertToBaseUnit_Feet() {
        assertEquals(60.0, LengthUnit.FEET.convertToBaseUnit(5.0), EPSILON);
    }

    @Test
    public void testConvertToBaseUnit_Inches() {
        assertEquals(12.0, LengthUnit.INCHES.convertToBaseUnit(12.0), EPSILON);
    }

    @Test
    public void testConvertToBaseUnit_Yards() {
        assertEquals(36.0, LengthUnit.YARDS.convertToBaseUnit(1.0), EPSILON);
    }

    @Test
    public void testConvertToBaseUnit_Centimeters() {
        assertEquals(12.0, LengthUnit.CENTIMETERS.convertToBaseUnit(30.48), EPSILON);
    }

    // =========================================================================
    // convertFromBaseUnit  (from INCHES to target unit)
    // =========================================================================

    @Test
    public void testConvertFromBaseUnit_ToFeet() {
        assertEquals(1.0, LengthUnit.FEET.convertFromBaseUnit(12.0), EPSILON);
    }

    @Test
    public void testConvertFromBaseUnit_ToInches() {
        assertEquals(12.0, LengthUnit.INCHES.convertFromBaseUnit(12.0), EPSILON);
    }

    @Test
    public void testConvertFromBaseUnit_ToYards() {
        assertEquals(1.0, LengthUnit.YARDS.convertFromBaseUnit(36.0), EPSILON);
    }

    @Test
    public void testConvertFromBaseUnit_ToCentimeters() {
        assertEquals(30.48, LengthUnit.CENTIMETERS.convertFromBaseUnit(12.0), EPSILON);
    }

    // =========================================================================
    // UNIT IDENTITY
    // =========================================================================

    @Test
    public void testGetUnitName() {
        assertEquals("FEET",        LengthUnit.FEET.getUnitName());
        assertEquals("INCHES",      LengthUnit.INCHES.getUnitName());
        assertEquals("YARDS",       LengthUnit.YARDS.getUnitName());
        assertEquals("CENTIMETERS", LengthUnit.CENTIMETERS.getUnitName());
    }

    @Test
    public void testGetMeasurementType() {
        assertEquals("LengthUnit", LengthUnit.FEET.getMeasurementType());
        assertEquals("LengthUnit", LengthUnit.INCHES.getMeasurementType());
        assertEquals("LengthUnit", LengthUnit.YARDS.getMeasurementType());
        assertEquals("LengthUnit", LengthUnit.CENTIMETERS.getMeasurementType());
    }

    @Test
    public void testEnumConstants_AllPresent() {
        assertDoesNotThrow(() -> LengthUnit.valueOf("FEET"));
        assertDoesNotThrow(() -> LengthUnit.valueOf("INCHES"));
        assertDoesNotThrow(() -> LengthUnit.valueOf("YARDS"));
        assertDoesNotThrow(() -> LengthUnit.valueOf("CENTIMETERS"));
    }

    @Test
    public void testEnumConstants_AreEnumInstances() {
        assertTrue(LengthUnit.FEET        instanceof Enum);
        assertTrue(LengthUnit.INCHES      instanceof Enum);
        assertTrue(LengthUnit.YARDS       instanceof Enum);
        assertTrue(LengthUnit.CENTIMETERS instanceof Enum);
    }

    // =========================================================================
    // ARITHMETIC SUPPORT — LengthUnit implements SupportsArithmetic
    // =========================================================================

    @Test
    public void testSupportsArithmetic_AllLengthUnits() {
        assertTrue(LengthUnit.FEET        instanceof SupportsArithmetic);
        assertTrue(LengthUnit.INCHES      instanceof SupportsArithmetic);
        assertTrue(LengthUnit.YARDS       instanceof SupportsArithmetic);
        assertTrue(LengthUnit.CENTIMETERS instanceof SupportsArithmetic);
    }
}