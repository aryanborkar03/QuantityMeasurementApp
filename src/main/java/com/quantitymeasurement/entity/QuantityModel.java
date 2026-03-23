package com.quantitymeasurement.entity;

import com.quantitymeasurement.units.IMeasurable;

/**
 * QuantityModel
 *
 * Generic POJO model class for representing a quantity with its associated measurable unit.
 *
 * This model is primarily used in the service layer for performing quantity operations such as:
 * - Unit conversion
 * - Quantity comparison
 * - Arithmetic operations
 *
 * The class stores a numeric value and its corresponding measurement unit that implements
 * IMeasurable. Using generics ensures type safety so that only valid measurable unit types
 * can be associated with the model.
 *
 * @param <U> the unit type implementing IMeasurable
 *
 * @author Developer
 * @version 16.0
 * @since 1.0
 */
public class QuantityModel<U extends IMeasurable> {

    private final Double value;
    private final U unit;

    /**
     * Constructs a QuantityModel with the specified value and measurable unit.
     *
     * Validates that the unit is not null and the value is a finite number.
     *
     * @param value numeric quantity value
     * @param unit  measurable unit associated with the value
     * @throws IllegalArgumentException if unit is null or value is not finite
     */
    public QuantityModel(double value, U unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null");
        }
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Value must be finite");
        }
        this.value = value;
        this.unit = unit;
    }

    /**
     * Returns the numeric value of the quantity.
     *
     * @return quantity value
     */
    public double getValue() {
        return value;
    }

    /**
     * Returns the measurable unit associated with the quantity.
     *
     * @return measurable unit
     */
    public U getUnit() {
        return unit;
    }

    /**
     * Returns a formatted string representation of the quantity model.
     *
     * Example: "5 FEET", "10 KILOGRAM"
     *
     * @return formatted quantity string
     */
    @Override
    public String toString() {
        return String.format("%s %s", Double.toString(value).replace("\\.0+$", ""), unit.getUnitName());
    }
}