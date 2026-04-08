package com.app.quantitymeasurement.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class QuantityMeasurementExceptionTest {

    // =========================================================================
    // CLASS CONTRACT
    // =========================================================================

    @Test
    void testExtendsRuntimeException() {
        assertTrue(new QuantityMeasurementException("msg") instanceof RuntimeException);
    }

    @Test
    void testIsUnchecked_ExtendsRuntimeException() {
        // Verified via class hierarchy - not by instanceof on an instance
        assertTrue(RuntimeException.class.isAssignableFrom(
            QuantityMeasurementException.class));
        assertFalse(
            // Must not directly extend Exception (which would make it checked)
            QuantityMeasurementException.class.getSuperclass().equals(Exception.class));
    }

    // =========================================================================
    // MESSAGE-ONLY CONSTRUCTOR
    // =========================================================================

    @Test
    void testConstructor_Message_StoresMessage() {
        QuantityMeasurementException ex =
            new QuantityMeasurementException("Invalid unit provided");
        assertEquals("Invalid unit provided", ex.getMessage());
    }

    @Test
    void testConstructor_Message_CauseIsNull() {
        QuantityMeasurementException ex =
            new QuantityMeasurementException("some error");
        assertNull(ex.getCause());
    }

    @Test
    void testConstructor_EmptyMessage_Stored() {
        QuantityMeasurementException ex = new QuantityMeasurementException("");
        assertEquals("", ex.getMessage());
    }

    // =========================================================================
    // MESSAGE + CAUSE CONSTRUCTOR
    // =========================================================================

    @Test
    void testConstructor_MessageAndCause_StoresBoth() {
        Throwable cause = new IllegalArgumentException("root cause");
        QuantityMeasurementException ex =
            new QuantityMeasurementException("wrapper message", cause);
        assertEquals("wrapper message", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void testConstructor_MessageAndCause_CauseMessageAccessible() {
        Throwable cause = new ArithmeticException("division by zero");
        QuantityMeasurementException ex =
            new QuantityMeasurementException("arithmetic error", cause);
        assertEquals("division by zero", ex.getCause().getMessage());
    }

    // =========================================================================
    // THROW AND CATCH BEHAVIOUR
    // =========================================================================

    @Test
    void testThrowAndCatch_MessageOnly() {
        QuantityMeasurementException caught = assertThrows(
            QuantityMeasurementException.class,
            () -> { throw new QuantityMeasurementException("test error"); }
        );
        assertEquals("test error", caught.getMessage());
    }

    @Test
    void testThrowAndCatch_WithCause() {
        IllegalArgumentException root = new IllegalArgumentException("bad arg");
        QuantityMeasurementException caught = assertThrows(
            QuantityMeasurementException.class,
            () -> { throw new QuantityMeasurementException("wrapper", root); }
        );
        assertSame(root, caught.getCause());
    }

    @Test
    void testCaughtAsRuntimeException() {
        // Confirms it can be caught as the parent type
        assertThrows(
            RuntimeException.class,
            () -> { throw new QuantityMeasurementException("caught as RuntimeException"); }
        );
    }
}
