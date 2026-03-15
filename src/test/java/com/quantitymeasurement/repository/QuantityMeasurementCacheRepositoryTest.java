package com.quantitymeasurement.repository;

import com.quantitymeasurement.entity.QuantityMeasurementEntity;
import com.quantitymeasurement.units.IMeasurable;
import com.quantitymeasurement.model.QuantityModel;
import com.quantitymeasurement.units.LengthUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QuantityMeasurementCacheRepositoryTest
 *
 * Tests the cache repository layer:
 * - getInstance() always returns the same singleton instance
 * - save() adds entities to the in-memory cache
 * - getAllMeasurements() returns the full list
 * - Multiple saves accumulate correctly
 * - Implements IQuantityMeasurementRepository contract
 *
 * Note: File-persistence (saveToDisk / loadFromDisk) is a side effect
 * of save(). Those paths are exercised indirectly here. Direct disk tests
 * are excluded to keep unit tests free of file-system dependencies.
 */
public class QuantityMeasurementCacheRepositoryTest {

    private QuantityMeasurementCacheRepository repository;

    // Shared fixtures
    private QuantityModel<IMeasurable> q1;
    private QuantityModel<IMeasurable> q2;
    private QuantityModel<IMeasurable> resultModel;

    @BeforeEach
    public void setUp() {
        repository  = QuantityMeasurementCacheRepository.getInstance();
        q1          = new QuantityModel<>(2.0,  LengthUnit.FEET);
        q2          = new QuantityModel<>(24.0, LengthUnit.INCHES);
        resultModel = new QuantityModel<>(4.0,  LengthUnit.FEET);
    }

    // =========================================================================
    // SINGLETON
    // =========================================================================

    @Test
    public void testGetInstance_ReturnsSameInstance() {
        QuantityMeasurementCacheRepository a = QuantityMeasurementCacheRepository.getInstance();
        QuantityMeasurementCacheRepository b = QuantityMeasurementCacheRepository.getInstance();
        assertSame(a, b);
    }

    @Test
    public void testGetInstance_NotNull() {
        assertNotNull(QuantityMeasurementCacheRepository.getInstance());
    }

    // =========================================================================
    // IQuantityMeasurementRepository contract
    // =========================================================================

    @Test
    public void testImplementsRepositoryInterface() {
        assertTrue(repository instanceof IQuantityMeasurementRepository);
    }

    // =========================================================================
    // save() and getAllMeasurements()
    // =========================================================================

    @Test
    public void testSave_EntityAppearsInCache() {
        int sizeBefore = repository.getAllMeasurements().size();

        QuantityMeasurementEntity entity =
            new QuantityMeasurementEntity(q1, q2, "ADD", resultModel);
        repository.save(entity);

        List<QuantityMeasurementEntity> all = repository.getAllMeasurements();
        assertEquals(sizeBefore + 1, all.size());
        assertTrue(all.contains(entity));
    }

    @Test
    public void testSave_MultipleEntities_AllAppearInCache() {
        int sizeBefore = repository.getAllMeasurements().size();

        QuantityMeasurementEntity e1 =
            new QuantityMeasurementEntity(q1, q2, "COMPARE", "Equal");
        QuantityMeasurementEntity e2 =
            new QuantityMeasurementEntity(q1, q2, "ADD", resultModel);

        repository.save(e1);
        repository.save(e2);

        List<QuantityMeasurementEntity> all = repository.getAllMeasurements();
        assertEquals(sizeBefore + 2, all.size());
    }

    @Test
    public void testGetAllMeasurements_ReturnsNonNullList() {
        assertNotNull(repository.getAllMeasurements());
    }

    @Test
    public void testGetAllMeasurements_ReturnsSameListReference_AfterSave() {
        // The list returned should always reflect the latest state
        int sizeBefore = repository.getAllMeasurements().size();
        repository.save(new QuantityMeasurementEntity(q1, q2, "DIVIDE", "1.0"));
        assertEquals(sizeBefore + 1, repository.getAllMeasurements().size());
    }
}
