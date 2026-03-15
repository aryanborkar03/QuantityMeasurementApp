package com.quantitymeasurement.model;
import com.quantitymeasurement.units.LengthUnit;
import com.quantitymeasurement.units.WeightUnit;
import com.quantitymeasurement.units.VolumeUnit;
import com.quantitymeasurement.units.TemperatureUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QuantityConversionTest
 *
 * Tests the Quantity.convertTo() method across all unit categories.
 * Covers: directional conversions, round-trips, same-unit, zero/negative
 * values, null guard, and NaN/Infinity construction guard.
 */
public class QuantityConversionTest {

    private static final double EPSILON = 1e-6;

    // -------------------------------------------------------------------------
    // Length Conversions
    // -------------------------------------------------------------------------

    @Test
    public void testConversion_FeetToInches() {
        Quantity<LengthUnit> result = new Quantity<>(1.0, LengthUnit.FEET).convertTo(LengthUnit.INCHES);
        assertEquals(12.0, result.getValue(), EPSILON);
        assertEquals(LengthUnit.INCHES, result.getUnit());
    }

    @Test
    public void testConversion_InchesToFeet() {
        Quantity<LengthUnit> result = new Quantity<>(24.0, LengthUnit.INCHES).convertTo(LengthUnit.FEET);
        assertEquals(2.0, result.getValue(), EPSILON);
        assertEquals(LengthUnit.FEET, result.getUnit());
    }

    @Test
    public void testConversion_YardsToInches() {
        Quantity<LengthUnit> result = new Quantity<>(1.0, LengthUnit.YARDS).convertTo(LengthUnit.INCHES);
        assertEquals(36.0, result.getValue(), EPSILON);
    }

    @Test
    public void testConversion_InchesToYards() {
        Quantity<LengthUnit> result = new Quantity<>(72.0, LengthUnit.INCHES).convertTo(LengthUnit.YARDS);
        assertEquals(2.0, result.getValue(), EPSILON);
    }

    @Test
    public void testConversion_FeetToYards() {
        Quantity<LengthUnit> result = new Quantity<>(6.0, LengthUnit.FEET).convertTo(LengthUnit.YARDS);
        assertEquals(2.0, result.getValue(), EPSILON);
    }

    @Test
    public void testConversion_CentimetersToInches() {
        Quantity<LengthUnit> result = new Quantity<>(2.54, LengthUnit.CENTIMETERS).convertTo(LengthUnit.INCHES);
        assertEquals(1.0, result.getValue(), EPSILON);
    }

    @Test
    public void testConversion_Length_SameUnit_NoChange() {
        Quantity<LengthUnit> result = new Quantity<>(5.0, LengthUnit.FEET).convertTo(LengthUnit.FEET);
        assertEquals(new Quantity<>(5.0, LengthUnit.FEET), result);
    }

    @Test
    public void testConversion_Length_ZeroValue() {
        Quantity<LengthUnit> result = new Quantity<>(0.0, LengthUnit.FEET).convertTo(LengthUnit.INCHES);
        assertEquals(new Quantity<>(0.0, LengthUnit.INCHES), result);
    }

    @Test
    public void testConversion_Length_NegativeValue() {
        Quantity<LengthUnit> result = new Quantity<>(-1.0, LengthUnit.FEET).convertTo(LengthUnit.INCHES);
        assertEquals(new Quantity<>(-12.0, LengthUnit.INCHES), result);
    }

    @Test
    public void testConversion_Length_RoundTrip_FeetAndInches() {
        Quantity<LengthUnit> original  = new Quantity<>(3.0, LengthUnit.FEET);
        Quantity<LengthUnit> converted = original.convertTo(LengthUnit.INCHES).convertTo(LengthUnit.FEET);
        assertEquals(original, converted);
    }

    @Test
    public void testConversion_Length_RoundTrip_FeetAndCentimeters() {
        Quantity<LengthUnit> original  = new Quantity<>(5.0, LengthUnit.FEET);
        Quantity<LengthUnit> converted = original.convertTo(LengthUnit.CENTIMETERS).convertTo(LengthUnit.FEET);
        assertEquals(original, converted);
    }

    @Test
    public void testConversion_Length_NullTargetUnit_Throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Quantity<>(1.0, LengthUnit.FEET).convertTo(null));
    }

    @Test
    public void testConversion_Length_NaN_Throws() {
        assertThrows(IllegalArgumentException.class, () -> new Quantity<>(Double.NaN, LengthUnit.FEET));
    }

    @Test
    public void testConversion_Length_Infinity_Throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Quantity<>(Double.POSITIVE_INFINITY, LengthUnit.INCHES));
    }

    // -------------------------------------------------------------------------
    // Weight Conversions
    // -------------------------------------------------------------------------

    @Test
    public void testConversion_PoundToKilogram() {
        Quantity<WeightUnit> result = new Quantity<>(2.204624, WeightUnit.POUND).convertTo(WeightUnit.KILOGRAM);
        assertEquals(1.0, result.getValue(), EPSILON);
        assertEquals(WeightUnit.KILOGRAM, result.getUnit());
    }

    @Test
    public void testConversion_KilogramToPound() {
        Quantity<WeightUnit> result = new Quantity<>(1.0, WeightUnit.KILOGRAM).convertTo(WeightUnit.POUND);
        assertEquals(2.204624, result.getValue(), EPSILON);
        assertEquals(WeightUnit.POUND, result.getUnit());
    }

    @Test
    public void testConversion_KilogramToGram() {
        Quantity<WeightUnit> result = new Quantity<>(1.0, WeightUnit.KILOGRAM).convertTo(WeightUnit.GRAM);
        assertEquals(1000.0, result.getValue(), EPSILON);
    }

    @Test
    public void testConversion_Weight_SameUnit() {
        Quantity<WeightUnit> result = new Quantity<>(5.0, WeightUnit.KILOGRAM).convertTo(WeightUnit.KILOGRAM);
        assertEquals(new Quantity<>(5.0, WeightUnit.KILOGRAM), result);
    }

    // -------------------------------------------------------------------------
    // Volume Conversions
    // -------------------------------------------------------------------------

    @Test
    public void testConversion_LitreToMillilitre() {
        Quantity<VolumeUnit> result = new Quantity<>(1.0, VolumeUnit.LITRE).convertTo(VolumeUnit.MILLILITRE);
        assertEquals(1000.0, result.getValue(), EPSILON);
        assertEquals(VolumeUnit.MILLILITRE, result.getUnit());
    }

    @Test
    public void testConversion_MillilitreToLitre() {
        Quantity<VolumeUnit> result = new Quantity<>(1000.0, VolumeUnit.MILLILITRE).convertTo(VolumeUnit.LITRE);
        assertEquals(1.0, result.getValue(), EPSILON);
        assertEquals(VolumeUnit.LITRE, result.getUnit());
    }

    @Test
    public void testConversion_GallonToLitre() {
        Quantity<VolumeUnit> result = new Quantity<>(1.0, VolumeUnit.GALLON).convertTo(VolumeUnit.LITRE);
        assertEquals(3.785412, result.getValue(), EPSILON);
    }

    @Test
    public void testConversion_LitreToGallon() {
        Quantity<VolumeUnit> result = new Quantity<>(3.785412, VolumeUnit.LITRE).convertTo(VolumeUnit.GALLON);
        assertEquals(1.0, result.getValue(), EPSILON);
    }

    @Test
    public void testConversion_Volume_SameUnit() {
        Quantity<VolumeUnit> result = new Quantity<>(5.0, VolumeUnit.LITRE).convertTo(VolumeUnit.LITRE);
        assertEquals(new Quantity<>(5.0, VolumeUnit.LITRE), result);
    }

    @Test
    public void testConversion_Volume_ZeroAndNegative() {
        assertEquals(
            new Quantity<>(0.0, VolumeUnit.MILLILITRE),
            new Quantity<>(0.0, VolumeUnit.LITRE).convertTo(VolumeUnit.MILLILITRE)
        );
        assertEquals(
            new Quantity<>(-1000.0, VolumeUnit.MILLILITRE),
            new Quantity<>(-1.0, VolumeUnit.LITRE).convertTo(VolumeUnit.MILLILITRE)
        );
    }

    @Test
    public void testConversion_Volume_RoundTrip() {
        Quantity<VolumeUnit> original  = new Quantity<>(1.5, VolumeUnit.LITRE);
        Quantity<VolumeUnit> roundTrip = original.convertTo(VolumeUnit.MILLILITRE).convertTo(VolumeUnit.LITRE);
        assertEquals(original, roundTrip);
    }

    // -------------------------------------------------------------------------
    // Temperature Conversions
    // -------------------------------------------------------------------------

    @Test
    public void testTemperatureConversion_CelsiusToFahrenheit_0C_Is_32F() {
        Quantity<TemperatureUnit> result =
            new Quantity<>(0.0, TemperatureUnit.CELSIUS).convertTo(TemperatureUnit.FAHRENHEIT);
        assertEquals(32.0, result.getValue(), EPSILON);
    }

    @Test
    public void testTemperatureConversion_CelsiusToFahrenheit_100C_Is_212F() {
        Quantity<TemperatureUnit> result =
            new Quantity<>(100.0, TemperatureUnit.CELSIUS).convertTo(TemperatureUnit.FAHRENHEIT);
        assertEquals(212.0, result.getValue(), EPSILON);
    }

    @Test
    public void testTemperatureConversion_FahrenheitToCelsius_Various() {
        assertEquals(50.0,  new Quantity<>(122.0, TemperatureUnit.FAHRENHEIT).convertTo(TemperatureUnit.CELSIUS).getValue(), EPSILON);
        assertEquals(-20.0, new Quantity<>(-4.0,  TemperatureUnit.FAHRENHEIT).convertTo(TemperatureUnit.CELSIUS).getValue(), EPSILON);
        assertEquals(-40.0, new Quantity<>(-40.0, TemperatureUnit.FAHRENHEIT).convertTo(TemperatureUnit.CELSIUS).getValue(), EPSILON);
    }

    @Test
    public void testTemperatureConversion_CelsiusToFahrenheit_Various() {
        assertEquals(122.0, new Quantity<>(50.0,  TemperatureUnit.CELSIUS).convertTo(TemperatureUnit.FAHRENHEIT).getValue(), EPSILON);
        assertEquals(-4.0,  new Quantity<>(-20.0, TemperatureUnit.CELSIUS).convertTo(TemperatureUnit.FAHRENHEIT).getValue(), EPSILON);
        assertEquals(-40.0, new Quantity<>(-40.0, TemperatureUnit.CELSIUS).convertTo(TemperatureUnit.FAHRENHEIT).getValue(), EPSILON);
    }

    @Test
    public void testTemperatureConversion_CelsiusToKelvin() {
        Quantity<TemperatureUnit> result =
            new Quantity<>(100.0, TemperatureUnit.CELSIUS).convertTo(TemperatureUnit.KELVIN);
        assertEquals(373.15,
            Math.round(result.getValue() * 1_000_000.0) / 1_000_000.0, EPSILON);
    }

    @Test
    public void testTemperatureConversion_SameUnit() {
        Quantity<TemperatureUnit> result =
            new Quantity<>(25.0, TemperatureUnit.CELSIUS).convertTo(TemperatureUnit.CELSIUS);
        assertEquals(25.0, result.getValue(), EPSILON);
    }

    @Test
    public void testTemperatureConversion_ZeroValue() {
        assertEquals(32.0,
            new Quantity<>(0.0, TemperatureUnit.CELSIUS)
                .convertTo(TemperatureUnit.FAHRENHEIT).getValue(), EPSILON);
    }

    @Test
    public void testTemperatureConversion_NegativeValues() {
        double fahrenheit = new Quantity<>(-273.15, TemperatureUnit.CELSIUS)
            .convertTo(TemperatureUnit.FAHRENHEIT).getValue();
        assertEquals(-459.67, Math.round(fahrenheit * 100.0) / 100.0, EPSILON);
    }

    @Test
    public void testTemperatureConversion_LargeValue() {
        double expected = (1000.0 * 9.0 / 5.0) + 32.0;
        assertEquals(expected,
            new Quantity<>(1000.0, TemperatureUnit.CELSIUS)
                .convertTo(TemperatureUnit.FAHRENHEIT).getValue(), EPSILON);
    }

    @Test
    public void testTemperatureConversion_RoundTrip_PreservesValue() {
        double original = 37.5;
        Quantity<TemperatureUnit> q = new Quantity<>(original, TemperatureUnit.CELSIUS);
        Quantity<TemperatureUnit> roundTrip =
            q.convertTo(TemperatureUnit.FAHRENHEIT).convertTo(TemperatureUnit.CELSIUS);
        assertEquals(original, roundTrip.getValue(), 1e-4);
    }

    @Test
    public void testTemperatureConversion_VerySmallDifference_RoundTrip() {
        Quantity<TemperatureUnit> a = new Quantity<>(0.000001, TemperatureUnit.CELSIUS);
        Quantity<TemperatureUnit> b =
            a.convertTo(TemperatureUnit.FAHRENHEIT).convertTo(TemperatureUnit.CELSIUS);
        assertEquals(a.getValue(), b.getValue(), EPSILON);
    }
}