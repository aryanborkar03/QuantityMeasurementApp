package com.quantitymeasurement;

public enum VolumeUnit implements IMeasurable {
    LITRE(1.0),
    MILLILITRE(0.001),
    GALLON(3.785412);

    private final double conversionFactor;

    VolumeUnit(double conversionFactor) {
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