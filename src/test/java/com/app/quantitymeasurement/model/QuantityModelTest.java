package com.app.quantitymeasurement.model;

import com.app.quantitymeasurement.model.QuantityModel;
import com.app.quantitymeasurement.unit.LengthUnit;
import com.app.quantitymeasurement.unit.WeightUnit;
import com.app.quantitymeasurement.unit.VolumeUnit;
import com.app.quantitymeasurement.unit.TemperatureUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class QuantityModelTest {

    private static final double EPSILON = 1e-6;

    // =========================================================================
    // CONSTRUCTOR — happy path across all categories
    // =========================================================================

    @Test
    void testConstructor_Length_StoresValueAndUnit() {
        QuantityModel<LengthUnit> model = new QuantityModel<>(5.0, LengthUnit.FEET);
        assertEquals(5.0,           model.getValue(), EPSILON);
        assertEquals(LengthUnit.FEET, model.getUnit());
    }

    @Test
    void testConstructor_Weight_StoresValueAndUnit() {
        QuantityModel<WeightUnit> model = new QuantityModel<>(10.0, WeightUnit.KILOGRAM);
        assertEquals(10.0,               model.getValue(), EPSILON);
        assertEquals(WeightUnit.KILOGRAM, model.getUnit());
    }

    @Test
    void testConstructor_Volume_StoresValueAndUnit() {
        QuantityModel<VolumeUnit> model = new QuantityModel<>(3.5, VolumeUnit.LITRE);
        assertEquals(3.5,              model.getValue(), EPSILON);
        assertEquals(VolumeUnit.LITRE, model.getUnit());
    }

    @Test
    void testConstructor_Temperature_StoresValueAndUnit() {
        QuantityModel<TemperatureUnit> model = new QuantityModel<>(25.0, TemperatureUnit.CELSIUS);
        assertEquals(25.0,                    model.getValue(), EPSILON);
        assertEquals(TemperatureUnit.CELSIUS, model.getUnit());
    }

    @Test
    void testConstructor_ZeroValue_Allowed() {
        QuantityModel<LengthUnit> model = new QuantityModel<>(0.0, LengthUnit.INCHES);
        assertEquals(0.0, model.getValue(), EPSILON);
    }

    @Test
    void testConstructor_NegativeValue_Allowed() {
        QuantityModel<WeightUnit> model = new QuantityModel<>(-5.0, WeightUnit.GRAM);
        assertEquals(-5.0, model.getValue(), EPSILON);
    }

    // =========================================================================
    // CONSTRUCTOR — validation guards
    // =========================================================================

    @Test
    void testConstructor_NullUnit_ThrowsIllegalArgument() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new QuantityModel<>(10.0, null)
        );
        assertNotNull(ex.getMessage());
    }

    @Test
    void testConstructor_NaN_ThrowsIllegalArgument() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new QuantityModel<>(Double.NaN, LengthUnit.FEET)
        );
    }

    @Test
    void testConstructor_PositiveInfinity_ThrowsIllegalArgument() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new QuantityModel<>(Double.POSITIVE_INFINITY, LengthUnit.FEET)
        );
    }

    @Test
    void testConstructor_NegativeInfinity_ThrowsIllegalArgument() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new QuantityModel<>(Double.NEGATIVE_INFINITY, WeightUnit.KILOGRAM)
        );
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    @Test
    void testGetValue_ReturnsStoredValue() {
        assertEquals(7.77, new QuantityModel<>(7.77, LengthUnit.YARDS).getValue(), EPSILON);
    }

    @Test
    void testGetUnit_ReturnsStoredUnit() {
        assertSame(VolumeUnit.GALLON, new QuantityModel<>(1.0, VolumeUnit.GALLON).getUnit());
    }

    @Test
    void testGetUnit_UnitNameAccessibleThroughGetter() {
        QuantityModel<LengthUnit> model = new QuantityModel<>(3.0, LengthUnit.YARDS);
        assertEquals("YARDS", model.getUnit().getUnitName());
    }

    @Test
    void testGetUnit_MeasurementTypeAccessibleThroughGetter() {
        QuantityModel<WeightUnit> model = new QuantityModel<>(2.0, WeightUnit.GRAM);
        assertEquals("WeightUnit", model.getUnit().getMeasurementType());
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    void testToString_ContainsValueAndUnitName() {
        QuantityModel<LengthUnit> model = new QuantityModel<>(5.0, LengthUnit.FEET);
        String s = model.toString();
        assertTrue(s.contains("5"),    "toString should contain the value");
        assertTrue(s.contains("FEET"), "toString should contain the unit name");
    }

    @Test
    void testToString_DecimalValue_ContainsDecimal() {
        QuantityModel<VolumeUnit> model = new QuantityModel<>(3.5, VolumeUnit.LITRE);
        assertTrue(model.toString().contains("3.5"));
        assertTrue(model.toString().contains("LITRE"));
    }

    @Test
    void testToString_NegativeValue_ContainsSign() {
        QuantityModel<WeightUnit> model = new QuantityModel<>(-1.0, WeightUnit.KILOGRAM);
        assertTrue(model.toString().contains("-"));
        assertTrue(model.toString().contains("KILOGRAM"));
    }
}
