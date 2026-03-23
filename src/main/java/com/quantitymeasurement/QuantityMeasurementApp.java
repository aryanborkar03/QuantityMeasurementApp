package com.quantitymeasurement;

import java.util.List;
import java.util.logging.Logger;

import com.quantitymeasurement.controller.QuantityMeasurementController;
import com.quantitymeasurement.entity.QuantityDTO;
import com.quantitymeasurement.entity.QuantityMeasurementEntity;
import com.quantitymeasurement.repository.IQuantityMeasurementRepository;
import com.quantitymeasurement.repository.QuantityMeasurementCacheRepository;
import com.quantitymeasurement.repository.QuantityMeasurementDatabaseRepository;
import com.quantitymeasurement.service.QuantityMeasurementServiceImpl;
import com.quantitymeasurement.util.ApplicationConfig;

/**
 * QuantityMeasurementApp
 *
 * Main application class responsible for initializing and running the Quantity Measurement
 * system. Acts as the Application Layer entry point and coordinates the initialization of
 * core components following an N-Tier architecture.
 *
 * UC16 enhancements over UC15:
 * - Uses ApplicationConfig to determine which repository implementation to use
 *   (cache or database) based on the "repository.type" property in application.properties.
 * - Switches to QuantityMeasurementDatabaseRepository by default for persistent storage.
 * - Added SLF4J-compatible java.util.logging throughout for structured, configurable logging.
 * - Added closeResources() for graceful shutdown and connection pool cleanup.
 * - Added deleteAllMeasurements() for test reset capability.
 * - Main method now reports all stored measurements and calls deleteAllMeasurements() at end.
 *
 * Responsibilities:
 * - Initializing the repository layer (cache or database based on config)
 * - Creating the service layer
 * - Creating the controller layer
 * - Providing a singleton application instance
 * - Executing sample quantity measurement operations
 *
 * @author Developer
 * @version 16.0
 * @since 1.0
 */
public class QuantityMeasurementApp {

    /**
     * Logger for recording application lifecycle events.
     * Replaces System.out.println for structured, configurable logging.
     */
    private static final Logger logger = Logger.getLogger(
        QuantityMeasurementApp.class.getName()
    );

    /** Singleton application instance */
    private static QuantityMeasurementApp instance;

    /** Controller layer — handles all user-facing operations */
    public QuantityMeasurementController controller;

    /** Repository layer — persists measurement operation history */
    public IQuantityMeasurementRepository repository;

    /**
     * Private constructor used to initialize the application components.
     *
     * Reads the "repository.type" property from ApplicationConfig. If the value is
     * "database" (the default in application.properties), it initializes the
     * QuantityMeasurementDatabaseRepository. Otherwise, it falls back to the
     * in-memory QuantityMeasurementCacheRepository.
     *
     * Creates the service layer and controller layer after the repository is ready.
     */
    private QuantityMeasurementApp() {
        /*
         * Read the repository type from the centralized application configuration.
         * This allows switching storage without changing any code.
         */
        ApplicationConfig config = ApplicationConfig.getInstance();
        String repoType = config.getProperty("repository.type", "database");

        logger.info("Initializing repository type: " + repoType);

        if ("database".equalsIgnoreCase(repoType)) {
            /*
             * Use database-backed repository for persistent, JDBC-based storage.
             * This is the default for UC16.
             */
            this.repository = QuantityMeasurementDatabaseRepository.getInstance();
            logger.info("Using QuantityMeasurementDatabaseRepository (H2 JDBC).");
        } else {
            /*
             * Fall back to in-memory cache repository — useful for development or testing
             * without any database setup.
             */
            this.repository = QuantityMeasurementCacheRepository.getInstance();
            logger.info("Using QuantityMeasurementCacheRepository (in-memory).");
        }

        QuantityMeasurementServiceImpl service = new QuantityMeasurementServiceImpl(this.repository);
        this.controller = new QuantityMeasurementController(service);

        logger.info("QuantityMeasurementApp components initialized successfully.");
    }

    /**
     * Returns the singleton instance of the application.
     *
     * @return application instance
     */
    public static QuantityMeasurementApp getInstance() {
        if (instance == null) {
            instance = new QuantityMeasurementApp();
        }
        return instance;
    }

    /**
     * Returns the controller held by this application instance.
     * Used by integration tests to access the controller without re-initializing.
     *
     * @return the QuantityMeasurementController
     */
    public QuantityMeasurementController getController() {
        return controller;
    }

    /**
     * Returns the repository held by this application instance.
     * Used by integration tests to verify persistence directly.
     *
     * @return the IQuantityMeasurementRepository
     */
    public IQuantityMeasurementRepository getRepository() {
        return repository;
    }

    /**
     * Closes all resources held by the repository — primarily the database connection pool.
     *
     * Should be called before the application exits to ensure no connections are leaked.
     * Safe to call on cache repositories (default releaseResources() is a no-op).
     */
    public void closeResources() {
        if (repository != null) {
            repository.releaseResources();
            logger.info("Repository resources released. Application shutting down.");
        }
        /* Reset singleton so tests can create a fresh instance */
        instance = null;
    }

    /**
     * Deletes all measurements from the repository.
     *
     * Useful for testing purposes to clear the repository before running tests,
     * or to reset the application state between demonstration runs.
     *
     * Use with caution in production — this permanently removes all stored history.
     */
    public void deleteAllMeasurements() {
        repository.deleteAll();
        logger.info("All measurements deleted from repository.");
    }

    /**
     * Entry point of the Quantity Measurement Application.
     *
     * Demonstrates execution of different measurement operations including comparison,
     * conversion, arithmetic calculations, and retrieval of stored measurement records.
     *
     * UC16 additions:
     * - Logs which repository implementation is active at startup
     * - Reports connection pool statistics (for database repository)
     * - Reports total stored count at the end
     * - Calls deleteAllMeasurements() after the demo run to clean up
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        logger.info("\n**** Quantity Measurement Application Started (UC16) ****\n");

        QuantityMeasurementApp app = QuantityMeasurementApp.getInstance();
        QuantityMeasurementController controller = app.controller;

        /*
         * Report the pool statistics at startup so we can verify the database
         * connection pool was initialized correctly.
         */
        logger.info("Repository pool stats: " + app.repository.getPoolStatistics());

        /* ------------------------------------------------------------------
         * Example 1: Length Equality — 2 FEET vs 24 INCHES (should be equal)
         * ------------------------------------------------------------------ */
        QuantityDTO quantity1 = new QuantityDTO(
            2,
            QuantityDTO.LengthUnit.FEET.getUnitName(),
            QuantityDTO.LengthUnit.FEET.getMeasurementType()
        );
        QuantityDTO quantity2 = new QuantityDTO(
            24,
            QuantityDTO.LengthUnit.INCHES.getUnitName(),
            QuantityDTO.LengthUnit.INCHES.getMeasurementType()
        );
        controller.demonstrateComparison(quantity1, quantity2);
        System.out.println();

        /* ------------------------------------------------------------------
         * Example 2: Temperature Conversion — 0°C to °F (should be 32°F)
         * ------------------------------------------------------------------ */
        QuantityDTO temp1 = new QuantityDTO(
            0,
            QuantityDTO.TemperatureUnit.CELSIUS.getUnitName(),
            QuantityDTO.TemperatureUnit.CELSIUS.getMeasurementType()
        );
        QuantityDTO temp2 = new QuantityDTO(
            0,
            QuantityDTO.TemperatureUnit.FAHRENHEIT.getUnitName(),
            QuantityDTO.TemperatureUnit.FAHRENHEIT.getMeasurementType()
        );
        controller.demonstrateConversion(temp1, temp2);
        System.out.println();

        /* ------------------------------------------------------------------
         * Example 3: Temperature Addition — should produce an error (not supported)
         * ------------------------------------------------------------------ */
        QuantityDTO tempTarget = new QuantityDTO(
            0,
            QuantityDTO.TemperatureUnit.CELSIUS.getUnitName(),
            QuantityDTO.TemperatureUnit.CELSIUS.getMeasurementType()
        );
        controller.demonstrateAddition(temp1, temp2, tempTarget);
        System.out.println();

        /* ------------------------------------------------------------------
         * Example 4: Cross-Category Addition — length + weight (should error)
         * ------------------------------------------------------------------ */
        QuantityDTO weightQuantity = new QuantityDTO(
            10,
            QuantityDTO.WeightUnit.KILOGRAM.getUnitName(),
            QuantityDTO.WeightUnit.KILOGRAM.getMeasurementType()
        );
        controller.demonstrateAddition(quantity1, weightQuantity);
        System.out.println();

        /* ------------------------------------------------------------------
         * Example 5: Full length operation suite
         * ------------------------------------------------------------------ */
        QuantityDTO yardsTarget = new QuantityDTO(
            0,
            QuantityDTO.LengthUnit.YARDS.getUnitName(),
            QuantityDTO.LengthUnit.YARDS.getMeasurementType()
        );

        controller.demonstrateConversion(quantity2, yardsTarget);
        System.out.println();

        controller.demonstrateAddition(quantity1, quantity2);
        System.out.println();

        controller.demonstrateAddition(quantity1, quantity2, yardsTarget);
        System.out.println();

        controller.demonstrateSubtraction(quantity1, quantity2);
        System.out.println();

        controller.demonstrateDivision(quantity1, quantity2);
        System.out.println();

        /* ------------------------------------------------------------------
         * Report all measurements now stored in the repository
         * ------------------------------------------------------------------ */
        System.out.println("---- Stored Measurements ----");
        List<QuantityMeasurementEntity> allMeasurements = app.repository.getAllMeasurements();
        allMeasurements.forEach(System.out::println);

        logger.info("Total measurements stored: " + app.repository.getTotalCount());
        logger.info("Repository pool stats after run: " + app.repository.getPoolStatistics());

        /* ------------------------------------------------------------------
         * Clean up — delete all measurements and release database connections.
         * In a real application you would typically NOT delete measurements here;
         * this is included to demonstrate the deleteAllMeasurements() capability.
         * ------------------------------------------------------------------ */
        app.deleteAllMeasurements();
        logger.info("Measurements cleared after demo run.");

        app.closeResources();
        logger.info("\n**** Quantity Measurement Application Stopped ****\n");
        
        
    }
}