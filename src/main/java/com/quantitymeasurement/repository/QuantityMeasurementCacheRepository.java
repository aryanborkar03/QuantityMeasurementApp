package com.quantitymeasurement.repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.quantitymeasurement.entity.QuantityMeasurementEntity;

/**
 * QuantityMeasurementCacheRepository
 *
 * Repository implementation responsible for managing
 * {@link QuantityMeasurementEntity} objects.
 *
 * This repository performs the following responsibilities:
 * <ul>
 * <li>Implements the {@link IQuantityMeasurementRepository} interface</li>
 * <li>Maintains an in-memory cache of quantity measurement entities</li>
 * <li>Provides a Singleton repository instance</li>
 * <li>Persists entities to disk using Java serialization</li>
 * <li>Loads previously stored entities from disk on initialization</li>
 * </ul>
 *
 * Persistence is implemented using file-based serialization
 * so that quantity measurement operations can be stored and
 * retrieved across application runs.
 *
 * The repository uses a custom {@code AppendableObjectOutputStream}
 * implementation to safely append serialized objects to an
 * existing file without corrupting the stream header.
 *
 * This class follows the Repository Pattern and Singleton Pattern
 * to ensure centralized and consistent data access.
 */
public class QuantityMeasurementCacheRepository implements IQuantityMeasurementRepository {

    /**
     * Custom ObjectOutputStream implementation that allows
     * appending objects to an existing file without writing
     * a new stream header.
     *
     * Writing a header multiple times would corrupt the
     * serialized object stream, therefore this class ensures
     * the header is written only when the file is empty.
     */
    static class AppendableObjectOutputStream extends ObjectOutputStream {

        /**
         * Constructs the appendable object output stream.
         *
         * @param out underlying output stream
         * @throws IOException if stream creation fails
         */
        public AppendableObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        /**
         * Writes the stream header only if the file
         * is empty or newly created.
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
     * File location used for storing serialized repository data.
     */
    public static final String FILE_NAME = "data/quantity_measurement_repo.ser";

    /**
     * In-memory cache storing quantity measurement entities.
     */
    private List<QuantityMeasurementEntity> quantityMeasurementEntityCache;

    /**
     * Singleton repository instance.
     */
    private static QuantityMeasurementCacheRepository instance;

    /**
     * Private constructor to enforce Singleton pattern.
     *
     * Initializes the in-memory cache and loads previously
     * stored entities from disk.
     */
    private QuantityMeasurementCacheRepository() {
        quantityMeasurementEntityCache = new ArrayList<>();
        loadFromDisk();
    }

    /**
     * Returns the singleton instance of the repository.
     *
     * @return repository instance
     */
    public static QuantityMeasurementCacheRepository getInstance() {
        if (instance == null) {
            instance = new QuantityMeasurementCacheRepository();
        }
        return instance;
    }

    /**
     * Saves a {@link QuantityMeasurementEntity} into the repository.
     *
     * The entity is added to the in-memory cache and then
     * serialized to disk for persistence.
     *
     * @param entity quantity measurement entity to store
     */
    @Override
    public void save(QuantityMeasurementEntity entity) {

        quantityMeasurementEntityCache.add(entity);

        saveToDisk(entity);
    }

    /**
     * Returns all stored quantity measurement entities.
     *
     * @return list of cached entities
     */
    @Override
    public List<QuantityMeasurementEntity> getAllMeasurements() {

        return quantityMeasurementEntityCache;
    }

    /**
     * Persists a quantity measurement entity to disk.
     *
     * Serialization is performed in append mode so that
     * previously stored objects remain intact.
     *
     * @param entity entity to persist
     */
    private void saveToDisk(QuantityMeasurementEntity entity) {

        try (
                FileOutputStream fos = new FileOutputStream(FILE_NAME, true);
                AppendableObjectOutputStream oos = new AppendableObjectOutputStream(fos)
        ) {
            oos.writeObject(entity);

        } catch (IOException e) {
            System.err.println("Error saving entity: " + e.getMessage());
        }
    }

    /**
     * Loads previously stored entities from disk into
     * the in-memory cache.
     *
     * This method is invoked during repository initialization
     * to restore previously saved measurement records.
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

            System.out.println(
                    "Loaded " + quantityMeasurementEntityCache.size()
                            + " quantity measurement entities from storage"
            );

        } catch (IOException | ClassNotFoundException ex) {

            System.err.println(
                    "Error loading quantity measurement entities: "
                            + ex.getMessage()
            );
        }
    }

    /**
     * Main method for testing repository functionality.
     *
     * Demonstrates repository initialization, entity
     * persistence, cache retrieval, and serialization behavior.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        System.out.println("---- Testing QuantityMeasurementCacheRepository ----");

        QuantityMeasurementCacheRepository repo =
                QuantityMeasurementCacheRepository.getInstance();

        System.out.println("Initial cached entities: "
                + repo.getAllMeasurements().size());

        com.quantitymeasurement.units.IMeasurable feet =
                com.quantitymeasurement.units.LengthUnit.FEET;

        com.quantitymeasurement.units.IMeasurable inches =
                com.quantitymeasurement.units.LengthUnit.INCHES;

        com.quantitymeasurement.model.QuantityModel<
                com.quantitymeasurement.units.IMeasurable> q1 =
                new com.quantitymeasurement.model.QuantityModel<>(2, feet);

        com.quantitymeasurement.model.QuantityModel<
                com.quantitymeasurement.units.IMeasurable> q2 =
                new com.quantitymeasurement.model.QuantityModel<>(24, inches);

        com.quantitymeasurement.model.QuantityModel<
                com.quantitymeasurement.units.IMeasurable> result =
                new com.quantitymeasurement.model.QuantityModel<>(4, feet);

        com.quantitymeasurement.entity.QuantityMeasurementEntity entity =
                new com.quantitymeasurement.entity.QuantityMeasurementEntity(
                        q1,
                        q2,
                        "ADD",
                        result
                );

        repo.save(entity);

        System.out.println("Entity saved successfully.");

        repo.save(entity);

        System.out.println("Second entity saved (testing appendable stream).");

        java.util.List<
                com.quantitymeasurement.entity.QuantityMeasurementEntity> list =
                repo.getAllMeasurements();

        System.out.println("\nTotal cached entities: " + list.size());

        System.out.println("\n---- Stored Entities ----");

        for (com.quantitymeasurement.entity.QuantityMeasurementEntity e : list) {
            System.out.println(e);
        }

        System.out.println("\nRestart the program to verify loadFromDisk() works.");

        System.out.println("---- QuantityMeasurementCacheRepository Test Completed ----");
    }
}