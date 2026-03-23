package com.quantitymeasurement.entity;

/**
 * QuantityDTO
 *
 * Data Transfer Object (POJO) used for transferring quantity measurement data
 * between layers of the application.
 *
 * This DTO encapsulates the essential information required for performing quantity
 * measurement operations such as comparison, conversion, addition, subtraction, and
 * division. It stores the quantity value, unit of measurement, and measurement type.
 *
 * The DTO is primarily used to transfer data between the Application Layer, Controller
 * Layer, and Service Layer without exposing internal domain models.
 *
 * Supported measurement categories:
 * - Length (FEET, INCHES, YARDS, CENTIMETERS)
 * - Volume (LITRE, MILLILITRE, GALLON)
 * - Weight (KILOGRAM, GRAM, POUND)
 * - Temperature (CELSIUS, FAHRENHEIT, KELVIN)
 *
 * Each category defines its own unit enumeration which implements the IMeasurableUnit
 * interface. The enum constants are made public so they have visibility outside the package.
 *
 * @author Developer
 * @version 16.0
 * @since 1.0
 */
public class QuantityDTO {

    /**
     * Interface representing measurable units used inside the DTO.
     *
     * All unit enumerations inside this DTO implement this interface to provide
     * a common contract for retrieving unit metadata (unit name and measurement type).
     */
    public interface IMeasurableUnit {
        public String getUnitName();
        public String getMeasurementType();
    }

    /**
     * Enumeration representing supported length units.
     * Made public for visibility outside the entity package.
     */
    public enum LengthUnit implements IMeasurableUnit {
        FEET,
        INCHES,
        YARDS,
        CENTIMETERS;

        /**
         * Returns the enum constant name as the unit identifier.
         *
         * @return unit name
         */
        @Override
        public String getUnitName() {
            return this.name();
        }

        /**
         * Returns "LengthUnit" as the measurement type.
         *
         * @return measurement type name
         */
        @Override
        public String getMeasurementType() {
            return this.getClass().getSimpleName();
        }
    }

    /**
     * Enumeration representing supported volume units.
     * Made public for visibility outside the entity package.
     */
    public enum VolumeUnit implements IMeasurableUnit {
        LITRE,
        MILLILITRE,
        GALLON;

        /**
         * Returns the enum constant name as the unit identifier.
         *
         * @return unit name
         */
        @Override
        public String getUnitName() {
            return this.name();
        }

        /**
         * Returns "VolumeUnit" as the measurement type.
         *
         * @return measurement type name
         */
        @Override
        public String getMeasurementType() {
            return this.getClass().getSimpleName();
        }
    }

    /**
     * Enumeration representing supported weight units.
     * Made public for visibility outside the entity package.
     */
    public enum WeightUnit implements IMeasurableUnit {
        KILOGRAM,
        GRAM,
        POUND;

        /**
         * Returns the enum constant name as the unit identifier.
         *
         * @return unit name
         */
        @Override
        public String getUnitName() {
            return this.name();
        }

        /**
         * Returns "WeightUnit" as the measurement type.
         *
         * @return measurement type name
         */
        @Override
        public String getMeasurementType() {
            return this.getClass().getSimpleName();
        }
    }

    /**
     * Enumeration representing supported temperature units.
     * Made public for visibility outside the entity package.
     */
    public enum TemperatureUnit implements IMeasurableUnit {
        CELSIUS, FAHRENHEIT, KELVIN;

        /**
         * Returns the enum constant name as the unit identifier.
         *
         * @return unit name
         */
        @Override
        public String getUnitName() {
            return this.name();
        }

        /**
         * Returns "TemperatureUnit" as the measurement type.
         *
         * @return measurement type name
         */
        @Override
        public String getMeasurementType() {
            return this.getClass().getSimpleName();
        }
    }

    /**
     * Numerical value of the quantity.
     */
    public double value;

    /**
     * Unit associated with the quantity value.
     */
    public String unit;

    /**
     * Measurement category of the unit.
     */
    public String measurementType;

    /**
     * Constructor for creating a QuantityDTO using a unit enumeration.
     *
     * @param value numerical quantity value
     * @param unit  measurable unit enumeration
     */
    public QuantityDTO(double value, IMeasurableUnit unit) {
        this.value = value;
        this.unit = unit.getUnitName();
        this.measurementType = unit.getMeasurementType();
    }

    /**
     * Constructor for creating a QuantityDTO using raw string values.
     *
     * @param value           numerical quantity value
     * @param unit            unit name
     * @param measurementType measurement category
     */
    public QuantityDTO(double value, String unit, String measurementType) {
        this.value = value;
        this.unit = unit;
        this.measurementType = measurementType;
    }

    /**
     * Returns the quantity value.
     *
     * @return quantity value
     */
    public double getValue() {
        return value;
    }

    /**
     * Returns the unit associated with the quantity.
     *
     * @return unit name
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Returns the measurement category of the quantity.
     *
     * @return measurement type
     */
    public String getMeasurementType() {
        return measurementType;
    }

    /**
     * Returns a formatted string representation of the quantity.
     *
     * @return formatted quantity string
     */
    @Override
    public String toString() {
        return String.format("%s %s", Double.toString(value).replace("\\.0+$", ""), unit);
    }
}