package com.quantitymeasurement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InchesTest {

	@Test
    void testEquality_SameReference() {
        Inches i1 = new Inches(4.0);
        assertTrue(i1.equals(i1));
    }

    @Test
    void testEquality_NonNumericInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Inches(Double.NaN);
        });
    }
    
}