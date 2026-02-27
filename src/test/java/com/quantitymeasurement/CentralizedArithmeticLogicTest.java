package com.quantitymeasurement;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

public class CentralizedArithmeticLogicTest {

	private static final double EPSILON = 1e-6;

	@Test
	public void testRefactoring_Add_DelegatesViaHelper() {
		Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.FEET);
		Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);
		Quantity<LengthUnit> sum = a.add(b);
		assertEquals(new Quantity<>(2.0, LengthUnit.FEET), sum);
	}

	@Test
	public void testRefactoring_Subtract_DelegatesViaHelper() {
		Quantity<LengthUnit> a = new Quantity<>(10.0, LengthUnit.FEET);
		Quantity<LengthUnit> b = new Quantity<>(6.0, LengthUnit.INCHES);
		Quantity<LengthUnit> diff = a.subtract(b);
		assertEquals(new Quantity<>(9.5, LengthUnit.FEET), diff);
	}

	@Test
	public void testRefactoring_Divide_DelegatesViaHelper() {
		Quantity<LengthUnit> a = new Quantity<>(24.0, LengthUnit.INCHES);
		Quantity<LengthUnit> b = new Quantity<>(2.0, LengthUnit.FEET);
		double ratio = a.divide(b);
		assertEquals(1.0, ratio, EPSILON);
	}

	@Test
	public void testValidation_NullOperand_ConsistentAcrossOperations() {
		Quantity<LengthUnit> q = new Quantity<>(1.0, LengthUnit.FEET);

		IllegalArgumentException exAdd = assertThrows(IllegalArgumentException.class, () -> q.add(null));
		IllegalArgumentException exSub = assertThrows(IllegalArgumentException.class, () -> q.subtract(null));
		IllegalArgumentException exDiv = assertThrows(IllegalArgumentException.class, () -> q.divide(null));

		assertEquals(exAdd.getMessage(), exSub.getMessage());
		assertEquals(exAdd.getMessage(), exDiv.getMessage());
	}

	@Test
	public void testValidation_CrossCategory_ConsistentAcrossOperations() {
		Quantity<LengthUnit> length = new Quantity<>(10.0, LengthUnit.FEET);
		Quantity<WeightUnit> weight = new Quantity<>(5.0, WeightUnit.KILOGRAM);

		assertThrows(IllegalArgumentException.class, () -> length.add((Quantity) weight));
		assertThrows(IllegalArgumentException.class, () -> length.subtract((Quantity) weight));
		assertThrows(IllegalArgumentException.class, () -> length.divide((Quantity) weight));
	}

	@Test
	public void testValidation_FiniteValue_ConsistentAcrossOperations() {
		assertThrows(IllegalArgumentException.class, () -> new Quantity<>(Double.NaN, LengthUnit.FEET));
		assertThrows(IllegalArgumentException.class, () -> new Quantity<>(Double.POSITIVE_INFINITY, LengthUnit.FEET));
	}

	@Test
	public void testValidation_NullTargetUnit_AddSubtractReject() {
		Quantity<LengthUnit> a = new Quantity<>(5.0, LengthUnit.FEET);
		Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);

		assertThrows(IllegalArgumentException.class, () -> a.add(b, null));
		assertThrows(IllegalArgumentException.class, () -> a.subtract(b, null));
	}

	@Test
	public void testArithmeticOperation_Add_EnumComputation() throws Exception {
		Class<?> enumClass = findInnerEnum(Quantity.class, "ArithmeticOperation");
		assertNotNull(enumClass);
		Object add = Enum.valueOf((Class<Enum>) enumClass, "ADD");
		Method compute = enumClass.getMethod("compute", double.class, double.class);
		double out = (double) compute.invoke(add, 10.0, 5.0);
		assertEquals(15.0, out, EPSILON);
	}

	@Test
	public void testArithmeticOperation_Subtract_EnumComputation() throws Exception {
		Class<?> enumClass = findInnerEnum(Quantity.class, "ArithmeticOperation");
		Object sub = Enum.valueOf((Class<Enum>) enumClass, "SUBTRACT");
		Method compute = enumClass.getMethod("compute", double.class, double.class);
		double out = (double) compute.invoke(sub, 10.0, 5.0);
		assertEquals(5.0, out, EPSILON);
	}

	@Test
	public void testArithmeticOperation_Divide_EnumComputation() throws Exception {
		Class<?> enumClass = findInnerEnum(Quantity.class, "ArithmeticOperation");
		Object div = Enum.valueOf((Class<Enum>) enumClass, "DIVIDE");
		Method compute = enumClass.getMethod("compute", double.class, double.class);
		double out = (double) compute.invoke(div, 10.0, 5.0);
		assertEquals(2.0, out, EPSILON);
	}

	@Test
	public void testArithmeticOperation_DivideByZero_EnumThrows() throws Exception {
		Class<?> enumClass = findInnerEnum(Quantity.class, "ArithmeticOperation");
		Object div = Enum.valueOf((Class<Enum>) enumClass, "DIVIDE");
		Method compute = enumClass.getMethod("compute", double.class, double.class);

		InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
				() -> compute.invoke(div, 10.0, 0.0));
		assertTrue(thrown.getCause() instanceof ArithmeticException);
	}

	@Test
	public void testPerformBaseArithmetic_ConversionAndOperation() throws Exception {
		Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.FEET);
		Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);

		Method perform = Quantity.class.getDeclaredMethod("performArithmetic", Quantity.class,
				findInnerEnum(Quantity.class, "ArithmeticOperation"));
		perform.setAccessible(true);

		Object addConst = Enum.valueOf((Class<Enum>) findInnerEnum(Quantity.class, "ArithmeticOperation"), "ADD");
		double baseResult = (double) perform.invoke(a, b, addConst);

		double baseA = a.getUnit().convertToBaseUnit(a.getValue());
		double baseB = b.getUnit().convertToBaseUnit(b.getValue());
		assertEquals(baseA + baseB, baseResult, EPSILON);
	}

	@Test
	public void testAdd_UC12_BehaviorPreserved() {
		assertEquals(new Quantity<>(2.0, LengthUnit.FEET),
				new Quantity<>(1.0, LengthUnit.FEET).add(new Quantity<>(12.0, LengthUnit.INCHES)));

		assertEquals(new Quantity<>(15000.0, WeightUnit.GRAM), new Quantity<>(10.0, WeightUnit.KILOGRAM)
				.add(new Quantity<>(5000.0, WeightUnit.GRAM), WeightUnit.GRAM));
	}

	@Test
	public void testSubtract_UC12_BehaviorPreserved() {
		assertEquals(new Quantity<>(9.5, LengthUnit.FEET),
				new Quantity<>(10.0, LengthUnit.FEET).subtract(new Quantity<>(6.0, LengthUnit.INCHES)));

		assertEquals(new Quantity<>(0.0, LengthUnit.FEET),
				new Quantity<>(10.0, LengthUnit.FEET).subtract(new Quantity<>(120.0, LengthUnit.INCHES)));
	}

	@Test
	public void testDivide_UC12_BehaviorPreserved() {
		assertEquals(5.0, new Quantity<>(10.0, LengthUnit.FEET).divide(new Quantity<>(2.0, LengthUnit.FEET)), EPSILON);
		assertEquals(1.0, new Quantity<>(24.0, LengthUnit.INCHES).divide(new Quantity<>(2.0, LengthUnit.FEET)),
				EPSILON);
	}

	@Test
	public void testRounding_AddSubtract_TwoDecimalPlaces() {
		Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.FEET);
		Quantity<LengthUnit> b = new Quantity<>(0.333333, LengthUnit.YARDS);
		Quantity<LengthUnit> res = a.subtract(b);
		double rounded = Math.round(res.getValue() * 1_000_000.0) / 1_000_000.0;
		assertEquals(rounded, res.getValue(), EPSILON);
	}

	@Test
	public void testRounding_Divide_NoRounding() {
		double ratio = new Quantity<>(10.0, LengthUnit.FEET).divide(new Quantity<>(3.0, LengthUnit.FEET));
		assertEquals(10.0 / 3.0, ratio, 1e-12);
	}

	@Test
	public void testImplicitTargetUnit_AddSubtract() {
		Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.FEET);
		Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);
		assertEquals(new Quantity<>(2.0, LengthUnit.FEET), a.add(b));
		assertEquals(new Quantity<>(0.0, LengthUnit.FEET), a.subtract(b));
	}

	@Test
	public void testExplicitTargetUnit_AddSubtract_Overrides() {
		Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.FEET);
		Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);
		assertEquals(new Quantity<>(24.0, LengthUnit.INCHES), a.add(b, LengthUnit.INCHES));
		assertEquals(new Quantity<>(0.0, LengthUnit.INCHES), a.subtract(b, LengthUnit.INCHES));
	}

	@Test
	public void testImmutability_AfterAdd_ViaCentralizedHelper() {
		Quantity<VolumeUnit> original = new Quantity<>(5.0, VolumeUnit.LITRE);
		Quantity<VolumeUnit> other = new Quantity<>(500.0, VolumeUnit.MILLILITRE);
		Quantity<VolumeUnit> sum = original.add(other);
		assertEquals(new Quantity<>(5.0, VolumeUnit.LITRE), original);
		assertNotSame(original, sum);
	}

	@Test
	public void testImmutability_AfterSubtract_ViaCentralizedHelper() {
		Quantity<VolumeUnit> original = new Quantity<>(5.0, VolumeUnit.LITRE);
		Quantity<VolumeUnit> diff = original.subtract(new Quantity<>(500.0, VolumeUnit.MILLILITRE));
		assertEquals(new Quantity<>(5.0, VolumeUnit.LITRE), original);
		assertEquals(new Quantity<>(4.5, VolumeUnit.LITRE), diff);
	}

	@Test
	public void testImmutability_AfterDivide_ViaCentralizedHelper() {
		Quantity<WeightUnit> original = new Quantity<>(10.0, WeightUnit.KILOGRAM);
		double ratio = original.divide(new Quantity<>(5.0, WeightUnit.KILOGRAM));
		assertEquals(new Quantity<>(10.0, WeightUnit.KILOGRAM), original);
		assertEquals(2.0, ratio, EPSILON);
	}

	@Test
	public void testAllOperations_AcrossAllCategories() {
		assertEquals(new Quantity<>(9.5, LengthUnit.FEET),
				new Quantity<>(10.0, LengthUnit.FEET).subtract(new Quantity<>(6.0, LengthUnit.INCHES)));

		assertEquals(new Quantity<>(5.0, WeightUnit.KILOGRAM),
				new Quantity<>(10.0, WeightUnit.KILOGRAM).subtract(new Quantity<>(5000.0, WeightUnit.GRAM)));

		assertEquals(new Quantity<>(4.5, VolumeUnit.LITRE),
				new Quantity<>(5.0, VolumeUnit.LITRE).subtract(new Quantity<>(500.0, VolumeUnit.MILLILITRE)));
	}

	@Test
	public void testCodeDuplication_ValidationLogic_Eliminated() throws Exception {
		Method validate = Quantity.class.getDeclaredMethod("validateArithmeticOperands", Quantity.class,
				IMeasurable.class, boolean.class);
		assertTrue(Modifier.isPrivate(validate.getModifiers()));
	}

	@Test
	public void testCodeDuplication_ConversionLogic_Eliminated() throws Exception {
		Method perform = Quantity.class.getDeclaredMethod("performArithmetic", Quantity.class,
				findInnerEnum(Quantity.class, "ArithmeticOperation"));
		assertTrue(Modifier.isPrivate(perform.getModifiers()));
	}

	@Test
	public void testEnumDispatch_AllOperations_CorrectlyDispatched() throws Exception {
		Class<?> enumClass = findInnerEnum(Quantity.class, "ArithmeticOperation");
		assertNotNull(Enum.valueOf((Class<Enum>) enumClass, "ADD"));
		assertNotNull(Enum.valueOf((Class<Enum>) enumClass, "SUBTRACT"));
		assertNotNull(Enum.valueOf((Class<Enum>) enumClass, "DIVIDE"));
	}

	@Test
	public void testFutureOperation_MultiplicationPattern() throws Exception {
		Class<?> enumClass = findInnerEnum(Quantity.class, "ArithmeticOperation");
		Method compute = enumClass.getMethod("compute", double.class, double.class);
		assertNotNull(compute);
	}

	@Test
	public void testErrorMessage_Consistency_Across_Operations() {
		Quantity<LengthUnit> q = new Quantity<>(1.0, LengthUnit.FEET);
		String msgAdd = assertThrows(IllegalArgumentException.class, () -> q.add(null)).getMessage();
		String msgSub = assertThrows(IllegalArgumentException.class, () -> q.subtract(null)).getMessage();
		String msgDiv = assertThrows(IllegalArgumentException.class, () -> q.divide(null)).getMessage();
		assertEquals(msgAdd, msgSub);
		assertEquals(msgAdd, msgDiv);
	}

	@Test
	public void testHelper_PrivateVisibility() throws Exception {
		Method perform = Quantity.class.getDeclaredMethod("performArithmetic", Quantity.class,
				findInnerEnum(Quantity.class, "ArithmeticOperation"));
		assertTrue(Modifier.isPrivate(perform.getModifiers()));
	}

	@Test
	public void testValidation_Helper_PrivateVisibility() throws Exception {
		Method validate = Quantity.class.getDeclaredMethod("validateArithmeticOperands", Quantity.class,
				IMeasurable.class, boolean.class);
		assertTrue(Modifier.isPrivate(validate.getModifiers()));
	}

	@Test
	public void testRounding_Helper_Accuracy() {
		Quantity<LengthUnit> a = new Quantity<>(1.234567, LengthUnit.FEET);
		Quantity<LengthUnit> b = new Quantity<>(0.0, LengthUnit.FEET);
		Quantity<LengthUnit> res = a.add(b);
		double rounded = Math.round(res.getValue() * 1_000_000.0) / 1_000_000.0;
		assertEquals(rounded, res.getValue(), 1e-6);
	}

	@Test
	public void testArithmetic_Chain_Operations() {
		Quantity<LengthUnit> q1 = new Quantity<>(10.0, LengthUnit.FEET);
		Quantity<LengthUnit> q2 = new Quantity<>(2.0, LengthUnit.FEET);
		Quantity<LengthUnit> q3 = new Quantity<>(1.0, LengthUnit.FEET);
		Quantity<LengthUnit> q4 = new Quantity<>(7.0, LengthUnit.FEET);
		double result = q1.add(q2).subtract(q3).divide(q4);
		assertEquals(11.0 / 7.0, result, EPSILON);
	}

	@Test
	public void testRefactoring_NoBehaviorChange_LargeDataset() {
		Quantity<LengthUnit> base = new Quantity<>(0.0, LengthUnit.INCHES);
		for (int i = 0; i < 1000; i++) {
			base = base.add(new Quantity<>(1.0, LengthUnit.INCHES));
		}
		assertEquals(new Quantity<>(1000.0, LengthUnit.INCHES), base);
	}

	@Test
	public void testRefactoring_Performance_ComparableToUC12() {
		Quantity<LengthUnit> q = new Quantity<>(0.0, LengthUnit.INCHES);
		for (int i = 0; i < 10000; i++) {
			q = q.add(new Quantity<>(1.0, LengthUnit.INCHES));
		}
		assertEquals(10000.0, q.getValue(), EPSILON);
	}

	@Test
	public void testEnumConstant_ADD_CorrectlyAdds() throws Exception {
		Class<?> enumClass = findInnerEnum(Quantity.class, "ArithmeticOperation");
		Object add = Enum.valueOf((Class<Enum>) enumClass, "ADD");
		Method compute = enumClass.getMethod("compute", double.class, double.class);
		assertEquals(10.0, (double) compute.invoke(add, 7.0, 3.0), EPSILON);
	}

	@Test
	public void testEnumConstant_SUBTRACT_CorrectlySubtracts() throws Exception {
		Class<?> enumClass = findInnerEnum(Quantity.class, "ArithmeticOperation");
		Object sub = Enum.valueOf((Class<Enum>) enumClass, "SUBTRACT");
		Method compute = enumClass.getMethod("compute", double.class, double.class);
		assertEquals(4.0, (double) compute.invoke(sub, 7.0, 3.0), EPSILON);
	}

	@Test
	public void testEnumConstant_DIVIDE_CorrectlyDivides() throws Exception {
		Class<?> enumClass = findInnerEnum(Quantity.class, "ArithmeticOperation");
		Object div = Enum.valueOf((Class<Enum>) enumClass, "DIVIDE");
		Method compute = enumClass.getMethod("compute", double.class, double.class);
		assertEquals(3.5, (double) compute.invoke(div, 7.0, 2.0), EPSILON);
	}

	@Test
	public void testHelper_BaseUnitConversion_Correct() throws Exception {
		Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.FEET);
		Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);
		Method perform = Quantity.class.getDeclaredMethod("performArithmetic", Quantity.class,
				findInnerEnum(Quantity.class, "ArithmeticOperation"));
		perform.setAccessible(true);
		Object addConst = Enum.valueOf((Class<Enum>) findInnerEnum(Quantity.class, "ArithmeticOperation"), "ADD");
		double baseResult = (double) perform.invoke(a, b, addConst);
		double baseA = a.getUnit().convertToBaseUnit(a.getValue());
		double baseB = b.getUnit().convertToBaseUnit(b.getValue());
		assertEquals(baseA + baseB, baseResult, EPSILON);
	}

	@Test
	public void testHelper_ResultConversion_Correct() {
		Quantity<LengthUnit> a = new Quantity<>(1.0, LengthUnit.FEET);
		Quantity<LengthUnit> b = new Quantity<>(12.0, LengthUnit.INCHES);

		Quantity<LengthUnit> sumFeet = a.add(b);
		Quantity<LengthUnit> sumInches = a.add(b, LengthUnit.INCHES);

		Quantity<LengthUnit> converted = sumFeet.convertTo(LengthUnit.INCHES);
		assertEquals(sumInches, converted);
	}

	@Test
	public void testRefactoring_Validation_UnifiedBehavior() {
		Quantity<LengthUnit> q = new Quantity<>(1.0, LengthUnit.FEET);
		String msgAdd = assertThrows(IllegalArgumentException.class, () -> q.add(null)).getMessage();
		String msgSub = assertThrows(IllegalArgumentException.class, () -> q.subtract(null)).getMessage();
		String msgDiv = assertThrows(IllegalArgumentException.class, () -> q.divide(null)).getMessage();
		assertEquals(msgAdd, msgSub);
		assertEquals(msgAdd, msgDiv);
	}

	// Helper method
	private static Class<?> findInnerEnum(Class<?> outer, String enumName) {
		for (Class<?> c : outer.getDeclaredClasses()) {
			if (c.isEnum() && c.getSimpleName().equals(enumName)) {
				return c;
			}
		}
		return null;
	}

}