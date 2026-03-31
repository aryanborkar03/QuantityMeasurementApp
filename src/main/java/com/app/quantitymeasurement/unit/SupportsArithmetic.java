package com.app.quantitymeasurement.unit;

/**
 * SupportsArithmetic
 *
 * Marker interface that tags a measurement unit as eligible for arithmetic operations
 * (addition, subtraction, division).
 *
 * The service layer checks for this marker at runtime using {@code instanceof}
 * before allowing add, subtract, or divide operations. Units that do not
 * implement this interface — specifically {@code TemperatureUnit} — will cause those
 * operations to throw {@link UnsupportedOperationException}.
 *
 * Implementations: {@code LengthUnit}, {@code WeightUnit}, {@code VolumeUnit}.
 */
public interface SupportsArithmetic {
}
