package com.quantitymeasurement.repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.quantitymeasurement.entity.QuantityMeasurementEntity;

/**
 * QuantityMeasurementCacheRepository
 *
 * In-memory cache repository implementation for managing QuantityMeasurementEntity objects.
 *
 * Implements the IQuantityMeasurementRepository interface and:
 * - Maintains an in-memory cache of quantity measurement entities
 * - Provides a Singleton repository instance
 * - Persists entities to disk using Java serialization for durability across restarts
 * - Loads previously stored entities from disk on initialization
 * - Supports all new UC16 query methods (filtering, counting, deleting)
 *
 * The repository uses a custom AppendableObjectOutputStream to safely append serialized
 * objects to an existing file without corrupting the stream header.
 *
 * Note: This implementation is not suitable for concurrent multi-JVM access.
 * Use QuantityMeasurementDatabaseRepository for shared/distributed scenarios.
 *
 * @author Developer
 * @version 16.0
 * @since 1.0
 */
public class QuantityMeasurementCacheRepository implements IQuantityMeasurementRepository {

    /**
     * Logger for recording cache repository operations and errors.
     */
    private static final Logger logger = Logger.getLogger(
        QuantityMeasurementCacheRepository.class.getName()
    );

    /**
     * Custom ObjectOutputStream that allows appending objects to an existing serialized file
     * without writing a new stream header. Writing a header multiple times would corrupt
     * the stream; this class skips the header if the file already has content.
     */
    static class AppendableObjectOutputStream extends ObjectOutputStream {

        /**
         * Constructs the appendable stream wrapping the given output stream.
         *
         * @param out underlying output stream
         * @throws IOException if stream creation fails
         */
        public AppendableObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        /**
         * Overrides stream header writing — only writes the header if the file
         * is new/empty. For existing files it calls reset() instead to avoid
         * a corrupt stream header.
         *
         * @throws IOException if writing fails
         */
        @Override
        protected void writeStreamHeader() throws IOException {
            File file = new File(QuantityMeasurementCacheRepository.FILE_NAME);
            if (!file.exists() || file.length() == 0) {
                super.writeStreamHeader();
            } else {
                reset();
            }
        }
    }

    /**
     * File location for serialized repository data (relative to project root).
     */
    public static final String FILE_NAME = "data/quantity_measurement_repo.ser";

    /**
     * In-memory cache storing all loaded and newly saved measurement entities.
     */
    private List<QuantityMeasurementEntity> quantityMeasurementEntityCache;

    /**
     * Singleton instance of the cache repository.
     */
    private static QuantityMeasurementCacheRepository instance;

    /**
     * Private constructor — initializes the in-memory cache and loads previously
     * persisted entities from disk.
     */
    private QuantityMeasurementCacheRepository() {
        quantityMeasurementEntityCache = new ArrayList<>();
        loadFromDisk();
        logger.info("QuantityMeasurementCacheRepository initialized with "
            + quantityMeasurementEntityCache.size() + " cached entities.");
    }

    /**
     * Returns the singleton instance of the cache repository.
     *
     * @return singleton QuantityMeasurementCacheRepository
     */
    public static QuantityMeasurementCacheRepository getInstance() {
        if (instance == null) {
            instance = new QuantityMeasurementCacheRepository();
        }
        return instance;
    }

    /**
     * Saves a QuantityMeasurementEntity to the in-memory cache and persists it to disk.
     *
     * @param entity the entity to store
     */
    @Override
    public void save(QuantityMeasurementEntity entity) {
        quantityMeasurementEntityCache.add(entity);
        saveToDisk(entity);
        logger.fine("Entity saved to cache and disk: " + entity.operation);
    }

    /**
     * Returns all stored quantity measurement entities from the in-memory cache.
     *
     * @return list of all cached entities
     */
    @Override
    public List<QuantityMeasurementEntity> getAllMeasurements() {
        return quantityMeasurementEntityCache;
    }

    /**
     * Returns all measurements matching the given operation type (e.g., "ADD", "COMPARE").
     * Uses Java streams to filter the in-memory cache.
     *
     * @param operation operation type to filter by
     * @return filtered list of entities
     */
    @Override
    public List<QuantityMeasurementEntity> getMeasurementsByOperation(String operation) {
        return quantityMeasurementEntityCache.stream()
            .filter(e -> operation.equalsIgnoreCase(e.operation))
            .collect(Collectors.toList());
    }

    /**
     * Returns all measurements matching the given measurement type (e.g., "LengthUnit").
     * Uses Java streams to filter the in-memory cache by thisMeasurementType.
     *
     * @param measurementType measurement category to filter by
     * @return filtered list of entities
     */
    @Override
    public List<QuantityMeasurementEntity> getMeasurementsByType(String measurementType) {
        return quantityMeasurementEntityCache.stream()
            .filter(e -> measurementType.equalsIgnoreCase(e.thisMeasurementType))
            .collect(Collectors.toList());
    }

    /**
     * Returns the total count of measurements currently held in the cache.
     *
     * @return total measurement count
     */
    @Override
    public int getTotalCount() {
        return quantityMeasurementEntityCache.size();
    }

    /**
     * Deletes all measurements from the in-memory cache and removes the serialized
     * file from disk. Useful for test isolation and resetting the repository state.
     */
    @Override
    public void deleteAll() {
        quantityMeasurementEntityCache.clear();
        File file = new File(FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
        logger.info("All measurements cleared from cache repository.");
    }

    /**
     * Returns pool statistics — cache repository has no connection pool.
     *
     * @return descriptive string indicating no pool is in use
     */
    @Override
    public String getPoolStatistics() {
        return "CacheRepository: no connection pool. Cache size=" + quantityMeasurementEntityCache.size();
    }

    /**
     * Persists a single entity to disk by appending it to the serialized file.
     * Uses AppendableObjectOutputStream to avoid corrupting the stream header.
     *
     * @param entity entity to write to disk
     */
    private void saveToDisk(QuantityMeasurementEntity entity) {
        try (
            FileOutputStream fos = new FileOutputStream(FILE_NAME, true);
            AppendableObjectOutputStream oos = new AppendableObjectOutputStream(fos)
        ) {
            oos.writeObject(entity);
        } catch (IOException e) {
            logger.severe("Error saving entity to disk: " + e.getMessage());
        }
    }

    /**
     * Loads previously persisted entities from the serialized file on disk into
     * the in-memory cache. Called during repository initialization to restore
     * the measurement history from previous application runs.
     */
    private void loadFromDisk() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }
        try (
            FileInputStream fis = new FileInputStream(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            while (true) {
                try {
                    QuantityMeasurementEntity entity =
                        (QuantityMeasurementEntity) ois.readObject();
                    quantityMeasurementEntityCache.add(entity);
                } catch (EOFException e) {
                    break;
                }
            }
            logger.info("Loaded " + quantityMeasurementEntityCache.size()
                + " entities from disk.");
        } catch (IOException | ClassNotFoundException ex) {
            logger.severe("Error loading entities from disk: " + ex.getMessage());
        }
    }
}