package com.quantitymeasurement.units;

/**
 * LengthUnit
 *
 * Enumeration representing supported length measurement units in the
 * Quantity Measurement system.
 *
 * This enum implements the IMeasurable interface which defines the contract for
 * measurable units in the application. It also implements the SupportsArithmetic
 * marker interface indicating that arithmetic operations such as addition,
 * subtraction, and division are supported for length measurements.
 *
 * Supported length units:
 * - FEET
 * - INCHES
 * - YARDS
 * - CENTIMETERS
 *
 * Each unit contains a conversion factor used to convert the value to the base unit.
 * The base unit for length measurements is INCHES. All conversions are performed by
 * first converting to the base unit and then to the target unit.
 *
 * @author Developer
 * @version 16.0
 * @since 1.0
 */
public enum LengthUnit implements IMeasurable, SupportsArithmetic {

    FEET(12.0),
    INCHES(1.0),
    YARDS(36.0),
    CENTIMETERS(1 / 2.54);

    /**
     * Conversion factor used to convert the unit value to the base unit
     * (INCHES) of the length measurement category.
     */
    private final double conversionFactor;

    /**
     * Constructor for LengthUnit enum constants.
     *
     * Each enum constant is initialized with a conversion factor that defines
     * how the unit converts to the base unit (INCHES).
     *
     * @param conversionFactor the multiplier used to convert the unit value to base unit
     */
    LengthUnit(double conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    /**
     * Converts the given unit value to the base unit (INCHES).
     *
     * The conversion multiplies the given value with the conversion factor.
     * Result is rounded to 6 decimal places for consistent precision.
     *
     * @param value the value in the current unit
     * @return the converted value in the base unit
     */
    @Override
    public double convertToBaseUnit(double value) {
        double result = value * conversionFactor;
        return Math.round(result * 1_000_000.0) / 1_000_000.0;
    }

    /**
     * Converts a value from the base unit (INCHES) to the current unit.
     *
     * Performs the reverse conversion by dividing the base unit value with
     * the conversion factor. Result is rounded to 6 decimal places.
     *
     * @param baseValue the value in the base unit
     * @return the converted value in the current unit
     */
    @Override
    public double convertFromBaseUnit(double baseValue) {
        double result = baseValue / conversionFactor;
        return Math.round(result * 1_000_000.0) / 1_000_000.0;
    }

    /**
     * Returns the name of the unit (enum constant name).
     *
     * @return unit name
     */
    @Override
    public String getUnitName() {
        return name();
    }

    /**
     * Returns the measurement type associated with this unit.
     * Returns "LengthUnit" to identify the measurement category.
     *
     * @return measurement type name
     */
    @Override
    public String getMeasurementType() {
        return this.getClass().getSimpleName();
    }

    /**
     * Retrieves the unit instance corresponding to the given unit name.
     *
     * Searches through all LengthUnit constants using case-insensitive comparison.
     * Throws IllegalArgumentException if the unit name is not found.
     *
     * @param unitName the name of the unit to retrieve
     * @return the corresponding IMeasurable unit instance
     * @throws IllegalArgumentException if the unit name is invalid
     */
    @Override
    public IMeasurable getUnitInstance(String unitName) {
        for (LengthUnit unit : LengthUnit.values()) {
            if (unit.getUnitName().equalsIgnoreCase(unitName)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Invalid length unit: " + unitName);
    }
}