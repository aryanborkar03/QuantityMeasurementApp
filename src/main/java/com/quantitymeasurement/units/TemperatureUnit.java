
package com.quantitymeasurement.units;

import java.util.function.Function;

/**
 * TemperatureUnit
 *
 * Enumeration representing supported temperature measurement units in the
 * Quantity Measurement system.
 *
 * Supported temperature units:
 * - CELSIUS (base unit)
 * - FAHRENHEIT
 * - KELVIN
 *
 * Temperature conversions involve non-linear transformations unlike length, weight,
 * or volume. Each constant stores two functional conversion strategies:
 * - toBase:   converts from the unit to CELSIUS
 * - fromBase: converts from CELSIUS to the unit
 *
 * Note: TemperatureUnit does NOT implement SupportsArithmetic, so arithmetic
 * operations (add, subtract) are not allowed for temperature measurements.
 *
 * @author Developer
 * @version 16.0
 * @since 1.0
 */
public enum TemperatureUnit implements IMeasurable {

    CELSIUS(
        v -> v,           /* Celsius to Celsius: identity */
        v -> v            /* Celsius to Celsius: identity */
    ),
    FAHRENHEIT(
        v -> (v - 32.0) * 5.0 / 9.0,   /* Fahrenheit to Celsius */
        v -> v * 9.0 / 5.0 + 32.0      /* Celsius to Fahrenheit */
    ),
    KELVIN(
        v -> v - 273.15,    /* Kelvin to Celsius */
        v -> v + 273.15     /* Celsius to Kelvin */
    );

    /**
     * Function to convert from this temperature unit to the base unit (CELSIUS).
     */
    private final Function<Double, Double> toBase;

    /**
     * Function to convert from the base unit (CELSIUS) to this temperature unit.
     */
    private final Function<Double, Double> fromBase;

    /**
     * Constructor for TemperatureUnit enum constants.
     *
     * @param toBase   function to convert this unit's value to CELSIUS
     * @param fromBase function to convert CELSIUS value to this unit
     */
    TemperatureUnit(Function<Double, Double> toBase, Function<Double, Double> fromBase) {
        this.toBase = toBase;
        this.fromBase = fromBase;
    }

    /**
     * Returns the enum constant name as the unit identifier.
     *
     * @return unit name
     */
    @Override
    public String getUnitName() {
        return this.name();
    }

    /**
     * Converts the given temperature value to the base unit (CELSIUS).
     * The toBase functional strategy is applied.
     *
     * @param value value in current temperature unit
     * @return value in CELSIUS
     */
    @Override
    public double convertToBaseUnit(double value) {
        return toBase.apply(value);
    }

    /**
     * Converts a CELSIUS value to this temperature unit.
     * The fromBase functional strategy is applied.
     *
     * @param baseValue value in CELSIUS
     * @return value in this temperature unit
     */
    @Override
    public double convertFromBaseUnit(double baseValue) {
        return fromBase.apply(baseValue);
    }

    /**
     * Converts a temperature value from this unit to the specified target unit.
     * Step 1: converts to CELSIUS. Step 2: converts from CELSIUS to target.
     *
     * @param value  temperature value in this unit
     * @param target target temperature unit
     * @return converted temperature value
     */
    public double convertTo(double value, TemperatureUnit target) {
        double base = convertToBaseUnit(value);
        return target.convertFromBaseUnit(base);
    }

    /**
     * Returns "TemperatureUnit" to identify the measurement category.
     *
     * @return measurement type name
     */
    @Override
    public String getMeasurementType() {
        return this.getClass().getSimpleName();
    }

    /**
     * Retrieves the TemperatureUnit instance by name (case-insensitive).
     *
     * @param unitName name of the unit
     * @return matching TemperatureUnit
     * @throws IllegalArgumentException if unit not found
     */
    @Override
    public IMeasurable getUnitInstance(String unitName) {
        for (TemperatureUnit unit : TemperatureUnit.values()) {
            if (unit.getUnitName().equalsIgnoreCase(unitName)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Invalid temperature unit: " + unitName);
    }
}
