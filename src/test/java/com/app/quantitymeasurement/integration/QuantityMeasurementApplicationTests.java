package com.app.quantitymeasurement.integration;

import com.app.quantitymeasurement.dto.request.AuthRequest;
import com.app.quantitymeasurement.dto.response.AuthResponse;
import com.app.quantitymeasurement.dto.request.RegisterRequest;
import com.app.quantitymeasurement.dto.response.QuantityDTO;
import com.app.quantitymeasurement.dto.request.QuantityInputDTO;
import com.app.quantitymeasurement.dto.request.QuantityMeasurementDTO;
import com.app.quantitymeasurement.entity.QuantityMeasurementEntity;
import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.enums.AuthProvider;
import com.app.quantitymeasurement.enums.Role;
import com.app.quantitymeasurement.repository.QuantityMeasurementRepository;
import com.app.quantitymeasurement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

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


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class QuantityMeasurementApplicationTests {

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Base URL for all API requests — constructed using the random port.
     */
    private String baseUrl;

    private String authUrl;
    private String testToken;
    private String adminToken;

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
    void setUp() {
        baseUrl  = "http://localhost:" + port + "/api/v1/quantities";
        authUrl  = "http://localhost:" + port + "/api/v1/auth";
        repository.deleteAll();
        userRepository.deleteAll();
        testToken = registerAndGetToken("test@example.com", "Strong@123");
        adminToken = registerAdminAndGetToken("admin@example.com", "Strong@123");

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
    void testSpringBootApplicationStarts() {
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
    void testRestEndpointCompareQuantities() {
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);

        ResponseEntity<QuantityMeasurementDTO> response = 
        	restTemplate.exchange(
                baseUrl + "/compare", 
                HttpMethod.POST,
                withToken(input, testToken), 
                QuantityMeasurementDTO.class
            );

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
    void testRestEndpointCompareQuantities_NotEqual() {
        QuantityDTO twoFeet = new QuantityDTO(2.0, QuantityDTO.LengthUnit.FEET);
        QuantityInputDTO input = new QuantityInputDTO(twoFeet, inchesDTO, null);

        ResponseEntity<QuantityMeasurementDTO> response = 
        	restTemplate.exchange(
                baseUrl + "/compare", 
                HttpMethod.POST,
                withToken(input, testToken), 
                QuantityMeasurementDTO.class
            );

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
    void testRestEndpointConvertQuantities() {
        QuantityDTO targetDTO = new QuantityDTO(0.0, QuantityDTO.LengthUnit.INCHES);
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, targetDTO, null);

        ResponseEntity<QuantityMeasurementDTO> response = 
        		restTemplate.exchange(
        			baseUrl + "/convert", 
        			HttpMethod.POST,
        		    withToken(input, testToken),
        			QuantityMeasurementDTO.class
        		);

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
    void testRestEndpointAddQuantities() {
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);

        ResponseEntity<QuantityMeasurementDTO> response = 
        	restTemplate.exchange(
                baseUrl + "/add", 
                HttpMethod.POST,
                withToken(input, testToken),
                QuantityMeasurementDTO.class
            );

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
    void testRestEndpointAddQuantities_WithTargetUnit() {
        QuantityDTO yardsTarget = new QuantityDTO(0.0, QuantityDTO.LengthUnit.YARDS);
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, yardsTarget);

        ResponseEntity<QuantityMeasurementDTO> response = 
        	restTemplate.exchange(
                baseUrl + "/add", 
                HttpMethod.POST,
                withToken(input, testToken), 
                QuantityMeasurementDTO.class
            );

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
    void testRestEndpointSubtractQuantities() {
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);

        ResponseEntity<QuantityMeasurementDTO> response = 
        	restTemplate.exchange(
                baseUrl + "/subtract", 
                HttpMethod.POST,
                withToken(input, testToken), 
                QuantityMeasurementDTO.class
            );

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
    void testRestEndpointDivideQuantities() {
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, feetDTO, null);

        ResponseEntity<QuantityMeasurementDTO> response = 
        	restTemplate.exchange(
    			baseUrl + "/divide", 
    			HttpMethod.POST,
    		    withToken(input, testToken),
    			QuantityMeasurementDTO.class
    		);

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
    void testRestEndpointInvalidInput_InvalidUnit_Returns400() {
        String badJson = "{"
            + "\"thisQuantityDTO\": {\"value\": 1.0, \"unit\": \"FOOT\", \"measurementType\": \"LengthUnit\"},"
            + "\"thatQuantityDTO\": {\"value\": 12.0, \"unit\": \"INCHE\", \"measurementType\": \"LengthUnit\"}"
            + "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(badJson, headers);

        ResponseEntity<Map> response = 
        	restTemplate.exchange(
                baseUrl + "/compare", 
                HttpMethod.POST,
                withToken(entity, testToken),
                Map.class
            );

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
    void testRestEndpointIncompatibleTypes_Returns400() {
        QuantityDTO kilogramDTO = new QuantityDTO(1.0, QuantityDTO.WeightUnit.KILOGRAM);
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, kilogramDTO, null);

        ResponseEntity<Map> response = 
        	restTemplate.exchange(
                baseUrl + "/add", 
                HttpMethod.POST,
                withToken(input, testToken), 
                Map.class
            );

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
    void testGetOperationHistory_AfterCompare_ReturnsRecord() {
        // First perform a compare
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);
        restTemplate.exchange(baseUrl + "/compare", HttpMethod.POST, withToken(input, testToken), QuantityMeasurementDTO.class);

        // Then retrieve history
        ResponseEntity<List<QuantityMeasurementDTO>> response = restTemplate.exchange(baseUrl + "/history/operation/compare", HttpMethod.GET, withToken(testToken),
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
    void testGetHistoryByType_AfterOperations_ReturnsMatchingRecords() {
        // Perform two operations
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);
        restTemplate.exchange(baseUrl + "/compare", HttpMethod.POST, withToken(input, testToken), QuantityMeasurementDTO.class);
        restTemplate.exchange(baseUrl + "/add", HttpMethod.POST, withToken(input, testToken), QuantityMeasurementDTO.class);

        ResponseEntity<List<QuantityMeasurementDTO>> response = restTemplate.exchange(baseUrl + "/history/type/LengthUnit", HttpMethod.GET, withToken(testToken),
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
    void testGetErrorHistory_AfterFailedOperation_ReturnsErrorRecord() {
        // Trigger an error by adding incompatible types
        QuantityDTO kilogramDTO = new QuantityDTO(1.0, QuantityDTO.WeightUnit.KILOGRAM);
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, kilogramDTO, null);
        restTemplate.exchange(
            baseUrl + "/add",
            HttpMethod.POST,
            withToken(input, adminToken),
            Map.class
        );

        ResponseEntity<List<QuantityMeasurementDTO>> response = 
        	restTemplate.exchange(
        		baseUrl + "/history/errored", 
        		HttpMethod.GET, 
        		withToken(adminToken),
        		new ParameterizedTypeReference<List<QuantityMeasurementDTO>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
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
    void testGetOperationCount_AfterCompare_ReturnsCorrectCount() {
        // Perform two compare operations
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);
        restTemplate.exchange(
        	baseUrl + "/compare", 
        	HttpMethod.POST, 
        	withToken(input, testToken), 
        	QuantityMeasurementDTO.class
        );
        restTemplate.exchange(
        	baseUrl + "/compare", 
        	HttpMethod.POST, 
        	withToken(input, testToken), 
        	QuantityMeasurementDTO.class
        );

        ResponseEntity<Long> response = 
        	restTemplate.exchange(
        		baseUrl + "/count/compare",
        		HttpMethod.GET,
        		withToken(input, testToken),
        		Long.class
            );

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
    void testJPARepositoryFindByOperation_ReturnsCorrectEntities() {
        // Perform an add operation via REST (which saves to DB)
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);
        restTemplate.exchange(baseUrl + "/add", HttpMethod.POST, withToken(input, testToken), QuantityMeasurementDTO.class);

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
    void testJPARepositoryFindByIsErrorTrue_ReturnsErrorEntities() {
        // First a successful operation
        QuantityInputDTO goodInput = new QuantityInputDTO(feetDTO, inchesDTO, null);
        restTemplate.exchange(baseUrl + "/compare", HttpMethod.POST, withToken(goodInput, testToken), QuantityMeasurementDTO.class);

        // Then a failing operation
        QuantityDTO kilogramDTO = new QuantityDTO(1.0, QuantityDTO.WeightUnit.KILOGRAM);
        QuantityInputDTO badInput = new QuantityInputDTO(feetDTO, kilogramDTO, null);
        restTemplate.exchange(baseUrl + "/add", HttpMethod.POST, withToken(badInput, testToken), Map.class);

        List<QuantityMeasurementEntity> errors = repository.findByErrorTrue();
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().allMatch(QuantityMeasurementEntity::isError));
    }

    /**
     * testJPARepositoryCountByOperationAndIsErrorFalse
     * Verifies countByOperationAndErrorFalse() counts only successful operations.
     */
    @Test
    void testJPARepositoryCountByOperationAndIsErrorFalse() {
        // Two compare operations
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);
        restTemplate.exchange(baseUrl + "/compare", HttpMethod.POST, withToken(input, testToken), QuantityMeasurementDTO.class);
        restTemplate.exchange(baseUrl + "/compare", HttpMethod.POST, withToken(input, testToken), QuantityMeasurementDTO.class);

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
    void testActuatorHealthEndpoint_ReturnsUp() {
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
    void testH2ConsoleLaunches() {
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
    void testSwaggerUILoads() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/swagger-ui.html", String.class);

        assertNotEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * testOpenAPIDocumentation_Returns200
     * Verifies the OpenAPI JSON spec is available at /v3/api-docs.
     */
    @Test
    void testOpenAPIDocumentation_Returns200() {
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
    void testContentNegotiation_ResponseIsJSON() {
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);

        ResponseEntity<QuantityMeasurementDTO> response = 
    		restTemplate.exchange(
        			baseUrl + "/compare", 
        			HttpMethod.POST,
        		    withToken(input, testToken),
        			QuantityMeasurementDTO.class
        		);

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
    void testIntegrationTest_MultipleOperations_AllPersisted() {
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);

        restTemplate.exchange(baseUrl + "/compare", HttpMethod.POST, withToken(input, testToken), QuantityMeasurementDTO.class);

        QuantityDTO targetInches = new QuantityDTO(0.0, QuantityDTO.LengthUnit.INCHES);
        QuantityInputDTO convertInput = new QuantityInputDTO(feetDTO, targetInches, null);
        restTemplate.exchange(baseUrl + "/convert", HttpMethod.POST, withToken(convertInput, testToken), QuantityMeasurementDTO.class);

        restTemplate.exchange(baseUrl + "/add", HttpMethod.POST, withToken(input, testToken), QuantityMeasurementDTO.class);

        // Verify all 3 records are in the database
        List<QuantityMeasurementEntity> all = repository.findAll();
        assertEquals(3, all.size());
    }
    // =========================================================================
    // UC18 Auth helpers — register a user and obtain a JWT for protected calls
    // =========================================================================

    private String registerAdminAndGetToken(String email, String password) {
        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setProvider(AuthProvider.LOCAL);
        admin.setRole(Role.ADMIN);
        
        userRepository.save(admin);

        AuthRequest loginReq = new AuthRequest(email, password);
        ResponseEntity<AuthResponse> resp =
            restTemplate.postForEntity(
                authUrl + "/login",
                loginReq,
                AuthResponse.class
            );

        return resp.getBody().getAccessToken();
    }
    
    /**
     * Registers a new user via POST /api/v1/auth/register and returns the JWT.
     * All quantity endpoint calls must include this token as a Bearer header.
     */
    private String registerAndGetToken(String email, String password) {
        RegisterRequest req = new RegisterRequest(email, password, "Test User");
        ResponseEntity<AuthResponse> resp = restTemplate.postForEntity(
            authUrl + "/register", req, AuthResponse.class);
        assertNotNull(resp.getBody(), "register response body must not be null");
        return resp.getBody().getAccessToken();
    }

    /**
     * Builds an HttpEntity with the Bearer token header and the given body.
     */
    private <T> HttpEntity<T> withToken(T body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    /**
     * Builds an HttpEntity with only the Bearer token header (no body, for GET requests).
     */
    private HttpEntity<Void> withToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

}
