package com.quantitymeasurement.util;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * ApplicationConfig
 *
 * DatabaseConfig is a singleton class responsible for loading and managing database
 * configuration properties. It supports loading configurations from a properties file,
 * system properties, or environment variables. The class provides methods to retrieve
 * configuration values and manage different environments (development, testing, production).
 *
 * The configuration properties include database connection details such as driver class, URL,
 * username, password, and connection pool settings. The class also defines enums for
 * configuration keys and environments to ensure type safety and avoid hardcoding strings.
 *
 * Usage:
 *   ApplicationConfig config = ApplicationConfig.getInstance();
 *   String dbUrl = config.getProperty(ApplicationConfig.ConfigKey.DB_URL.getKey());
 */
public class ApplicationConfig {

    /**
     * Logger for recording configuration loading events and errors.
     * Using java.util.logging for consistency across the application.
     */
    private static final Logger logger = Logger.getLogger(
        ApplicationConfig.class.getName()
    );

    /** Singleton instance — volatile for thread-safe lazy initialization */
    private static ApplicationConfig instance;

    /** Loaded configuration properties from application.properties or defaults */
    private Properties properties;

    /** Current runtime environment determined from system property or config file */
    private Environment environment;

    /**
     * Enum for managing different environments.
     * The environment can be set via system property "app.env" or environment variable "APP_ENV".
     */
    public enum Environment {
        DEVELOPMENT, TESTING, PRODUCTION
    }

    /**
     * Enum for all the configuration keys to avoid hardcoding and typos.
     * Each key maps to its property name in application.properties.
     */
    public enum ConfigKey {
        REPOSITORY_TYPE(          "repository.type"),
        DB_DRIVER_CLASS(          "db.driver"),
        DB_URL(                   "db.url"),
        DB_USERNAME(              "db.username"),
        DB_PASSWORD(              "db.password"),
        DB_POOL_SIZE(             "db.pool-size"),
        HIKARI_MAX_POOL_SIZE(     "db.hikari.maximum-pool-size"),
        HIKARI_MIN_IDLE(          "db.hikari.minimum-idle"),
        HIKARI_CONNECTION_TIMEOUT("db.hikari.connection-timeout"),
        HIKARI_IDLE_TIMEOUT(      "db.hikari.idle-timeout"),
        HIKARI_MAX_LIFETIME(      "db.hikari.max-lifetime"),
        HIKARI_POOL_NAME(         "db.hikari.pool-name"),
        HIKARI_CONNECTION_TEST_QUERY("db.hikari.connection-test-query");

        private final String key;

        ConfigKey(String key) {
            this.key = key;
        }

        /**
         * Returns the property key string for use in Properties lookups.
         *
         * @return property key name
         */
        public String getKey() {
            return key;
        }
    }

    /**
     * Private constructor — loads configuration on creation.
     * Called only once via the singleton getInstance() method.
     */
    private ApplicationConfig() {
        loadConfiguration();
    }

    /**
     * Returns the singleton instance of ApplicationConfig.
     *
     * Thread-safe using synchronized to prevent race conditions during initialization.
     *
     * @return singleton ApplicationConfig instance
     */
    public static synchronized ApplicationConfig getInstance() {
        if (instance == null) {
            instance = new ApplicationConfig();
        }
        return instance;
    }

    /**
     * Loads configuration properties from a properties file, system properties, or
     * environment variables. The method first attempts to load from a properties file
     * named "application.properties" located in the classpath. If the file is not found,
     * it falls back to loading default values. The environment can be specified through
     * system properties or environment variables, and if not set, it defaults to
     * "development". The method also includes error handling to log any issues encountered
     * during the loading process and ensures that the application can still run with default
     * configurations if necessary.
     */
    private void loadConfiguration() {
        properties = new Properties();
        try {
            /*
             * Check for environment override from system property first,
             * then fall back to the APP_ENV environment variable.
             */
            String env = System.getProperty("app.env");
            if (env == null || env.isEmpty()) {
                env = System.getenv("APP_ENV");
            }

            String configFile = "application.properties";
            InputStream input = ApplicationConfig.class
                .getClassLoader()
                .getResourceAsStream(configFile);

            if (input != null) {
                properties.load(input);
                logger.info("Configuration loaded from " + configFile);
                /*
                 * If env was not set via system/environment, read from properties file
                 * and default to "development" if still absent.
                 */
                if (env == null || env.isEmpty()) {
                    env = properties.getProperty("app.env", "development");
                }
                this.environment = Environment.valueOf(env.toUpperCase());
            } else {
                logger.warning("Configuration file not found, using defaults");
                loadDefaults();
            }
        } catch (Exception e) {
            logger.severe("Error loading configuration: " + e.getMessage());
            loadDefaults();
        }
    }

    /**
     * Loads default configuration values. This method is called when the configuration
     * file is not found or when there is an error loading the configuration. It sets
     * default values for database connection properties such as driver class, URL,
     * username, password, and connection pool size. These defaults are suitable for a
     * local H2 database, which can be used for testing and development purposes. The
     * method ensures that the application can still run with reasonable defaults even
     * if the configuration file is missing or cannot be loaded.
     */
    private void loadDefaults() {
        /*
         * H2 in-memory database defaults — suitable for development and testing
         * without any external database setup required.
         */
        properties.setProperty("repository.type",           "database");
        properties.setProperty("app.env",                   "development");
        properties.setProperty("db.driver",                 "org.h2.Driver");
        properties.setProperty("db.url",                    "jdbc:h2:./data/quantitymeasurementdb;AUTO_SERVER=TRUE");
        properties.setProperty("db.username",               "sa");
        properties.setProperty("db.password",               "");
        properties.setProperty("db.pool-size",              "5");
        properties.setProperty("db.hikari.maximum-pool-size", "10");
        properties.setProperty("db.hikari.minimum-idle",    "2");
        properties.setProperty("db.hikari.connection-test-query", "SELECT 1");
        this.environment = Environment.DEVELOPMENT;
        logger.info("Default H2 configuration loaded");
    }

    /**
     * Returns the value for a given property key, or null if not present.
     *
     * @param key property key name
     * @return property value or null
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Returns the value for a given property key, falling back to defaultValue if absent.
     *
     * @param key          property key name
     * @param defaultValue value to return if key is not found
     * @return property value or defaultValue
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Returns the integer value for a given property key, or defaultValue if absent or invalid.
     *
     * @param key          property key name
     * @param defaultValue value to return if key is missing or not a valid integer
     * @return integer property value or defaultValue
     */
    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Returns the current environment name (e.g., "DEVELOPMENT", "TESTING", "PRODUCTION").
     *
     * @return environment name string
     */
    public String getEnvironment() {
        return environment.name();
    }

    /**
     * Checks if the provided key is a valid configuration key defined in the ConfigKey enum.
     * This method can be used to validate configuration keys before attempting to retrieve
     * their values.
     *
     * @param key the configuration key to check
     * @return true if the key is a valid configuration key, false otherwise
     */
    public boolean isConfigKey(String key) {
        for (ConfigKey ck : ConfigKey.values()) {
            if (ck.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Print all configuration properties for debugging purposes. This method can be
     * called during application startup to log the loaded configuration, which can
     * help in troubleshooting configuration issues.
     */
    public void printAllProperties() {
        logger.info("=== ApplicationConfig Properties ===");
        logger.info("Environment: " + environment.name());
        /*
         * Print all properties except the password to avoid exposing credentials in logs.
         */
        properties.forEach((key, value) -> {
            if (!key.toString().contains("password")) {
                logger.info(key + " = " + value);
            }
        });
        logger.info("====================================");
    }

    /**
     * Main method for testing ApplicationConfig loading.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        ApplicationConfig config = ApplicationConfig.getInstance();
        config.printAllProperties();
    }
}