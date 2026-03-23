package com.quantitymeasurement.repository;

import java.util.List;

import com.quantitymeasurement.entity.QuantityMeasurementEntity;

/**
 * IQuantityMeasurementRepository
 *
 * Repository interface for managing QuantityMeasurementEntity objects.
 *
 * This interface defines the contract for data access operations related to quantity
 * measurement entities. It abstracts the persistence mechanism so that the service
 * layer does not depend on implementation details of storage.
 *
 * Implementations of this interface may store data using:
 * - In-memory cache (QuantityMeasurementCacheRepository)
 * - Database storage via JDBC (QuantityMeasurementDatabaseRepository)
 *
 * UC16 enhancements over UC15:
 * - Added getMeasurementsByOperation() for filtering by operation type
 * - Added getMeasurementsByType() for filtering by measurement category
 * - Added getTotalCount() for reporting and monitoring
 * - Added deleteAll() for test isolation and state reset
 * - Added default methods for pool statistics and resource cleanup
 *
 * This design follows the Repository Pattern and supports loose coupling between
 * the Service Layer and the data persistence layer.
 *
 * @author Developer
 * @version 16.0
 * @since 1.0
 */
public interface IQuantityMeasurementRepository {

    /**
     * Saves a QuantityMeasurementEntity to the repository.
     *
     * @param entity the quantity measurement entity to be stored
     */
    void save(QuantityMeasurementEntity entity);

    /**
     * Retrieves all stored quantity measurement entities.
     *
     * @return list of all stored QuantityMeasurementEntity objects
     */
    List<QuantityMeasurementEntity> getAllMeasurements();

    /**
     * Retrieves all measurements that match the given operation type.
     *
     * Useful for filtering the history by operation (e.g., "ADD", "COMPARE").
     *
     * @param operation the operation type to filter by
     * @return list of matching QuantityMeasurementEntity objects
     */
    List<QuantityMeasurementEntity> getMeasurementsByOperation(String operation);

    /**
     * Retrieves all measurements that match the given measurement type (category).
     *
     * Useful for filtering the history by category (e.g., "LengthUnit", "WeightUnit").
     *
     * @param measurementType the measurement type/category to filter by
     * @return list of matching QuantityMeasurementEntity objects
     */
    List<QuantityMeasurementEntity> getMeasurementsByType(String measurementType);

    /**
     * Returns the total count of all measurements stored in the repository.
     *
     * Can be used for monitoring, reporting, and test assertions.
     *
     * @return total number of stored measurements
     */
    int getTotalCount();

    /**
     * Deletes all measurements from the repository.
     *
     * Particularly useful for test isolation — clean state before each test run.
     * Use with caution in production environments.
     */
    void deleteAll();

    /**
     * Returns statistics about the connection pool (if applicable).
     *
     * Default implementation returns a message indicating pool statistics are
     * not available for this repository type. Database-backed implementations
     * should override this to return meaningful pool metrics.
     *
     * @return string describing pool statistics
     */
    default String getPoolStatistics() {
        return "Pool statistics not available for this repository type";
    }

    /**
     * Releases resources held by the repository, such as closing database
     * connections or clearing caches. Repository implementations that manage
     * resources should override this method to ensure proper cleanup when the
     * repository is no longer needed.
     */
    default void releaseResources() {
        /* Default implementation does nothing — override in resource-holding implementations */
    }
}