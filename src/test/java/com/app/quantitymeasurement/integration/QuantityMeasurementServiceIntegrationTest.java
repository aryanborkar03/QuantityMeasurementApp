package com.app.quantitymeasurement.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.app.quantitymeasurement.dto.request.QuantityMeasurementDTO;
import com.app.quantitymeasurement.dto.response.QuantityDTO;
import com.app.quantitymeasurement.entity.QuantityMeasurementEntity;
import com.app.quantitymeasurement.exception.QuantityMeasurementException;
import com.app.quantitymeasurement.repository.QuantityMeasurementRepository;
import com.app.quantitymeasurement.service.QuantityMeasurementServiceImpl;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QuantityMeasurementServiceIntegrationTest {

    private static final double EPSILON = 1e-6;

    @Mock
    private QuantityMeasurementRepository repository;
    @InjectMocks
    private QuantityMeasurementServiceImpl service;

    @BeforeEach
    void setUp() {
        when(repository.save(any(QuantityMeasurementEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // =========================================================================
    // COMPARISON
    // =========================================================================

    @Test
    void testCompare_Length_FeetVsInches_Equal() {
        assertTrue(Bool(service.compare(feet(2), inches(24))));
    }

    @Test
    void testCompare_Length_FeetVsInches_NotEqual() {
        assertFalse(Bool(service.compare(feet(1), inches(24))));
    }

    @Test
    void testCompare_Length_YardVsFeet_Equal() {
        assertTrue(Bool(service.compare(yards(1), feet(3))));
    }

    @Test
    void testCompare_Weight_KilogramVsGram_Equal() {
        assertTrue(Bool(service.compare(kg(1), gram(1000))));
    }

    @Test
    void testCompare_Volume_LitreVsMillilitre_Equal() {
        assertTrue(Bool(service.compare(litre(1), ml(1000))));
    }

    @Test
    void testCompare_Temperature_CelsiusVsFahrenheit_Equal() {
        assertTrue(Bool(service.compare(celsius(0), fahrenheit(32))));
    }

    @Test
    void testCompare_Temperature_100C_vs_212F_Equal() {
        assertTrue(Bool(service.compare(celsius(100), fahrenheit(212))));
    }

    // =========================================================================
    // CONVERSION
    // =========================================================================

    @Test
    void testConvert_Length_InchesToYards() {
        QuantityMeasurementDTO r = service.convert(inches(24), yards(0));
        assertEquals("YARDS", r.getResultUnit());
        assertEquals(0.666667, r.getResultValue(), EPSILON);
    }

    @Test
    void testConvert_Length_FeetToInches() {
        QuantityMeasurementDTO r = service.convert(feet(2), inches(0));
        assertEquals(24.0, r.getResultValue(), EPSILON);
        assertEquals("INCHES", r.getResultUnit());
    }

    @Test
    void testConvert_Weight_KilogramToPound() {
        QuantityMeasurementDTO r = service.convert(kg(1), pound(0));
        assertEquals(2.204624, r.getResultValue(), EPSILON);
        assertEquals("POUND", r.getResultUnit());
    }

    @Test
    void testConvert_Volume_LitreToMillilitre() {
        QuantityMeasurementDTO r = service.convert(litre(1), ml(0));
        assertEquals(1000.0, r.getResultValue(), EPSILON);
        assertEquals("MILLILITRE", r.getResultUnit());
    }

    @Test
    void testConvert_Temperature_CelsiusToFahrenheit() {
        QuantityMeasurementDTO r = service.convert(celsius(100), fahrenheit(0));
        assertEquals(212.0, r.getResultValue(), EPSILON);
        assertEquals("FAHRENHEIT", r.getResultUnit());
    }

    @Test
    void testConvert_Temperature_FahrenheitToCelsius() {
        QuantityMeasurementDTO r = service.convert(fahrenheit(32), celsius(0));
        assertEquals(0.0, r.getResultValue(), EPSILON);
        assertEquals("CELSIUS", r.getResultUnit());
    }

    // =========================================================================
    // ADDITION
    // =========================================================================

    @Test
    void testAdd_Length_FeetPlusInches_DefaultUnit() {
        QuantityMeasurementDTO r = service.add(feet(2), inches(24));
        assertEquals(4.0, r.getResultValue(), EPSILON);
        assertEquals("FEET", r.getResultUnit());
    }

    @Test
    void testAdd_Length_FeetPlusInches_TargetYards() {
        QuantityMeasurementDTO r = service.add(feet(2), inches(24), yards(0));
        assertEquals(1.333333, r.getResultValue(), EPSILON);
        assertEquals("YARDS", r.getResultUnit());
    }

    @Test
    void testAdd_Weight_KilogramPlusGram() {
        QuantityMeasurementDTO r = service.add(kg(1), gram(1000));
        assertEquals(2.0, r.getResultValue(), EPSILON);
        assertEquals("KILOGRAM", r.getResultUnit());
    }

    @Test
    void testAdd_Volume_LitrePlusMillilitre() {
        QuantityMeasurementDTO r = service.add(litre(1), ml(1000));
        assertEquals(2.0, r.getResultValue(), EPSILON);
        assertEquals("LITRE", r.getResultUnit());
    }

    @Test
    void testAdd_Length_InchPlusInch() {
        QuantityMeasurementDTO r = service.add(inches(6), inches(6));
        assertEquals(12.0, r.getResultValue(), EPSILON);
        assertEquals("INCHES", r.getResultUnit());
    }

    @Test
    void testAdd_Length_YardPlusFeet() {
        QuantityMeasurementDTO r = service.add(yards(1), feet(3));
        assertEquals(2.0, r.getResultValue(), EPSILON);
        assertEquals("YARDS", r.getResultUnit());
    }

    // =========================================================================
    // SUBTRACTION
    // =========================================================================

    @Test
    void testSubtract_Length_FeetMinusInches_DefaultUnit() {
        QuantityMeasurementDTO r = service.subtract(feet(2), inches(24));
        assertEquals(0.0, r.getResultValue(), EPSILON);
        assertEquals("FEET", r.getResultUnit());
    }

    @Test
    void testSubtract_Length_FeetMinusInches_ExplicitTarget() {
        QuantityMeasurementDTO r = service.subtract(feet(10), inches(6), feet(0));
        assertEquals(9.5, r.getResultValue(), EPSILON);
        assertEquals("FEET", r.getResultUnit());
    }

    @Test
    void testSubtract_Weight_KilogramMinusGram() {
        QuantityMeasurementDTO r = service.subtract(kg(2), gram(500));
        assertEquals(1.5, r.getResultValue(), EPSILON);
        assertEquals("KILOGRAM", r.getResultUnit());
    }

    @Test
    void testSubtract_Volume_LitreMinusMillilitre() {
        QuantityMeasurementDTO r = service.subtract(litre(2), ml(500));
        assertEquals(1.5, r.getResultValue(), EPSILON);
        assertEquals("LITRE", r.getResultUnit());
    }

    // =========================================================================
    // DIVISION
    // =========================================================================

    @Test
    void testDivide_Length_EqualQuantities_ReturnsOne() {
        QuantityMeasurementDTO r = service.divide(feet(2), inches(24));
        assertEquals(1.0, r.getResultValue(), EPSILON);
    }

    @Test
    void testDivide_Length_FourFeetOverTwoFeet_ReturnsTwo() {
        QuantityMeasurementDTO r = service.divide(feet(4), feet(2));
        assertEquals(2.0, r.getResultValue(), EPSILON);
    }

    @Test
    void testDivide_Weight_TwoKgOverOneKg_ReturnsTwo() {
        QuantityMeasurementDTO r = service.divide(kg(2), kg(1));
        assertEquals(2.0, r.getResultValue(), EPSILON);
    }

    @Test
    void testDivide_Volume_TwoLitresOverOneLitre_ReturnsTwo() {
        QuantityMeasurementDTO r = service.divide(litre(2), litre(1));
        assertEquals(2.0, r.getResultValue(), EPSILON);
    }

    // =========================================================================
    // EXCEPTION HANDLING
    // =========================================================================

    @Test
    void testService_ExceptionHandling_DivideByZero_Throws() {
        assertThrows(ArithmeticException.class,
                () -> service.divide(feet(1), inches(0)));
    }

    @Test
    void testService_ExceptionHandling_Temperature_Add_Throws() {
        assertThrows(QuantityMeasurementException.class,
                () -> service.add(celsius(100), fahrenheit(50)));
    }

    @Test
    void testService_ExceptionHandling_Temperature_Subtract_Throws() {
        assertThrows(QuantityMeasurementException.class,
                () -> service.subtract(celsius(100), fahrenheit(50)));
    }

    @Test
    void testService_ExceptionHandling_Temperature_Divide_Throws() {
        assertThrows(QuantityMeasurementException.class,
                () -> service.divide(celsius(100), celsius(50)));
    }

    @Test
    void testService_ExceptionHandling_CrossCategory_Add_Throws() {
        assertThrows(QuantityMeasurementException.class,
                () -> service.add(feet(1), kg(1)));
    }

    @Test
    void testService_ExceptionHandling_CrossCategory_Subtract_Throws() {
        assertThrows(QuantityMeasurementException.class,
                () -> service.subtract(feet(1), kg(1)));
    }

    @Test
    void testService_ExceptionHandling_CrossCategory_Compare_Throws() {
        assertThrows(QuantityMeasurementException.class,
                () -> service.compare(feet(1), kg(1)));
    }

    // =========================================================================
    // ENTITY / REPOSITORY TRACKING (spec 36)
    // =========================================================================

    @Test
    void testEntity_OperationType_Tracking_Compare() {
        service.compare(feet(1), inches(12));
        verify(repository, times(1)).save(any(QuantityMeasurementEntity.class));
    }

    @Test
    void testEntity_OperationType_Tracking_Add() {
        service.add(feet(1), inches(12));
        verify(repository, times(1)).save(any(QuantityMeasurementEntity.class));
    }

    @Test
    void testEntity_OperationType_Tracking_Convert() {
        service.convert(feet(1), inches(0));
        verify(repository, times(1)).save(any(QuantityMeasurementEntity.class));
    }

    @Test
    void testEntity_OperationType_Tracking_Subtract() {
        service.subtract(feet(2), inches(24));
        verify(repository, times(1)).save(any(QuantityMeasurementEntity.class));
    }

    @Test
    void testEntity_OperationType_Tracking_Divide() {
        service.divide(feet(2), inches(24));
        verify(repository, times(1)).save(any(QuantityMeasurementEntity.class));
    }

    @Test
    void testEntity_ErrorRecord_SavedOnException() {
        try {
            service.add(feet(1), kg(1));
        } catch (Exception ignored) {
        }
        // error entity should also be saved
        verify(repository, atLeastOnce()).save(any(QuantityMeasurementEntity.class));
    }

    // =========================================================================
    // INTEGRATION — end-to-end flows (spec 31-32)
    // =========================================================================

    @Test
    void testIntegration_EndToEnd_LengthAddition() {
        assertTrue(Bool(service.compare(feet(2), inches(24))));
        assertEquals("YARDS", service.convert(inches(24), yards(0)).getResultUnit());
        assertEquals(4.0, service.add(feet(2), inches(24)).getResultValue(), EPSILON);
        assertEquals("YARDS", service.add(feet(2), inches(24), yards(0)).getResultUnit());
        assertEquals(0.0, service.subtract(feet(2), inches(24)).getResultValue(), EPSILON);
        assertEquals(1.0, service.divide(feet(2), inches(24)).getResultValue(), EPSILON);
    }

    @Test
    void testIntegration_EndToEnd_TemperatureAddition_IsRejected() {
        assertThrows(QuantityMeasurementException.class,
                () -> service.add(celsius(100), celsius(50)));
    }

    @Test
    void testIntegration_EndToEnd_TemperatureConversion_Succeeds() {
        QuantityMeasurementDTO r = service.convert(celsius(-40), fahrenheit(0));
        assertEquals(-40.0, r.getResultValue(), EPSILON);
        assertEquals("FAHRENHEIT", r.getResultUnit());
    }

    // =========================================================================
    // EXTENSIBILITY / SCALABILITY (spec 35, 40)
    // =========================================================================

    @Test
    void testService_AllUnitImplementations_Convert() {
        assertEquals("INCHES", service.convert(feet(1), inches(0)).getResultUnit());
        assertEquals("GRAM", service.convert(kg(1), gram(0)).getResultUnit());
        assertEquals("MILLILITRE", service.convert(litre(1), ml(0)).getResultUnit());
        assertEquals("FAHRENHEIT", service.convert(celsius(100), fahrenheit(0)).getResultUnit());
    }

    @Test
    void testScalability_ExistingOperations_ProduceSameResults_AfterFullSuiteRun() {
        assertTrue(Bool(service.compare(feet(2), inches(24))));
        assertEquals(4.0, service.add(feet(2), inches(24)).getResultValue(), EPSILON);
        assertEquals(0.0, service.subtract(feet(2), inches(24)).getResultValue(), EPSILON);
        assertEquals(1.0, service.divide(feet(2), inches(24)).getResultValue(), EPSILON);
    }

    // =========================================================================
    // Helpers — boolean extraction and DTO factory shorthands
    // =========================================================================

    private boolean Bool(QuantityMeasurementDTO dto) {
        return "true".equals(dto.getResultString());
    }

    private QuantityDTO feet(double v) {
        return new QuantityDTO(v, QuantityDTO.LengthUnit.FEET);
    }

    private QuantityDTO inches(double v) {
        return new QuantityDTO(v, QuantityDTO.LengthUnit.INCHES);
    }

    private QuantityDTO yards(double v) {
        return new QuantityDTO(v, QuantityDTO.LengthUnit.YARDS);
    }

    private QuantityDTO kg(double v) {
        return new QuantityDTO(v, QuantityDTO.WeightUnit.KILOGRAM);
    }

    private QuantityDTO gram(double v) {
        return new QuantityDTO(v, QuantityDTO.WeightUnit.GRAM);
    }

    private QuantityDTO pound(double v) {
        return new QuantityDTO(v, QuantityDTO.WeightUnit.POUND);
    }

    private QuantityDTO litre(double v) {
        return new QuantityDTO(v, QuantityDTO.VolumeUnit.LITRE);
    }

    private QuantityDTO ml(double v) {
        return new QuantityDTO(v, QuantityDTO.VolumeUnit.MILLILITRE);
    }

    private QuantityDTO celsius(double v) {
        return new QuantityDTO(v, QuantityDTO.TemperatureUnit.CELSIUS);
    }

    private QuantityDTO fahrenheit(double v) {
        return new QuantityDTO(v, QuantityDTO.TemperatureUnit.FAHRENHEIT);
    }
}
