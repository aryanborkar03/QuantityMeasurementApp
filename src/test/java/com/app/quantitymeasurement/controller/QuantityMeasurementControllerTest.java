package com.app.quantitymeasurement.controller;

import com.app.quantitymeasurement.config.SecurityConfig;
import com.app.quantitymeasurement.dto.response.QuantityDTO;
import com.app.quantitymeasurement.dto.request.QuantityInputDTO;
import com.app.quantitymeasurement.dto.request.QuantityMeasurementDTO;
import com.app.quantitymeasurement.service.IQuantityMeasurementService;
import com.app.quantitymeasurement.repository.UserRepository;
import com.app.quantitymeasurement.security.oauth2.CustomOAuth2UserService;
import com.app.quantitymeasurement.security.CustomUserDetailsService;
import com.app.quantitymeasurement.security.jwt.JwtAuthenticationEntryPoint;
import com.app.quantitymeasurement.security.jwt.JwtAuthenticationFilter;
import com.app.quantitymeasurement.security.jwt.JwtAccessDeniedHandler;
import com.app.quantitymeasurement.security.jwt.JwtTokenProvider;
import com.app.quantitymeasurement.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.app.quantitymeasurement.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class QuantityMeasurementControllerTest {

    private static final double EPSILON = 1e-6;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private IQuantityMeasurementService quantityMeasurementService;

    // UC18 security beans required by SecurityConfig in @WebMvcTest context
    @MockBean  private JwtAuthenticationFilter    jwtAuthenticationFilter;
    @MockBean  private JwtTokenProvider           jwtTokenProvider;
    @MockBean  private CustomUserDetailsService   customUserDetailsService;
    @MockBean  private CustomOAuth2UserService    customOAuth2UserService;
    @MockBean  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @MockBean  private JwtAccessDeniedHandler     jwtAccessDeniedHandler;
    @MockBean  private OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    @MockBean  private OAuth2AuthenticationFailureHandler oAuth2FailureHandler;
    @MockBean  private UserRepository             userRepository;
    @MockBean  private PasswordEncoder            passwordEncoder;

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
    @WithMockUser(roles = "USER")
    @Test public void testLayerSeparation_ControllerIndependence_StubService() throws Exception {
        when(quantityMeasurementService.compare(any(), any())).thenReturn(equalResult);
        doPost("compare", new QuantityInputDTO(twoFeet, twentyFourInches, null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultString").value("true"));
        Mockito.verify(quantityMeasurementService, Mockito.times(1)).compare(any(QuantityDTO.class), any(QuantityDTO.class));
    }

    /** testController_NullBody_Returns400 — null body causes 400, controller has no null guard */
    @WithMockUser(roles = "USER")
    @Test public void testController_NullBody_Returns400() throws Exception {
        mockMvc.perform(post("/api/v1/quantities/compare").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // COMPARISON
    // =========================================================================

    /** testPerformComparison_Equal_ReturnsTrue (spec 15) */
    @WithMockUser(roles = "USER")
    @Test public void testPerformComparison_Equal_ReturnsTrue() throws Exception {
        when(quantityMeasurementService.compare(any(), any())).thenReturn(equalResult);
        doPost("compare", new QuantityInputDTO(twoFeet, twentyFourInches, null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultString").value("true")).andExpect(jsonPath("$.error").value(false));
    }

    /** testPerformComparison_NotEqual_ReturnsFalse */
    @WithMockUser(roles = "USER")
    @Test public void testPerformComparison_NotEqual_ReturnsFalse() throws Exception {
        when(quantityMeasurementService.compare(any(), any())).thenReturn(notEqualResult);
        doPost("compare", new QuantityInputDTO(new QuantityDTO(1.0, QuantityDTO.LengthUnit.FEET), twentyFourInches, null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultString").value("false"));
    }

    // =========================================================================
    // CONVERSION
    // =========================================================================

    /** testPerformConversion_InchesToYards_CorrectResult (spec 16) */
    @WithMockUser(roles = "USER")
    @Test public void testPerformConversion_InchesToYards_CorrectResult() throws Exception {
        when(quantityMeasurementService.convert(any(), any())).thenReturn(
            QuantityMeasurementDTO.builder().operation("convert").resultValue(0.666667).resultUnit("YARDS").error(false).build());
        doPost("convert", new QuantityInputDTO(twentyFourInches, zeroYards, null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultUnit").value("YARDS"));
    }

    /** testPerformConversion_FeetToInches_CorrectResult */
    @WithMockUser(roles = "USER")
    @Test public void testPerformConversion_FeetToInches_CorrectResult() throws Exception {
        when(quantityMeasurementService.convert(any(), any())).thenReturn(
            QuantityMeasurementDTO.builder().operation("convert").resultValue(24.0).resultUnit("INCHES").error(false).build());
        doPost("convert", new QuantityInputDTO(twoFeet, new QuantityDTO(0.0, QuantityDTO.LengthUnit.INCHES), null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(24.0));
    }

    /** testPerformConversion_Temperature_CelsiusToFahrenheit */
    @WithMockUser(roles = "USER")
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
    @WithMockUser(roles = "USER")
    @Test public void testPerformAddition_TwoOperands_DefaultUnit() throws Exception {
        when(quantityMeasurementService.add(any(), any())).thenReturn(buildResult("add", 4.0, "FEET", "LengthUnit"));
        doPost("add", new QuantityInputDTO(twoFeet, twentyFourInches, null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(4.0)).andExpect(jsonPath("$.resultUnit").value("FEET"));
    }

    /** testPerformAddition_ThreeOperands_ExplicitTargetUnit */
    @WithMockUser(roles = "USER")
    @Test public void testPerformAddition_ThreeOperands_ExplicitTargetUnit() throws Exception {
        when(quantityMeasurementService.add(any(), any(), any())).thenReturn(buildResult("add", 1.333333, "YARDS", "LengthUnit"));
        doPost("add", new QuantityInputDTO(twoFeet, twentyFourInches, zeroYards))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultUnit").value("YARDS"));
    }

    /** testPerformAddition_Weight_KilogramPlusGram */
    @WithMockUser(roles = "USER")
    @Test public void testPerformAddition_Weight_KilogramPlusGram() throws Exception {
        when(quantityMeasurementService.add(any(), any())).thenReturn(buildResult("add", 2.0, "KILOGRAM", "WeightUnit"));
        doPost("add", new QuantityInputDTO(new QuantityDTO(1.0, QuantityDTO.WeightUnit.KILOGRAM),
            new QuantityDTO(1000.0, QuantityDTO.WeightUnit.GRAM), null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(2.0)).andExpect(jsonPath("$.resultUnit").value("KILOGRAM"));
    }

    /** testPerformAddition_Volume_LitrePlusMillilitre */
    @WithMockUser(roles = "USER")
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
    @WithMockUser(roles = "USER")
    @Test public void testPerformSubtraction_TwoOperands_DefaultUnit() throws Exception {
        when(quantityMeasurementService.subtract(any(), any())).thenReturn(buildResult("subtract", 0.0, "FEET", "LengthUnit"));
        doPost("subtract", new QuantityInputDTO(twoFeet, twentyFourInches, null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(0.0)).andExpect(jsonPath("$.resultUnit").value("FEET"));
    }

    /** testPerformSubtraction_ThreeOperands_ExplicitTargetUnit */
    @WithMockUser(roles = "USER")
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
    @WithMockUser(roles = "USER")
    @Test public void testPerformDivision_EqualQuantities_ReturnsOne() throws Exception {
        when(quantityMeasurementService.divide(any(), any())).thenReturn(
            QuantityMeasurementDTO.builder().operation("divide").resultValue(1.0).error(false).build());
        doPost("divide", new QuantityInputDTO(twoFeet, twentyFourInches, null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(1.0));
    }

    /** testPerformDivision_FourFeetOverTwoFeet_ReturnsTwo */
    @WithMockUser(roles = "USER")
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
    @WithMockUser(roles = "USER")
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
    @WithMockUser(roles = "USER")
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

    @WithMockUser(roles = "USER")
    @Test public void testBackwardCompatibility_UC1_CompareEqualLengths() throws Exception {
        when(quantityMeasurementService.compare(any(), any())).thenReturn(equalResult);
        doPost("compare", new QuantityInputDTO(twoFeet, twentyFourInches, null)).andExpect(status().isOk()).andExpect(jsonPath("$.resultString").value("true"));
    }

    @WithMockUser(roles = "USER")
    @Test public void testBackwardCompatibility_UC5_ConvertInchesToYards() throws Exception {
        when(quantityMeasurementService.convert(any(), any())).thenReturn(buildResult("convert", 0.666667, "YARDS", "LengthUnit"));
        doPost("convert", new QuantityInputDTO(twentyFourInches, zeroYards, null)).andExpect(status().isOk()).andExpect(jsonPath("$.resultUnit").value("YARDS"));
    }

    @WithMockUser(roles = "USER")
    @Test public void testBackwardCompatibility_UC6_AddFeetAndInches() throws Exception {
        when(quantityMeasurementService.add(any(), any())).thenReturn(buildResult("add", 4.0, "FEET", "LengthUnit"));
        doPost("add", new QuantityInputDTO(twoFeet, twentyFourInches, null)).andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(4.0));
    }

    @WithMockUser(roles = "USER")
    @Test public void testBackwardCompatibility_UC7_AddWithTargetUnit() throws Exception {
        when(quantityMeasurementService.add(any(), any(), any())).thenReturn(buildResult("add", 1.333333, "YARDS", "LengthUnit"));
        doPost("add", new QuantityInputDTO(twoFeet, twentyFourInches, zeroYards)).andExpect(status().isOk()).andExpect(jsonPath("$.resultUnit").value("YARDS"));
    }

    @WithMockUser(roles = "USER")
    @Test public void testBackwardCompatibility_SubtractFeetMinusInches() throws Exception {
        when(quantityMeasurementService.subtract(any(), any())).thenReturn(buildResult("subtract", 9.5, "FEET", "LengthUnit"));
        doPost("subtract", new QuantityInputDTO(new QuantityDTO(10.0, QuantityDTO.LengthUnit.FEET), new QuantityDTO(6.0, QuantityDTO.LengthUnit.INCHES), null))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(9.5));
    }

    @WithMockUser(roles = "USER")
    @Test public void testBackwardCompatibility_DivideEqualQuantities() throws Exception {
        when(quantityMeasurementService.divide(any(), any())).thenReturn(QuantityMeasurementDTO.builder().operation("divide").resultValue(1.0).error(false).build());
        doPost("divide", new QuantityInputDTO(twoFeet, twentyFourInches, null)).andExpect(status().isOk()).andExpect(jsonPath("$.resultValue").value(1.0));
    }

    // =========================================================================
    // VALIDATION / GET endpoints
    // =========================================================================

    @WithMockUser(roles = "USER")
    @Test public void testCompareQuantities_InvalidInput_Returns400() throws Exception {
        mockMvc.perform(post("/api/v1/quantities/compare").contentType(MediaType.APPLICATION_JSON)
            .content("{\"thisQuantityDTO\":null,\"thatQuantityDTO\":null}")).andExpect(status().isBadRequest());
    }

    @WithMockUser(roles = "USER")
    @Test public void testCompareQuantities_InvalidUnitName_Returns400() throws Exception {
        String bad = "{\"thisQuantityDTO\":{\"value\":1.0,\"unit\":\"FOOT\",\"measurementType\":\"LengthUnit\"},"
            + "\"thatQuantityDTO\":{\"value\":12.0,\"unit\":\"INCHES\",\"measurementType\":\"LengthUnit\"}}";
        mockMvc.perform(post("/api/v1/quantities/compare").contentType(MediaType.APPLICATION_JSON).content(bad))
            .andExpect(status().isBadRequest());
    }

    @WithMockUser(roles = "USER")
    @Test public void testGetOperationHistory_ReturnsListOfDTOs() throws Exception {
        when(quantityMeasurementService.getHistoryByOperation("compare")).thenReturn(List.of(equalResult));
        mockMvc.perform(get("/api/v1/quantities/history/operation/compare")).andExpect(status().isOk()).andExpect(jsonPath("$[0].operation").value("compare"));
    }

    @WithMockUser(roles = "USER")
    @Test public void testGetMeasurementHistory_ByType_ReturnsList() throws Exception {
        when(quantityMeasurementService.getHistoryByMeasurementType("LengthUnit")).thenReturn(List.of(equalResult));
        mockMvc.perform(get("/api/v1/quantities/history/type/LengthUnit")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @WithMockUser(roles = "ADMIN")
    @Test public void testGetErrorHistory_ReturnsErrorRecords() throws Exception {
        when(quantityMeasurementService.getErrorHistory()).thenReturn(
            List.of(QuantityMeasurementDTO.builder().operation("add").error(true).errorMessage("Incompatible types").build()));
        mockMvc.perform(get("/api/v1/quantities/history/errored")).andExpect(status().isOk()).andExpect(jsonPath("$[0].error").value(true));
    }

    @WithMockUser(roles = "USER")
    @Test public void testGetOperationCount_ReturnsCount() throws Exception {
        when(quantityMeasurementService.getOperationCount("COMPARE")).thenReturn(5L);
        mockMvc.perform(get("/api/v1/quantities/count/COMPARE")).andExpect(status().isOk()).andExpect(content().string("5"));
    }
}