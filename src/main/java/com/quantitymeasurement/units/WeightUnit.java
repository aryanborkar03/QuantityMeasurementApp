package com.quantitymeasurement.units;

/**
 * WeightUnit
 *
 * Enumeration representing supported weight measurement units in the
 * Quantity Measurement system.
 *
 * Supported weight units:
 * - KILOGRAM (base unit)
 * - GRAM
 * - POUND
 *
 * Implements IMeasurable and SupportsArithmetic. The base unit is KILOGRAM.
 *
 * @author Developer
 * @version 16.0
 * @since 1.0
 */
public enum WeightUnit implements IMeasurable, SupportsArithmetic {

    KILOGRAM(1.0),
    GRAM(0.001),
    POUND(0.453592);

    /**
     * Conversion factor used to convert the unit value to the base unit (KILOGRAM).
     */
    private final double conversionFactor;

    /**
     * Constructor for WeightUnit enum constants.
     *
     * @param conversionFactor multiplier to convert this unit to KILOGRAM
     */
    WeightUnit(double conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    /**
     * Converts the given unit value to the base unit (KILOGRAM).
     * Result is rounded to 6 decimal places for precision.
     *
     * @param value value in current unit
     * @return value in KILOGRAM
     */
    @Override
    public double convertToBaseUnit(double value) {
        double result = value * conversionFactor;
        return Math.round(result * 1_000_000.0) / 1_000_000.0;
    }

    /**
     * Converts a value from KILOGRAM to the current unit.
     * Result is rounded to 6 decimal places for precision.
     *
     * @param baseValue value in KILOGRAM
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
     * Returns "WeightUnit" to identify the measurement category.
     *
     * @return measurement type name
     */
    @Override
    public String getMeasurementType() {
        return this.getClass().getSimpleName();
    }

    /**
     * Retrieves the WeightUnit instance by name (case-insensitive).
     *
     * @param unitName name of the unit
     * @return matching WeightUnit
     * @throws IllegalArgumentException if unit not found
     */
    @Override
    public IMeasurable getUnitInstance(String unitName) {
        for (WeightUnit unit : WeightUnit.values()) {
            if (unit.getUnitName().equalsIgnoreCase(unitName)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Invalid weight unit: " + unitName);
    }
}
