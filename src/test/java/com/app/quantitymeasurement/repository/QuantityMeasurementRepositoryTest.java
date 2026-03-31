package com.app.quantitymeasurement.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.app.quantitymeasurement.entity.QuantityMeasurementEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QuantityMeasurementRepositoryTest
 *
 * Integration tests for the Spring Data JPA QuantityMeasurementRepository.
 *
 * @DataJpaTest configures a minimal Spring context with JPA/H2 only (no web layer).
 * Each test runs in a transaction that is rolled back after the test, ensuring isolation.
 *
 * Tests verify:
 * - findByOperation() returns correct records
 * - findByThisMeasurementType() filters correctly
 * - findByCreatedAtAfter() returns recent records
 * - findSuccessfulByOperation() (custom @Query) filters by isError=false
 * - countByOperationAndErrorFalse() counts correctly
 * - findByErrorTrue() returns only error records
 * - Standard JPA save and findAll work correctly
 */
@DataJpaTest
@ActiveProfiles("test")
public class QuantityMeasurementRepositoryTest {

    @Autowired
    private QuantityMeasurementRepository repository;

    @BeforeEach
    public void setUp() {
        repository.deleteAll();
    }

    // =========================================================================
    // Save and findAll
    // =========================================================================

    @Test
    public void testSave_AndFindAll_ReturnsPersistedEntity() {
        QuantityMeasurementEntity entity = buildEntity("compare", "FEET", "LengthUnit",
            "INCHES", "LengthUnit", "true", null, false, null);
        repository.save(entity);

        List<QuantityMeasurementEntity> all = repository.findAll();
        assertEquals(1, all.size());
        assertEquals("compare", all.get(0).getOperation());
    }

    @Test
    public void testSave_GeneratesId() {
        QuantityMeasurementEntity entity = buildEntity("add", "FEET", "LengthUnit",
            "INCHES", "LengthUnit", null, 2.0, false, null);
        QuantityMeasurementEntity saved = repository.save(entity);
        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);
    }

    // =========================================================================
    // findByOperation
    // =========================================================================

    @Test
    public void testFindByOperation_Compare_ReturnsOnlyCompareRecords() {
        repository.save(buildEntity("compare", "FEET", "LengthUnit",
            "INCHES", "LengthUnit", "true", null, false, null));
        repository.save(buildEntity("add", "FEET", "LengthUnit",
            "INCHES", "LengthUnit", null, 2.0, false, null));

        List<QuantityMeasurementEntity> result = repository.findByOperation("compare");
        assertEquals(1, result.size());
        assertEquals("compare", result.get(0).getOperation());
    }

    @Test
    public void testFindByOperation_NoMatch_ReturnsEmpty() {
        List<QuantityMeasurementEntity> result = repository.findByOperation("divide");
        assertTrue(result.isEmpty());
    }

    // =========================================================================
    // findByThisMeasurementType
    // =========================================================================

    @Test
    public void testFindByThisMeasurementType_LengthUnit_ReturnsCorrect() {
        repository.save(buildEntity("compare", "FEET", "LengthUnit",
            "INCHES", "LengthUnit", "true", null, false, null));
        repository.save(buildEntity("compare", "KILOGRAM", "WeightUnit",
            "GRAM", "WeightUnit", "false", null, false, null));

        List<QuantityMeasurementEntity> result =
            repository.findByThisMeasurementType("LengthUnit");
        assertEquals(1, result.size());
        assertEquals("LengthUnit", result.get(0).getThisMeasurementType());
    }

    // =========================================================================
    // findByCreatedAtAfter
    // =========================================================================

    @Test
    public void testFindByCreatedAtAfter_ReturnsRecentRecords() throws InterruptedException {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        repository.save(buildEntity("compare", "FEET", "LengthUnit",
            "INCHES", "LengthUnit", "true", null, false, null));

        List<QuantityMeasurementEntity> result = repository.findByCreatedAtAfter(before);
        assertEquals(1, result.size());
    }

    @Test
    public void testFindByCreatedAtAfter_FutureDate_ReturnsEmpty() {
        repository.save(buildEntity("compare", "FEET", "LengthUnit",
            "INCHES", "LengthUnit", "true", null, false, null));

        LocalDateTime future = LocalDateTime.now().plusDays(1);
        List<QuantityMeasurementEntity> result = repository.findByCreatedAtAfter(future);
        assertTrue(result.isEmpty());
    }

    // =========================================================================
    // findSuccessfulByOperation (custom @Query)
    // =========================================================================

    @Test
    public void testFindSuccessfulByOperation_ExcludesErrors() {
        repository.save(buildEntity("add", "FEET", "LengthUnit",
            "INCHES", "LengthUnit", null, 2.0, false, null));
        repository.save(buildEntity("add", "FEET", "LengthUnit",
            "KILOGRAM", "WeightUnit", null, null, true, "Cannot add different categories"));

        List<QuantityMeasurementEntity> result =
            repository.findSuccessfulByOperation("add");
        assertEquals(1, result.size());
        assertFalse(result.get(0).isError());
    }

    // =========================================================================
    // countByOperationAndIsErrorFalse
    // =========================================================================

    @Test
    public void testCountByOperationAndIsErrorFalse_CountsCorrectly() {
        repository.save(buildEntity("compare", "FEET", "LengthUnit",
            "INCHES", "LengthUnit", "true", null, false, null));
        repository.save(buildEntity("compare", "FEET", "LengthUnit",
            "INCHES", "LengthUnit", "false", null, false, null));
        repository.save(buildEntity("compare", "FEET", "LengthUnit",
            "KILOGRAM", "WeightUnit", null, null, true, "Error"));

        long count = repository.countByOperationAndErrorFalse("compare");
        assertEquals(2L, count);
    }

    // =========================================================================
    // findByIsErrorTrue
    // =========================================================================

    @Test
    public void testFindByIsErrorTrue_ReturnsOnlyErrors() {
        repository.save(buildEntity("add", "FEET", "LengthUnit",
            "INCHES", "LengthUnit", null, 2.0, false, null));
        repository.save(buildEntity("add", "FEET", "LengthUnit",
            "KILOGRAM", "WeightUnit", null, null, true, "Type mismatch"));

        List<QuantityMeasurementEntity> errors = repository.findByErrorTrue();
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).isError());
        assertEquals("Type mismatch", errors.get(0).getErrorMessage());
    }

    /* -----------------------------------------------------------------------
     * Helper — builds a QuantityMeasurementEntity with direct field setting
     * --------------------------------------------------------------------- */

    /**
     * Builds a test QuantityMeasurementEntity using setters to avoid dependency
     * on the QuantityModel constructors (which require IMeasurable instances).
     *
     * @param operation       the operation name
     * @param thisUnit        first operand unit
     * @param thisType        first operand measurement type
     * @param thatUnit        second operand unit
     * @param thatType        second operand measurement type
     * @param resultString    string result (for compare)
     * @param resultValue     numeric result (for arithmetic)
     * @param isError         error flag
     * @param errorMessage    error message
     * @return constructed entity
     */
    private QuantityMeasurementEntity buildEntity(
            String operation,
            String thisUnit, String thisType,
            String thatUnit, String thatType,
            String resultString, Double resultValue,
            boolean isError, String errorMessage) {

        QuantityMeasurementEntity e = new QuantityMeasurementEntity();
        e.setThisValue(1.0);
        e.setThisUnit(thisUnit);
        e.setThisMeasurementType(thisType);
        e.setThatValue(12.0);
        e.setThatUnit(thatUnit);
        e.setThatMeasurementType(thatType);
        e.setOperation(operation);
        e.setResultString(resultString);
        e.setResultValue(resultValue);
        e.setError(isError);
        e.setErrorMessage(errorMessage);
        return e;
    }
}