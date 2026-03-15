package com.quantitymeasurement.units;

/**
 * VolumeUnit – Enumeration representing supported volume measurement units
 * in the Quantity Measurement system.
 *
 * This enum implements the {@link IMeasurable} interface which defines
 * the contract for measurable units used within the application. It also
 * implements the {@link SupportsArithmetic} marker interface indicating
 * that arithmetic operations such as addition, subtraction, and division
 * are supported for volume measurements.
 *
 * <p>The supported volume units include:</p>
 * <ul>
 * <li>Litre</li>
 * <li>Millilitre</li>
 * <li>Gallon</li>
 * </ul>
 *
 * Each unit defines a conversion factor used to convert the unit value
 * to the base unit of the volume measurement category.
 *
 * <p>In this implementation, the base unit for volume measurement is
 * considered to be {@code LITRE}. All conversions between volume units
 * follow a two-step process:</p>
 *
 * 1. Convert the given unit value to the base unit.
 * 2. Convert the base unit value to the desired target unit.
 *
 * This design ensures accurate and consistent conversions between
 * different volume measurement units across the application.
 *
 * The VolumeUnit enum is primarily used by the Service Layer while
 * performing quantity conversions, comparisons, and arithmetic
 * operations involving volume measurements.
 */
public enum VolumeUnit implements IMeasurable, SupportsArithmetic {

    LITRE(1.0),
    MILLILITRE(0.001),
    GALLON(3.785412);

    /**
     * Conversion factor used to convert the unit value
     * to the base unit of the volume measurement category.
     */
    private final double conversionFactor;

    /**
     * Constructor for VolumeUnit enum constants.
     *
     * Each enum constant is initialized with a conversion
     * factor that determines how the unit converts to
     * the base unit of the volume measurement category.
     *
     * @param conversionFactor the multiplier used to convert
     *                         the unit value to the base unit
     */
    VolumeUnit(double conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    /**
     * Converts the given unit value to the base unit.
     *
     * The conversion multiplies the given value by the
     * conversion factor associated with the unit.
     *
     * The result is rounded to 6 decimal places to ensure
     * precision and consistency during conversion operations.
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
     * Converts a value from the base unit to the current unit.
     *
     * This method performs the reverse conversion by dividing
     * the base unit value by the conversion factor associated
     * with the unit.
     *
     * The result is rounded to 6 decimal places to maintain
     * numerical precision during conversions.
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
     * Returns the name of the volume unit.
     *
     * The enum constant name is used as the unit
     * identifier within the Quantity Measurement system.
     *
     * @return the name of the unit
     */
    @Override
    public String getUnitName() {
        return name();
    }
    
    /**
     * Returns the measurement type associated with this unit.
     *
     * This method returns the simple name of the enum class,
     * which identifies the measurement category for the unit.
     *
     * Example:
     * VolumeUnit -> "VolumeUnit"
     *
     * @return the measurement type name
     */
    @Override
	public String getMeasurementType() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Retrieves the unit instance corresponding to the
	 * given volume unit name.
	 *
	 * This method iterates through all available enum
	 * constants of {@code VolumeUnit} and returns the
	 * matching unit instance.
	 *
	 * The comparison is case-insensitive.
	 *
	 * If the provided unit name does not match any
	 * supported volume unit, an
	 * {@link IllegalArgumentException} is thrown.
	 *
	 * @param unitName the name of the volume unit
	 * @return the corresponding {@link IMeasurable} unit instance
	 * @throws IllegalArgumentException if the unit name is invalid
	 */
	@Override
	public IMeasurable getUnitInstance(String unitName) {
		for(VolumeUnit unit : VolumeUnit.values()) {			
			if(unit.getUnitName().equalsIgnoreCase(unitName)) {				
				return unit;
			}
		}
		
		throw new IllegalArgumentException(
			"Invalid volume unit: " + unitName
		);
	}
}