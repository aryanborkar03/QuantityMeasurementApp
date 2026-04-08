package com.app.quantitymeasurement.unit;

import com.app.quantitymeasurement.model.Quantity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class TemperatureUnitTest {

    private static final double EPSILON = 1e-6;

    // =========================================================================
    // ENUM IDENTITY
    // =========================================================================

    @Test
    void testEnumConstants_AllPresent() {
        assertNotNull(TemperatureUnit.CELSIUS);
        assertNotNull(TemperatureUnit.FAHRENHEIT);
        assertNotNull(TemperatureUnit.KELVIN);
    }

    @Test
    void testGetUnitName() {
        assertEquals("CELSIUS",    TemperatureUnit.CELSIUS.getUnitName());
        assertEquals("FAHRENHEIT", TemperatureUnit.FAHRENHEIT.getUnitName());
        assertEquals("KELVIN",     TemperatureUnit.KELVIN.getUnitName());
    }

    @Test
    void testGetMeasurementType() {
        assertEquals("TemperatureUnit", TemperatureUnit.CELSIUS.getMeasurementType());
        assertEquals("TemperatureUnit", TemperatureUnit.FAHRENHEIT.getMeasurementType());
        assertEquals("TemperatureUnit", TemperatureUnit.KELVIN.getMeasurementType());
    }

    // =========================================================================
    // CONVERSION FACTOR — CELSIUS is the base unit (identity function)
    // =========================================================================

    @Test
    void testConversionFactor_Celsius_IsIdentityFunction() {
        // CELSIUS is the base unit: convertToBaseUnit is an identity function
        assertEquals(0.0,   TemperatureUnit.CELSIUS.convertToBaseUnit(0.0),   EPSILON);
        assertEquals(100.0, TemperatureUnit.CELSIUS.convertToBaseUnit(100.0), EPSILON);
        assertEquals(-40.0, TemperatureUnit.CELSIUS.convertToBaseUnit(-40.0), EPSILON);
    }

    // =========================================================================
    // convertToBaseUnit  (result in CELSIUS)
    // =========================================================================

    @Test
    void testConvertToBaseUnit_Celsius_Identity() {
        assertEquals(25.0, TemperatureUnit.CELSIUS.convertToBaseUnit(25.0), EPSILON);
        assertEquals(0.0,  TemperatureUnit.CELSIUS.convertToBaseUnit(0.0),  EPSILON);
    }

    @Test
    void testConvertToBaseUnit_Fahrenheit_32F_Is_0C() {
        assertEquals(0.0, TemperatureUnit.FAHRENHEIT.convertToBaseUnit(32.0), EPSILON);
    }

    @Test
    void testConvertToBaseUnit_Fahrenheit_212F_Is_100C() {
        assertEquals(100.0, TemperatureUnit.FAHRENHEIT.convertToBaseUnit(212.0), EPSILON);
    }

    @Test
    void testConvertToBaseUnit_Fahrenheit_Minus40_IsEqual() {
        assertEquals(-40.0, TemperatureUnit.FAHRENHEIT.convertToBaseUnit(-40.0), EPSILON);
    }

    @Test
    void testConvertToBaseUnit_Kelvin_0K_IsMinus273_15C() {
        assertEquals(-273.15, TemperatureUnit.KELVIN.convertToBaseUnit(0.0), EPSILON);
    }

    // =========================================================================
    // convertFromBaseUnit  (from CELSIUS to target unit)
    // =========================================================================

    @Test
    void testConvertFromBaseUnit_Celsius_Identity() {
        assertEquals(25.0, TemperatureUnit.CELSIUS.convertFromBaseUnit(25.0), EPSILON);
    }

    @Test
    void testConvertFromBaseUnit_ToFahrenheit_0C_Is_32F() {
        assertEquals(32.0, TemperatureUnit.FAHRENHEIT.convertFromBaseUnit(0.0), EPSILON);
    }

    @Test
    void testConvertFromBaseUnit_ToFahrenheit_100C_Is_212F() {
        assertEquals(212.0, TemperatureUnit.FAHRENHEIT.convertFromBaseUnit(100.0), EPSILON);
    }

    @Test
    void testConvertFromBaseUnit_ToKelvin_0C_Is_273_15K() {
        assertEquals(273.15, TemperatureUnit.KELVIN.convertFromBaseUnit(0.0), EPSILON);
    }

    // =========================================================================
    // ARITHMETIC SUPPORT — TemperatureUnit does NOT implement SupportsArithmetic
    // =========================================================================

    @Test
    void testNotSupportsArithmetic_Celsius() {
        assertFalse(TemperatureUnit.CELSIUS.supportsArithmetic());
    }

    @Test
    void testNotSupportsArithmetic_Fahrenheit() {
        assertFalse(TemperatureUnit.FAHRENHEIT.supportsArithmetic());
    }

    @Test
    void testNotSupportsArithmetic_Kelvin() {
        assertFalse(TemperatureUnit.KELVIN.supportsArithmetic());
    }

    /**
     * Because TemperatureUnit does not implement SupportsArithmetic,
     * any arithmetic attempted through Quantity must throw UnsupportedOperationException.
     * These tests confirm that the contract is enforced at the Quantity level.
     */
    @Test
    void testArithmeticRejected_Add_Celsius() {
        UnsupportedOperationException ex = assertThrows(
            UnsupportedOperationException.class,
            () -> new Quantity<>(100.0, TemperatureUnit.CELSIUS)
                      .add(new Quantity<>(50.0, TemperatureUnit.CELSIUS))
        );
        assertTrue(ex.getMessage().toLowerCase().contains("not supported"));
    }

    @Test
    void testArithmeticRejected_Divide_Fahrenheit() {
        assertThrows(
            UnsupportedOperationException.class,
            () -> new Quantity<>(212.0, TemperatureUnit.FAHRENHEIT)
                      .divide(new Quantity<>(32.0, TemperatureUnit.FAHRENHEIT))
        );
    }

    @Test
    void testArithmeticRejected_Subtract_Kelvin() {
        assertThrows(
            UnsupportedOperationException.class,
            () -> new Quantity<>(373.15, TemperatureUnit.KELVIN)
                      .subtract(new Quantity<>(273.15, TemperatureUnit.KELVIN))
        );
    }

    // =========================================================================
    // IMeasurable INTERFACE IMPLEMENTATION
    // =========================================================================

    @Test
    void testImplementsIMeasurable() {
        assertTrue(
            com.app.quantitymeasurement.unit.IMeasurable.class
                .isAssignableFrom(TemperatureUnit.class)
        );
    }
}
