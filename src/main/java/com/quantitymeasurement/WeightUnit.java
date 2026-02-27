package com.quantitymeasurement;

public enum WeightUnit implements IMeasurable {
	
	KILOGRAM(1.0), 
	GRAM(0.001), 
	POUND(0.453592);

	private final double conversionFactor;

	WeightUnit(double conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	@Override
	public double getConversionFactor() {
		return conversionFactor;
	}

	@Override
	public double convertToBaseUnit(double value) {
		double result = value * conversionFactor;
		return Math.round(result * 1_000_000.0) / 1_000_000.0;
	}

	@Override
	public double convertFromBaseUnit(double baseValue) {
		double result = baseValue / conversionFactor;
		return Math.round(result * 1_000_000.0) / 1_000_000.0;
	}
	
	@Override
    public String getUnitName() {
        return name();
    }
}