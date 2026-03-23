package com.quantitymeasurement.repository;

import com.quantitymeasurement.entity.QuantityMeasurementEntity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QuantityMeasurementDatabaseRepositoryTest
 *
 * Unit tests for the QuantityMeasurementDatabaseRepository class.
 *
 * Tests verify:
 * - save() persists entities correctly
 * - getAllMeasurements() returns all saved records
 * - getMeasurementsByOperation() filters by operation type
 * - getMeasurementsByType() filters by measurement category
 * - getTotalCount() returns the accurate record count
 * - deleteAll() removes all records and resets count to zero
 *
 * Uses H2 in-memory database via test/resources/application.properties.
 * The database is cleaned before and after each test for full isolation.
 *
 * @author Developer
 * @version 16.0
 */
public class QuantityMeasurementDatabaseRepositoryTest {

    /** Repository under test — uses H2 via ApplicationConfig */
    private QuantityMeasurementDatabaseRepository repository;

    /** Reusable test entity created in setUp() */
    private QuantityMeasurementEntity testEntity;

    /**
     * Sets the system property so ApplicationConfig picks up the test H2 configuration
     * before the first test class runs. @BeforeAll runs once per class in JUnit 5.
     */
    @BeforeAll
    public static void setUpDatabase() {
        /*
         * Point ApplicationConfig to the test environment so it uses an isolated
         * H2 in-memory database instead of the production file-based configuration.
         */
        System.setProperty("app.env", "test");
    }

    /**
     * Runs before each test method.
     * Gets the singleton repository, clears all records for isolation, and creates
     * a fresh test entity ready for use.
     */
    @BeforeEach
    public void setUp() {
        repository = QuantityMeasurementDatabaseRepository.getInstance();
        repository.deleteAll();
        createTestEntity();
    }

    /**
     * Runs after each test method.
     * Deletes all records so the next test always starts with a clean database.
     */
    @AfterEach
    public void tearDown() {
        repository.deleteAll();
    }

    /**
     * Test: save() stores the entity so getTotalCount() returns 1.
     */
    @Test
    public void testSaveEntity() {
        repository.save(testEntity);
        assertEquals(1, repository.getTotalCount(),
            "After one save, total count should be 1");
    }

    /**
     * Test: getAllMeasurements() returns all saved records.
     * Save two entities; expect a list of size 2.
     */
    @Test
    public void testGetAllMeasurements() {
        repository.save(testEntity);
        repository.save(createTestEntityCopy(5.0));

        List<QuantityMeasurementEntity> all = repository.getAllMeasurements();
        assertNotNull(all, "getAllMeasurements() should not return null");
        assertEquals(2, all.size(), "Should return 2 measurements");
    }

    /**
     * Test: getMeasurementsByOperation() returns only entities matching the given operation.
     * Save one COMPARE and one ADD entity; expect only COMPARE to be returned.
     */
    @Test
    public void testGetMeasurementsByOperation() {
        repository.save(testEntity);

        QuantityMeasurementEntity addEntity = createTestEntityCopy(2.0);
        addEntity.operation = "ADD";
        repository.save(addEntity);

        List<QuantityMeasurementEntity> compareResults =
            repository.getMeasurementsByOperation("COMPARE");
        assertEquals(1, compareResults.size(),
            "Only 1 COMPARE entity should be returned");
        assertEquals("COMPARE", compareResults.get(0).operation);
    }

    /**
     * Test: getMeasurementsByType() returns only entities matching the measurement category.
     */
    @Test
    public void testGetMeasurementsByType() {
        repository.save(testEntity);

        List<QuantityMeasurementEntity> lengthResults =
            repository.getMeasurementsByType("LengthUnit");
        assertFalse(lengthResults.isEmpty(),
            "Should find at least one LengthUnit measurement");
        assertEquals("LengthUnit", lengthResults.get(0).thisMeasurementType);
    }

    /**
     * Test: getTotalCount() returns the correct count after saving multiple entities.
     */
    @Test
    public void testGetTotalCount() {
        assertEquals(0, repository.getTotalCount(), "Initial count should be 0");

        repository.save(testEntity);
        assertEquals(1, repository.getTotalCount(), "Count should be 1 after first save");

        repository.save(createTestEntityCopy(3.0));
        assertEquals(2, repository.getTotalCount(), "Count should be 2 after second save");
    }

    /**
     * Test: deleteAll() removes all records so getTotalCount() returns 0.
     */
    @Test
    public void testDeleteAll() {
        repository.save(testEntity);
        repository.save(createTestEntityCopy(7.0));

        repository.deleteAll();

        assertEquals(0, repository.getTotalCount(), "After deleteAll(), count should be 0");
        assertTrue(repository.getAllMeasurements().isEmpty(),
            "After deleteAll(), getAllMeasurements() should return empty list");
    }

    /* --------------------------------------------------------------------- */

    /**
     * Helper — creates the default test entity used by most tests.
     * Entity: 2 FEET COMPARE 24 INCHES => "Equal"
     */
    private void createTestEntity() {
        testEntity = new QuantityMeasurementEntity();
        testEntity.thisValue           = 2.0;
        testEntity.thisUnit            = "FEET";
        testEntity.thisMeasurementType = "LengthUnit";
        testEntity.thatValue           = 24.0;
        testEntity.thatUnit            = "INCHES";
        testEntity.thatMeasurementType = "LengthUnit";
        testEntity.operation           = "COMPARE";
        testEntity.resultString        = "Equal";
        testEntity.isError             = false;
    }

    /**
     * Helper — creates a copy of the test entity with a different thisValue.
     *
     * @param value the thisValue to use for the copy
     * @return a new QuantityMeasurementEntity with the specified value
     */
    private QuantityMeasurementEntity createTestEntityCopy(double value) {
        QuantityMeasurementEntity copy = new QuantityMeasurementEntity();
        copy.thisValue           = value;
        copy.thisUnit            = testEntity.thisUnit;
        copy.thisMeasurementType = testEntity.thisMeasurementType;
        copy.thatValue           = testEntity.thatValue;
        copy.thatUnit            = testEntity.thatUnit;
        copy.thatMeasurementType = testEntity.thatMeasurementType;
        copy.operation           = testEntity.operation;
        copy.resultString        = testEntity.resultString;
        copy.isError             = testEntity.isError;
        return copy;
    }
}