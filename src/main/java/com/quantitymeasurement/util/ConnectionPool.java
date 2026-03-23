package com.quantitymeasurement.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * ConnectionPool
 *
 * ConnectionPool class manages a pool of database connections for efficient reuse.
 * It initializes a specified number of connections based on the configuration and
 * provides methods to acquire and release connections. The pool ensures that the
 * number of active connections does not exceed the configured pool size and handles
 * connection creation and closure gracefully. It also provides methods to retrieve
 * pool statistics for monitoring purposes.
 *
 * The ConnectionPool class is designed to be a singleton, ensuring that only one
 * instance of the connection pool exists throughout the application. This design
 * allows for centralized management of database connections. The class uses JDBC
 * for connection management and includes error handling to manage exceptions that
 * may occur during connection creation and closure.
 *
 * @author Developer
 * @version 16.0
 * @since 16.0
 */
public class ConnectionPool {

    /**
     * Logger for recording pool operations, errors, and statistics.
     */
    private static final Logger logger = Logger.getLogger(
        ConnectionPool.class.getName()
    );

    /** Singleton instance — volatile for thread-safe lazy initialization */
    private static ConnectionPool instance;

    /** List of connections currently available for use */
    private List<Connection> availableConnections;

    /** List of connections currently checked out and in use */
    private List<Connection> usedConnections;

    /** Maximum number of connections this pool will manage */
    private final int poolSize;

    /** JDBC database URL loaded from ApplicationConfig */
    private final String dbUrl;

    /** Database username loaded from ApplicationConfig */
    private final String dbUsername;

    /** Database password loaded from ApplicationConfig */
    private final String dbPassword;

    /** JDBC driver class name loaded from ApplicationConfig */
    private final String driverClass;

    /** SQL query used to validate that a connection is still alive */
    private final String testQuery;

    /**
     * Private constructor to initialize the connection pool based on the configuration.
     *
     * Reads all database settings from ApplicationConfig and pre-creates
     * the initial pool of connections.
     *
     * @throws SQLException if there is an error initializing the connection pool
     */
    private ConnectionPool() throws SQLException {
        ApplicationConfig config = ApplicationConfig.getInstance();

        /*
         * Load all database configuration values from application.properties.
         * Default values are provided for each setting in case they are absent.
         */
        this.dbUrl        = config.getProperty("db.url",      "jdbc:h2:./data/quantitymeasurementdb;AUTO_SERVER=TRUE");
        this.dbUsername   = config.getProperty("db.username", "sa");
        this.dbPassword   = config.getProperty("db.password", "");
        this.driverClass  = config.getProperty("db.driver",   "org.h2.Driver");
        this.testQuery    = config.getProperty("db.hikari.connection-test-query", "SELECT 1");
        this.poolSize     = config.getIntProperty("db.pool-size", 5);

        this.availableConnections = new ArrayList<>();
        this.usedConnections      = new ArrayList<>();

        /*
         * Pre-load the JDBC driver so that DriverManager can create connections.
         */
        try {
            Class.forName(driverClass);
            logger.info("JDBC driver loaded: " + driverClass);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC driver not found: " + driverClass, e);
        }

        /*
         * Pre-create the specified number of connections to fill the pool.
         */
        initializeConnections();
        logger.info("ConnectionPool initialized with " + poolSize + " connections.");
    }

    /**
     * Get the singleton instance of the ConnectionPool.
     *
     * Thread-safe using synchronized to prevent concurrent initialization.
     *
     * @return the singleton instance of the ConnectionPool
     * @throws SQLException if there is an error initializing the connection pool
     */
    public static synchronized ConnectionPool getInstance() throws SQLException {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    /**
     * Initializes the connection pool by creating the specified number of connections.
     *
     * Each connection is opened and added to the available connections list.
     *
     * @throws SQLException if there is an error creating connections
     */
    private void initializeConnections() throws SQLException {
        for (int i = 0; i < poolSize; i++) {
            availableConnections.add(createConnection());
        }
    }

    /**
     * Creates a new database connection using the configured parameters.
     *
     * Uses DriverManager to open a fresh JDBC connection with the configured
     * URL, username, and password.
     *
     * @return a new database connection
     * @throws SQLException if there is an error creating the connection
     */
    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    /**
     * Acquires a connection from the pool. If no connections are available and the
     * pool has not reached its maximum size, a new connection will be created. If
     * the pool has reached its maximum size, an SQLException will be thrown.
     *
     * The method is synchronized to ensure thread-safe access to the pool lists.
     * This prevents two threads from acquiring the same connection simultaneously.
     *
     * @return a database connection from the pool
     * @throws SQLException if there are no available connections and the pool has
     *                      reached its maximum size
     */
    public synchronized Connection getConnection() throws SQLException {
        /*
         * If there are available connections, take the first one from the list
         * and move it to the used connections list.
         */
        if (!availableConnections.isEmpty()) {
            Connection conn = availableConnections.remove(0);
            /*
             * Validate the connection is still alive before handing it out.
             * If it is stale, create a fresh replacement.
             */
            if (!validateConnection(conn)) {
                logger.warning("Stale connection detected — creating fresh replacement");
                conn = createConnection();
            }
            usedConnections.add(conn);
            logger.fine("Connection acquired. Available: " + availableConnections.size()
                + ", Used: " + usedConnections.size());
            return conn;
        }

        /*
         * No available connections — if we have not yet reached the pool limit,
         * create a new connection on demand.
         */
        if (usedConnections.size() < poolSize) {
            Connection conn = createConnection();
            usedConnections.add(conn);
            logger.info("New connection created on demand. Used: " + usedConnections.size());
            return conn;
        }

        /*
         * Pool is exhausted — throw an exception so callers can handle backpressure.
         */
        throw new SQLException(
            "Connection pool exhausted. All " + poolSize + " connections are in use."
        );
    }

    /**
     * Releases a connection back to the pool. The connection is moved from the used
     * connections list to the available connections list. If the connection is null,
     * it will be ignored.
     *
     * The method is synchronized to ensure thread-safe manipulation of pool lists.
     *
     * @param connection the database connection to be released back to the pool
     */
    public synchronized void releaseConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        /*
         * Move the connection from used back to available so other threads can acquire it.
         */
        usedConnections.remove(connection);
        availableConnections.add(connection);
        logger.fine("Connection released. Available: " + availableConnections.size()
            + ", Used: " + usedConnections.size());
    }

    /**
     * Execute Test Query to validate a connection. This method can be used to check
     * if a connection is valid and can successfully communicate with the database.
     * It executes a simple query (e.g., "SELECT 1") and returns true if the query
     * executes successfully, indicating that the connection is valid. If the query fails,
     * it returns false, indicating that the connection is not valid. This method can be
     * useful for connection validation before using a connection from the pool.
     *
     * @param connection the database connection to validate
     * @return true if the connection is valid and working, false otherwise
     */
    public boolean validateConnection(Connection connection) {
        try (var stmt = connection.createStatement()) {
            stmt.execute(this.testQuery);
            return true;
        } catch (SQLException e) {
            logger.warning("Connection validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Closes all connections in the pool. This method should be called when the
     * application is shutting down to ensure that all database connections are properly
     * closed. It iterates through both the available and used connections and attempts
     * to close each one, logging any exceptions that occur during the closure process.
     */
    public synchronized void closeAll() {
        /*
         * Close all available (idle) connections first.
         */
        for (Connection conn : availableConnections) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.warning("Error closing available connection: " + e.getMessage());
            }
        }
        availableConnections.clear();

        /*
         * Then close any connections still in use — this handles graceful shutdown.
         */
        for (Connection conn : usedConnections) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.warning("Error closing used connection: " + e.getMessage());
            }
        }
        usedConnections.clear();

        /*
         * Reset the singleton so a fresh pool can be created if needed (e.g., in tests).
         */
        instance = null;
        logger.info("All connections closed. Connection pool shut down.");
    }

    /**
     * Returns the number of connections currently available in the pool.
     *
     * @return available connection count
     */
    public int getAvailableConnectionCount() {
        return availableConnections.size();
    }

    /**
     * Returns the number of connections currently in use.
     *
     * @return used connection count
     */
    public int getUsedConnectionCount() {
        return usedConnections.size();
    }

    /**
     * Returns the total number of connections managed by the pool (available + used).
     *
     * @return total connection count
     */
    public int getTotalConnectionCount() {
        return availableConnections.size() + usedConnections.size();
    }

    /**
     * toString method for debugging purposes. This method provides a string
     * representation of the connection pool, including the number of available
     * and used connections. It can be useful for logging the state of the connection
     * pool during application execution, especially when monitoring connection
     * usage and debugging connection-related issues.
     *
     * @return a string representation of the connection pool, including the number of
     *         available and used connections
     */
    @Override
    public String toString() {
        return "ConnectionPool{"
            + "poolSize=" + poolSize
            + ", available=" + availableConnections.size()
            + ", used=" + usedConnections.size()
            + "}";
    }

    /**
     * Main method for testing ConnectionPool initialization and operations.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            ConnectionPool pool = ConnectionPool.getInstance();
            Connection conn1 = pool.getConnection();
            logger.getLogger(ConnectionPool.class.getName()).info(
                "Validate connection: " + (pool.validateConnection(conn1) ? "Success" : "Failure"));
            logger.getLogger(ConnectionPool.class.getName()).info(
                "Available connections after acquiring 1: " + pool.getAvailableConnectionCount());
            logger.getLogger(ConnectionPool.class.getName()).info(
                "Used connections after acquiring 1: " + pool.getUsedConnectionCount());
            pool.releaseConnection(conn1);
            logger.getLogger(ConnectionPool.class.getName()).info(
                "Available connections after releasing 1: " + pool.getAvailableConnectionCount());
            logger.getLogger(ConnectionPool.class.getName()).info(
                "Used connections after releasing 1: " + pool.getUsedConnectionCount());
            pool.closeAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
