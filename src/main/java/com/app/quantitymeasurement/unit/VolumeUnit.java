package com.app.quantitymeasurement.unit;


public enum VolumeUnit implements IMeasurable, SupportsArithmetic {

    LITRE(1.0),
    MILLILITRE(0.001),
    GALLON(3.785412);

    /** Multiplier used to convert a value in this unit to the base unit (LITRE). */
    private final double conversionFactor;

    VolumeUnit(double conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    /**
     * Converts {@code value} from this unit to LITRE.
     * Result is rounded to 6 decimal places.
     *
     * @param value value in this unit
     * @return equivalent value in LITRE
     */
    @Override
    public double convertToBaseUnit(double value) {
        return Math.round(value * conversionFactor * 1_000_000.0) / 1_000_000.0;
    }

    /**
     * Converts {@code baseValue} from LITRE to this unit.
     * Result is rounded to 6 decimal places.
     *
     * @param baseValue value in LITRE
     * @return equivalent value in this unit
     */
    @Override
    public double convertFromBaseUnit(double baseValue) {
        return Math.round(baseValue / conversionFactor * 1_000_000.0) / 1_000_000.0;
    }

    /** @return the enum constant name as the unit identifier */
    @Override
    public String getUnitName() { return name(); }

    /** @return {@code "VolumeUnit"} */
    @Override
    public String getMeasurementType() { return this.getClass().getSimpleName(); }

    /**
     * Returns the {@code VolumeUnit} constant whose name matches {@code unitName}
     * (case-insensitive).
     *
     * @param unitName name of the unit to look up
     * @return matching constant
     * @throws IllegalArgumentException if no match is found
     */
    @Override
    public IMeasurable getUnitInstance(String unitName) {
        for (VolumeUnit unit : VolumeUnit.values()) {
            if (unit.getUnitName().equalsIgnoreCase(unitName)) return unit;
        }
        throw new IllegalArgumentException("Invalid volume unit: " + unitName);
    }
}
