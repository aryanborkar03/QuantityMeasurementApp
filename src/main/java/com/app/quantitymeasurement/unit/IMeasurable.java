package com.app.quantitymeasurement.unit;


public interface IMeasurable {

    /**
     * Returns the canonical name of this unit (e.g., {@code "FEET"}, {@code "CELSIUS"}).
     *
     * @return unit name
     */
    String getUnitName();

    /**
     * Converts the given value from this unit to the category's base unit.
     *
     * @param value value in this unit
     * @return equivalent value in the base unit
     */
    double convertToBaseUnit(double value);

    /**
     * Converts the given base-unit value back to this unit.
     *
     * @param baseValue value in the base unit
     * @return equivalent value in this unit
     */
    double convertFromBaseUnit(double baseValue);

    /**
     * Returns the measurement category that this unit belongs to
     * (e.g., {@code "LengthUnit"}, {@code "TemperatureUnit"}).
     *
     * @return measurement type name
     */
    String getMeasurementType();

    /**
     * Resolves the unit constant whose name matches {@code unitName}
     * (case-insensitive search within the same category).
     *
     * @param unitName the name of the unit to look up
     * @return the matching {@code IMeasurable} constant
     * @throws IllegalArgumentException if no match is found
     */
    IMeasurable getUnitInstance(String unitName);

    /**
     * Returns {@code true} when this unit supports arithmetic operations.
     * The default implementation checks whether the unit implements
     * {@link SupportsArithmetic}.
     *
     * @return {@code true} if arithmetic is supported
     */
    default boolean supportsArithmetic() {
        return this instanceof SupportsArithmetic;
    }

    /**
     * Throws {@link UnsupportedOperationException} if this unit does not support
     * arithmetic, providing a clear error message that includes the operation name.
     *
     * @param operationName name of the attempted operation
     * @throws UnsupportedOperationException if arithmetic is not supported
     */
    default void validateOperationSupport(String operationName) {
        if (!supportsArithmetic()) {
            throw new UnsupportedOperationException(
                getMeasurementType() + " does not support " + operationName);
        }
    }

    /**
     * Returns the factor used to convert one unit of this type to the base unit.
     * For linear units this equals {@code convertToBaseUnit(1.0)}.
     * Temperature units override this to return {@code 1.0} because their
     * conversion is non-linear.
     *
     * @return conversion factor
     */
    default double getConversionFactor() {
        return convertToBaseUnit(1.0);
    }
}
