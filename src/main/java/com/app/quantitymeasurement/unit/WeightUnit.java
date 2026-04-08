package com.app.quantitymeasurement.unit;


public enum WeightUnit implements IMeasurable, SupportsArithmetic {

    KILOGRAM(1.0),
    GRAM(0.001),
    POUND(0.453592);

    /** Multiplier used to convert a value in this unit to the base unit (KILOGRAM). */
    private final double conversionFactor;

    WeightUnit(double conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    /**
     * Converts {@code value} from this unit to KILOGRAM.
     * Result is rounded to 6 decimal places.
     *
     * @param value value in this unit
     * @return equivalent value in KILOGRAM
     */
    @Override
    public double convertToBaseUnit(double value) {
        return Math.round(value * conversionFactor * 1_000_000.0) / 1_000_000.0;
    }

    /**
     * Converts {@code baseValue} from KILOGRAM to this unit.
     * Result is rounded to 6 decimal places.
     *
     * @param baseValue value in KILOGRAM
     * @return equivalent value in this unit
     */
    @Override
    public double convertFromBaseUnit(double baseValue) {
        return Math.round(baseValue / conversionFactor * 1_000_000.0) / 1_000_000.0;
    }

    /** @return the enum constant name as the unit identifier */
    @Override
    public String getUnitName() { return name(); }

    /** @return {@code "WeightUnit"} */
    @Override
    public String getMeasurementType() { return this.getClass().getSimpleName(); }

    /**
     * Returns the {@code WeightUnit} constant whose name matches {@code unitName}
     * (case-insensitive).
     *
     * @param unitName name of the unit to look up
     * @return matching constant
     * @throws IllegalArgumentException if no match is found
     */
    @Override
    public IMeasurable getUnitInstance(String unitName) {
        for (WeightUnit unit : WeightUnit.values()) {
            if (unit.getUnitName().equalsIgnoreCase(unitName)) return unit;
        }
        throw new IllegalArgumentException("Invalid weight unit: " + unitName);
    }
}
