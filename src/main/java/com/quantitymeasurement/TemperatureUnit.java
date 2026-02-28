package com.quantitymeasurement;

import java.util.function.Function;

public enum TemperatureUnit implements IMeasurable {

	CELSIUS(
		v -> v, 
		v -> v, 
		() -> false
	), 
	FAHRENHEIT(
		v -> (v - 32.0) * 5.0 / 9.0,
		v -> v * 9.0 / 5.0 + 32.0,
		() -> false
	),
	KELVIN(
		v -> v - 273.15,
		v -> v + 273.15,
		() -> false
	);

	private final Function<Double, Double> toBase;
	private final Function<Double, Double> fromBase;
	private final SupportsArithmetic supports;

	TemperatureUnit(Function<Double, Double> toBase, Function<Double, Double> fromBase, SupportsArithmetic supports) {
		this.toBase = toBase;
		this.fromBase = fromBase;
		this.supports = supports;
	}

	@Override
	public String getUnitName() {
		return this.name();
	}

	@Override
	public double getConversionFactor() {
		return 1.0;
	}

	@Override
	public double convertToBaseUnit(double value) {
		return toBase.apply(value);
	}

	@Override
	public double convertFromBaseUnit(double baseValue) {
		return fromBase.apply(baseValue);
	}

	public double convertTo(double value, TemperatureUnit target) {
		double base = convertToBaseUnit(value);
		return target.convertFromBaseUnit(base);
	}

	@Override
	public boolean supportsArithmetic() {
		return supports.isSupported();
	}

	@Override
	public void validateOperationSupport(String operation) {
		if (!supports.isSupported()) {
			if ("SUBTRACT".equals(operation)) {
				return;
			}
			throw new UnsupportedOperationException(this.name() + " does not support " + operation + " operations.");
		}
	}
}