package com.quantitymeasurement.units;

/**
 * WeightUnit – Enumeration representing supported weight measurement units
 * in the Quantity Measurement system.
 *
 * This enum implements the {@link IMeasurable} interface which defines
 * the contract for measurable units used within the application. It also
 * implements the {@link SupportsArithmetic} marker interface indicating
 * that arithmetic operations such as addition, subtraction, and division
 * are supported for weight measurements.
 *
 * <p>The supported weight units include:</p>
 * <ul>
 * <li>Kilogram</li>
 * <li>Gram</li>
 * <li>Pound</li>
 * </ul>
 *
 * Each unit is associated with a conversion factor used to convert
 * the unit value to the base unit of the weight measurement category.
 *
 * <p>In this implementation, the base unit for weight measurement is
 * considered to be {@code KILOGRAM}. All conversions between weight
 * units follow a two-step process:</p>
 *
 * 1. Convert the given unit value to the base unit.
 * 2. Convert the base unit value to the desired target unit.
 *
 * This approach ensures consistent and accurate conversions between
 * different weight measurement units across the application.
 *
 * The WeightUnit enum is mainly used by the Service Layer during
 * quantity comparison, conversion, and arithmetic operations
 * involving weight measurements.
 */
public enum WeightUnit implements IMeasurable, SupportsArithmetic {
	
	KILOGRAM(1.0), 
	GRAM(0.001), 
	POUND(0.453592);

	/**
	 * Conversion factor used to convert the unit value
	 * to the base unit of the weight measurement category.
	 */
	private final double conversionFactor;

	/**
	 * Constructor for WeightUnit enum constants.
	 *
	 * Each enum constant is initialized with a conversion
	 * factor that determines how the unit converts to
	 * the base unit of the weight measurement category.
	 *
	 * @param conversionFactor the multiplier used to convert
	 *                         the unit value to the base unit
	 */
	WeightUnit(double conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	/**
	 * Converts the given unit value to the base unit.
	 *
	 * The conversion multiplies the given value with the
	 * conversion factor associated with the unit.
	 *
	 * The result is rounded to 6 decimal places to ensure
	 * consistent numerical precision during conversions.
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
	 * numerical precision across conversions.
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
	 * Returns the name of the weight unit.
	 *
	 * The enum constant name is used as the
	 * unit identifier within the Quantity
	 * Measurement system.
	 *
	 * @return the unit name
	 */
	@Override
    public String getUnitName() {
        return name();
    }
	
	/**
	 * Returns the measurement type associated with this unit.
	 *
	 * The measurement type corresponds to the simple
	 * name of the enum class, identifying the category
	 * of measurement.
	 *
	 * Example:
	 * WeightUnit -> "WeightUnit"
	 *
	 * @return the measurement type name
	 */
	@Override
	public String getMeasurementType() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Retrieves the unit instance corresponding to the
	 * given weight unit name.
	 *
	 * This method iterates through all available enum
	 * constants of {@code WeightUnit} and returns the
	 * matching unit instance.
	 *
	 * The comparison is case-insensitive.
	 *
	 * If the provided unit name does not match any
	 * supported weight unit, an
	 * {@link IllegalArgumentException} is thrown.
	 *
	 * @param unitName the name of the weight unit
	 * @return the corresponding {@link IMeasurable} unit instance
	 * @throws IllegalArgumentException if the unit name is invalid
	 */
	@Override
	public IMeasurable getUnitInstance(String unitName) {
		for(WeightUnit unit : WeightUnit.values()) {			
			if(unit.getUnitName().equalsIgnoreCase(unitName)) {				
				return unit;
			}
		}
		
		throw new IllegalArgumentException(
			"Invalid weight unit: " + unitName
		);
	}
}