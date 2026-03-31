package com.app.quantitymeasurement.integration;

import com.app.quantitymeasurement.model.QuantityDTO;
import com.app.quantitymeasurement.model.QuantityInputDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementEntity;
import com.app.quantitymeasurement.repository.QuantityMeasurementRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QuantityMeasurementApplicationTests
 *
 * Full integration tests for the Quantity Measurement Spring Boot application.
 *
 * Uses @SpringBootTest to start the complete application context on a random port,
 * and TestRestTemplate to perform actual HTTP requests against the running server.
 * The H2 in-memory database is used for isolation — each test run starts clean.
 *
 * These tests verify the entire application stack:
 * - REST controller receives and validates requests
 * - Service layer performs business logic
 * - Repository persists results via Spring Data JPA
 * - Responses are serialized to JSON correctly
 * - Exception handling returns structured error responses
 * - Swagger UI is accessible
 * - H2 Console is accessible
 * - Actuator health endpoint returns UP
 *
 * Test class name matches the Spring Initializr convention for integration tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class QuantityMeasurementIntegrationTest {

    /**
     * Injected local server port — dynamically assigned during tests to avoid port conflicts.
     */
    @LocalServerPort
    private int port;

    /**
     * TestRestTemplate for performing actual HTTP requests to the running server.
     * Configured by Spring Boot automatically for integration tests.
     */
    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * JPA repository injected directly for verifying database state in tests.
     */
    @Autowired
    private QuantityMeasurementRepository repository;

    /**
     * Base URL for all API requests — constructed using the random port.
     */
    private String baseUrl;

    /**
     * Shared test DTOs for quantity operations.
     */
    private QuantityDTO feetDTO;
    private QuantityDTO inchesDTO;

    /**
     * Sets up common test data and clears the repository before each test.
     * This ensures test isolation — each test starts with a clean database state.
     */
    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/quantities";
        repository.deleteAll();

        feetDTO   = new QuantityDTO(1.0, QuantityDTO.LengthUnit.FEET);
        inchesDTO = new QuantityDTO(12.0, QuantityDTO.LengthUnit.INCHES);
    }

    // =========================================================================
    // Application context and server startup
    // =========================================================================

    /**
     * testSpringBootApplicationStarts
     * Verifies that the Spring application context loads successfully.
     * If this test passes, the application wired up correctly.
     */
    @Test
    public void testSpringBootApplicationStarts() {
        /*
         * If this test method runs without throwing, the application context
         * started successfully and all beans were registered correctly.
         */
        assertNotNull(restTemplate);
        assertNotNull(repository);
    }

    // =========================================================================
    // POST /compare
    // =========================================================================

    /**
     * testRestEndpointCompareQuantities
     * Verifies POST /compare with 1 FEET and 12 INCHES returns true (equal).
     */
    @Test
    public void testRestEndpointCompareQuantities() {
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);

        ResponseEntity<QuantityMeasurementDTO> response = restTemplate.postForEntity(
            baseUrl + "/compare", input, QuantityMeasurementDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("compare", response.getBody().getOperation());
        assertEquals("true", response.getBody().getResultString());
        assertFalse(response.getBody().isError());
    }

    /**
     * testRestEndpointCompareQuantities_NotEqual
     * Verifies POST /compare with non-equal quantities returns false.
     */
    @Test
    public void testRestEndpointCompareQuantities_NotEqual() {
        QuantityDTO twoFeet = new QuantityDTO(2.0, QuantityDTO.LengthUnit.FEET);
        QuantityInputDTO input = new QuantityInputDTO(twoFeet, inchesDTO, null);

        ResponseEntity<QuantityMeasurementDTO> response = restTemplate.postForEntity(
            baseUrl + "/compare", input, QuantityMeasurementDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("false", response.getBody().getResultString());
    }

    // =========================================================================
    // POST /convert
    // =========================================================================

    /**
     * testRestEndpointConvertQuantities
     * Verifies POST /convert converts 1 FEET to 12 INCHES correctly.
     */
    @Test
    public void testRestEndpointConvertQuantities() {
        QuantityDTO targetDTO = new QuantityDTO(0.0, QuantityDTO.LengthUnit.INCHES);
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, targetDTO, null);

        ResponseEntity<QuantityMeasurementDTO> response = restTemplate.postForEntity(
            baseUrl + "/convert", input, QuantityMeasurementDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("convert", response.getBody().getOperation());
        assertEquals(12.0, response.getBody().getResultValue(), 1e-4);
        assertFalse(response.getBody().isError());
    }

    // =========================================================================
    // POST /add
    // =========================================================================

    /**
     * testRestEndpointAddQuantities
     * Verifies POST /add with 1 FEET + 12 INCHES = 2 FEET.
     */
    @Test
    public void testRestEndpointAddQuantities() {
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);

        ResponseEntity<QuantityMeasurementDTO> response = restTemplate.postForEntity(
            baseUrl + "/add", input, QuantityMeasurementDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("add", response.getBody().getOperation());
        assertEquals(2.0, response.getBody().getResultValue(), 1e-4);
        assertEquals("FEET", response.getBody().getResultUnit());
        assertFalse(response.getBody().isError());
    }

    /**
     * testRestEndpointAddQuantities_WithTargetUnit
     * Verifies POST /add with targetUnitDTO converts result to YARDS.
     */
    @Test
    public void testRestEndpointAddQuantities_WithTargetUnit() {
        QuantityDTO yardsTarget = new QuantityDTO(0.0, QuantityDTO.LengthUnit.YARDS);
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, yardsTarget);

        ResponseEntity<QuantityMeasurementDTO> response = restTemplate.postForEntity(
            baseUrl + "/add", input, QuantityMeasurementDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("YARDS", response.getBody().getResultUnit());
        assertTrue(response.getBody().getResultValue() > 0.0);
    }

    // =========================================================================
    // POST /subtract
    // =========================================================================

    /**
     * testRestEndpointSubtractQuantities
     * Verifies POST /subtract with 1 FEET - 12 INCHES = 0 FEET.
     */
    @Test
    public void testRestEndpointSubtractQuantities() {
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);

        ResponseEntity<QuantityMeasurementDTO> response = restTemplate.postForEntity(
            baseUrl + "/subtract", input, QuantityMeasurementDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("subtract", response.getBody().getOperation());
        assertEquals(0.0, response.getBody().getResultValue(), 1e-4);
    }

    // =========================================================================
    // POST /divide
    // =========================================================================

    /**
     * testRestEndpointDivideQuantities
     * Verifies POST /divide with 1 FEET / 1 FEET = 1.0.
     */
    @Test
    public void testRestEndpointDivideQuantities() {
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, feetDTO, null);

        ResponseEntity<QuantityMeasurementDTO> response = restTemplate.postForEntity(
            baseUrl + "/divide", input, QuantityMeasurementDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("divide", response.getBody().getOperation());
        assertEquals(1.0, response.getBody().getResultValue(), 1e-4);
    }

    // =========================================================================
    // Invalid input — 400 Bad Request
    // =========================================================================

    /**
     * testRestEndpointInvalidInput_InvalidUnit_Returns400
     * Verifies that invalid unit names trigger validation and return 400.
     */
    @Test
    public void testRestEndpointInvalidInput_InvalidUnit_Returns400() {
        String badJson = "{"
            + "\"thisQuantityDTO\": {\"value\": 1.0, \"unit\": \"FOOT\", \"measurementType\": \"LengthUnit\"},"
            + "\"thatQuantityDTO\": {\"value\": 12.0, \"unit\": \"INCHE\", \"measurementType\": \"LengthUnit\"}"
            + "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(badJson, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/compare", entity, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("status"));
        assertEquals(400, response.getBody().get("status"));
    }

    /**
     * testRestEndpointIncompatibleTypes_Returns400
     * Verifies that adding a LengthUnit to a WeightUnit returns 400.
     */
    @Test
    public void testRestEndpointIncompatibleTypes_Returns400() {
        QuantityDTO kilogramDTO = new QuantityDTO(1.0, QuantityDTO.WeightUnit.KILOGRAM);
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, kilogramDTO, null);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/add", input, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").toString().contains("LengthUnit"));
    }

    // =========================================================================
    // GET /history/operation/{operation}
    // =========================================================================

    /**
     * testGetOperationHistory_AfterCompare_ReturnsRecord
     * Verifies that after a compare operation, the history endpoint returns the record.
     */
    @Test
    public void testGetOperationHistory_AfterCompare_ReturnsRecord() {
        // First perform a compare
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);
        restTemplate.postForEntity(baseUrl + "/compare", input, QuantityMeasurementDTO.class);

        // Then retrieve history
        ResponseEntity<List<QuantityMeasurementDTO>> response = restTemplate.exchange(
            baseUrl + "/history/operation/compare",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<QuantityMeasurementDTO>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        assertEquals("compare", response.getBody().get(0).getOperation());
    }

    // =========================================================================
    // GET /history/type/{measurementType}
    // =========================================================================

    /**
     * testGetHistoryByType_AfterOperations_ReturnsMatchingRecords
     * Verifies that history by measurement type returns correct records.
     */
    @Test
    public void testGetHistoryByType_AfterOperations_ReturnsMatchingRecords() {
        // Perform two operations
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);
        restTemplate.postForEntity(baseUrl + "/compare", input, QuantityMeasurementDTO.class);
        restTemplate.postForEntity(baseUrl + "/add", input, QuantityMeasurementDTO.class);

        ResponseEntity<List<QuantityMeasurementDTO>> response = restTemplate.exchange(
            baseUrl + "/history/type/LengthUnit",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<QuantityMeasurementDTO>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().size() >= 2);
    }

    // =========================================================================
    // GET /history/errored
    // =========================================================================

    /**
     * testGetErrorHistory_AfterFailedOperation_ReturnsErrorRecord
     * Verifies that failed operations are recorded in the error history.
     */
    @Test
    public void testGetErrorHistory_AfterFailedOperation_ReturnsErrorRecord() {
        // Trigger an error by adding incompatible types
        QuantityDTO kilogramDTO = new QuantityDTO(1.0, QuantityDTO.WeightUnit.KILOGRAM);
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, kilogramDTO, null);
        restTemplate.postForEntity(baseUrl + "/add", input, Map.class);

        ResponseEntity<List<QuantityMeasurementDTO>> response = restTemplate.exchange(
            baseUrl + "/history/errored",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<QuantityMeasurementDTO>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        assertTrue(response.getBody().get(0).isError());
    }

    // =========================================================================
    // GET /count/{operation}
    // =========================================================================

    /**
     * testGetOperationCount_AfterCompare_ReturnsCorrectCount
     * Verifies the count endpoint returns the number of successful compare operations.
     */
    @Test
    public void testGetOperationCount_AfterCompare_ReturnsCorrectCount() {
        // Perform two compare operations
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);
        restTemplate.postForEntity(baseUrl + "/compare", input, QuantityMeasurementDTO.class);
        restTemplate.postForEntity(baseUrl + "/compare", input, QuantityMeasurementDTO.class);

        ResponseEntity<Long> response = restTemplate.getForEntity(
            baseUrl + "/count/compare", Long.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2L, response.getBody());
    }

    // =========================================================================
    // JPA Repository — direct database verification
    // =========================================================================

    /**
     * testJPARepositoryFindByOperation_ReturnsCorrectEntities
     * Verifies Spring Data JPA repository findByOperation() works correctly.
     */
    @Test
    public void testJPARepositoryFindByOperation_ReturnsCorrectEntities() {
        // Perform an add operation via REST (which saves to DB)
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);
        restTemplate.postForEntity(baseUrl + "/add", input, QuantityMeasurementDTO.class);

        // Query repository directly
        List<QuantityMeasurementEntity> entities = repository.findByOperation("add");
        assertFalse(entities.isEmpty());
        assertEquals("add", entities.get(0).getOperation());
    }

    /**
     * testJPARepositoryFindByIsErrorTrue_ReturnsErrorEntities
     * Verifies findByErrorTrue() returns only error records.
     */
    @Test
    public void testJPARepositoryFindByIsErrorTrue_ReturnsErrorEntities() {
        // First a successful operation
        QuantityInputDTO goodInput = new QuantityInputDTO(feetDTO, inchesDTO, null);
        restTemplate.postForEntity(baseUrl + "/compare", goodInput, QuantityMeasurementDTO.class);

        // Then a failing operation
        QuantityDTO kilogramDTO = new QuantityDTO(1.0, QuantityDTO.WeightUnit.KILOGRAM);
        QuantityInputDTO badInput = new QuantityInputDTO(feetDTO, kilogramDTO, null);
        restTemplate.postForEntity(baseUrl + "/add", badInput, Map.class);

        List<QuantityMeasurementEntity> errors = repository.findByErrorTrue();
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().allMatch(QuantityMeasurementEntity::isError));
    }

    /**
     * testJPARepositoryCountByOperationAndIsErrorFalse
     * Verifies countByOperationAndErrorFalse() counts only successful operations.
     */
    @Test
    public void testJPARepositoryCountByOperationAndIsErrorFalse() {
        // Two compare operations
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);
        restTemplate.postForEntity(baseUrl + "/compare", input, QuantityMeasurementDTO.class);
        restTemplate.postForEntity(baseUrl + "/compare", input, QuantityMeasurementDTO.class);

        long count = repository.countByOperationAndErrorFalse("compare");
        assertEquals(2L, count);
    }

    // =========================================================================
    // Actuator health endpoint
    // =========================================================================

    /**
     * testActuatorHealthEndpoint_ReturnsUp
     * Verifies /actuator/health returns status UP.
     */
    @Test
    public void testActuatorHealthEndpoint_ReturnsUp() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/actuator/health", Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
    }

    // =========================================================================
    // H2 Console
    // =========================================================================

    /**
     * testH2ConsoleLaunches
     * Verifies the H2 console page loads at /h2-console.
     */
    @Test
    public void testH2ConsoleLaunches() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/h2-console", String.class);

        /*
         * H2 console returns 200 OK with an HTML page (or a redirect).
         * We just verify it does not return 404 or 500.
         */
        assertNotEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // =========================================================================
    // Swagger UI
    // =========================================================================

    /**
     * testSwaggerUILoads
     * Verifies the Swagger UI HTML loads at /swagger-ui.html.
     */
    @Test
    public void testSwaggerUILoads() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/swagger-ui.html", String.class);

        assertNotEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * testOpenAPIDocumentation_Returns200
     * Verifies the OpenAPI JSON spec is available at /v3/api-docs.
     */
    @Test
    public void testOpenAPIDocumentation_Returns200() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/v3/api-docs", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("openapi") || response.getBody().contains("paths"));
    }

    // =========================================================================
    // Content negotiation
    // =========================================================================

    /**
     * testContentNegotiation_ResponseIsJSON
     * Verifies that all API responses use Content-Type application/json.
     */
    @Test
    public void testContentNegotiation_ResponseIsJSON() {
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);

        ResponseEntity<QuantityMeasurementDTO> response = restTemplate.postForEntity(
            baseUrl + "/compare", input, QuantityMeasurementDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().toString()
            .contains(MediaType.APPLICATION_JSON_VALUE));
    }

    // =========================================================================
    // Multi-operation integration flow
    // =========================================================================

    /**
     * testIntegrationTest_MultipleOperations_AllPersisted
     * Starts a full scenario: compare, convert, add — verifies all are persisted.
     */
    @Test
    public void testIntegrationTest_MultipleOperations_AllPersisted() {
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);

        restTemplate.postForEntity(baseUrl + "/compare", input, QuantityMeasurementDTO.class);

        QuantityDTO targetInches = new QuantityDTO(0.0, QuantityDTO.LengthUnit.INCHES);
        QuantityInputDTO convertInput = new QuantityInputDTO(feetDTO, targetInches, null);
        restTemplate.postForEntity(baseUrl + "/convert", convertInput, QuantityMeasurementDTO.class);

        restTemplate.postForEntity(baseUrl + "/add", input, QuantityMeasurementDTO.class);

        // Verify all 3 records are in the database
        List<QuantityMeasurementEntity> all = repository.findAll();
        assertEquals(3, all.size());
    }
}