package com.quantitymeasurement;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QuantityAdditionTest {

	@Test
	public void testAddition_SameUnit_FeetPlusFeet() {
		Quantity<LengthUnit> result = new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(2.0, LengthUnit.FEET));
		assertEquals(new Quantity<>(3.0, LengthUnit.FEET), result);
	}

	@Test
	public void testAddition_SameUnit_InchPlusInch() {
		Quantity<LengthUnit> result = new Quantity<>(6.0, LengthUnit.INCHES)
				.add(new Quantity<>(6.0, LengthUnit.INCHES));
		assertEquals(new Quantity<>(12.0, LengthUnit.INCHES), result);
	}

	@Test
	public void testAddition_CrossUnit_FeetPlusInches() {
		Quantity<LengthUnit> result = new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(12.0, LengthUnit.INCHES));
		assertEquals(new Quantity<>(2.0, LengthUnit.FEET), result);
	}

	@Test
	public void testAddition_CrossUnit_InchPlusFeet() {
		Quantity<LengthUnit> result = new Quantity<>(12.0, LengthUnit.INCHES).add(new Quantity<>(1.0, LengthUnit.FEET));
		assertEquals(new Quantity<>(24.0, LengthUnit.INCHES), result);
	}

	@Test
	public void testAddition_CrossUnit_YardPlusFeet() {
		Quantity<LengthUnit> result = new Quantity<>(1.0, LengthUnit.YARDS).add(new Quantity<>(3.0, LengthUnit.FEET));
		assertEquals(new Quantity<>(2.0, LengthUnit.YARDS), result);
	}

	@Test
	public void testAddition_CrossUnit_CentimeterPlusInch() {
		Quantity<LengthUnit> result = new Quantity<>(2.54, LengthUnit.CENTIMETERS)
				.add(new Quantity<>(1.0, LengthUnit.INCHES));
		assertEquals(new Quantity<>(5.08, LengthUnit.CENTIMETERS), result);
	}

	@Test
	public void testAddition_Commutativity() {
		Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.FEET);
		Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);
		assertEquals(a.add(b), b.add(a).convertTo(LengthUnit.FEET));
	}

	@Test
	public void testAddition_WithZero() {
		Quantity<LengthUnit> result = new Quantity<>(5.0, LengthUnit.FEET).add(new Quantity<>(0.0, LengthUnit.INCHES));
		assertEquals(new Quantity<>(5.0, LengthUnit.FEET), result);
	}

	@Test
	public void testAddition_NegativeValues() {
		Quantity<LengthUnit> result = new Quantity<>(5.0, LengthUnit.FEET).add(new Quantity<>(-2.0, LengthUnit.FEET));
		assertEquals(new Quantity<>(3.0, LengthUnit.FEET), result);
	}

	@Test
	public void testAddition_NullSecondOperandThrows() {
		assertThrows(IllegalArgumentException.class, () -> new Quantity<>(1.0, LengthUnit.FEET).add(null));
	}

	@Test
	public void testAddition_LargeValues() {
		Quantity<LengthUnit> result = new Quantity<>(1e6, LengthUnit.FEET).add(new Quantity<>(1e6, LengthUnit.FEET));
		assertEquals(new Quantity<>(2e6, LengthUnit.FEET), result);
	}

	@Test
	public void testAddition_SmallValues() {
		Quantity<LengthUnit> result = new Quantity<>(0.001, LengthUnit.FEET)
				.add(new Quantity<>(0.002, LengthUnit.FEET));
		assertEquals(new Quantity<>(0.003, LengthUnit.FEET), result);
	}

	@Test
	public void testAddition_ExplicitTargetUnit_Feet() {
		Quantity<LengthUnit> result = new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(12.0, LengthUnit.INCHES),
				LengthUnit.FEET);
		assertEquals(new Quantity<>(2.0, LengthUnit.FEET), result);
	}

	@Test
	public void testAddition_ExplicitTargetUnit_Inches() {
		Quantity<LengthUnit> result = new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(12.0, LengthUnit.INCHES),
				LengthUnit.INCHES);
		assertEquals(new Quantity<>(24.0, LengthUnit.INCHES), result);
	}

	@Test
	public void testAddition_ExplicitTargetUnit_Yards() {
		Quantity<LengthUnit> result = new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(12.0, LengthUnit.INCHES),
				LengthUnit.YARDS);
		assertEquals(new Quantity<>(0.666667, LengthUnit.YARDS), result);
	}

	@Test
	public void testAddition_ExplicitTargetUnit_Centimeters() {
		Quantity<LengthUnit> result = new Quantity<>(1.0, LengthUnit.INCHES).add(new Quantity<>(1.0, LengthUnit.INCHES),
				LengthUnit.CENTIMETERS);
		assertEquals(new Quantity<>(5.079998, LengthUnit.CENTIMETERS), result);
	}

	@Test
	public void testAddition_ExplicitTargetUnit_SameAsFirstOperand() {
		Quantity<LengthUnit> result = new Quantity<>(2.0, LengthUnit.YARDS).add(new Quantity<>(3.0, LengthUnit.FEET),
				LengthUnit.YARDS);
		assertEquals(new Quantity<>(3.0, LengthUnit.YARDS), result);
	}

	@Test
	public void testAddition_ExplicitTargetUnit_SameAsSecondOperand() {
		Quantity<LengthUnit> result = new Quantity<>(2.0, LengthUnit.YARDS).add(new Quantity<>(3.0, LengthUnit.FEET),
				LengthUnit.FEET);
		assertEquals(new Quantity<>(9.0, LengthUnit.FEET), result);
	}

	@Test
	public void testAddition_ExplicitTargetUnit_Commutativity() {
		Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.FEET);
		Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);
		Quantity<LengthUnit> result1 = a.add(b, LengthUnit.YARDS);
		Quantity<LengthUnit> result2 = b.add(a, LengthUnit.YARDS);
		assertEquals(result1, result2);
	}

	@Test
	public void testAddition_ExplicitTargetUnit_WithZero() {
		Quantity<LengthUnit> result = new Quantity<>(5.0, LengthUnit.FEET).add(new Quantity<>(0.0, LengthUnit.INCHES),
				LengthUnit.YARDS);
		assertEquals(new Quantity<>(1.666667, LengthUnit.YARDS), result);
	}

	@Test
	public void testAddition_ExplicitTargetUnit_NegativeValues() {
		Quantity<LengthUnit> result = new Quantity<>(5.0, LengthUnit.FEET).add(new Quantity<>(-2.0, LengthUnit.FEET),
				LengthUnit.INCHES);
		assertEquals(new Quantity<>(36.0, LengthUnit.INCHES), result);
	}

	@Test
	public void testAddition_ExplicitTargetUnit_NullTargetUnitThrows() {
		assertThrows(IllegalArgumentException.class,
				() -> new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(12.0, LengthUnit.INCHES), null));
	}

	@Test
	public void testAddition_ExplicitTargetUnit_LargeToSmallScale() {
		Quantity<LengthUnit> result = new Quantity<>(1000.0, LengthUnit.FEET)
				.add(new Quantity<>(500.0, LengthUnit.FEET), LengthUnit.INCHES);
		assertEquals(new Quantity<>(18000.0, LengthUnit.INCHES), result);
	}

	@Test
	public void testAddition_ExplicitTargetUnit_SmallToLargeScale() {
		Quantity<LengthUnit> result = new Quantity<>(12.0, LengthUnit.INCHES)
				.add(new Quantity<>(12.0, LengthUnit.INCHES), LengthUnit.YARDS);
		assertEquals(new Quantity<>(0.666667, LengthUnit.YARDS), result);
	}

	@Test
	public void testAddition_ExplicitTargetUnit_AllUnitCombinations() {
		Quantity<LengthUnit> result1 = new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(1.0, LengthUnit.YARDS),
				LengthUnit.INCHES);
		assertEquals(new Quantity<>(48.0, LengthUnit.INCHES), result1);

		Quantity<LengthUnit> result2 = new Quantity<>(2.54, LengthUnit.CENTIMETERS)
				.add(new Quantity<>(1.0, LengthUnit.INCHES), LengthUnit.FEET);
		assertEquals(new Quantity<>(0.166667, LengthUnit.FEET), result2);
	}

	@Test
	public void testAddition_ExplicitTargetUnit_PrecisionTolerance() {
		Quantity<LengthUnit> result = new Quantity<>(0.1, LengthUnit.FEET).add(new Quantity<>(0.2, LengthUnit.FEET),
				LengthUnit.INCHES);
		assertEquals(new Quantity<>(3.6, LengthUnit.INCHES), result);
	}
}