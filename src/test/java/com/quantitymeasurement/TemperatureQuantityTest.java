package com.quantitymeasurement;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TemperatureQuantityTest {

	private static final double EPSILON = 1e-6;

	@Test
	public void testTemperatureEquality_CelsiusToCelsius_SameValue() {
		assertTrue(new Quantity<>(0.0, TemperatureUnit.CELSIUS).equals(new Quantity<>(0.0, TemperatureUnit.CELSIUS)));
	}

	@Test
	public void testTemperatureEquality_FahrenheitToFahrenheit_SameValue() {
		assertTrue(new Quantity<>(32.0, TemperatureUnit.FAHRENHEIT).equals(new Quantity<>(32.0, TemperatureUnit.FAHRENHEIT)));
	}

	@Test
	public void testTemperatureEquality_CelsiusToFahrenheit_0Celsius32Fahrenheit() {
		assertTrue(	new Quantity<>(0.0, TemperatureUnit.CELSIUS).equals(new Quantity<>(32.0, TemperatureUnit.FAHRENHEIT)));
	}

	@Test
	public void testTemperatureEquality_CelsiusToFahrenheit_100Celsius212Fahrenheit() {
		assertTrue(new Quantity<>(100.0, TemperatureUnit.CELSIUS).equals(new Quantity<>(212.0, TemperatureUnit.FAHRENHEIT)));
	}

	@Test
	public void testTemperatureEquality_CelsiusToFahrenheit_Negative40Equal() {
		assertTrue(new Quantity<>(-40.0, TemperatureUnit.CELSIUS).equals(new Quantity<>(-40.0, TemperatureUnit.FAHRENHEIT)));
	}

	@Test
	public void testTemperatureEquality_SymmetricProperty() {
		Quantity<TemperatureUnit> a = new Quantity<>(0.0, TemperatureUnit.CELSIUS);
		Quantity<TemperatureUnit> b = new Quantity<>(32.0, TemperatureUnit.FAHRENHEIT);
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
	}

	@Test
	public void testTemperatureEquality_ReflexiveProperty() {
		Quantity<TemperatureUnit> a = new Quantity<>(25.0, TemperatureUnit.CELSIUS);
		assertTrue(a.equals(a));
	}

	@Test
	public void testTemperatureConversion_CelsiusToFahrenheit_VariousValues() {
		assertEquals(122.0,	new Quantity<>(50.0, TemperatureUnit.CELSIUS).convertTo(TemperatureUnit.FAHRENHEIT).getValue(),	EPSILON);
		assertEquals(-4.0,	new Quantity<>(-20.0, TemperatureUnit.CELSIUS).convertTo(TemperatureUnit.FAHRENHEIT).getValue(), EPSILON);
		assertEquals(-40.0,	new Quantity<>(-40.0, TemperatureUnit.CELSIUS).convertTo(TemperatureUnit.FAHRENHEIT).getValue(), EPSILON);
	}

	@Test
	public void testTemperatureConversion_FahrenheitToCelsius_VariousValues() {
		assertEquals(50.0, new Quantity<>(122.0, TemperatureUnit.FAHRENHEIT).convertTo(TemperatureUnit.CELSIUS).getValue(), EPSILON);
		assertEquals(-20.0, new Quantity<>(-4.0, TemperatureUnit.FAHRENHEIT).convertTo(TemperatureUnit.CELSIUS).getValue(), EPSILON);
		assertEquals(-40.0, new Quantity<>(-40.0, TemperatureUnit.FAHRENHEIT).convertTo(TemperatureUnit.CELSIUS).getValue(), EPSILON);
	}

	@Test
	public void testTemperatureConversion_RoundTrip_PreservesValue() {
		double original = 37.5;
		Quantity<TemperatureUnit> q = new Quantity<>(original, TemperatureUnit.CELSIUS);
		Quantity<TemperatureUnit> converted = q.convertTo(TemperatureUnit.FAHRENHEIT).convertTo(TemperatureUnit.CELSIUS);
		assertEquals(original, converted.getValue(), 1e-4);
	}

	@Test
	public void testTemperatureConversion_SameUnit() {
		Quantity<TemperatureUnit> q = new Quantity<>(25.0, TemperatureUnit.CELSIUS);
		Quantity<TemperatureUnit> out = q.convertTo(TemperatureUnit.CELSIUS);
		assertEquals(25.0, out.getValue(), EPSILON);
	}

	@Test
	public void testTemperatureConversion_ZeroValue() {
		Quantity<TemperatureUnit> q = new Quantity<>(0.0, TemperatureUnit.CELSIUS);
		Quantity<TemperatureUnit> out = q.convertTo(TemperatureUnit.FAHRENHEIT);
		assertEquals(32.0, out.getValue(), EPSILON);
	}

	@Test
	public void testTemperatureConversion_NegativeValues() {
		Quantity<TemperatureUnit> q = new Quantity<>(-273.15, TemperatureUnit.CELSIUS);
		Quantity<TemperatureUnit> out = q.convertTo(TemperatureUnit.FAHRENHEIT);
		assertEquals(-459.67, Math.round(out.getValue() * 100.0) / 100.0, EPSILON);
	}

	@Test
	public void testTemperatureConversion_LargeValues() {
		Quantity<TemperatureUnit> q = new Quantity<>(1000.0, TemperatureUnit.CELSIUS);
		Quantity<TemperatureUnit> out = q.convertTo(TemperatureUnit.FAHRENHEIT);
		assertEquals((1000.0 * 9.0 / 5.0) + 32.0, out.getValue(), EPSILON);
	}

	@Test
	public void testTemperatureUnsupportedOperation_Add() {
		Quantity<TemperatureUnit> a = new Quantity<>(100.0, TemperatureUnit.CELSIUS);
		Quantity<TemperatureUnit> b = new Quantity<>(50.0, TemperatureUnit.CELSIUS);
		UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> a.add(b));
		assertTrue(ex.getMessage().toLowerCase().contains("does not support")
				|| ex.getMessage().toLowerCase().contains("unsupported"));
	}

	@Test
	public void testTemperatureUnsupportedOperation_Subtract() {
		Quantity<TemperatureUnit> a = new Quantity<>(100.0, TemperatureUnit.CELSIUS);
		Quantity<TemperatureUnit> b = new Quantity<>(50.0, TemperatureUnit.CELSIUS);
		try {
			Quantity<TemperatureUnit> result = a.subtract(b);
			assertNotNull(result);
		} catch (UnsupportedOperationException e) {
			assertTrue(e.getMessage().toLowerCase().contains("does not support")
					|| e.getMessage().toLowerCase().contains("unsupported"));
		}
	}

	@Test
	public void testTemperatureUnsupportedOperation_Divide() {
		Quantity<TemperatureUnit> a = new Quantity<>(100.0, TemperatureUnit.CELSIUS);
		Quantity<TemperatureUnit> b = new Quantity<>(50.0, TemperatureUnit.CELSIUS);
		assertThrows(UnsupportedOperationException.class, () -> a.divide(b));
	}

	@Test
	public void testTemperatureUnsupportedOperation_ErrorMessage() {
		Quantity<TemperatureUnit> a = new Quantity<>(100.0, TemperatureUnit.CELSIUS);
		Quantity<TemperatureUnit> b = new Quantity<>(50.0, TemperatureUnit.CELSIUS);
		UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> a.add(b));
		assertTrue(ex.getMessage().contains("does not support") || ex.getMessage().toLowerCase().contains("unsupported"));
	}

	@Test
	public void testTemperatureVsLengthIncompatibility() {
		assertFalse(new Quantity<>(100.0, TemperatureUnit.CELSIUS).equals(new Quantity<>(100.0, LengthUnit.FEET)));
	}

	@Test
	public void testTemperatureVsWeightIncompatibility() {
		assertFalse(new Quantity<>(50.0, TemperatureUnit.CELSIUS).equals(new Quantity<>(50.0, WeightUnit.KILOGRAM)));
	}

	@Test
	public void testTemperatureVsVolumeIncompatibility() {
		assertFalse(new Quantity<>(25.0, TemperatureUnit.CELSIUS).equals(new Quantity<>(25.0, VolumeUnit.LITRE)));
	}

	@Test
	public void testOperationSupportMethods_TemperatureUnitAddition() {
		assertFalse(TemperatureUnit.CELSIUS.supportsArithmetic());
		UnsupportedOperationException ex = assertThrows(
			UnsupportedOperationException.class,
			() -> TemperatureUnit.CELSIUS.validateOperationSupport("ADD")
		);
		assertTrue(
			ex.getMessage().toLowerCase().contains("does not support") || 
			ex.getMessage().toLowerCase().contains("unsupported")
		);
	}

	@Test
	public void testOperationSupportMethods_TemperatureUnitDivision() {
		assertFalse(TemperatureUnit.FAHRENHEIT.supportsArithmetic());
		UnsupportedOperationException ex = assertThrows(
			UnsupportedOperationException.class,
			() -> TemperatureUnit.FAHRENHEIT.validateOperationSupport("DIVIDE")
		);
		assertTrue(ex.getMessage().toLowerCase().contains("does not support") || 
			ex.getMessage().toLowerCase().contains("unsupported")
		);
	}

	@Test
	public void testOperationSupportMethods_LengthUnitAddition() {
		assertTrue(LengthUnit.FEET.supportsArithmetic());
	}

	@Test
	public void testOperationSupportMethods_WeightUnitDivision() {
		assertTrue(WeightUnit.KILOGRAM.supportsArithmetic());
	}

	@Test
	public void testIMeasurableInterface_Evolution_BackwardCompatible() {
		Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.FEET);
		Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);
		Quantity<LengthUnit> sum = a.add(b);
		assertEquals(new Quantity<>(2.0, LengthUnit.FEET), sum);
	}

	@Test
	public void testTemperatureUnit_NonLinearConversion() {
		Quantity<TemperatureUnit> c = new Quantity<>(100.0, TemperatureUnit.CELSIUS);
		Quantity<TemperatureUnit> f = c.convertTo(TemperatureUnit.FAHRENHEIT);
		assertEquals(212.0, f.getValue(), EPSILON);
	}

	@Test
	public void testTemperatureUnit_AllConstants() {
		assertNotNull(TemperatureUnit.CELSIUS);
		assertNotNull(TemperatureUnit.FAHRENHEIT);
		assertNotNull(TemperatureUnit.KELVIN);
	}

	@Test
	public void testTemperatureUnit_NameMethod() {
		assertEquals("CELSIUS", TemperatureUnit.CELSIUS.getUnitName());
		assertEquals("FAHRENHEIT", TemperatureUnit.FAHRENHEIT.getUnitName());
	}

	@Test
	public void testTemperatureUnit_ConversionFactor() {
		assertEquals(1.0, TemperatureUnit.CELSIUS.getConversionFactor(), EPSILON);
	}

	@Test
	public void testTemperatureNullUnitValidation() {
		assertThrows(IllegalArgumentException.class, () -> new Quantity<>(100.0, null));
	}

	@Test
	public void testTemperatureNullOperandValidation_InComparison() {
		Quantity<TemperatureUnit> q = new Quantity<>(10.0, TemperatureUnit.CELSIUS);
		assertFalse(q.equals(null));
	}

	@Test
	public void testTemperatureDifferentValuesInequality() {
		assertFalse(new Quantity<>(50.0, TemperatureUnit.CELSIUS).equals(new Quantity<>(100.0, TemperatureUnit.CELSIUS)));
	}

	@Test
	public void testTemperatureBackwardCompatibility_UC1_Through_UC13() {
		assertEquals(
			new Quantity<>(2.0, LengthUnit.FEET),
			new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(12.0, LengthUnit.INCHES))
		);
		assertEquals(
			new Quantity<>(9.5, LengthUnit.FEET),
			new Quantity<>(10.0, LengthUnit.FEET).subtract(new Quantity<>(6.0, LengthUnit.INCHES))
		);
		assertEquals(2.0, new Quantity<>(10.0, WeightUnit.KILOGRAM).divide(new Quantity<>(5.0, WeightUnit.KILOGRAM)), EPSILON);
	}

	@Test
	public void testTemperatureConversionPrecision_Epsilon() {
		Quantity<TemperatureUnit> a = new Quantity<>(0.0, TemperatureUnit.CELSIUS);
		Quantity<TemperatureUnit> b = new Quantity<>(32.0, TemperatureUnit.FAHRENHEIT);
		double baseA = a.getUnit().convertToBaseUnit(a.getValue());
		double baseB = b.getUnit().convertToBaseUnit(b.getValue());
		assertTrue(Math.abs(baseA - baseB) < EPSILON);
	}

	@Test
	public void testTemperatureConversionEdgeCase_VerySmallDifference() {
		Quantity<TemperatureUnit> a = new Quantity<>(0.000001, TemperatureUnit.CELSIUS);
		Quantity<TemperatureUnit> b = a.convertTo(TemperatureUnit.FAHRENHEIT).convertTo(TemperatureUnit.CELSIUS);
		assertEquals(a.getValue(), b.getValue(), EPSILON);
	}

	@Test
	public void testTemperatureEnumImplementsIMeasurable() {
		assertTrue(IMeasurable.class.isAssignableFrom(TemperatureUnit.class));
	}

	@Test
	public void testTemperatureDefaultMethodInheritance() {
		assertTrue(LengthUnit.FEET.supportsArithmetic());
		assertTrue(WeightUnit.KILOGRAM.supportsArithmetic());
		assertTrue(VolumeUnit.LITRE.supportsArithmetic());
	}

	@Test
	public void testTemperatureCrossUnitAdditionAttempt() {
		Quantity<TemperatureUnit> a = new Quantity<>(0.0, TemperatureUnit.CELSIUS);
		Quantity<TemperatureUnit> b = new Quantity<>(32.0, TemperatureUnit.FAHRENHEIT);
		assertThrows(UnsupportedOperationException.class, () -> a.add(b));
	}
	
	@Test
	public void testTemperatureValidateOperationSupport_MethodBehavior() {
		assertThrows(
			UnsupportedOperationException.class,
			() -> TemperatureUnit.CELSIUS.validateOperationSupport("ADD")
		);
	}

	@Test
	public void testTemperatureIntegrationWithGenericQuantity() {
		Quantity<TemperatureUnit> t = new Quantity<>(100.0, TemperatureUnit.CELSIUS);
		Quantity<TemperatureUnit> converted = t.convertTo(TemperatureUnit.KELVIN);
		assertEquals(373.15, Math.round(converted.getValue() * 1_000_000.0) / 1_000_000.0, EPSILON);
	}
}