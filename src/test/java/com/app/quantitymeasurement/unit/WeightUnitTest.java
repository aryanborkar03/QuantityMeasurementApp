package com.app.quantitymeasurement.unit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class WeightUnitTest {

    private static final double EPSILON = 1e-6;

    // =========================================================================
    // CONVERSION FACTOR  (factor = convertToBaseUnit(1.0) for linear units)
    // =========================================================================

    @Test
    void testConversionFactor_Kilogram() {
        assertEquals(1.0, WeightUnit.KILOGRAM.convertToBaseUnit(1.0), EPSILON);
    }

    @Test
    void testConversionFactor_Gram() {
        assertEquals(0.001, WeightUnit.GRAM.convertToBaseUnit(1.0), EPSILON);
    }

    @Test
    void testConversionFactor_Pound() {
        assertEquals(0.453592, WeightUnit.POUND.convertToBaseUnit(1.0), EPSILON);
    }

    // =========================================================================
    // convertToBaseUnit  (result in KILOGRAM)
    // =========================================================================

    @Test
    void testConvertToBaseUnit_Kilogram() {
        assertEquals(1.0, WeightUnit.KILOGRAM.convertToBaseUnit(1.0), EPSILON);
    }

    @Test
    void testConvertToBaseUnit_Gram() {
        assertEquals(1.0, WeightUnit.GRAM.convertToBaseUnit(1000.0), EPSILON);
    }

    @Test
    void testConvertToBaseUnit_Pound() {
        assertEquals(0.453592, WeightUnit.POUND.convertToBaseUnit(1.0), EPSILON);
    }

    // =========================================================================
    // convertFromBaseUnit  (from KILOGRAM to target unit)
    // =========================================================================

    @Test
    void testConvertFromBaseUnit_ToKilogram() {
        assertEquals(1.0, WeightUnit.KILOGRAM.convertFromBaseUnit(1.0), EPSILON);
    }

    @Test
    void testConvertFromBaseUnit_ToGram() {
        assertEquals(1000.0, WeightUnit.GRAM.convertFromBaseUnit(1.0), EPSILON);
    }

    @Test
    void testConvertFromBaseUnit_ToPound() {
        assertEquals(2.204624, WeightUnit.POUND.convertFromBaseUnit(1.0), EPSILON);
    }

    // =========================================================================
    // UNIT IDENTITY
    // =========================================================================

    @Test
    void testGetUnitName() {
        assertEquals("KILOGRAM", WeightUnit.KILOGRAM.getUnitName());
        assertEquals("GRAM",     WeightUnit.GRAM.getUnitName());
        assertEquals("POUND",    WeightUnit.POUND.getUnitName());
    }

    @Test
    void testGetMeasurementType() {
        assertEquals("WeightUnit", WeightUnit.KILOGRAM.getMeasurementType());
        assertEquals("WeightUnit", WeightUnit.GRAM.getMeasurementType());
        assertEquals("WeightUnit", WeightUnit.POUND.getMeasurementType());
    }

    @Test
    void testEnumConstants_AllPresent() {
        assertDoesNotThrow(() -> WeightUnit.valueOf("KILOGRAM"));
        assertDoesNotThrow(() -> WeightUnit.valueOf("GRAM"));
        assertDoesNotThrow(() -> WeightUnit.valueOf("POUND"));
    }

    @Test
    void testEnumConstants_AreEnumInstances() {
        assertTrue(WeightUnit.KILOGRAM instanceof Enum);
        assertTrue(WeightUnit.GRAM     instanceof Enum);
        assertTrue(WeightUnit.POUND    instanceof Enum);
    }

    // =========================================================================
    // ARITHMETIC SUPPORT — WeightUnit implements SupportsArithmetic
    // =========================================================================

    @Test
    void testSupportsArithmetic_AllWeightUnits() {
        assertTrue(WeightUnit.KILOGRAM instanceof SupportsArithmetic);
        assertTrue(WeightUnit.GRAM     instanceof SupportsArithmetic);
        assertTrue(WeightUnit.POUND    instanceof SupportsArithmetic);
    }
}
