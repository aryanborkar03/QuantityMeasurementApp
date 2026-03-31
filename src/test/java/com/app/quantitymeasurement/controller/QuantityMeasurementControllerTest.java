package com.app.quantitymeasurement.controller;

import com.app.quantitymeasurement.config.SecurityConfig;
import com.app.quantitymeasurement.model.QuantityDTO;
import com.app.quantitymeasurement.model.QuantityInputDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementDTO;
import com.app.quantitymeasurement.service.IQuantityMeasurementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * QuantityMeasurementControllerTest
 * Full 23-test suite ported from UC16 to UC17 @WebMvcTest style.
 * All UC16 scenarios preserved: layer separation, comparison, conversion,
 * addition (2+3 args), subtraction, division, all-ops pass, data flow,
 * backward compatibility, validation, GET endpoints, Mockito.verify.
 */
@WebMvcTest(QuantityMeasurementController.class)
@org.springframework.context.annotation.Import(SecurityConfig.class)
public class QuantityMeasurementControllerTest {

    private static final double EPSILON = 1e-6;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private IQuantityMeasurementService quantityMeasurementService;

    private QuantityDTO twoFeet, twentyFourInches, zeroYards;
    private QuantityMeasurementDTO equalResult, notEqualResult;

    @BeforeEach
    public void setUp() {
        twoFeet          = new QuantityDTO(2.0,  QuantityDTO.LengthUnit.FEET);
        twentyFourInches = new QuantityDTO(24.0, QuantityDTO.LengthUnit.INCHES);
        zeroYards        = new QuantityDTO(0.0,  QuantityDTO.LengthUnit.YARDS);
        equalResult    = QuantityMeasurementDTO.builder().operation("compare").resultString("true").error(false).build();
        notEqualResult = QuantityMeasurementDTO.builder().operation("compare").resultString("false").error(false).build();
    }

    private ResultActions doPost(String ep, QuantityInputDTO input) throws Exception {
        return mockMvc.perform(post("/api/v1/quantities/" + ep)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input)));
    }

    private QuantityMeasurementDTO buildResult(String op, Double val, String unit, String mType) {
        return QuantityMeasurementDTO.builder()
            .operation(op).resultValue(val).resultUnit(unit).resultMeasurementType(mType).error(false).build();
    }

    // =========================================================================
    // LAYER SEPARATION
    // =========================================================================

    /** testLayerSeparation_ControllerIndependence_StubService (spec 22) */
    @Test public void testLayerSeparation_ControllerIndependence_StubService() throws Exception {
        when(quantityMeasurementService.compare(any(), any())).thenReturn(equalResult);
        doPost("compare", new QuantityInputDTO(twoFeet, twentyFourInches, null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultString").value("true"));
        Mockito.verify(quantityMeasurementService, Mockito.times(1)).compare(any(QuantityDTO.class), any(QuantityDTO.class));
    }

    /** testController_NullBody_Returns400 — null body causes 400, controller has no null guard */
    @Test public void testController_NullBody_Returns400() throws Exception {
        mockMvc.perform(post("/api/v1/quantities/compare").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // COMPARISON
    // =========================================================================

    /** testPerformComparison_Equal_ReturnsTrue (spec 15) */
    @Test public void testPerformComparison_Equal_ReturnsTrue() throws Exception {
        when(quantityMeasurementService.compare(any(), any())).thenReturn(equalResult);
        doPost("compare", new QuantityInputDTO(twoFeet, twentyFourInches, null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultString").value("true")).andExpect(jsonPath("$.error").value(false));
    }

    /** testPerformComparison_NotEqual_ReturnsFalse */
    @Test public void testPerformComparison_NotEqual_ReturnsFalse() throws Exception {
        when(quantityMeasurementService.compare(any(), any())).thenReturn(notEqualResult);
        doPost("compare", new QuantityInputDTO(new QuantityDTO(1.0, QuantityDTO.LengthUnit.FEET), twentyFourInches, null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultString").value("false"));
    }

    // =========================================================================
    // CONVERSION
    // =========================================================================

    /** testPerformConversion_InchesToYards_CorrectResult (spec 16) */
    @Test public void testPerformConversion_InchesToYards_CorrectResult() throws Exception {
        when(quantityMeasurementService.convert(any(), any())).thenReturn(
            QuantityMeasurementDTO.builder().operation("convert").resultValue(0.666667).resultUnit("YARDS").error(false).build());
        doPost("convert", new QuantityInputDTO(twentyFourInches, zeroYards, null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultUnit").value("YARDS"));
    }

    /** testPerformConversion_FeetToInches_CorrectResult */
    @Test public void testPerformConversion_FeetToInches_CorrectResult() throws Exception {
        when(quantityMeasurementService.convert(any(), any())).thenReturn(
            QuantityMeasurementDTO.builder().operation("convert").resultValue(24.0).resultUnit("INCHES").error(false).build());
        doPost("convert", new QuantityInputDTO(twoFeet, new QuantityDTO(0.0, QuantityDTO.LengthUnit.INCHES), null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(24.0));
    }

    /** testPerformConversion_Temperature_CelsiusToFahrenheit */
    @Test public void testPerformConversion_Temperature_CelsiusToFahrenheit() throws Exception {
        when(quantityMeasurementService.convert(any(), any())).thenReturn(
            QuantityMeasurementDTO.builder().operation("convert").resultValue(212.0).resultUnit("FAHRENHEIT").error(false).build());
        doPost("convert", new QuantityInputDTO(
            new QuantityDTO(100.0, QuantityDTO.TemperatureUnit.CELSIUS),
            new QuantityDTO(0.0,   QuantityDTO.TemperatureUnit.FAHRENHEIT), null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(212.0)).andExpect(jsonPath("$.resultUnit").value("FAHRENHEIT"));
    }

    // =========================================================================
    // ADDITION
    // =========================================================================

    /** testPerformAddition_TwoOperands_DefaultUnit (spec 17) */
    @Test public void testPerformAddition_TwoOperands_DefaultUnit() throws Exception {
        when(quantityMeasurementService.add(any(), any())).thenReturn(buildResult("add", 4.0, "FEET", "LengthUnit"));
        doPost("add", new QuantityInputDTO(twoFeet, twentyFourInches, null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(4.0)).andExpect(jsonPath("$.resultUnit").value("FEET"));
    }

    /** testPerformAddition_ThreeOperands_ExplicitTargetUnit */
    @Test public void testPerformAddition_ThreeOperands_ExplicitTargetUnit() throws Exception {
        when(quantityMeasurementService.add(any(), any(), any())).thenReturn(buildResult("add", 1.333333, "YARDS", "LengthUnit"));
        doPost("add", new QuantityInputDTO(twoFeet, twentyFourInches, zeroYards))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultUnit").value("YARDS"));
    }

    /** testPerformAddition_Weight_KilogramPlusGram */
    @Test public void testPerformAddition_Weight_KilogramPlusGram() throws Exception {
        when(quantityMeasurementService.add(any(), any())).thenReturn(buildResult("add", 2.0, "KILOGRAM", "WeightUnit"));
        doPost("add", new QuantityInputDTO(new QuantityDTO(1.0, QuantityDTO.WeightUnit.KILOGRAM),
            new QuantityDTO(1000.0, QuantityDTO.WeightUnit.GRAM), null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(2.0)).andExpect(jsonPath("$.resultUnit").value("KILOGRAM"));
    }

    /** testPerformAddition_Volume_LitrePlusMillilitre */
    @Test public void testPerformAddition_Volume_LitrePlusMillilitre() throws Exception {
        when(quantityMeasurementService.add(any(), any())).thenReturn(buildResult("add", 2.0, "LITRE", "VolumeUnit"));
        doPost("add", new QuantityInputDTO(new QuantityDTO(1.0, QuantityDTO.VolumeUnit.LITRE),
            new QuantityDTO(1000.0, QuantityDTO.VolumeUnit.MILLILITRE), null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultUnit").value("LITRE"));
    }

    // =========================================================================
    // SUBTRACTION
    // =========================================================================

    /** testPerformSubtraction_TwoOperands_DefaultUnit */
    @Test public void testPerformSubtraction_TwoOperands_DefaultUnit() throws Exception {
        when(quantityMeasurementService.subtract(any(), any())).thenReturn(buildResult("subtract", 0.0, "FEET", "LengthUnit"));
        doPost("subtract", new QuantityInputDTO(twoFeet, twentyFourInches, null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(0.0)).andExpect(jsonPath("$.resultUnit").value("FEET"));
    }

    /** testPerformSubtraction_ThreeOperands_ExplicitTargetUnit */
    @Test public void testPerformSubtraction_ThreeOperands_ExplicitTargetUnit() throws Exception {
        when(quantityMeasurementService.subtract(any(), any(), any())).thenReturn(buildResult("subtract", 9.5, "FEET", "LengthUnit"));
        doPost("subtract", new QuantityInputDTO(new QuantityDTO(10.0, QuantityDTO.LengthUnit.FEET),
            new QuantityDTO(6.0, QuantityDTO.LengthUnit.INCHES), new QuantityDTO(0.0, QuantityDTO.LengthUnit.FEET)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(9.5));
    }

    // =========================================================================
    // DIVISION
    // =========================================================================

    /** testPerformDivision_EqualQuantities_ReturnsOne */
    @Test public void testPerformDivision_EqualQuantities_ReturnsOne() throws Exception {
        when(quantityMeasurementService.divide(any(), any())).thenReturn(
            QuantityMeasurementDTO.builder().operation("divide").resultValue(1.0).error(false).build());
        doPost("divide", new QuantityInputDTO(twoFeet, twentyFourInches, null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(1.0));
    }

    /** testPerformDivision_FourFeetOverTwoFeet_ReturnsTwo */
    @Test public void testPerformDivision_FourFeetOverTwoFeet_ReturnsTwo() throws Exception {
        when(quantityMeasurementService.divide(any(), any())).thenReturn(
            QuantityMeasurementDTO.builder().operation("divide").resultValue(2.0).error(false).build());
        doPost("divide", new QuantityInputDTO(new QuantityDTO(4.0, QuantityDTO.LengthUnit.FEET), twoFeet, null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(2.0));
    }

    // =========================================================================
    // ALL OPERATIONS SINGLE PASS (spec 26)
    // =========================================================================

    /** testAllOperations_RouteCorrectly */
    @Test public void testAllOperations_RouteCorrectly() throws Exception {
        when(quantityMeasurementService.compare(any(), any())).thenReturn(equalResult);
        when(quantityMeasurementService.convert(any(), any())).thenReturn(buildResult("convert", 0.666667, "YARDS", "LengthUnit"));
        when(quantityMeasurementService.add(any(), any())).thenReturn(buildResult("add", 4.0, "FEET", "LengthUnit"));
        when(quantityMeasurementService.add(any(), any(), any())).thenReturn(buildResult("add", 1.333333, "YARDS", "LengthUnit"));
        when(quantityMeasurementService.subtract(any(), any())).thenReturn(buildResult("subtract", 0.0, "FEET", "LengthUnit"));
        when(quantityMeasurementService.divide(any(), any())).thenReturn(
            QuantityMeasurementDTO.builder().operation("divide").resultValue(1.0).error(false).build());

        doPost("compare",  new QuantityInputDTO(twoFeet, twentyFourInches, null)).andExpect(status().isOk()).andExpect(jsonPath("$.resultString").value("true"));
        doPost("convert",  new QuantityInputDTO(twentyFourInches, zeroYards, null)).andExpect(status().isOk()).andExpect(jsonPath("$.resultUnit").value("YARDS"));
        doPost("add",      new QuantityInputDTO(twoFeet, twentyFourInches, null)).andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(4.0));
        doPost("add",      new QuantityInputDTO(twoFeet, twentyFourInches, zeroYards)).andExpect(status().isOk()).andExpect(jsonPath("$.resultUnit").value("YARDS"));
        doPost("subtract", new QuantityInputDTO(twoFeet, twentyFourInches, null)).andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(0.0));
        doPost("divide",   new QuantityInputDTO(twoFeet, twentyFourInches, null)).andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(1.0));
    }

    // =========================================================================
    // DATA FLOW (spec 23-24)
    // =========================================================================

    /** testDataFlow_InputPassedThrough_OutputReturnedUnmodified */
    @Test public void testDataFlow_InputPassedThrough_OutputReturnedUnmodified() throws Exception {
        QuantityMeasurementDTO expected = QuantityMeasurementDTO.builder()
            .operation("add").resultValue(2.0).resultUnit("FEET").resultMeasurementType("LengthUnit").error(false).build();
        when(quantityMeasurementService.add(any(), any())).thenReturn(expected);
        doPost("add", new QuantityInputDTO(new QuantityDTO(1.0, QuantityDTO.LengthUnit.FEET), new QuantityDTO(12.0, QuantityDTO.LengthUnit.INCHES), null))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultValue").value(2.0))
            .andExpect(jsonPath("$.resultUnit").value("FEET"))
            .andExpect(jsonPath("$.resultMeasurementType").value("LengthUnit"));
    }

    // =========================================================================
    // BACKWARD COMPATIBILITY (spec 25)
    // =========================================================================

    @Test public void testBackwardCompatibility_UC1_CompareEqualLengths() throws Exception {
        when(quantityMeasurementService.compare(any(), any())).thenReturn(equalResult);
        doPost("compare", new QuantityInputDTO(twoFeet, twentyFourInches, null)).andExpect(status().isOk()).andExpect(jsonPath("$.resultString").value("true"));
    }

    @Test public void testBackwardCompatibility_UC5_ConvertInchesToYards() throws Exception {
        when(quantityMeasurementService.convert(any(), any())).thenReturn(buildResult("convert", 0.666667, "YARDS", "LengthUnit"));
        doPost("convert", new QuantityInputDTO(twentyFourInches, zeroYards, null)).andExpect(status().isOk()).andExpect(jsonPath("$.resultUnit").value("YARDS"));
    }

    @Test public void testBackwardCompatibility_UC6_AddFeetAndInches() throws Exception {
        when(quantityMeasurementService.add(any(), any())).thenReturn(buildResult("add", 4.0, "FEET", "LengthUnit"));
        doPost("add", new QuantityInputDTO(twoFeet, twentyFourInches, null)).andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(4.0));
    }

    @Test public void testBackwardCompatibility_UC7_AddWithTargetUnit() throws Exception {
        when(quantityMeasurementService.add(any(), any(), any())).thenReturn(buildResult("add", 1.333333, "YARDS", "LengthUnit"));
        doPost("add", new QuantityInputDTO(twoFeet, twentyFourInches, zeroYards)).andExpect(status().isOk()).andExpect(jsonPath("$.resultUnit").value("YARDS"));
    }

    @Test public void testBackwardCompatibility_SubtractFeetMinusInches() throws Exception {
        when(quantityMeasurementService.subtract(any(), any())).thenReturn(buildResult("subtract", 9.5, "FEET", "LengthUnit"));
        doPost("subtract", new QuantityInputDTO(new QuantityDTO(10.0, QuantityDTO.LengthUnit.FEET), new QuantityDTO(6.0, QuantityDTO.LengthUnit.INCHES), null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(9.5));
    }

    @Test public void testBackwardCompatibility_DivideEqualQuantities() throws Exception {
        when(quantityMeasurementService.divide(any(), any())).thenReturn(QuantityMeasurementDTO.builder().operation("divide").resultValue(1.0).error(false).build());
        doPost("divide", new QuantityInputDTO(twoFeet, twentyFourInches, null)).andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(1.0));
    }

    // =========================================================================
    // VALIDATION / GET endpoints
    // =========================================================================

    @Test public void testCompareQuantities_InvalidInput_Returns400() throws Exception {
        mockMvc.perform(post("/api/v1/quantities/compare").contentType(MediaType.APPLICATION_JSON)
            .content("{\"thisQuantityDTO\":null,\"thatQuantityDTO\":null}")).andExpect(status().isBadRequest());
    }

    @Test public void testCompareQuantities_InvalidUnitName_Returns400() throws Exception {
        String bad = "{\"thisQuantityDTO\":{\"value\":1.0,\"unit\":\"FOOT\",\"measurementType\":\"LengthUnit\"},"
            + "\"thatQuantityDTO\":{\"value\":12.0,\"unit\":\"INCHES\",\"measurementType\":\"LengthUnit\"}}";
        mockMvc.perform(post("/api/v1/quantities/compare").contentType(MediaType.APPLICATION_JSON).content(bad))
            .andExpect(status().isBadRequest());
    }

    @Test public void testGetOperationHistory_ReturnsListOfDTOs() throws Exception {
        when(quantityMeasurementService.getHistoryByOperation("compare")).thenReturn(List.of(equalResult));
        mockMvc.perform(get("/api/v1/quantities/history/operation/compare")).andExpect(status().isOk()).andExpect(jsonPath("$[0].operation").value("compare"));
    }

    @Test public void testGetMeasurementHistory_ByType_ReturnsList() throws Exception {
        when(quantityMeasurementService.getHistoryByMeasurementType("LengthUnit")).thenReturn(List.of(equalResult));
        mockMvc.perform(get("/api/v1/quantities/history/type/LengthUnit")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test public void testGetErrorHistory_ReturnsErrorRecords() throws Exception {
        when(quantityMeasurementService.getErrorHistory()).thenReturn(
            List.of(QuantityMeasurementDTO.builder().operation("add").error(true).errorMessage("Incompatible types").build()));
        mockMvc.perform(get("/api/v1/quantities/history/errored")).andExpect(status().isOk()).andExpect(jsonPath("$[0].error").value(true));
    }

    @Test public void testGetOperationCount_ReturnsCount() throws Exception {
        when(quantityMeasurementService.getOperationCount("COMPARE")).thenReturn(5L);
        mockMvc.perform(get("/api/v1/quantities/count/COMPARE")).andExpect(status().isOk()).andExpect(content().string("5"));
    }
}