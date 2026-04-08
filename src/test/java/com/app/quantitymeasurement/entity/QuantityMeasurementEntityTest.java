package com.app.quantitymeasurement.entity;

import com.app.quantitymeasurement.entity.QuantityMeasurementEntity;
import com.app.quantitymeasurement.model.QuantityModel;
import com.app.quantitymeasurement.unit.IMeasurable;
import com.app.quantitymeasurement.unit.LengthUnit;
import com.app.quantitymeasurement.unit.WeightUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class QuantityMeasurementEntityTest {

    private QuantityModel<IMeasurable> q1;      // 2.0 FEET
    private QuantityModel<IMeasurable> q2;      // 24.0 INCHES
    private QuantityModel<IMeasurable> result;  // 4.0 FEET

    @BeforeEach
    void setUp() {
        q1     = new QuantityModel<>(2.0,  LengthUnit.FEET);
        q2     = new QuantityModel<>(24.0, LengthUnit.INCHES);
        result = new QuantityModel<>(4.0,  LengthUnit.FEET);
    }

    // =========================================================================
    // STRING-RESULT CONSTRUCTOR (COMPARE / CONVERT)
    // =========================================================================

    @Test
    void testStringResultConstructor_StoresOperands() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");

        assertEquals(2.0,      entity.getThisValue(), 1e-6);
        assertEquals("FEET",   entity.getThisUnit());
        assertEquals(24.0,     entity.getThatValue(), 1e-6);
        assertEquals("INCHES", entity.getThatUnit());
        assertEquals("COMPARE", entity.getOperation());
    }

    @Test
    void testStringResultConstructor_StoresResultString() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        assertEquals("Equal", entity.getResultString());
    }

    @Test
    void testStringResultConstructor_IsNotError() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Not Equal");
        assertFalse(entity.isError());
    }

    @Test
    void testStringResultConstructor_MeasurementType_Stored() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        assertEquals("LengthUnit", entity.getThisMeasurementType());
        assertEquals("LengthUnit", entity.getThatMeasurementType());
    }

    // =========================================================================
    // MODEL-RESULT CONSTRUCTOR (ADD / SUBTRACT / DIVIDE)
    // =========================================================================

    @Test
    void testModelResultConstructor_StoresResultFields() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "ADD", result);

        assertEquals(4.0,          entity.getResultValue(), 1e-6);
        assertEquals("FEET",       entity.getResultUnit());
        assertEquals("LengthUnit", entity.getResultMeasurementType());
    }

    @Test
    void testModelResultConstructor_StoresOperands() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "ADD", result);

        assertEquals(2.0,   entity.getThisValue(), 1e-6);
        assertEquals(24.0,  entity.getThatValue(), 1e-6);
        assertEquals("ADD", entity.getOperation());
    }

    @Test
    void testModelResultConstructor_IsNotError() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "ADD", result);
        assertFalse(entity.isError());
    }

    @Test
    void testModelResultConstructor_NullResultString() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "ADD", result);
        assertNull(entity.getResultString());
    }

    // =========================================================================
    // ERROR CONSTRUCTOR
    // =========================================================================

    @Test
    void testErrorConstructor_StoresErrorFlag() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "DIVIDE", "Division by zero", true);
        assertTrue(entity.isError());
    }

    @Test
    void testErrorConstructor_StoresErrorMessage() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "DIVIDE", "Division by zero", true);
        assertEquals("Division by zero", entity.getErrorMessage());
    }

    @Test
    void testErrorConstructor_StoresOperandsAndOperation() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "DIVIDE", "error msg", true);
        assertEquals(2.0,      entity.getThisValue(),  1e-6);
        assertEquals(24.0,     entity.getThatValue(),  1e-6);
        assertEquals("DIVIDE", entity.getOperation());
    }

    @Test
    void testErrorConstructor_NoResultValue() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "DIVIDE", "error", true);
        assertNull(entity.getResultValue());
        assertNull(entity.getResultUnit());
    }

    // =========================================================================
    // NULL GUARD
    // =========================================================================

    @Test
    void testNullFirstOperand_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> new QuantityMeasurementEntity(null, q2, "COMPARE", "Equal"));
    }

    @Test
    void testNullSecondOperand_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> new QuantityMeasurementEntity(q1, null, "COMPARE", "Equal"));
    }

    // =========================================================================
    // equals() — Lombok @Data compares all fields
    // =========================================================================

    @Test
    void testEquals_SameOperandsAndOperation_DifferentResult_Equal() {
        // equals() is based on operands + operation only — result string does NOT affect equality
        QuantityMeasurementEntity e1 =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        QuantityMeasurementEntity e2 =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Not Equal");
        assertEquals(e1, e2);
    }

    @Test
    void testEquals_DifferentOperation_NotEqual() {
        QuantityMeasurementEntity compare =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        QuantityMeasurementEntity add =
            new QuantityMeasurementEntity(q1, q2, "ADD", result);
        assertNotEquals(compare, add);
    }

    @Test
    void testEquals_DifferentFirstOperandValue_NotEqual() {
        QuantityModel<IMeasurable> other = new QuantityModel<>(5.0, LengthUnit.FEET);
        QuantityMeasurementEntity e1 = new QuantityMeasurementEntity(q1,    q2, "ADD", result);
        QuantityMeasurementEntity e2 = new QuantityMeasurementEntity(other, q2, "ADD", result);
        assertNotEquals(e1, e2);
    }

    @Test
    void testEquals_DifferentFirstOperandUnit_NotEqual() {
        QuantityModel<IMeasurable> inFeet  = new QuantityModel<>(2.0, LengthUnit.FEET);
        QuantityModel<IMeasurable> inYards = new QuantityModel<>(2.0, LengthUnit.YARDS);
        QuantityMeasurementEntity e1 = new QuantityMeasurementEntity(inFeet,  q2, "COMPARE", "x");
        QuantityMeasurementEntity e2 = new QuantityMeasurementEntity(inYards, q2, "COMPARE", "x");
        assertNotEquals(e1, e2);
    }

    @Test
    void testEquals_Reflexive() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        assertEquals(entity, entity);
    }

    @Test
    void testEquals_NullComparison_ReturnsFalse() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        assertNotEquals(entity, null);
    }

    @Test
    void testEquals_DifferentClass_ReturnsFalse() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        assertFalse(entity.equals("some string"));
    }

    // =========================================================================
    // toString()
    // =========================================================================

    @Test
    void testToString_SuccessWithResultString_ContainsSuccessTag() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        String s = entity.toString();
        assertTrue(s.contains("[SUCCESS]"));
        assertTrue(s.contains("COMPARE"));
        assertTrue(s.contains("Equal"));
    }

    @Test
    void testToString_SuccessWithResultModel_ContainsResultValue() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "ADD", result);
        String s = entity.toString();
        assertTrue(s.contains("[SUCCESS]"));
        assertTrue(s.contains("ADD"));
        assertTrue(s.contains("4.0"));
        assertTrue(s.contains("FEET"));
    }

    @Test
    void testToString_Error_ContainsErrorTagAndMessage() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "DIVIDE", "Division by zero", true);
        String s = entity.toString();
        assertTrue(s.contains("[ERROR]"));
        assertTrue(s.contains("Division by zero"));
    }

    @Test
    void testToString_ContainsBothOperands() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "ADD", result);
        String s = entity.toString();
        assertTrue(s.contains("FEET"));
        assertTrue(s.contains("INCHES"));
    }

    // =========================================================================
    // Serializable
    // =========================================================================

    @Test
    void testImplementsSerializable() {
        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        assertTrue(entity instanceof java.io.Serializable);
    }
}
