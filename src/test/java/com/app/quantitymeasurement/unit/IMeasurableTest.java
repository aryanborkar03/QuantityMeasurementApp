package com.app.quantitymeasurement.unit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class IMeasurableTest {

    private static final double EPSILON = 1e-6;

    // =========================================================================
    // LengthUnit implements IMeasurable
    // =========================================================================

    @Test
    void testIMeasurable_LengthUnit_Feet() {
        IMeasurable unit = LengthUnit.FEET;
        assertEquals("FEET",       unit.getUnitName());
        assertEquals("LengthUnit", unit.getMeasurementType());
        assertEquals(12.0,         unit.convertToBaseUnit(1.0),    EPSILON);
        assertEquals(1.0,          unit.convertFromBaseUnit(12.0), EPSILON);
    }

    @Test
    void testIMeasurable_LengthUnit_Inches() {
        IMeasurable unit = LengthUnit.INCHES;
        assertEquals("INCHES",     unit.getUnitName());
        assertEquals(1.0,          unit.convertToBaseUnit(1.0),  EPSILON);
        assertEquals(1.0,          unit.convertFromBaseUnit(1.0), EPSILON);
    }

    @Test
    void testIMeasurable_ConsistentBehavior_LengthAndWeight() {
        IMeasurable length = LengthUnit.INCHES;
        IMeasurable weight = WeightUnit.GRAM;
        assertNotNull(length.getUnitName());
        assertNotNull(weight.getUnitName());
        assertNotNull(length.getMeasurementType());
        assertNotNull(weight.getMeasurementType());
    }

    // =========================================================================
    // WeightUnit implements IMeasurable
    // =========================================================================

    @Test
    void testIMeasurable_WeightUnit_Kilogram() {
        IMeasurable unit = WeightUnit.KILOGRAM;
        assertEquals("KILOGRAM",   unit.getUnitName());
        assertEquals("WeightUnit", unit.getMeasurementType());
        assertEquals(1.0,          unit.convertToBaseUnit(1.0),    EPSILON);
        assertEquals(1.0,          unit.convertFromBaseUnit(1.0),  EPSILON);
    }

    // =========================================================================
    // VolumeUnit implements IMeasurable
    // =========================================================================

    @Test
    void testIMeasurable_VolumeUnit_Litre() {
        IMeasurable unit = VolumeUnit.LITRE;
        assertEquals("LITRE",      unit.getUnitName());
        assertEquals("VolumeUnit", unit.getMeasurementType());
        assertEquals(1.0,          unit.convertToBaseUnit(1.0),    EPSILON);
        assertEquals(1.0,          unit.convertFromBaseUnit(1.0),  EPSILON);
    }

    // =========================================================================
    // TemperatureUnit implements IMeasurable
    // =========================================================================

    @Test
    void testIMeasurable_TemperatureUnit_Celsius_IsBaseUnit() {
        IMeasurable unit = TemperatureUnit.CELSIUS;
        assertEquals("CELSIUS",         unit.getUnitName());
        assertEquals("TemperatureUnit", unit.getMeasurementType());
        // CELSIUS is the base unit — convertToBaseUnit is identity
        assertEquals(100.0, unit.convertToBaseUnit(100.0),  EPSILON);
        assertEquals(100.0, unit.convertFromBaseUnit(100.0), EPSILON);
    }

    // =========================================================================
    // SupportsArithmetic — arithmetic vs non-arithmetic units
    // =========================================================================

    @Test
    void testSupportsArithmetic_LengthUnit_IsSupported() {
        assertTrue(LengthUnit.FEET   instanceof SupportsArithmetic);
        assertTrue(LengthUnit.INCHES instanceof SupportsArithmetic);
    }

    @Test
    void testSupportsArithmetic_WeightUnit_IsSupported() {
        assertTrue(WeightUnit.KILOGRAM instanceof SupportsArithmetic);
        assertTrue(WeightUnit.GRAM     instanceof SupportsArithmetic);
    }

    @Test
    void testSupportsArithmetic_VolumeUnit_IsSupported() {
        assertTrue(VolumeUnit.LITRE      instanceof SupportsArithmetic);
        assertTrue(VolumeUnit.MILLILITRE instanceof SupportsArithmetic);
    }

    @Test
    void testSupportsArithmetic_TemperatureUnit_IsNotSupported() {
    	assertFalse(TemperatureUnit.CELSIUS.supportsArithmetic());
    	assertFalse(TemperatureUnit.FAHRENHEIT.supportsArithmetic());
    	assertFalse(TemperatureUnit.KELVIN.supportsArithmetic());
    }
}
