package com.app.quantitymeasurement.unit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VolumeUnitTest
 *
 * Tests the VolumeUnit enum's conversion factors, base unit conversions,
 * unit name, measurement type, and arithmetic support.
 *
 * Base unit for volume is LITRE.
 * Conversion factor for each unit equals convertToBaseUnit(1.0).
 */
public class VolumeUnitTest {

    private static final double EPSILON = 1e-6;

    // =========================================================================
    // CONVERSION FACTOR  (factor = convertToBaseUnit(1.0) for linear units)
    // =========================================================================

    @Test
    public void testConversionFactor_Litre() {
        assertEquals(1.0, VolumeUnit.LITRE.convertToBaseUnit(1.0), EPSILON);
    }

    @Test
    public void testConversionFactor_Millilitre() {
        assertEquals(0.001, VolumeUnit.MILLILITRE.convertToBaseUnit(1.0), EPSILON);
    }

    @Test
    public void testConversionFactor_Gallon() {
        assertEquals(3.785412, VolumeUnit.GALLON.convertToBaseUnit(1.0), EPSILON);
    }

    // =========================================================================
    // convertToBaseUnit  (result in LITRE)
    // =========================================================================

    @Test
    public void testConvertToBaseUnit_Litre() {
        assertEquals(5.0, VolumeUnit.LITRE.convertToBaseUnit(5.0), EPSILON);
    }

    @Test
    public void testConvertToBaseUnit_Millilitre() {
        assertEquals(1.0, VolumeUnit.MILLILITRE.convertToBaseUnit(1000.0), EPSILON);
    }

    @Test
    public void testConvertToBaseUnit_Gallon() {
        assertEquals(3.785412, VolumeUnit.GALLON.convertToBaseUnit(1.0), EPSILON);
    }

    // =========================================================================
    // convertFromBaseUnit  (from LITRE to target unit)
    // =========================================================================

    @Test
    public void testConvertFromBaseUnit_ToLitre() {
        assertEquals(2.0, VolumeUnit.LITRE.convertFromBaseUnit(2.0), EPSILON);
    }

    @Test
    public void testConvertFromBaseUnit_ToMillilitre() {
        assertEquals(1000.0, VolumeUnit.MILLILITRE.convertFromBaseUnit(1.0), EPSILON);
    }

    @Test
    public void testConvertFromBaseUnit_ToGallon() {
        assertEquals(1.0, VolumeUnit.GALLON.convertFromBaseUnit(3.785412), EPSILON);
    }

    // =========================================================================
    // UNIT IDENTITY
    // =========================================================================

    @Test
    public void testGetUnitName() {
        assertEquals("LITRE",      VolumeUnit.LITRE.getUnitName());
        assertEquals("MILLILITRE", VolumeUnit.MILLILITRE.getUnitName());
        assertEquals("GALLON",     VolumeUnit.GALLON.getUnitName());
    }

    @Test
    public void testGetMeasurementType() {
        assertEquals("VolumeUnit", VolumeUnit.LITRE.getMeasurementType());
        assertEquals("VolumeUnit", VolumeUnit.MILLILITRE.getMeasurementType());
        assertEquals("VolumeUnit", VolumeUnit.GALLON.getMeasurementType());
    }

    @Test
    public void testEnumConstants_AllPresent() {
        assertDoesNotThrow(() -> VolumeUnit.valueOf("LITRE"));
        assertDoesNotThrow(() -> VolumeUnit.valueOf("MILLILITRE"));
        assertDoesNotThrow(() -> VolumeUnit.valueOf("GALLON"));
    }

    // =========================================================================
    // ARITHMETIC SUPPORT — VolumeUnit implements SupportsArithmetic
    // =========================================================================

    @Test
    public void testSupportsArithmetic_AllVolumeUnits() {
        assertTrue(VolumeUnit.LITRE       instanceof SupportsArithmetic);
        assertTrue(VolumeUnit.MILLILITRE  instanceof SupportsArithmetic);
        assertTrue(VolumeUnit.GALLON      instanceof SupportsArithmetic);
    }
}