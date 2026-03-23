package com.quantitymeasurement.integration;

import com.quantitymeasurement.QuantityMeasurementApp;
import com.quantitymeasurement.controller.QuantityMeasurementController;
import com.quantitymeasurement.entity.QuantityDTO;
import com.quantitymeasurement.repository.IQuantityMeasurementRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QuantityMeasurementIntegrationTest
 *
 * End-to-end integration tests for the Quantity Measurement Application.
 *
 * Tests the full stack:
 * QuantityMeasurementApp → Controller → Service → Repository (H2)
 *
 * Verifies:
 * - Comparison operations produce correct boolean results end-to-end
 * - Conversion operations produce correct converted values end-to-end
 * - Addition operations persist results to the repository
 * - Multiple consecutive operations all get stored correctly
 * - Weight equality works across different units
 * - Temperature arithmetic is handled gracefully without crashing
 *
 * Uses the singleton QuantityMeasurementApp so the full initialization path
 * including ApplicationConfig and the database connection pool is exercised.
 *
 * @author Developer
 * @version 16.0
 */
public class QuantityMeasurementIntegrationTest {

    private QuantityMeasurementApp app;
    private QuantityMeasurementController controller;
    private IQuantityMeasurementRepository repository;

    /**
     * Points ApplicationConfig to the test H2 in-memory database
     * before the first test in this class runs. Runs once per class.
     */
    @BeforeAll
    public static void setUpTestEnvironment() {
        System.setProperty("app.env", "test");
    }

    /**
     * Gets the singleton app instance and cleans the repository before each test.
     * Accessing controller and repository through the app ensures the same
     * wiring used in production is exercised.
     */
    @BeforeEach
    public void setUp() {
        app        = QuantityMeasurementApp.getInstance();
        controller = app.getController();
        repository = app.getRepository();
        repository.deleteAll();
    }

    /**
     * Cleans the repository after each test for isolation.
     */
    @AfterEach
    public void tearDown() {
        repository.deleteAll();
    }

    /* =========================================================================
     * End-to-end comparison
     * ====================================================================== */

    /**
     * End-to-end: 1 FOOT == 12 INCHES should return true through the full stack.
     */
    @Test
    public void testEndToEndLengthComparison() {
        QuantityDTO q1 = new QuantityDTO(1.0,  "FEET",   "LengthUnit");
        QuantityDTO q2 = new QuantityDTO(12.0, "INCHES", "LengthUnit");

        boolean result = controller.performComparison(q1, q2);

        assertTrue(result, "1 FOOT should equal 12 INCHES end-to-end");
    }

    /* =========================================================================
     * End-to-end conversion
     * ====================================================================== */

    /**
     * End-to-end: 0°C converted to °F should equal 32°F through the full stack.
     */
    @Test
    public void testEndToEndTemperatureConversion() {
        QuantityDTO thisDto = new QuantityDTO(0.0, "CELSIUS",    "TemperatureUnit");
        QuantityDTO thatDto = new QuantityDTO(0.0, "FAHRENHEIT", "TemperatureUnit");

        QuantityDTO resultDTO = controller.performConversion(thisDto, thatDto);

        assertNotNull(resultDTO, "Conversion result should not be null");
        assertEquals(32.0, resultDTO.getValue(), 0.01, "0°C should convert to 32°F");
    }

    /* =========================================================================
     * Persistence
     * ====================================================================== */

    /**
     * End-to-end: adding two length quantities should persist the result to the repository.
     */
    @Test
    public void testRepositoryPersistence() {
        QuantityDTO q1 = new QuantityDTO(5.0, "FEET", "LengthUnit");
        QuantityDTO q2 = new QuantityDTO(5.0, "FEET", "LengthUnit");

        controller.performAddition(q1, q2);

        assertTrue(repository.getAllMeasurements().size() > 0,
            "Repository should have at least 1 record after performAddition");
    }

    /**
     * End-to-end: multiple consecutive operations should all be persisted.
     * Comparison, addition, subtraction, and division — each should produce one record.
     */
    @Test
    public void testMultipleOperationsPersisted() {
        QuantityDTO q1 = new QuantityDTO(2.0,  "FEET",   "LengthUnit");
        QuantityDTO q2 = new QuantityDTO(24.0, "INCHES", "LengthUnit");

        controller.performComparison(q1, q2);
        controller.performAddition(q1, q2);
        controller.performSubtraction(q1, q2);
        controller.performDivision(q1, q2);

        assertEquals(4, repository.getTotalCount(),
            "All 4 operations should each produce one stored record");
    }

    /* =========================================================================
     * Cross-unit equality
     * ====================================================================== */

    /**
     * End-to-end: 1 KILOGRAM should equal 1000 GRAM.
     */
    @Test
    public void testWeightComparison_KilogramEqualsGram() {
        QuantityDTO kg = new QuantityDTO(1.0,    "KILOGRAM", "WeightUnit");
        QuantityDTO g  = new QuantityDTO(1000.0, "GRAM",     "WeightUnit");

        assertTrue(controller.performComparison(kg, g),
            "1 KILOGRAM should equal 1000 GRAM");
    }

    /* =========================================================================
     * Error handling
     * ====================================================================== */

    /**
     * End-to-end: temperature addition should be handled gracefully.
     * demonstrateAddition() catches the UnsupportedOperationException internally
     * and prints an error message — it must not propagate the exception to the test.
     */
    @Test
    public void testTemperatureArithmeticHandledGracefully() {
        QuantityDTO t1 = new QuantityDTO(10, "CELSIUS", "TemperatureUnit");
        QuantityDTO t2 = new QuantityDTO(20, "CELSIUS", "TemperatureUnit");

        /*
         * demonstrateAddition catches UnsupportedOperationException and prints
         * an error message. No exception should reach this test.
         */
        assertDoesNotThrow(() -> controller.demonstrateAddition(t1, t2),
            "Temperature arithmetic should be handled gracefully without crashing");
    }
}