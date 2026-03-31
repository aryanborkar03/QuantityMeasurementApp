package com.app.quantitymeasurement.model;

import com.app.quantitymeasurement.dto.response.QuantityDTO;
import com.app.quantitymeasurement.entity.QuantityMeasurementEntity;
import com.app.quantitymeasurement.unit.IMeasurable;

/**
 * QuantityModel
 *
 * Immutable internal model pairing a numeric value with its measurable unit.
 * Used by the service layer to carry operands and results during comparison,
 * conversion, and arithmetic calculations.
 *
 * This class is distinct from {@link QuantityDTO} (which is used for API
 * communication) and from {@link QuantityMeasurementEntity} (which is used for
 * persistence). The service converts incoming DTOs to {@code QuantityModel}
 * instances before processing, and converts the results back to DTOs before
 * returning them to the controller.
 *
 * @param the unit type, which must implement {@link IMeasurab
 */
public class QuantityModel<U extends IMeasurable> {

    private final Double value;
    private final U unit;

    /**
     * Constructs a {@code QuantityModel} with the given value and unit.
     *
     * @param value numeric quantity value; must be a finite number
     * @param unit  measurable unit; must not be {@code null}
     * @throws IllegalArgumentException if {@code unit} is {@code null} or
     *                                  {@code value} is not finite
     */
    public QuantityModel(double value, U unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null");
        }
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Value must be finite");
        }
        this.value = value;
        this.unit  = unit;
    }

    /**
     * Returns the numeric value of this quantity.
     *
     * @return quantity value
     */
    public double getValue() {
        return value;
    }

    /**
     * Returns the unit associated with this quantity.
     *
     * @return measurable unit
     */
    public U getUnit() {
        return unit;
    }

    /**
     * Returns a human-readable representation such as {@code "5 FEET"} or
     * {@code "10 KILOGRAM"}.
     *
     * @return formatted quantity string
     */
    @Override
    public String toString() {
        return String.format("%s %s",
            Double.toString(value).replaceAll("\\.0+$", ""),
            unit.getUnitName());
    }
}
