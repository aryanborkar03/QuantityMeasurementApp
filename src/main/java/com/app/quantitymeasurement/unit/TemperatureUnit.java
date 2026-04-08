package com.app.quantitymeasurement.unit;

import java.util.function.Function;


public enum TemperatureUnit implements IMeasurable {

    CELSIUS(
        v -> v,                          // Celsius → Celsius (identity)
        v -> v                           // Celsius → Celsius (identity)
    ),
    FAHRENHEIT(
        v -> (v - 32.0) * 5.0 / 9.0,    // Fahrenheit → Celsius
        v -> v * 9.0 / 5.0 + 32.0       // Celsius → Fahrenheit
    ),
    KELVIN(
        v -> v - 273.15,                 // Kelvin → Celsius
        v -> v + 273.15                  // Celsius → Kelvin
    );

    /** Converts a value in this unit to the base unit (CELSIUS). */
    private final Function<Double, Double> toBase;

    /** Converts a CELSIUS value to this unit. */
    private final Function<Double, Double> fromBase;

    TemperatureUnit(Function<Double, Double> toBase, Function<Double, Double> fromBase) {
        this.toBase   = toBase;
        this.fromBase = fromBase;
    }

    /**
     * Converts {@code value} from this unit to CELSIUS.
     *
     * @param value temperature in this unit
     * @return equivalent temperature in CELSIUS
     */
    @Override
    public double convertToBaseUnit(double value) {
        return toBase.apply(value);
    }

    /**
     * Converts {@code baseValue} from CELSIUS to this unit.
     *
     * @param baseValue temperature in CELSIUS
     * @return equivalent temperature in this unit
     */
    @Override
    public double convertFromBaseUnit(double baseValue) {
        return fromBase.apply(baseValue);
    }

    /**
     * Converts {@code value} from this unit to {@code target} in one step.
     *
     * @param value  temperature in this unit
     * @param target desired target unit
     * @return converted temperature value
     */
    public double convertTo(double value, TemperatureUnit target) {
        return target.convertFromBaseUnit(convertToBaseUnit(value));
    }

    /** @return the enum constant name as the unit identifier */
    @Override
    public String getUnitName() { return this.name(); }

    /** @return {@code "TemperatureUnit"} */
    @Override
    public String getMeasurementType() { return this.getClass().getSimpleName(); }

    /**
     * Returns the {@code TemperatureUnit} constant whose name matches {@code unitName}
     * (case-insensitive).
     *
     * @param unitName name of the unit to look up
     * @return matching constant
     * @throws IllegalArgumentException if no match is found
     */
    @Override
    public IMeasurable getUnitInstance(String unitName) {
        for (TemperatureUnit unit : TemperatureUnit.values()) {
            if (unit.getUnitName().equalsIgnoreCase(unitName)) return unit;
        }
        throw new IllegalArgumentException("Invalid temperature unit: " + unitName);
    }
}
