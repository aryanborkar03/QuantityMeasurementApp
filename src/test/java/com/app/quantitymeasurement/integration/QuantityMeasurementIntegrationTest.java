package com.app.quantitymeasurement.integration;

import com.app.quantitymeasurement.model.QuantityDTO;
import com.app.quantitymeasurement.model.QuantityInputDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementDTO;
import com.app.quantitymeasurement.entity.QuantityMeasurementEntity; // ✅ FIXED
import com.app.quantitymeasurement.repository.QuantityMeasurementRepository;

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
public class QuantityMeasurementIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private QuantityMeasurementRepository repository;

    private String baseUrl;

    private QuantityDTO feetDTO;
    private QuantityDTO inchesDTO;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/quantities";
        repository.deleteAll();

        feetDTO   = new QuantityDTO(1.0, QuantityDTO.LengthUnit.FEET);
        inchesDTO = new QuantityDTO(12.0, QuantityDTO.LengthUnit.INCHES);
    }

    @Test
    public void testSpringBootApplicationStarts() {
        assertNotNull(restTemplate);
        assertNotNull(repository);
    }

    @Test
    public void testRestEndpointCompareQuantities() {
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);

        ResponseEntity<QuantityMeasurementDTO> response =
                restTemplate.postForEntity(baseUrl + "/compare", input, QuantityMeasurementDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("true", response.getBody().getResultString());
    }

    @Test
    public void testRestEndpointAddQuantities() {
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);

        ResponseEntity<QuantityMeasurementDTO> response =
                restTemplate.postForEntity(baseUrl + "/add", input, QuantityMeasurementDTO.class);

        assertEquals(2.0, response.getBody().getResultValue(), 1e-4);
    }

    @Test
    public void testJPARepositoryFindByOperation() {
        QuantityInputDTO input = new QuantityInputDTO(feetDTO, inchesDTO, null);
        restTemplate.postForEntity(baseUrl + "/add", input, QuantityMeasurementDTO.class);

        List<QuantityMeasurementEntity> list = repository.findByOperation("add");

        assertFalse(list.isEmpty());
    }
}
