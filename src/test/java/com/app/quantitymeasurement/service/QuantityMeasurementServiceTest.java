package com.app.quantitymeasurement.service;

import com.app.quantitymeasurement.exception.QuantityMeasurementException;
import com.app.quantitymeasurement.dto.response.QuantityDTO;
import com.app.quantitymeasurement.dto.request.QuantityMeasurementDTO;
import com.app.quantitymeasurement.entity.QuantityMeasurementEntity;
import com.app.quantitymeasurement.repository.QuantityMeasurementRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QuantityMeasurementServiceTest {

    /**
     * Mock of the JPA repository — prevents actual database calls during unit tests.
     */
    @Mock
    private QuantityMeasurementRepository repository;

    /**
     * The service under test — Mockito injects the mocked repository.
     */
    @InjectMocks
    private QuantityMeasurementServiceImpl service;

    private QuantityDTO feetDTO;
    private QuantityDTO inchesDTO;
    private QuantityDTO kilogramDTO;

    @BeforeEach
    void setUp() {
        feetDTO     = new QuantityDTO(1.0,  QuantityDTO.LengthUnit.FEET);
        inchesDTO   = new QuantityDTO(12.0, QuantityDTO.LengthUnit.INCHES);
        kilogramDTO = new QuantityDTO(1.0,  QuantityDTO.WeightUnit.KILOGRAM);
        when(repository.save(any(QuantityMeasurementEntity.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // =========================================================================
    // COMPARE
    // =========================================================================

    @Test
    void testCompare_EqualQuantities_ResultStringTrue() {
        QuantityMeasurementDTO result = service.compare(feetDTO, inchesDTO);
        assertNotNull(result);
        assertEquals("true", result.getResultString());
        assertEquals("compare", result.getOperation());
        assertFalse(result.isError());
        verify(repository, times(1)).save(any());
    }

    @Test
    void testCompare_NotEqual_ResultStringFalse() {
        QuantityDTO twoFeet = new QuantityDTO(2.0, QuantityDTO.LengthUnit.FEET);
        QuantityMeasurementDTO result = service.compare(twoFeet, inchesDTO);
        assertEquals("false", result.getResultString());
    }

    @Test
    void testCompare_DifferentCategories_ThrowsAndSavesError() {
        assertThrows(QuantityMeasurementException.class,
            () -> service.compare(feetDTO, kilogramDTO));
        // Error entity should be saved
        verify(repository, atLeastOnce()).save(any());
    }

    // =========================================================================
    // CONVERT
    // =========================================================================

    @Test
    void testConvert_FeetToInches_Returns12() {
        QuantityDTO targetInches = new QuantityDTO(0.0, QuantityDTO.LengthUnit.INCHES);
        QuantityMeasurementDTO result = service.convert(feetDTO, targetInches);
        assertEquals(12.0, result.getResultValue(), 1e-4);
        assertEquals("convert", result.getOperation());
    }

    @Test
    void testConvert_CelsiusToFahrenheit_Returns32() {
        QuantityDTO celsius = new QuantityDTO(0.0, QuantityDTO.TemperatureUnit.CELSIUS);
        QuantityDTO fahrenheit = new QuantityDTO(0.0, QuantityDTO.TemperatureUnit.FAHRENHEIT);
        QuantityMeasurementDTO result = service.convert(celsius, fahrenheit);
        assertEquals(32.0, result.getResultValue(), 1e-4);
    }

    // =========================================================================
    // ADD
    // =========================================================================

    @Test
    void testAdd_FeetPlusInches_ResultInFeet() {
        QuantityMeasurementDTO result = service.add(feetDTO, inchesDTO);
        assertEquals(2.0, result.getResultValue(), 1e-4);
        assertEquals("FEET", result.getResultUnit());
        assertEquals("add", result.getOperation());
    }

    @Test
    void testAdd_WithTargetUnit_ResultInYards() {
        QuantityDTO yardsTarget = new QuantityDTO(0.0, QuantityDTO.LengthUnit.YARDS);
        QuantityMeasurementDTO result = service.add(feetDTO, inchesDTO, yardsTarget);
        assertEquals("YARDS", result.getResultUnit());
        assertTrue(result.getResultValue() > 0.0);
    }

    @Test
    void testAdd_TemperatureUnits_ThrowsUnsupported() {
        QuantityDTO celsius = new QuantityDTO(10.0, QuantityDTO.TemperatureUnit.CELSIUS);
        QuantityDTO fahrenheit = new QuantityDTO(50.0, QuantityDTO.TemperatureUnit.FAHRENHEIT);
        assertThrows(QuantityMeasurementException.class,
            () -> service.add(celsius, fahrenheit));
    }

    @Test
    void testAdd_DifferentCategories_ThrowsAndSavesError() {
        assertThrows(QuantityMeasurementException.class,
            () -> service.add(feetDTO, kilogramDTO));
        verify(repository, atLeastOnce()).save(any());
    }

    // =========================================================================
    // SUBTRACT
    // =========================================================================

    @Test
    void testSubtract_FeetMinusInches_ResultZeroFeet() {
        QuantityMeasurementDTO result = service.subtract(feetDTO, inchesDTO);
        assertEquals(0.0, result.getResultValue(), 1e-4);
        assertEquals("FEET", result.getResultUnit());
    }

    @Test
    void testSubtract_WithTargetUnit_ResultInYards() {
        QuantityDTO yardsTarget = new QuantityDTO(0.0, QuantityDTO.LengthUnit.YARDS);
        QuantityMeasurementDTO result = service.subtract(feetDTO, inchesDTO, yardsTarget);
        assertEquals("YARDS", result.getResultUnit());
    }

    // =========================================================================
    // DIVIDE
    // =========================================================================

    @Test
    void testDivide_FeetByFeet_ResultOne() {
        QuantityMeasurementDTO result = service.divide(feetDTO, feetDTO);
        assertEquals(1.0, result.getResultValue(), 1e-4);
        assertEquals("divide", result.getOperation());
    }

    @Test
    void testDivide_ByZero_ThrowsArithmeticException() {
        QuantityDTO zeroInches = new QuantityDTO(0.0, QuantityDTO.LengthUnit.INCHES);
        assertThrows(ArithmeticException.class,
            () -> service.divide(feetDTO, zeroInches));
    }

    // =========================================================================
    // History / Count methods
    // =========================================================================

    @Test
    void testGetHistoryByOperation_DelegatesToRepository() {
        QuantityMeasurementEntity entity = new QuantityMeasurementEntity();
        entity.setOperation("compare");
        entity.setResultString("true");

        when(repository.findByOperation("compare")).thenReturn(List.of(entity));

        List<QuantityMeasurementDTO> result = service.getHistoryByOperation("compare");
        assertEquals(1, result.size());
        assertEquals("compare", result.get(0).getOperation());
        verify(repository, times(1)).findByOperation("compare");
    }

    @Test
    void testGetHistoryByMeasurementType_DelegatesToRepository() {
        QuantityMeasurementEntity entity = new QuantityMeasurementEntity();
        entity.setThisMeasurementType("LengthUnit");

        when(repository.findByThisMeasurementType("LengthUnit")).thenReturn(List.of(entity));

        List<QuantityMeasurementDTO> result = service.getHistoryByMeasurementType("LengthUnit");
        assertEquals(1, result.size());
        verify(repository, times(1)).findByThisMeasurementType("LengthUnit");
    }

    @Test
    void testGetOperationCount_DelegatesToRepository() {
        when(repository.countByOperationAndErrorFalse("compare")).thenReturn(3L);
        long count = service.getOperationCount("compare");
        assertEquals(3L, count);
    }

    @Test
    void testGetErrorHistory_DelegatesToRepository() {
        QuantityMeasurementEntity errorEntity = new QuantityMeasurementEntity();
        errorEntity.setError(true);
        errorEntity.setErrorMessage("Test error");

        when(repository.findByErrorTrue()).thenReturn(List.of(errorEntity));

        List<QuantityMeasurementDTO> result = service.getErrorHistory();
        assertEquals(1, result.size());
        assertTrue(result.get(0).isError());
    }
}
