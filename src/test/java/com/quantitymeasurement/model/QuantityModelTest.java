package com.quantitymeasurement.model;

import com.quantitymeasurement.entity.QuantityModel;
import com.quantitymeasurement.units.LengthUnit;
import com.quantitymeasurement.units.WeightUnit;
import com.quantitymeasurement.units.VolumeUnit;
import com.quantitymeasurement.units.TemperatureUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QuantityModelTest
 *
 * Tests the QuantityModel POJO used by the service layer:
 * - Constructor stores value and unit correctly
 * - Constructor rejects null unit and non-finite values
 * - getValue() and getUnit() return the stored fields
 * - toString() produces a readable representation
 * - Works correctly across all measurement categories
 */
public class QuantityModelTest {

    private static final double EPSILON = 1e-6;

    // =========================================================================
    // CONSTRUCTOR — happy path across all categories
    // =========================================================================

    @Test
    public void testConstructor_Length_StoresValueAndUnit() {
        QuantityModel<LengthUnit> model = new QuantityModel<>(5.0, LengthUnit.FEET);
        assertEquals(5.0,           model.getValue(), EPSILON);
        assertEquals(LengthUnit.FEET, model.getUnit());
    }

    @Test
    public void testConstructor_Weight_StoresValueAndUnit() {
        QuantityModel<WeightUnit> model = new QuantityModel<>(10.0, WeightUnit.KILOGRAM);
        assertEquals(10.0,               model.getValue(), EPSILON);
        assertEquals(WeightUnit.KILOGRAM, model.getUnit());
    }

    @Test
    public void testConstructor_Volume_StoresValueAndUnit() {
        QuantityModel<VolumeUnit> model = new QuantityModel<>(3.5, VolumeUnit.LITRE);
        assertEquals(3.5,              model.getValue(), EPSILON);
        assertEquals(VolumeUnit.LITRE, model.getUnit());
    }

    @Test
    public void testConstructor_Temperature_StoresValueAndUnit() {
        QuantityModel<TemperatureUnit> model = new QuantityModel<>(25.0, TemperatureUnit.CELSIUS);
        assertEquals(25.0,                    model.getValue(), EPSILON);
        assertEquals(TemperatureUnit.CELSIUS, model.getUnit());
    }

    @Test
    public void testConstructor_ZeroValue_Allowed() {
        QuantityModel<LengthUnit> model = new QuantityModel<>(0.0, LengthUnit.INCHES);
        assertEquals(0.0, model.getValue(), EPSILON);
    }

    @Test
    public void testConstructor_NegativeValue_Allowed() {
        QuantityModel<WeightUnit> model = new QuantityModel<>(-5.0, WeightUnit.GRAM);
        assertEquals(-5.0, model.getValue(), EPSILON);
    }

    // =========================================================================
    // CONSTRUCTOR — validation guards
    // =========================================================================

    @Test
    public void testConstructor_NullUnit_ThrowsIllegalArgument() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new QuantityModel<>(10.0, null)
        );
        assertNotNull(ex.getMessage());
    }

    @Test
    public void testConstructor_NaN_ThrowsIllegalArgument() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new QuantityModel<>(Double.NaN, LengthUnit.FEET)
        );
    }

    @Test
    public void testConstructor_PositiveInfinity_ThrowsIllegalArgument() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new QuantityModel<>(Double.POSITIVE_INFINITY, LengthUnit.FEET)
        );
    }

    @Test
    public void testConstructor_NegativeInfinity_ThrowsIllegalArgument() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new QuantityModel<>(Double.NEGATIVE_INFINITY, WeightUnit.KILOGRAM)
        );
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    @Test
    public void testGetValue_ReturnsStoredValue() {
        assertEquals(7.77, new QuantityModel<>(7.77, LengthUnit.YARDS).getValue(), EPSILON);
    }

    @Test
    public void testGetUnit_ReturnsStoredUnit() {
        assertSame(VolumeUnit.GALLON, new QuantityModel<>(1.0, VolumeUnit.GALLON).getUnit());
    }

    @Test
    public void testGetUnit_UnitNameAccessibleThroughGetter() {
        QuantityModel<LengthUnit> model = new QuantityModel<>(3.0, LengthUnit.YARDS);
        assertEquals("YARDS", model.getUnit().getUnitName());
    }

    @Test
    public void testGetUnit_MeasurementTypeAccessibleThroughGetter() {
        QuantityModel<WeightUnit> model = new QuantityModel<>(2.0, WeightUnit.GRAM);
        assertEquals("WeightUnit", model.getUnit().getMeasurementType());
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    public void testToString_ContainsValueAndUnitName() {
        QuantityModel<LengthUnit> model = new QuantityModel<>(5.0, LengthUnit.FEET);
        String s = model.toString();
        assertTrue(s.contains("5"),    "toString should contain the value");
        assertTrue(s.contains("FEET"), "toString should contain the unit name");
    }

    @Test
    public void testToString_DecimalValue_ContainsDecimal() {
        QuantityModel<VolumeUnit> model = new QuantityModel<>(3.5, VolumeUnit.LITRE);
        assertTrue(model.toString().contains("3.5"));
        assertTrue(model.toString().contains("LITRE"));
    }

    @Test
    public void testToString_NegativeValue_ContainsSign() {
        QuantityModel<WeightUnit> model = new QuantityModel<>(-1.0, WeightUnit.KILOGRAM);
        assertTrue(model.toString().contains("-"));
        assertTrue(model.toString().contains("KILOGRAM"));
    }
}
