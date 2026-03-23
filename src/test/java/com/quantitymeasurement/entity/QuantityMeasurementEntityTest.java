package com.quantitymeasurement.entity;
import com.quantitymeasurement.units.IMeasurable;
import com.quantitymeasurement.units.LengthUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QuantityMeasurementEntityTest
 *
 * Tests the QuantityMeasurementEntity domain object:
 * - String-result constructor (COMPARE / CONVERT operations)
 * - Model-result constructor (ADD / SUBTRACT / DIVIDE operations)
 * - Error constructor (failed operations)
 * - equals() is based on operand values, units, and operation — not results
 * - toString() format for success (resultString), success (resultModel), and error cases
 * - Null operand guard in base constructor
 * - Serializable contract (class implements Serializable)
 */
public class QuantityMeasurementEntityTest {

    // Shared test fixtures
    private QuantityModel<IMeasurable> q1;   // 2.0 FEET
    private QuantityModel<IMeasurable> q2;   // 24.0 INCHES
    private QuantityModel<IMeasurable> result; // 4.0 FEET

    @BeforeEach
    public void setUp() {
        q1     = new QuantityModel<>(2.0,  LengthUnit.FEET);
        q2     = new QuantityModel<>(24.0, LengthUnit.INCHES);
        result = new QuantityModel<>(4.0,  LengthUnit.FEET);
    }

    // =========================================================================
    // STRING-RESULT CONSTRUCTOR (COMPARE / CONVERT)
    // =========================================================================

    @Test
    public void testStringResultConstructor_StoresOperands() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");

        assertEquals(2.0,     entity.thisValue,           1e-6);
        assertEquals("FEET",  entity.thisUnit);
        assertEquals(24.0,    entity.thatValue,           1e-6);
        assertEquals("INCHES", entity.thatUnit);
        assertEquals("COMPARE", entity.operation);
    }

    @Test
    public void testStringResultConstructor_StoresResultString() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        assertEquals("Equal", entity.resultString);
    }

    @Test
    public void testStringResultConstructor_IsNotError() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Not Equal");
        assertFalse(entity.isError);
    }

    // =========================================================================
    // MODEL-RESULT CONSTRUCTOR (ADD / SUBTRACT / DIVIDE)
    // =========================================================================

    @Test
    public void testModelResultConstructor_StoresResultFields() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "ADD", result);

        assertEquals(4.0,          entity.resultValue,           1e-6);
        assertEquals("FEET",       entity.resultUnit);
        assertEquals("LengthUnit", entity.resultMeasurementType);
    }

    @Test
    public void testModelResultConstructor_StoresOperands() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "ADD", result);

        assertEquals(2.0,  entity.thisValue, 1e-6);
        assertEquals(24.0, entity.thatValue, 1e-6);
        assertEquals("ADD", entity.operation);
    }

    @Test
    public void testModelResultConstructor_IsNotError() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "ADD", result);
        assertFalse(entity.isError);
    }

    // =========================================================================
    // ERROR CONSTRUCTOR
    // =========================================================================

    @Test
    public void testErrorConstructor_StoresErrorFlag() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "DIVIDE", "Division by zero", true);
        assertTrue(entity.isError);
    }

    @Test
    public void testErrorConstructor_StoresErrorMessage() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "DIVIDE", "Division by zero", true);
        assertEquals("Division by zero", entity.errorMessage);
    }

    @Test
    public void testErrorConstructor_StoresOperandsAndOperation() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "DIVIDE", "error msg", true);
        assertEquals(2.0,    entity.thisValue,  1e-6);
        assertEquals(24.0,   entity.thatValue,  1e-6);
        assertEquals("DIVIDE", entity.operation);
    }

    // =========================================================================
    // NULL GUARD
    // =========================================================================

    @Test
    public void testNullFirstOperand_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> new QuantityMeasurementEntity(null, q2, "COMPARE", "Equal"));
    }

    @Test
    public void testNullSecondOperand_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> new QuantityMeasurementEntity(q1, null, "COMPARE", "Equal"));
    }

    // =========================================================================
    // equals()
    // =========================================================================

    @Test
    public void testEquals_SameOperandsAndOperation_Equal() {
        QuantityMeasurementEntity e1 =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        QuantityMeasurementEntity e2 =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Not Equal"); // different result
        assertEquals(e1, e2); // result string does NOT affect equality
    }

    @Test
    public void testEquals_DifferentOperation_NotEqual() {
        QuantityMeasurementEntity compare =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        QuantityMeasurementEntity add =
            new QuantityMeasurementEntity(q1, q2, "ADD", result);
        assertNotEquals(compare, add);
    }

    @Test
    public void testEquals_DifferentFirstOperandValue_NotEqual() {
        QuantityModel<IMeasurable> other = new QuantityModel<>(5.0, LengthUnit.FEET);
        QuantityMeasurementEntity e1 = new QuantityMeasurementEntity(q1,    q2, "ADD", result);
        QuantityMeasurementEntity e2 = new QuantityMeasurementEntity(other, q2, "ADD", result);
        assertNotEquals(e1, e2);
    }

    @Test
    public void testEquals_DifferentFirstOperandUnit_NotEqual() {
        QuantityModel<IMeasurable> inFeet   = new QuantityModel<>(2.0, LengthUnit.FEET);
        QuantityModel<IMeasurable> inYards  = new QuantityModel<>(2.0, LengthUnit.YARDS);
        QuantityMeasurementEntity e1 = new QuantityMeasurementEntity(inFeet,  q2, "COMPARE", "x");
        QuantityMeasurementEntity e2 = new QuantityMeasurementEntity(inYards, q2, "COMPARE", "x");
        assertNotEquals(e1, e2);
    }

    @Test
    public void testEquals_Reflexive() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        assertEquals(entity, entity);
    }

    @Test
    public void testEquals_NullComparison_ReturnsFalse() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        assertNotEquals(entity, null);
    }

    @Test
    public void testEquals_DifferentClass_ReturnsFalse() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        assertFalse(entity.equals("some string"));
    }

    // =========================================================================
    // toString()
    // =========================================================================

    @Test
    public void testToString_SuccessWithResultString_ContainsSuccessTag() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        String s = entity.toString();
        assertTrue(s.contains("[SUCCESS]"));
        assertTrue(s.contains("COMPARE"));
        assertTrue(s.contains("Equal"));
    }

    @Test
    public void testToString_SuccessWithResultModel_ContainsResultValue() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "ADD", result);
        String s = entity.toString();
        assertTrue(s.contains("[SUCCESS]"));
        assertTrue(s.contains("ADD"));
        assertTrue(s.contains("4.0"));
        assertTrue(s.contains("FEET"));
    }

    @Test
    public void testToString_Error_ContainsErrorTagAndMessage() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "DIVIDE", "Division by zero", true);
        String s = entity.toString();
        assertTrue(s.contains("[ERROR]"));
        assertTrue(s.contains("Division by zero"));
    }

    @Test
    public void testToString_ContainsBothOperands() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "ADD", result);
        String s = entity.toString();
        assertTrue(s.contains("FEET"));
        assertTrue(s.contains("INCHES"));
    }

    // =========================================================================
    // Serializable contract
    // =========================================================================

    @Test
    public void testImplementsSerializable() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        assertTrue(entity instanceof java.io.Serializable);
    }
}
