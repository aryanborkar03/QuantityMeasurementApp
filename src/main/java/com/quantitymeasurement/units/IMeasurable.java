
package com.quantitymeasurement.units;

/**
 * IMeasurable
 *
 * Interface representing a measurable unit in the
 * Quantity Measurement system.
 *
 * This interface defines the core contract that all
 * measurement unit types must implement, such as
 * LengthUnit, VolumeUnit, WeightUnit, and TemperatureUnit.
 *
 * Implementations of this interface provide the ability to:
 * <ul>
 * <li>Retrieve the unit name</li>
 * <li>Convert values to the base unit of a measurement type</li>
 * <li>Convert values from the base unit to a specific unit</li>
 * <li>Identify the measurement category</li>
 * <li>Retrieve unit instances dynamically using unit names</li>
 * </ul>
 *
 * This abstraction allows the service layer to perform
 * operations generically across different measurement
 * categories without depending on specific unit enums.
 */
public interface IMeasurable {

	/**
	 * Returns the name of the measurement unit.
	 *
	 * @return unit name
	 */
	public String getUnitName();

	/**
	 * Converts the given value to the base unit
	 * of the measurement category.
	 *
	 * @param value value in the current unit
	 * @return value converted to the base unit
	 */
	public double convertToBaseUnit(double value);

	/**
	 * Converts a value from the base unit to
	 * the current measurement unit.
	 *
	 * @param baseValue value in the base unit
	 * @return converted value in the current unit
	 */
	public double convertFromBaseUnit(double baseValue);
	
	/**
	 * Returns the measurement category associated
	 * with the unit.
	 *
	 * Example:
	 * LengthUnit, VolumeUnit, WeightUnit, TemperatureUnit.
	 *
	 * @return measurement type name
	 */
	public String getMeasurementType();
	
	/**
	 * Returns the unit instance corresponding to
	 * the provided unit name.
	 *
	 * This method enables dynamic retrieval of
	 * measurable unit implementations.
	 *
	 * @param unitName name of the unit
	 * @return measurable unit instance
	 */
	public IMeasurable getUnitInstance(String unitName);
	
	/**
	 * Returns whether this unit supports arithmetic operations.
	 * Default: true for all units that implement SupportsArithmetic.
	 */
	default boolean supportsArithmetic() {
	    return this instanceof SupportsArithmetic;
	}

	/**
	 * Validates that this unit supports the given operation.
	 * Throws UnsupportedOperationException if arithmetic is not supported.
	 *
	 * @param operationName name of the operation being attempted
	 */
	default void validateOperationSupport(String operationName) {
	    if (!supportsArithmetic()) {
	        throw new UnsupportedOperationException(
	            getMeasurementType() + " does not support " + operationName
	        );
	    }
	}
	
	/**
	 * Returns the conversion factor used to convert this unit
	 * to the base unit of its measurement category.
	 *
	 * For linear units (length, weight, volume) this equals
	 * convertToBaseUnit(1.0). Temperature overrides this
	 * to return a nominal factor of 1.0 since its conversion
	 * is non-linear.
	 *
	 * @return conversion factor
	 */
	default double getConversionFactor() {
	    return convertToBaseUnit(1.0);
	}
}

