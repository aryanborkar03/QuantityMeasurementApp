package com.quantitymeasurement;

public interface IMeasurable {

	public String getUnitName();

	public double getConversionFactor();

	public double convertToBaseUnit(double value);

	public double convertFromBaseUnit(double baseValue);

	SupportsArithmetic supportsArithmetic = () -> true;

	default boolean supportsArithmetic() {
		return supportsArithmetic.isSupported();
	}

	default void validateOperationSupport(String operation) {
	}
}