package com.quantitymeasurement.dto;

/**
 * QuantityDTO – Data Transfer Object (POJO) used for transferring
 * quantity measurement data between layers of the application.
 *
 * This DTO encapsulates the essential information required for
 * performing quantity measurement operations such as comparison,
 * conversion, addition, subtraction, and division.
 *
 * The DTO stores the following information:
 *
 * Quantity value
 * Unit of measurement
 * Measurement type
 * 
 *
 * The DTO is primarily used to transfer data between the
 * Application Layer, Controller Layer, and Service Layer
 * without exposing internal domain models.
 *
 * Supported measurement categories include:
 * 
 * Length
 * Volume
 * Weight
 * Temperature
 * 
 *
 * Each category defines its own unit enumeration which implements
 * the {@link IMeasurableUnit} interface.
 *
 * This design allows a uniform structure for representing
 * quantity measurements across different unit categories.
 */
public class QuantityDTO {

    /**
     * Interface representing measurable units used inside the DTO.
     *
     * All unit enumerations inside this DTO implement this interface
     * to provide a common contract for retrieving unit metadata.
     *
     * The interface provides methods to retrieve:
     * 
     * Unit name
     * Measurement type
     * 
     */
    public interface IMeasurableUnit {
        public String getUnitName();
        public String getMeasurementType();
    }

    /**
     * Enumeration representing supported length units.
     */
    public enum LengthUnit implements IMeasurableUnit {
        FEET, 
        INCHES, 
        YARDS, 
        CENTIMETERS;

        /**
         * Returns the name of the unit.
         *
         * @return unit name
         */
        @Override
        public String getUnitName() {
            return this.name();
        }

        /**
         * Returns the measurement type for the unit.
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
     */
    public enum VolumeUnit implements IMeasurableUnit {
        LITRE, 
        MILLILITRE, 
        GALLON;

        /**
         * Returns the name of the unit.
         *
         * @return unit name
         */
        @Override
        public String getUnitName() {
            return this.name();
        }

        /**
         * Returns the measurement type for the unit.
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
     */
    public enum WeightUnit implements IMeasurableUnit {
    	KILOGRAM, 
    	GRAM, 
    	POUND;

        /**
         * Returns the name of the unit.
         *
         * @return unit name
         */
        @Override
        public String getUnitName() {
            return this.name();
        }

        /**
         * Returns the measurement type for the unit.
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
     */
    public enum TemperatureUnit implements IMeasurableUnit {
        CELSIUS, FAHRENHEIT, KELVIN;

        /**
         * Returns the name of the unit.
         *
         * @return unit name
         */
        @Override
        public String getUnitName() {
            return this.name();
        }

        /**
         * Returns the measurement type for the unit.
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
     * Constructor for creating a QuantityDTO using
     * a unit enumeration.
     *
     * @param value numerical quantity value
     * @param unit measurable unit enumeration
     */
    public QuantityDTO(double value, IMeasurableUnit unit) {
        this.value = value;
        this.unit = unit.getUnitName();
        this.measurementType = unit.getMeasurementType();
    }

    /**
     * Constructor for creating a QuantityDTO using
     * raw string values.
     *
     * @param value numerical quantity value
     * @param unit unit name
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
     * Returns a formatted string representation
     * of the quantity.
     *
     * @return formatted quantity string
     */
    @Override
    public String toString() {
        return String.format("%s %s", Double.toString(value).replace("\\.0+$", ""), unit);
    }

    /**
     * Main method for quick testing of QuantityDTO.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        System.out.println("---- Testing QuantityDTO ----");

        QuantityDTO length1 =
                new QuantityDTO(2, LengthUnit.FEET);

        QuantityDTO length2 =
                new QuantityDTO(24, LengthUnit.INCHES);

        System.out.println("Length DTO 1 : " + length1);
        System.out.println("Length DTO 2 : " + length2);

        QuantityDTO volume1 =
                new QuantityDTO(3, VolumeUnit.LITRE);

        QuantityDTO volume2 =
                new QuantityDTO(500, VolumeUnit.MILLILITRE);

        System.out.println("Volume DTO 1 : " + volume1);
        System.out.println("Volume DTO 2 : " + volume2);

        QuantityDTO weight1 =
                new QuantityDTO(5, WeightUnit.KILOGRAM);

        QuantityDTO weight2 =
                new QuantityDTO(500, WeightUnit.GRAM);

        System.out.println("Weight DTO 1 : " + weight1);
        System.out.println("Weight DTO 2 : " + weight2);

        QuantityDTO temp1 =
                new QuantityDTO(25, TemperatureUnit.CELSIUS);

        QuantityDTO temp2 =
                new QuantityDTO(77, TemperatureUnit.FAHRENHEIT);

        System.out.println("Temperature DTO 1 : " + temp1);
        System.out.println("Temperature DTO 2 : " + temp2);

        QuantityDTO custom =
                new QuantityDTO(10, "FEET", "LengthUnit");

        System.out.println("String Constructor DTO : " + custom);

        System.out.println("---- DTO Testing Complete ----");
    }
}