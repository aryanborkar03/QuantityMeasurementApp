package com.quantitymeasurement.units;

/**
 * LengthUnit – Enumeration representing supported length measurement units
 * in the Quantity Measurement system.
 *
 * This enum implements the {@link IMeasurable} interface which defines the
 * contract for measurable units in the application. It also implements the
 * {@link SupportsArithmetic} marker interface indicating that arithmetic
 * operations such as addition, subtraction, and division are supported for
 * length measurements.
 *
 * <p>This enum defines the following supported length units:</p>
 * <ul>
 * <li>FEET</li>
 * <li>INCHES</li>
 * <li>YARDS</li>
 * <li>CENTIMETERS</li>
 * </ul>
 *
 * Each unit contains a conversion factor used to convert the value of the
 * unit to the base unit of the length measurement category.
 *
 * <p>In this implementation, the base unit for length measurements is
 * considered to be {@code INCHES}. All conversions between different
 * length units are performed by first converting the given value to the
 * base unit and then converting it to the target unit.</p>
 *
 * The conversion process follows two steps:
 * 1. Convert the given unit value to the base unit.
 * 2. Convert the base unit value to the desired target unit.
 *
 * <p>This design ensures accurate and consistent conversions across
 * all supported length measurement units.</p>
 *
 * This enum is primarily used by the Service Layer while performing
 * conversion, comparison, and arithmetic operations on quantities.
 */
public enum LengthUnit implements IMeasurable, SupportsArithmetic {

	FEET(12.0),
	INCHES(1.0),
	YARDS(36.0),
	CENTIMETERS(1 / 2.54);

	/**
	 * Conversion factor used to convert the unit value
	 * to the base unit of the measurement category.
	 */
	private final double conversionFactor;

	/**
	 * Constructor for LengthUnit enum constants.
	 *
	 * Each enum constant is initialized with a conversion
	 * factor that defines how the unit converts to the
	 * base unit of the length measurement category.
	 *
	 * @param conversionFactor the multiplier used to convert
	 *                         the unit value to the base unit
	 */
	LengthUnit(double conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	/**
	 * Converts the given unit value to the base unit.
	 *
	 * The conversion process multiplies the given value
	 * with the conversion factor associated with the
	 * specific unit.
	 *
	 * The result is rounded to 6 decimal places to ensure
	 * consistent precision during conversion operations.
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
	 * the base unit value with the conversion factor associated
	 * with the unit.
	 *
	 * The result is rounded to 6 decimal places to maintain
	 * numerical precision and consistency across conversions.
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
	 * Returns the name of the unit.
	 *
	 * This method returns the enum constant name which
	 * represents the unit name used throughout the
	 * Quantity Measurement application.
	 *
	 * @return the unit name as a String
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
	 * For example:
	 * LengthUnit -> "LengthUnit"
	 *
	 * @return the measurement type name
	 */
	@Override
	public String getMeasurementType() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Retrieves the unit instance corresponding to the given unit name.
	 *
	 * This method searches through all available enum constants
	 * of {@code LengthUnit} and returns the matching unit instance.
	 *
	 * The comparison is case-insensitive to improve usability.
	 *
	 * If the provided unit name does not match any defined
	 * length unit, an {@link IllegalArgumentException} is thrown.
	 *
	 * @param unitName the name of the unit to retrieve
	 * @return the corresponding {@link IMeasurable} unit instance
	 * @throws IllegalArgumentException if the unit name is invalid
	 */
	@Override
	public IMeasurable getUnitInstance(String unitName) {
		for(LengthUnit unit : LengthUnit.values()) {			
			if(unit.getUnitName().equalsIgnoreCase(unitName)) {				
				return unit;
			}
		}
		
		throw new IllegalArgumentException(
			"Invalid length unit: " + unitName
		);
	}
}