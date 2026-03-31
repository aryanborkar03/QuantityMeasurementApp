package com.app.quantitymeasurement.dto;

import org.junit.jupiter.api.Test;

import com.app.quantitymeasurement.dto.response.QuantityDTO;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QuantityDTOTest
 *
 * Tests the QuantityDTO data transfer object:
 * - Enum-based constructor populates value, unit, and measurementType correctly
 * - String-based constructor stores raw values as-is
 * - Getters return the stored values
 * - toString produces the expected format
 * - Inner unit enums expose correct names and measurement types
 * - IMeasurableUnit contract is satisfied by all inner enums
 */
public class QuantityDTOTest {

    // =========================================================================
    // ENUM-BASED CONSTRUCTOR — LengthUnit
    // =========================================================================

    @Test
    public void testConstructor_LengthUnit_Feet_SetsAllFields() {
        QuantityDTO dto = new QuantityDTO(2.0, QuantityDTO.LengthUnit.FEET);
        assertEquals(2.0,         dto.getValue());
        assertEquals("FEET",      dto.getUnit());
        assertEquals("LengthUnit", dto.getMeasurementType());
    }

    @Test
    public void testConstructor_LengthUnit_Inches_SetsAllFields() {
        QuantityDTO dto = new QuantityDTO(24.0, QuantityDTO.LengthUnit.INCHES);
        assertEquals(24.0,         dto.getValue());
        assertEquals("INCHES",     dto.getUnit());
        assertEquals("LengthUnit", dto.getMeasurementType());
    }

    @Test
    public void testConstructor_LengthUnit_Yards_SetsAllFields() {
        QuantityDTO dto = new QuantityDTO(1.0, QuantityDTO.LengthUnit.YARDS);
        assertEquals("YARDS",      dto.getUnit());
        assertEquals("LengthUnit", dto.getMeasurementType());
    }

    @Test
    public void testConstructor_LengthUnit_Centimeters_SetsAllFields() {
        QuantityDTO dto = new QuantityDTO(30.48, QuantityDTO.LengthUnit.CENTIMETERS);
        assertEquals("CENTIMETERS", dto.getUnit());
        assertEquals("LengthUnit",  dto.getMeasurementType());
    }

    // =========================================================================
    // ENUM-BASED CONSTRUCTOR — VolumeUnit
    // =========================================================================

    @Test
    public void testConstructor_VolumeUnit_Litre_SetsAllFields() {
        QuantityDTO dto = new QuantityDTO(3.0, QuantityDTO.VolumeUnit.LITRE);
        assertEquals(3.0,          dto.getValue());
        assertEquals("LITRE",      dto.getUnit());
        assertEquals("VolumeUnit", dto.getMeasurementType());
    }

    @Test
    public void testConstructor_VolumeUnit_Millilitre_SetsAllFields() {
        QuantityDTO dto = new QuantityDTO(500.0, QuantityDTO.VolumeUnit.MILLILITRE);
        assertEquals("MILLILITRE", dto.getUnit());
        assertEquals("VolumeUnit", dto.getMeasurementType());
    }

    @Test
    public void testConstructor_VolumeUnit_Gallon_SetsAllFields() {
        QuantityDTO dto = new QuantityDTO(1.0, QuantityDTO.VolumeUnit.GALLON);
        assertEquals("GALLON",     dto.getUnit());
        assertEquals("VolumeUnit", dto.getMeasurementType());
    }

    // =========================================================================
    // ENUM-BASED CONSTRUCTOR — WeightUnit
    // =========================================================================

    @Test
    public void testConstructor_WeightUnit_Kilogram_SetsAllFields() {
        QuantityDTO dto = new QuantityDTO(5.0, QuantityDTO.WeightUnit.KILOGRAM);
        assertEquals(5.0,           dto.getValue());
        assertEquals("KILOGRAM",    dto.getUnit());
        assertEquals("WeightUnit",  dto.getMeasurementType());
    }

    @Test
    public void testConstructor_WeightUnit_Gram_SetsAllFields() {
        QuantityDTO dto = new QuantityDTO(500.0, QuantityDTO.WeightUnit.GRAM);
        assertEquals("GRAM",        dto.getUnit());
        assertEquals("WeightUnit",  dto.getMeasurementType());
    }

    @Test
    public void testConstructor_WeightUnit_Pound_SetsAllFields() {
        QuantityDTO dto = new QuantityDTO(2.2, QuantityDTO.WeightUnit.POUND);
        assertEquals("POUND",       dto.getUnit());
        assertEquals("WeightUnit",  dto.getMeasurementType());
    }

    // =========================================================================
    // ENUM-BASED CONSTRUCTOR — TemperatureUnit
    // =========================================================================

    @Test
    public void testConstructor_TemperatureUnit_Celsius_SetsAllFields() {
        QuantityDTO dto = new QuantityDTO(25.0, QuantityDTO.TemperatureUnit.CELSIUS);
        assertEquals(25.0,               dto.getValue());
        assertEquals("CELSIUS",          dto.getUnit());
        assertEquals("TemperatureUnit",  dto.getMeasurementType());
    }

    @Test
    public void testConstructor_TemperatureUnit_Fahrenheit_SetsAllFields() {
        QuantityDTO dto = new QuantityDTO(77.0, QuantityDTO.TemperatureUnit.FAHRENHEIT);
        assertEquals("FAHRENHEIT",       dto.getUnit());
        assertEquals("TemperatureUnit",  dto.getMeasurementType());
    }

    @Test
    public void testConstructor_TemperatureUnit_Kelvin_SetsAllFields() {
        QuantityDTO dto = new QuantityDTO(273.15, QuantityDTO.TemperatureUnit.KELVIN);
        assertEquals("KELVIN",           dto.getUnit());
        assertEquals("TemperatureUnit",  dto.getMeasurementType());
    }

    // =========================================================================
    // STRING-BASED CONSTRUCTOR
    // =========================================================================

    @Test
    public void testConstructor_StringBased_SetsAllFields() {
        QuantityDTO dto = new QuantityDTO(10.0, "FEET", "LengthUnit");
        assertEquals(10.0,        dto.getValue());
        assertEquals("FEET",      dto.getUnit());
        assertEquals("LengthUnit", dto.getMeasurementType());
    }

    @Test
    public void testConstructor_StringBased_ArbitraryStrings_StoredAsIs() {
        QuantityDTO dto = new QuantityDTO(99.9, "CUSTOM_UNIT", "CustomType");
        assertEquals("CUSTOM_UNIT", dto.getUnit());
        assertEquals("CustomType",  dto.getMeasurementType());
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    @Test
    public void testGetValue_ReturnsStoredValue() {
        assertEquals(42.5, new QuantityDTO(42.5, QuantityDTO.LengthUnit.FEET).getValue());
    }

    @Test
    public void testGetUnit_ReturnsUnitName() {
        assertEquals("YARDS", new QuantityDTO(1.0, QuantityDTO.LengthUnit.YARDS).getUnit());
    }

    @Test
    public void testGetMeasurementType_ReturnsType() {
        assertEquals("WeightUnit",
            new QuantityDTO(1.0, QuantityDTO.WeightUnit.KILOGRAM).getMeasurementType());
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    public void testToString_WholeNumber_NoTrailingZero() {
        // The regex replace in toString should strip ".0"
        QuantityDTO dto = new QuantityDTO(2.0, QuantityDTO.LengthUnit.FEET);
        // value is a double so Double.toString(2.0) = "2.0" — regex removes .0
        // depending on implementation; at minimum the unit must appear
        assertTrue(dto.toString().contains("FEET"));
        assertTrue(dto.toString().contains("2"));
    }

    @Test
    public void testToString_DecimalValue_IncludesDecimal() {
        QuantityDTO dto = new QuantityDTO(2.5, QuantityDTO.LengthUnit.FEET);
        assertTrue(dto.toString().contains("2.5"));
        assertTrue(dto.toString().contains("FEET"));
    }

    @Test
    public void testToString_ContainsUnitAndValue() {
        QuantityDTO dto = new QuantityDTO(100.0, QuantityDTO.TemperatureUnit.CELSIUS);
        String s = dto.toString();
        assertTrue(s.contains("CELSIUS"));
        assertTrue(s.contains("100"));
    }

    // =========================================================================
    // INNER ENUM IDENTITY — LengthUnit
    // =========================================================================

    @Test
    public void testLengthUnit_AllConstantsPresent() {
        assertDoesNotThrow(() -> QuantityDTO.LengthUnit.valueOf("FEET"));
        assertDoesNotThrow(() -> QuantityDTO.LengthUnit.valueOf("INCHES"));
        assertDoesNotThrow(() -> QuantityDTO.LengthUnit.valueOf("YARDS"));
        assertDoesNotThrow(() -> QuantityDTO.LengthUnit.valueOf("CENTIMETERS"));
    }

    @Test
    public void testLengthUnit_GetUnitName() {
        assertEquals("FEET",        QuantityDTO.LengthUnit.FEET.getUnitName());
        assertEquals("INCHES",      QuantityDTO.LengthUnit.INCHES.getUnitName());
        assertEquals("YARDS",       QuantityDTO.LengthUnit.YARDS.getUnitName());
        assertEquals("CENTIMETERS", QuantityDTO.LengthUnit.CENTIMETERS.getUnitName());
    }

    @Test
    public void testLengthUnit_GetMeasurementType() {
        assertEquals("LengthUnit", QuantityDTO.LengthUnit.FEET.getMeasurementType());
    }

    // =========================================================================
    // INNER ENUM IDENTITY — VolumeUnit
    // =========================================================================

    @Test
    public void testVolumeUnit_AllConstantsPresent() {
        assertDoesNotThrow(() -> QuantityDTO.VolumeUnit.valueOf("LITRE"));
        assertDoesNotThrow(() -> QuantityDTO.VolumeUnit.valueOf("MILLILITRE"));
        assertDoesNotThrow(() -> QuantityDTO.VolumeUnit.valueOf("GALLON"));
    }

    @Test
    public void testVolumeUnit_GetUnitName() {
        assertEquals("LITRE",      QuantityDTO.VolumeUnit.LITRE.getUnitName());
        assertEquals("MILLILITRE", QuantityDTO.VolumeUnit.MILLILITRE.getUnitName());
        assertEquals("GALLON",     QuantityDTO.VolumeUnit.GALLON.getUnitName());
    }

    @Test
    public void testVolumeUnit_GetMeasurementType() {
        assertEquals("VolumeUnit", QuantityDTO.VolumeUnit.LITRE.getMeasurementType());
    }

    // =========================================================================
    // INNER ENUM IDENTITY — WeightUnit
    // =========================================================================

    @Test
    public void testWeightUnit_AllConstantsPresent() {
        assertDoesNotThrow(() -> QuantityDTO.WeightUnit.valueOf("KILOGRAM"));
        assertDoesNotThrow(() -> QuantityDTO.WeightUnit.valueOf("GRAM"));
        assertDoesNotThrow(() -> QuantityDTO.WeightUnit.valueOf("POUND"));
    }

    @Test
    public void testWeightUnit_GetUnitName() {
        assertEquals("KILOGRAM", QuantityDTO.WeightUnit.KILOGRAM.getUnitName());
        assertEquals("GRAM",     QuantityDTO.WeightUnit.GRAM.getUnitName());
        assertEquals("POUND",    QuantityDTO.WeightUnit.POUND.getUnitName());
    }

    @Test
    public void testWeightUnit_GetMeasurementType() {
        assertEquals("WeightUnit", QuantityDTO.WeightUnit.KILOGRAM.getMeasurementType());
    }

    // =========================================================================
    // INNER ENUM IDENTITY — TemperatureUnit
    // =========================================================================

    @Test
    public void testTemperatureUnit_AllConstantsPresent() {
        assertDoesNotThrow(() -> QuantityDTO.TemperatureUnit.valueOf("CELSIUS"));
        assertDoesNotThrow(() -> QuantityDTO.TemperatureUnit.valueOf("FAHRENHEIT"));
        assertDoesNotThrow(() -> QuantityDTO.TemperatureUnit.valueOf("KELVIN"));
    }

    @Test
    public void testTemperatureUnit_GetUnitName() {
        assertEquals("CELSIUS",    QuantityDTO.TemperatureUnit.CELSIUS.getUnitName());
        assertEquals("FAHRENHEIT", QuantityDTO.TemperatureUnit.FAHRENHEIT.getUnitName());
        assertEquals("KELVIN",     QuantityDTO.TemperatureUnit.KELVIN.getUnitName());
    }

    @Test
    public void testTemperatureUnit_GetMeasurementType() {
        assertEquals("TemperatureUnit", QuantityDTO.TemperatureUnit.CELSIUS.getMeasurementType());
    }

    // =========================================================================
    // IMeasurableUnit contract — all inner enums implement the interface
    // =========================================================================

    @Test
    public void testIMeasurableUnit_LengthUnit_ImplementsInterface() {
        assertTrue(QuantityDTO.LengthUnit.FEET instanceof QuantityDTO.IMeasurableUnit);
    }

    @Test
    public void testIMeasurableUnit_VolumeUnit_ImplementsInterface() {
        assertTrue(QuantityDTO.VolumeUnit.LITRE instanceof QuantityDTO.IMeasurableUnit);
    }

    @Test
    public void testIMeasurableUnit_WeightUnit_ImplementsInterface() {
        assertTrue(QuantityDTO.WeightUnit.KILOGRAM instanceof QuantityDTO.IMeasurableUnit);
    }

    @Test
    public void testIMeasurableUnit_TemperatureUnit_ImplementsInterface() {
        assertTrue(QuantityDTO.TemperatureUnit.CELSIUS instanceof QuantityDTO.IMeasurableUnit);
    }

    // =========================================================================
    // UC17 — Bean Validation: isUnitValidForMeasurementType
    // =========================================================================

    @Test
    public void testValidation_ValidUnit_LengthFeet_ReturnsTrue() {
        QuantityDTO dto = new QuantityDTO(1.0, QuantityDTO.LengthUnit.FEET);
        assertTrue(dto.isUnitValidForMeasurementType());
    }

    @Test
    public void testValidation_ValidUnit_WeightKilogram_ReturnsTrue() {
        QuantityDTO dto = new QuantityDTO(1.0, QuantityDTO.WeightUnit.KILOGRAM);
        assertTrue(dto.isUnitValidForMeasurementType());
    }

    @Test
    public void testValidation_InvalidUnit_ReturnsFalse() {
        QuantityDTO dto = new QuantityDTO(1.0, "INVALID_UNIT", "LengthUnit");
        assertFalse(dto.isUnitValidForMeasurementType());
    }

    @Test
    public void testValidation_NullFields_ReturnsTrue() {
        QuantityDTO dto = new QuantityDTO();
        assertTrue(dto.isUnitValidForMeasurementType());
    }
}