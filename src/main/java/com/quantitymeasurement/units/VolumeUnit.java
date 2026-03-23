package com.quantitymeasurement.units;

/**
 * VolumeUnit
 *
 * Enumeration representing supported volume measurement units in the
 * Quantity Measurement system.
 *
 * Supported volume units:
 * - LITRE (base unit)
 * - MILLILITRE
 * - GALLON
 *
 * Implements IMeasurable and SupportsArithmetic. The base unit is LITRE.
 *
 * @author Developer
 * @version 16.0
 * @since 1.0
 */
public enum VolumeUnit implements IMeasurable, SupportsArithmetic {

    LITRE(1.0),
    MILLILITRE(0.001),
    GALLON(3.785412);

    /**
     * Conversion factor used to convert the unit value to the base unit (LITRE).
     */
    private final double conversionFactor;

    /**
     * Constructor for VolumeUnit enum constants.
     *
     * @param conversionFactor multiplier to convert this unit to LITRE
     */
    VolumeUnit(double conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    /**
     * Converts the given unit value to the base unit (LITRE).
     * Result is rounded to 6 decimal places for precision.
     *
     * @param value value in current unit
     * @return value in LITRE
     */
    @Override
    public double convertToBaseUnit(double value) {
        double result = value * conversionFactor;
        return Math.round(result * 1_000_000.0) / 1_000_000.0;
    }

    /**
     * Converts a value from LITRE to the current unit.
     * Result is rounded to 6 decimal places for precision.
     *
     * @param baseValue value in LITRE
     * @return value in current unit
     */
    @Override
    public double convertFromBaseUnit(double baseValue) {
        double result = baseValue / conversionFactor;
        return Math.round(result * 1_000_000.0) / 1_000_000.0;
    }

    /**
     * Returns the enum constant name as the unit identifier.
     *
     * @return unit name
     */
    @Override
    public String getUnitName() {
        return name();
    }

    /**
     * Returns "VolumeUnit" to identify the measurement category.
     *
     * @return measurement type name
     */
    @Override
    public String getMeasurementType() {
        return this.getClass().getSimpleName();
    }

    /**
     * Retrieves the VolumeUnit instance by name (case-insensitive).
     *
     * @param unitName name of the unit
     * @return matching VolumeUnit
     * @throws IllegalArgumentException if unit not found
     */
    @Override
    public IMeasurable getUnitInstance(String unitName) {
        for (VolumeUnit unit : VolumeUnit.values()) {
            if (unit.getUnitName().equalsIgnoreCase(unitName)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Invalid volume unit: " + unitName);
    }
}
