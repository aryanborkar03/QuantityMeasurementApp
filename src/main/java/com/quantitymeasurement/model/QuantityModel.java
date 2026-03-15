package com.quantitymeasurement.model;


import com.quantitymeasurement.units.IMeasurable;

/**
 * QuantityModel – Generic POJO model class for representing
 * a quantity with its associated measurable unit.
 *
 * This model is primarily used in the service layer for
 * performing quantity operations such as:
 * <ul>
 * <li>Unit conversion</li>
 * <li>Quantity comparison</li>
 * <li>Arithmetic operations</li>
 * </ul>
 *
 * The class stores a numeric value and its corresponding
 * measurement unit that implements {@link IMeasurable}.
 *
 * Using generics ensures type safety so that only valid
 * measurable unit types can be associated with the model.
 *
 * @param <U> the unit type implementing IMeasurable.
 */
public class QuantityModel<U extends IMeasurable> {

	private final Double value;
	private final U unit;

	/**
	 * Constructs a QuantityModel with the specified value
	 * and measurable unit.
	 *
	 * @param value numeric quantity value
	 * @param unit measurable unit associated with the value
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
     * Returns the formatted string representation
     * of the quantity model.
     *
     * Example:
     * 5 FEET
     * 10 KILOGRAM
     *
     * @return formatted quantity string
     */
    @Override
    public String toString() {
        return String.format("%s %s", Double.toString(value).replace("\\.0+$", ""), unit.getUnitName());
    }

    /**
     * Main method for testing the functionality
     * of the QuantityModel class.
     *
     * Demonstrates creation of quantity models
     * for different measurement categories and
     * verifies validation behavior.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        System.out.println("---- Testing QuantityModel ----");

        com.quantitymeasurement.units.LengthUnit feet =
                com.quantitymeasurement.units.LengthUnit.FEET;

        QuantityModel<com.quantitymeasurement.units.LengthUnit> length =
                new QuantityModel<>(5, feet);

        System.out.println("Length Model : " + length);

        com.quantitymeasurement.units.WeightUnit kg =
                com.quantitymeasurement.units.WeightUnit.KILOGRAM;

        QuantityModel<com.quantitymeasurement.units.WeightUnit> weight =
                new QuantityModel<>(10, kg);

        System.out.println("Weight Model : " + weight);

        com.quantitymeasurement.units.VolumeUnit litre =
                com.quantitymeasurement.units.VolumeUnit.LITRE;

        QuantityModel<com.quantitymeasurement.units.VolumeUnit> volume =
                new QuantityModel<>(3.5, litre);

        System.out.println("Volume Model : " + volume);

        com.quantitymeasurement.units.TemperatureUnit celsius =
                com.quantitymeasurement.units.TemperatureUnit.CELSIUS;

        QuantityModel<com.quantitymeasurement.units.TemperatureUnit> temp =
                new QuantityModel<>(25, celsius);

        System.out.println("Temperature Model : " + temp);

        System.out.println("\nTesting getters:");

        System.out.println("Value : " + length.getValue());
        System.out.println("Unit  : " + length.getUnit().getUnitName());

        System.out.println("\nTesting exception handling:");

        try {
            new QuantityModel<>(10, null);
        } catch (Exception e) {
            System.out.println("Expected error: " + e.getMessage());
        }

        try {
            new QuantityModel<>(Double.POSITIVE_INFINITY, feet);
        } catch (Exception e) {
            System.out.println("Expected error: " + e.getMessage());
        }

        System.out.println("---- QuantityModel Test Completed ----");
    }
}