package com.quantitymeasurement.service;
import com.quantitymeasurement.controller.QuantityMeasurementController;
import com.quantitymeasurement.dto.QuantityDTO;
import com.quantitymeasurement.repository.IQuantityMeasurementRepository;
import com.quantitymeasurement.repository.QuantityMeasurementCacheRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QuantityMeasurementServiceTest
 *
 * Integration tests for the Service Layer operating through the Controller.
 * Uses QuantityDTO objects as input and verifies that all operations
 * (compare, convert, add, subtract, divide) produce correct results
 * across all measurement categories.
 *
 * Each test creates a fresh service and controller to avoid shared state.
 */
public class QuantityMeasurementServiceTest {

    private static final double EPSILON = 1e-6;

    private QuantityMeasurementController controller;

    @BeforeEach
    public void setUp() {
        IQuantityMeasurementRepository repository =
            QuantityMeasurementCacheRepository.getInstance();
        IQuantityMeasurementService service =
            new QuantityMeasurementServiceImpl(repository);
        controller = new QuantityMeasurementController(service);
    }

    // =========================================================================
    // COMPARISON
    // =========================================================================

    @Test
    public void testCompare_Length_FeetVsInches_Equal() {
        assertTrue(controller.performComparison(
            new QuantityDTO(2,  QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(24, QuantityDTO.LengthUnit.INCHES)
        ));
    }

    @Test
    public void testCompare_Length_FeetVsInches_NotEqual() {
        assertFalse(controller.performComparison(
            new QuantityDTO(1,  QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(24, QuantityDTO.LengthUnit.INCHES)
        ));
    }

    @Test
    public void testCompare_Length_YardVsFeet_Equal() {
        assertTrue(controller.performComparison(
            new QuantityDTO(1, QuantityDTO.LengthUnit.YARDS),
            new QuantityDTO(3, QuantityDTO.LengthUnit.FEET)
        ));
    }

    @Test
    public void testCompare_Weight_KilogramVsGram_Equal() {
        assertTrue(controller.performComparison(
            new QuantityDTO(1,    QuantityDTO.WeightUnit.KILOGRAM),
            new QuantityDTO(1000, QuantityDTO.WeightUnit.GRAM)
        ));
    }

    @Test
    public void testCompare_Volume_LitreVsMillilitre_Equal() {
        assertTrue(controller.performComparison(
            new QuantityDTO(1,    QuantityDTO.VolumeUnit.LITRE),
            new QuantityDTO(1000, QuantityDTO.VolumeUnit.MILLILITRE)
        ));
    }

    @Test
    public void testCompare_Temperature_CelsiusVsFahrenheit_Equal() {
        assertTrue(controller.performComparison(
            new QuantityDTO(0,  QuantityDTO.TemperatureUnit.CELSIUS),
            new QuantityDTO(32, QuantityDTO.TemperatureUnit.FAHRENHEIT)
        ));
    }

    @Test
    public void testCompare_Temperature_100C_vs_212F_Equal() {
        assertTrue(controller.performComparison(
            new QuantityDTO(100, QuantityDTO.TemperatureUnit.CELSIUS),
            new QuantityDTO(212, QuantityDTO.TemperatureUnit.FAHRENHEIT)
        ));
    }

    // =========================================================================
    // CONVERSION
    // =========================================================================

    @Test
    public void testConvert_Length_InchesToYards() {
        QuantityDTO result = controller.performConversion(
            new QuantityDTO(24, QuantityDTO.LengthUnit.INCHES),
            new QuantityDTO(0,  QuantityDTO.LengthUnit.YARDS)
        );
        assertEquals(0.666667, result.getValue(), EPSILON);
        assertEquals("YARDS", result.getUnit());
    }

    @Test
    public void testConvert_Length_FeetToInches() {
        QuantityDTO result = controller.performConversion(
            new QuantityDTO(2, QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(0, QuantityDTO.LengthUnit.INCHES)
        );
        assertEquals(24.0, result.getValue(), EPSILON);
        assertEquals("INCHES", result.getUnit());
    }

    @Test
    public void testConvert_Weight_KilogramToPound() {
        QuantityDTO result = controller.performConversion(
            new QuantityDTO(1, QuantityDTO.WeightUnit.KILOGRAM),
            new QuantityDTO(0, QuantityDTO.WeightUnit.POUND)
        );
        assertEquals(2.204624, result.getValue(), EPSILON);
        assertEquals("POUND", result.getUnit());
    }

    @Test
    public void testConvert_Volume_LitreToMillilitre() {
        QuantityDTO result = controller.performConversion(
            new QuantityDTO(1, QuantityDTO.VolumeUnit.LITRE),
            new QuantityDTO(0, QuantityDTO.VolumeUnit.MILLILITRE)
        );
        assertEquals(1000.0, result.getValue(), EPSILON);
        assertEquals("MILLILITRE", result.getUnit());
    }

    @Test
    public void testConvert_Temperature_CelsiusToFahrenheit() {
        QuantityDTO result = controller.performConversion(
            new QuantityDTO(100, QuantityDTO.TemperatureUnit.CELSIUS),
            new QuantityDTO(0,   QuantityDTO.TemperatureUnit.FAHRENHEIT)
        );
        assertEquals(212.0, result.getValue(), EPSILON);
        assertEquals("FAHRENHEIT", result.getUnit());
    }

    @Test
    public void testConvert_Temperature_FahrenheitToCelsius() {
        QuantityDTO result = controller.performConversion(
            new QuantityDTO(32, QuantityDTO.TemperatureUnit.FAHRENHEIT),
            new QuantityDTO(0,  QuantityDTO.TemperatureUnit.CELSIUS)
        );
        assertEquals(0.0, result.getValue(), EPSILON);
        assertEquals("CELSIUS", result.getUnit());
    }

    // =========================================================================
    // ADDITION
    // =========================================================================

    @Test
    public void testAdd_Length_FeetPlusInches_DefaultUnit() {
        QuantityDTO result = controller.performAddition(
            new QuantityDTO(2,  QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(24, QuantityDTO.LengthUnit.INCHES)
        );
        assertEquals(4.0,   result.getValue(), EPSILON);
        assertEquals("FEET", result.getUnit());
    }

    @Test
    public void testAdd_Length_FeetPlusInches_TargetYards() {
        QuantityDTO result = controller.performAddition(
            new QuantityDTO(2,  QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(24, QuantityDTO.LengthUnit.INCHES),
            new QuantityDTO(0,  QuantityDTO.LengthUnit.YARDS)
        );
        assertEquals(1.333333, result.getValue(), EPSILON);
        assertEquals("YARDS", result.getUnit());
    }

    @Test
    public void testAdd_Weight_KilogramPlusGram() {
        QuantityDTO result = controller.performAddition(
            new QuantityDTO(1,    QuantityDTO.WeightUnit.KILOGRAM),
            new QuantityDTO(1000, QuantityDTO.WeightUnit.GRAM)
        );
        assertEquals(2.0,        result.getValue(), EPSILON);
        assertEquals("KILOGRAM", result.getUnit());
    }

    @Test
    public void testAdd_Volume_LitrePlusMillilitre() {
        QuantityDTO result = controller.performAddition(
            new QuantityDTO(1,    QuantityDTO.VolumeUnit.LITRE),
            new QuantityDTO(1000, QuantityDTO.VolumeUnit.MILLILITRE)
        );
        assertEquals(2.0,     result.getValue(), EPSILON);
        assertEquals("LITRE", result.getUnit());
    }

    // =========================================================================
    // SUBTRACTION
    // =========================================================================

    @Test
    public void testSubtract_Length_FeetMinusInches_DefaultUnit() {
        QuantityDTO result = controller.performSubtraction(
            new QuantityDTO(2,  QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(24, QuantityDTO.LengthUnit.INCHES)
        );
        assertEquals(0.0,   result.getValue(), EPSILON);
        assertEquals("FEET", result.getUnit());
    }

    @Test
    public void testSubtract_Length_FeetMinusInches_TargetYards() {
        QuantityDTO result = controller.performSubtraction(
            new QuantityDTO(2,  QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(24, QuantityDTO.LengthUnit.INCHES),
            new QuantityDTO(0,  QuantityDTO.LengthUnit.YARDS)
        );
        assertEquals(0.0,    result.getValue(), EPSILON);
        assertEquals("YARDS", result.getUnit());
    }

    @Test
    public void testSubtract_Weight_KilogramMinusGram() {
        QuantityDTO result = controller.performSubtraction(
            new QuantityDTO(2,    QuantityDTO.WeightUnit.KILOGRAM),
            new QuantityDTO(500,  QuantityDTO.WeightUnit.GRAM)
        );
        assertEquals(1.5,        result.getValue(), EPSILON);
        assertEquals("KILOGRAM", result.getUnit());
    }

    @Test
    public void testSubtract_Volume_LitreMinusMillilitre() {
        QuantityDTO result = controller.performSubtraction(
            new QuantityDTO(5,    QuantityDTO.VolumeUnit.LITRE),
            new QuantityDTO(500,  QuantityDTO.VolumeUnit.MILLILITRE)
        );
        assertEquals(4.5,     result.getValue(), EPSILON);
        assertEquals("LITRE", result.getUnit());
    }

    // =========================================================================
    // DIVISION
    // =========================================================================

    @Test
    public void testDivide_Length_FeetOverInches_Equal() {
        double result = controller.performDivision(
            new QuantityDTO(2,  QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(24, QuantityDTO.LengthUnit.INCHES)
        );
        assertEquals(1.0, result, EPSILON);
    }

    @Test
    public void testDivide_Length_FeetOverFeet() {
        double result = controller.performDivision(
            new QuantityDTO(4, QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(2, QuantityDTO.LengthUnit.FEET)
        );
        assertEquals(2.0, result, EPSILON);
    }

    @Test
    public void testDivide_Weight_KilogramOverKilogram() {
        double result = controller.performDivision(
            new QuantityDTO(10, QuantityDTO.WeightUnit.KILOGRAM),
            new QuantityDTO(5,  QuantityDTO.WeightUnit.KILOGRAM)
        );
        assertEquals(2.0, result, EPSILON);
    }

    @Test
    public void testDivide_Volume_LitreOverLitre() {
        double result = controller.performDivision(
            new QuantityDTO(10, QuantityDTO.VolumeUnit.LITRE),
            new QuantityDTO(5,  QuantityDTO.VolumeUnit.LITRE)
        );
        assertEquals(2.0, result, EPSILON);
    }

    // =========================================================================
    // SPEC ITEMS 1-14: Service operation correctness via controller
    // =========================================================================

    /**
     * testService_CompareEquality_SameUnit_Success (spec item 6)
     */
    @Test
    public void testService_CompareEquality_SameUnit_Success() {
        assertTrue(controller.performComparison(
            new QuantityDTO(5, QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(5, QuantityDTO.LengthUnit.FEET)
        ));
    }

    /**
     * testService_CompareEquality_DifferentUnit_Success (spec item 7)
     */
    @Test
    public void testService_CompareEquality_DifferentUnit_Success() {
        // 1 YARD == 3 FEET
        assertTrue(controller.performComparison(
            new QuantityDTO(1, QuantityDTO.LengthUnit.YARDS),
            new QuantityDTO(3, QuantityDTO.LengthUnit.FEET)
        ));
        // 1 KILOGRAM == 1000 GRAM
        assertTrue(controller.performComparison(
            new QuantityDTO(1,    QuantityDTO.WeightUnit.KILOGRAM),
            new QuantityDTO(1000, QuantityDTO.WeightUnit.GRAM)
        ));
    }

    /**
     * testService_CompareEquality_CrossCategory_Error (spec item 8)
     *
     * The service throws IllegalArgumentException when asked to compare
     * quantities from different measurement categories (e.g. length vs weight),
     * because the unit types don't match.
     */
    @Test
    public void testService_CompareEquality_CrossCategory_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.performComparison(
                new QuantityDTO(1, QuantityDTO.LengthUnit.FEET),
                new QuantityDTO(1, QuantityDTO.WeightUnit.KILOGRAM)
            )
        );
    }

    /**
     * testService_Convert_Success (spec item 9)
     */
    @Test
    public void testService_Convert_Length_Success() {
        QuantityDTO result = controller.performConversion(
            new QuantityDTO(1, QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(0, QuantityDTO.LengthUnit.INCHES)
        );
        assertEquals(12.0,    result.getValue(), EPSILON);
        assertEquals("INCHES", result.getUnit());
    }

    @Test
    public void testService_Convert_Temperature_Success() {
        QuantityDTO result = controller.performConversion(
            new QuantityDTO(0,  QuantityDTO.TemperatureUnit.CELSIUS),
            new QuantityDTO(0,  QuantityDTO.TemperatureUnit.FAHRENHEIT)
        );
        assertEquals(32.0,        result.getValue(), EPSILON);
        assertEquals("FAHRENHEIT", result.getUnit());
    }

    /**
     * testService_Add_Success (spec item 10)
     */
    @Test
    public void testService_Add_Length_Success() {
        QuantityDTO result = controller.performAddition(
            new QuantityDTO(1,  QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(12, QuantityDTO.LengthUnit.INCHES)
        );
        assertEquals(2.0,   result.getValue(), EPSILON);
        assertEquals("FEET", result.getUnit());
    }

    @Test
    public void testService_Add_Weight_Success() {
        QuantityDTO result = controller.performAddition(
            new QuantityDTO(1,    QuantityDTO.WeightUnit.KILOGRAM),
            new QuantityDTO(500,  QuantityDTO.WeightUnit.GRAM)
        );
        assertEquals(1.5,        result.getValue(), EPSILON);
        assertEquals("KILOGRAM", result.getUnit());
    }

    @Test
    public void testService_Add_Volume_Success() {
        QuantityDTO result = controller.performAddition(
            new QuantityDTO(2,    QuantityDTO.VolumeUnit.LITRE),
            new QuantityDTO(500,  QuantityDTO.VolumeUnit.MILLILITRE)
        );
        assertEquals(2.5,     result.getValue(), EPSILON);
        assertEquals("LITRE", result.getUnit());
    }

    /**
     * testService_Subtract_Success (spec item 12)
     */
    @Test
    public void testService_Subtract_Length_Success() {
        QuantityDTO result = controller.performSubtraction(
            new QuantityDTO(10, QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(6,  QuantityDTO.LengthUnit.INCHES)
        );
        assertEquals(9.5,   result.getValue(), EPSILON);
        assertEquals("FEET", result.getUnit());
    }

    @Test
    public void testService_Subtract_Weight_Success() {
        QuantityDTO result = controller.performSubtraction(
            new QuantityDTO(2,   QuantityDTO.WeightUnit.KILOGRAM),
            new QuantityDTO(500, QuantityDTO.WeightUnit.GRAM)
        );
        assertEquals(1.5,        result.getValue(), EPSILON);
        assertEquals("KILOGRAM", result.getUnit());
    }

    /**
     * testService_Divide_Success (spec item 13)
     */
    @Test
    public void testService_Divide_Length_Success() {
        double result = controller.performDivision(
            new QuantityDTO(10, QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(2,  QuantityDTO.LengthUnit.FEET)
        );
        assertEquals(5.0, result, EPSILON);
    }

    @Test
    public void testService_Divide_CrossUnit_Success() {
        // 24 INCHES / 2 FEET → both convert to base (24 in / 24 in) = 1.0
        double result = controller.performDivision(
            new QuantityDTO(24, QuantityDTO.LengthUnit.INCHES),
            new QuantityDTO(2,  QuantityDTO.LengthUnit.FEET)
        );
        assertEquals(1.0, result, EPSILON);
    }

    /**
     * testService_Divide_ByZero_Error (spec item 14)
     *
     * The service throws ArithmeticException for division by zero.
     */
    @Test
    public void testService_Divide_ByZero_ThrowsArithmeticException() {
        assertThrows(ArithmeticException.class, () ->
            controller.performDivision(
                new QuantityDTO(10, QuantityDTO.LengthUnit.FEET),
                new QuantityDTO(0,  QuantityDTO.LengthUnit.FEET)
            )
        );
    }

    // =========================================================================
    // SPEC ITEMS 21-24: Layer separation and data flow
    // =========================================================================

    /**
     * testLayerSeparation_ServiceIndependence (spec item 21)
     *
     * The service can be instantiated and called directly without any
     * controller — confirming it is independently unit-testable.
     */
    @Test
    public void testLayerSeparation_ServiceCanBeCalledDirectly() {
        IQuantityMeasurementService service =
            new QuantityMeasurementServiceImpl(
                QuantityMeasurementCacheRepository.getInstance()
            );

        boolean result = service.compare(
            new QuantityDTO(1, QuantityDTO.LengthUnit.YARDS),
            new QuantityDTO(36, QuantityDTO.LengthUnit.INCHES)
        );
        assertTrue(result);
    }

    // =========================================================================
    // SPEC ITEM 26: All measurement categories work through the service
    // =========================================================================

    /**
     * testService_AllMeasurementCategories (spec item 26)
     */
    @Test
    public void testService_AllMeasurementCategories_CompareAndAdd() {
        // Length
        assertTrue(controller.performComparison(
            new QuantityDTO(1,  QuantityDTO.LengthUnit.YARDS),
            new QuantityDTO(36, QuantityDTO.LengthUnit.INCHES)
        ));

        // Weight
        assertTrue(controller.performComparison(
            new QuantityDTO(1,    QuantityDTO.WeightUnit.KILOGRAM),
            new QuantityDTO(1000, QuantityDTO.WeightUnit.GRAM)
        ));

        // Volume
        assertTrue(controller.performComparison(
            new QuantityDTO(1,    QuantityDTO.VolumeUnit.LITRE),
            new QuantityDTO(1000, QuantityDTO.VolumeUnit.MILLILITRE)
        ));

        // Temperature
        assertTrue(controller.performComparison(
            new QuantityDTO(0,  QuantityDTO.TemperatureUnit.CELSIUS),
            new QuantityDTO(32, QuantityDTO.TemperatureUnit.FAHRENHEIT)
        ));
    }

    // =========================================================================
    // SPEC ITEM 27: Validation consistency across operations
    // =========================================================================

    /**
     * testService_ValidationConsistency (spec item 27)
     *
     * Passing a null DTO to any service operation throws
     * IllegalArgumentException — the same guard is applied uniformly.
     */
    @Test
    public void testService_NullDTO_Compare_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.performComparison(null,
                new QuantityDTO(1, QuantityDTO.LengthUnit.FEET))
        );
    }

    @Test
    public void testService_NullDTO_Add_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.performAddition(null,
                new QuantityDTO(1, QuantityDTO.LengthUnit.FEET))
        );
    }

    @Test
    public void testService_NullDTO_Subtract_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.performSubtraction(null,
                new QuantityDTO(1, QuantityDTO.LengthUnit.FEET))
        );
    }

    @Test
    public void testService_NullDTO_Divide_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.performDivision(null,
                new QuantityDTO(1, QuantityDTO.LengthUnit.FEET))
        );
    }

    // =========================================================================
    // SPEC ITEM 29: All operations propagate exceptions consistently
    // =========================================================================

    /**
     * testService_ExceptionHandling_AllOperations (spec item 29)
     *
     * Every arithmetic operation rejects temperature (UnsupportedOperationException)
     * and rejects cross-category inputs (IllegalArgumentException).
     */
    @Test
    public void testService_ExceptionHandling_Temperature_Add_Throws() {
        assertThrows(UnsupportedOperationException.class, () ->
            controller.performAddition(
                new QuantityDTO(100, QuantityDTO.TemperatureUnit.CELSIUS),
                new QuantityDTO(50,  QuantityDTO.TemperatureUnit.CELSIUS)
            )
        );
    }

    @Test
    public void testService_ExceptionHandling_Temperature_Subtract_Throws() {
        assertThrows(UnsupportedOperationException.class, () ->
            controller.performSubtraction(
                new QuantityDTO(100, QuantityDTO.TemperatureUnit.CELSIUS),
                new QuantityDTO(50,  QuantityDTO.TemperatureUnit.CELSIUS)
            )
        );
    }

    @Test
    public void testService_ExceptionHandling_Temperature_Divide_Throws() {
        assertThrows(UnsupportedOperationException.class, () ->
            controller.performDivision(
                new QuantityDTO(100, QuantityDTO.TemperatureUnit.CELSIUS),
                new QuantityDTO(50,  QuantityDTO.TemperatureUnit.CELSIUS)
            )
        );
    }

    @Test
    public void testService_ExceptionHandling_CrossCategory_Add_Throws() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.performAddition(
                new QuantityDTO(1, QuantityDTO.LengthUnit.FEET),
                new QuantityDTO(1, QuantityDTO.WeightUnit.KILOGRAM)
            )
        );
    }

    // =========================================================================
    // SPEC ITEM 31-32: Integration — end-to-end through all layers
    // =========================================================================

    /**
     * testIntegration_EndToEnd_LengthAddition (spec item 31)
     *
     * Full stack: QuantityDTO → controller → service → result back to caller.
     */
    @Test
    public void testIntegration_EndToEnd_LengthAddition() {
        QuantityDTO q1 = new QuantityDTO(2,  QuantityDTO.LengthUnit.FEET);
        QuantityDTO q2 = new QuantityDTO(24, QuantityDTO.LengthUnit.INCHES);
        QuantityDTO target = new QuantityDTO(0, QuantityDTO.LengthUnit.YARDS);

        // compare
        assertTrue(controller.performComparison(q1, q2));

        // convert
        QuantityDTO converted = controller.performConversion(q2, target);
        assertEquals("YARDS", converted.getUnit());

        // add (default unit)
        QuantityDTO added = controller.performAddition(q1, q2);
        assertEquals(4.0, added.getValue(), EPSILON);

        // add (target unit)
        QuantityDTO addedYards = controller.performAddition(q1, q2, target);
        assertEquals("YARDS", addedYards.getUnit());

        // subtract
        QuantityDTO subtracted = controller.performSubtraction(q1, q2);
        assertEquals(0.0, subtracted.getValue(), EPSILON);

        // divide
        assertEquals(1.0, controller.performDivision(q1, q2), EPSILON);
    }

    /**
     * testIntegration_EndToEnd_TemperatureUnsupported (spec item 32)
     *
     * Full stack error path: temperature addition is rejected end-to-end.
     */
    @Test
    public void testIntegration_EndToEnd_TemperatureAddition_IsRejected() {
        assertThrows(UnsupportedOperationException.class, () ->
            controller.performAddition(
                new QuantityDTO(100, QuantityDTO.TemperatureUnit.CELSIUS),
                new QuantityDTO(50,  QuantityDTO.TemperatureUnit.CELSIUS)
            )
        );
    }

    /**
     * testIntegration_EndToEnd_TemperatureConversion_Succeeds
     *
     * Temperature conversion (non-arithmetic) works end-to-end.
     */
    @Test
    public void testIntegration_EndToEnd_TemperatureConversion_Succeeds() {
        QuantityDTO result = controller.performConversion(
            new QuantityDTO(-40, QuantityDTO.TemperatureUnit.CELSIUS),
            new QuantityDTO(0,   QuantityDTO.TemperatureUnit.FAHRENHEIT)
        );
        assertEquals(-40.0,       result.getValue(), EPSILON);
        assertEquals("FAHRENHEIT", result.getUnit());
    }

    // =========================================================================
    // SPEC ITEM 35: Service works with any IMeasurable implementation
    //               (all four categories tested end-to-end through operations)
    // =========================================================================

    /**
     * testService_AllUnitImplementations (spec item 35)
     */
    @Test
    public void testService_AllUnitImplementations_Convert() {
        // Length
        assertEquals("INCHES", controller.performConversion(
            new QuantityDTO(1, QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(0, QuantityDTO.LengthUnit.INCHES)).getUnit());

        // Weight
        assertEquals("GRAM", controller.performConversion(
            new QuantityDTO(1, QuantityDTO.WeightUnit.KILOGRAM),
            new QuantityDTO(0, QuantityDTO.WeightUnit.GRAM)).getUnit());

        // Volume
        assertEquals("MILLILITRE", controller.performConversion(
            new QuantityDTO(1, QuantityDTO.VolumeUnit.LITRE),
            new QuantityDTO(0, QuantityDTO.VolumeUnit.MILLILITRE)).getUnit());

        // Temperature
        assertEquals("FAHRENHEIT", controller.performConversion(
            new QuantityDTO(100, QuantityDTO.TemperatureUnit.CELSIUS),
            new QuantityDTO(0,   QuantityDTO.TemperatureUnit.FAHRENHEIT)).getUnit());
    }

    // =========================================================================
    // SPEC ITEM 36: Operation type is recorded in repository entities
    // =========================================================================

    /**
     * testEntity_OperationType_Tracking (spec item 36)
     *
     * After each controller operation, the repository contains an entity
     * whose operation field matches the operation performed.
     */
    @Test
    public void testEntity_OperationType_Tracking_Compare() {
        int before = QuantityMeasurementCacheRepository.getInstance()
            .getAllMeasurements().size();

        controller.performComparison(
            new QuantityDTO(1, QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(12, QuantityDTO.LengthUnit.INCHES)
        );

        int after = QuantityMeasurementCacheRepository.getInstance()
            .getAllMeasurements().size();

        // One new entity was persisted
        assertEquals(before + 1, after);

        // The most recent entity records the COMPARE operation
        String lastOp = QuantityMeasurementCacheRepository.getInstance()
            .getAllMeasurements()
            .get(after - 1)
            .operation;
        assertEquals("COMPARE", lastOp);
    }

    @Test
    public void testEntity_OperationType_Tracking_Add() {
        int before = QuantityMeasurementCacheRepository.getInstance()
            .getAllMeasurements().size();

        controller.performAddition(
            new QuantityDTO(1, QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(12, QuantityDTO.LengthUnit.INCHES)
        );

        int after = QuantityMeasurementCacheRepository.getInstance()
            .getAllMeasurements().size();
        assertEquals(before + 1, after);

        String lastOp = QuantityMeasurementCacheRepository.getInstance()
            .getAllMeasurements()
            .get(after - 1)
            .operation;
        assertEquals("ADD", lastOp);
    }

    // =========================================================================
    // SPEC ITEM 40: Extensibility — new operation does not modify existing ones
    // =========================================================================

    /**
     * testScalability_NewOperation_ExistingOperationsUnchanged (spec item 40)
     *
     * All existing operations continue to produce the same results even
     * when the full suite is exercised together (simulating a scenario where
     * a new operation has been added alongside them).
     */
    @Test
    public void testScalability_ExistingOperations_ProduceSameResults_AfterFullSuiteRun() {
        QuantityDTO feet = new QuantityDTO(2,  QuantityDTO.LengthUnit.FEET);
        QuantityDTO inch = new QuantityDTO(24, QuantityDTO.LengthUnit.INCHES);

        assertEquals(true,  controller.performComparison(feet, inch));
        assertEquals(4.0,   controller.performAddition(feet, inch).getValue(),    EPSILON);
        assertEquals(0.0,   controller.performSubtraction(feet, inch).getValue(), EPSILON);
        assertEquals(1.0,   controller.performDivision(feet, inch),                EPSILON);
    }
}
