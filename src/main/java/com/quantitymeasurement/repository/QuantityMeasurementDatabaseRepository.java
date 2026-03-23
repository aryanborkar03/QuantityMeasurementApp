package com.quantitymeasurement.repository;

import com.quantitymeasurement.entity.QuantityMeasurementEntity;
import com.quantitymeasurement.exception.DatabaseException;
import com.quantitymeasurement.util.ConnectionPool;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * QuantityMeasurementDatabaseRepository
 *
 * This class implements the IQuantityMeasurementRepository interface and provides methods
 * to interact with a relational database for storing and retrieving quantity measurement
 * data. It uses JDBC for database operations and a connection pool for efficient resource
 * management. The repository handles CRUD operations for QuantityMeasurementEntity objects
 * and includes methods to query measurements by operation type and measurement type. It
 * also includes error handling using a custom DatabaseException to encapsulate any
 * database-related issues. The repository is designed to be used by the service layer of
 * the application to persist the results of quantity measurement operations and to
 * retrieve historical data for analysis and reporting. It ensures that database connections
 * are properly managed and that resources are released after use to prevent leaks and ensure
 * optimal performance.
 *
 * @author Developer
 * @version 16.0
 * @since 16.0
 */
public class QuantityMeasurementDatabaseRepository
        implements IQuantityMeasurementRepository
{

    /* --- Logger for database operations and errors --- */
    private static final Logger logger =
        Logger.getLogger(QuantityMeasurementDatabaseRepository.class.getName());

    /* --- Singleton instance of the repository --- */
    private static QuantityMeasurementDatabaseRepository instance;

    /*
     * Pre-compiled SQL query constants.
     * Parameterized queries (?) prevent SQL injection by treating
     * user-supplied data as literal values, never as executable SQL.
     */

    /**
     * SQL INSERT statement for persisting a new measurement entity.
     * All fields except id (AUTO_INCREMENT) and timestamps (DEFAULT) are explicitly set.
     */
    private static final String INSERT_QUERY =
        "INSERT INTO quantity_measurement_entity " +
        "(this_value, this_unit, this_measurement_type, that_value, that_unit, " +
        "that_measurement_type, operation, result_value, result_unit, " +
        "result_measurement_type, result_string, is_error, error_message, " +
        "created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

    /**
     * SQL SELECT for retrieving all records, newest first.
     */
    private static final String SELECT_ALL_QUERY =
        "SELECT * FROM quantity_measurement_entity ORDER BY created_at DESC";

    /**
     * SQL SELECT filtered by operation type (e.g., "ADD", "COMPARE").
     */
    private static final String SELECT_BY_OPERATION =
        "SELECT * FROM quantity_measurement_entity WHERE operation = ? ORDER BY created_at DESC";

    /**
     * SQL SELECT filtered by measurement category (e.g., "LengthUnit").
     */
    private static final String SELECT_BY_MEASUREMENT_TYPE =
        "SELECT * FROM quantity_measurement_entity " +
        "WHERE this_measurement_type = ? ORDER BY created_at DESC";

    /**
     * SQL DELETE to remove all records — used for test teardown.
     */
    private static final String DELETE_ALL_QUERY =
        "DELETE FROM quantity_measurement_entity";

    /**
     * SQL COUNT to return the total number of stored records.
     */
    private static final String COUNT_QUERY =
        "SELECT COUNT(*) FROM quantity_measurement_entity";

    /**
     * SQL CREATE TABLE for the main measurement entity table.
     * Uses IF NOT EXISTS so it is safe to call on every startup without errors.
     */
    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE IF NOT EXISTS quantity_measurement_entity (" +
        "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "  this_value DOUBLE NOT NULL," +
        "  this_unit VARCHAR(50) NOT NULL," +
        "  this_measurement_type VARCHAR(50) NOT NULL," +
        "  that_value DOUBLE," +
        "  that_unit VARCHAR(50)," +
        "  that_measurement_type VARCHAR(50)," +
        "  operation VARCHAR(20) NOT NULL," +
        "  result_value DOUBLE," +
        "  result_unit VARCHAR(50)," +
        "  result_measurement_type VARCHAR(50)," +
        "  result_string VARCHAR(255)," +
        "  is_error BOOLEAN DEFAULT FALSE," +
        "  error_message VARCHAR(500)," +
        "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
        ")";

    /** Reference to the shared connection pool for acquiring/releasing connections */
    private ConnectionPool connectionPool;

    /**
     * Private constructor — initializes the connection pool and sets up the database schema.
     *
     * Called only once via getInstance(). Any SQLException during pool initialization is
     * wrapped in a DatabaseException so it propagates as an unchecked exception.
     */
    private QuantityMeasurementDatabaseRepository() {
        try {
            this.connectionPool = ConnectionPool.getInstance();
            logger.info("QuantityMeasurementDatabaseRepository initialized.");
            /*
             * Ensure the schema exists before any operations are attempted.
             * This is safe to call on every startup — the CREATE TABLE uses IF NOT EXISTS.
             */
            initializeDatabase();
        } catch (SQLException e) {
            throw DatabaseException.connectionFailed(
                "Failed to initialize connection pool", e);
        }
    }

    /**
     * Initializes the database schema for testing purposes. This method creates the
     * quantity_measurement_entity table if it does not already exist. It is called during
     * the construction of the repository to ensure that the database is ready for use. The
     * method uses a connection from the connection pool to execute the SQL statement for
     * creating the table. Error handling is included to catch any SQL exceptions that may
     * occur during the execution of the schema initialization and logs any errors encountered.
     *
     * Note: In a production environment, database schema management is typically handled
     * separately using migration tools like Flyway or Liquibase. This method is included here
     * for simplicity and testing purposes to ensure that the necessary table exists before
     * running any database operations.
     */
    private void initializeDatabase() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.createStatement();
            stmt.execute(CREATE_TABLE_SQL);
            logger.info("Database schema initialized (table verified or created).");
        } catch (SQLException e) {
            logger.severe("Failed to initialize database schema: " + e.getMessage());
        } finally {
            closeResources(stmt, conn);
        }
    }

    /**
     * Returns the singleton instance of QuantityMeasurementDatabaseRepository.
     *
     * Thread-safe using synchronized to prevent double-initialization under concurrency.
     *
     * @return singleton QuantityMeasurementDatabaseRepository
     */
    public static synchronized QuantityMeasurementDatabaseRepository getInstance() {
        if (instance == null) {
            instance = new QuantityMeasurementDatabaseRepository();
        }
        return instance;
    }

    /**
     * Saves a QuantityMeasurementEntity to the database. This method uses a prepared statement
     * to insert the entity's data into the quantity_measurement_entity table. It handles SQL
     * exceptions by catching them and throwing a custom DatabaseException with a meaningful
     * message. The method also ensures that all database resources are properly closed after
     * use to prevent leaks. Logging is included to track successful saves and any errors that
     * occur during the operation.
     *
     * @param entity the QuantityMeasurementEntity to be saved
     * @throws DatabaseException if the INSERT operation fails
     */
    @Override
    public void save(QuantityMeasurementEntity entity) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.prepareStatement(INSERT_QUERY);

            /*
             * Bind all entity fields to the parameterized query.
             * Parameterized binding prevents SQL injection — data is never concatenated into SQL.
             */
            stmt.setDouble(1,  entity.thisValue);
            stmt.setString(2,  entity.thisUnit);
            stmt.setString(3,  entity.thisMeasurementType);
            stmt.setDouble(4,  entity.thatValue  != null ? entity.thatValue  : 0.0);
            stmt.setString(5,  entity.thatUnit   != null ? entity.thatUnit   : "");
            stmt.setString(6,  entity.thatMeasurementType != null ? entity.thatMeasurementType : "");
            stmt.setString(7,  entity.operation);
            /*
             * Result fields may be null for comparison operations — use setNull if needed.
             */
            if (entity.resultValue != null) {
                stmt.setDouble(8, entity.resultValue);
            } else {
                stmt.setNull(8, Types.DOUBLE);
            }
            stmt.setString(9,  entity.resultUnit);
            stmt.setString(10, entity.resultMeasurementType);
            stmt.setString(11, entity.resultString);
            stmt.setBoolean(12, entity.isError);
            stmt.setString(13, entity.errorMessage);

            stmt.executeUpdate();
            logger.fine("Entity saved to database: operation=" + entity.operation);
        } catch (SQLException e) {
            throw DatabaseException.queryFailed("INSERT entity", e);
        } finally {
            closeResources(stmt, conn);
        }
    }

    /**
     * Retrieves all QuantityMeasurementEntity instances from the database. This method executes a
     * SQL query to select all records from the quantity_measurement_entity table, ordered by
     * creation date in descending order. It uses a statement to execute the query and maps the
     * result set to a list of QuantityMeasurementEntity objects. The method includes error handling
     * to catch any SQL exceptions that may occur during the database interaction and rethrows them
     * as DatabaseException with a meaningful message. Finally, it ensures that all database
     * resources are properly closed to prevent leaks. Logging is included to track the number of
     * measurements retrieved and any errors that occur during the operation.
     *
     * @return a list of all QuantityMeasurementEntity instances retrieved from the database
     * @throws DatabaseException if the SELECT operation fails
     */
    @Override
    public List<QuantityMeasurementEntity> getAllMeasurements() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<QuantityMeasurementEntity> results = new ArrayList<>();
        try {
            conn = connectionPool.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(SELECT_ALL_QUERY);
            while (rs.next()) {
                results.add(mapResultSetToEntity(rs));
            }
            logger.info("Retrieved " + results.size() + " measurements from database.");
        } catch (SQLException e) {
            throw DatabaseException.queryFailed("SELECT all measurements", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return results;
    }

    /**
     * Get measurements by operation type. This method retrieves all quantity measurement
     * entities from the database that match the specified operation type (e.g., "Addition",
     * "Subtraction", "Multiplication", "Division"). It uses a prepared statement to execute
     * the query and maps the result set to a list of QuantityMeasurementEntity objects.
     * The method also includes error handling to catch any SQL exceptions that may occur
     * during the database interaction and rethrows them as DatabaseException with a
     * meaningful message. Finally, it ensures that all database resources are properly closed
     * to prevent leaks.
     *
     * @param operation the type of operation to filter measurements by (e.g., "Addition")
     * @return a list of QuantityMeasurementEntity objects that match the specified operation type
     * @throws DatabaseException if the query fails
     */
    @Override
    public List<QuantityMeasurementEntity> getMeasurementsByOperation(String operation) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<QuantityMeasurementEntity> results = new ArrayList<>();
        try {
            conn = connectionPool.getConnection();
            stmt = conn.prepareStatement(SELECT_BY_OPERATION);
            stmt.setString(1, operation);
            rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(mapResultSetToEntity(rs));
            }
            logger.fine("Retrieved " + results.size() + " measurements for operation=" + operation);
        } catch (SQLException e) {
            throw DatabaseException.queryFailed("SELECT by operation=" + operation, e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return results;
    }

    /**
     * Get measurements by measurement type (Length, Weight, Volume, Temperature).
     * This method retrieves all quantity measurement entities from the database that
     * match the specified measurement type (e.g., "Length", "Weight", "Volume",
     * "Temperature"). It uses a prepared statement to execute the query and maps the
     * result set to a list of QuantityMeasurementEntity objects. The method also
     * includes error handling to catch any SQL exceptions that may occur during
     * the database interaction and rethrows them as DatabaseException. Finally, it
     * ensures that all database resources are properly closed to prevent leaks.
     *
     * @param measurementType the type of measurement to filter by (e.g., "Length",
     *                        "Weight", "Volume", "Temperature")
     * @return a list of QuantityMeasurementEntity objects that match the specified
     *         measurement type
     * @throws DatabaseException if the query fails
     */
    @Override
    public List<QuantityMeasurementEntity> getMeasurementsByType(String measurementType) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<QuantityMeasurementEntity> results = new ArrayList<>();
        try {
            conn = connectionPool.getConnection();
            stmt = conn.prepareStatement(SELECT_BY_MEASUREMENT_TYPE);
            stmt.setString(1, measurementType);
            rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(mapResultSetToEntity(rs));
            }
            logger.fine("Retrieved " + results.size() + " measurements for type=" + measurementType);
        } catch (SQLException e) {
            throw DatabaseException.queryFailed("SELECT by type=" + measurementType, e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return results;
    }

    /**
     * Get count of all measurements. This method executes a SQL query to count the
     * total number of quantity measurement entities in the database. It uses a statement
     * to execute the COUNT query and retrieves the result from the result set. The method
     * includes error handling to catch any SQL exceptions that may occur during the
     * database interaction and rethrows them as DatabaseException with a meaningful
     * message. Finally, it ensures that all database resources are properly closed to
     * prevent leaks.
     *
     * @return the total count of quantity measurement entities in the database
     * @throws DatabaseException if the COUNT query fails
     */
    @Override
    public int getTotalCount() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(COUNT_QUERY);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw DatabaseException.queryFailed("SELECT COUNT(*)", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    /**
     * Delete all measurements (useful for testing). This method executes a SQL query
     * to delete all quantity measurement entities from the database. It uses a statement
     * to execute the DELETE query and includes error handling to catch any SQL exceptions
     * that may occur during the database interaction, rethrowing them as DatabaseException
     * with a meaningful message. Finally, it ensures that all database resources are
     * properly closed to prevent leaks. This method is particularly useful for testing
     * purposes to reset the state of the database before running test cases.
     *
     * Note: Use this method with caution in a production environment as it will permanently
     * delete all measurement data. It is recommended to use this method only in a testing
     * context or with appropriate safeguards in place to prevent accidental data loss.
     *
     * @throws DatabaseException if the DELETE query fails
     */
    @Override
    public void deleteAll() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.createStatement();
            stmt.execute(DELETE_ALL_QUERY);
            logger.info("All measurements deleted from database.");
        } catch (SQLException e) {
            throw DatabaseException.queryFailed("DELETE all measurements", e);
        } finally {
            closeResources(stmt, conn);
        }
    }

    /**
     * Get pool statistics. This method provides insights into the current state of
     * the connection pool, such as the number of available and used connections.
     *
     * This can be useful for monitoring and debugging database connection issues. The
     * method can be overridden by repository implementations that utilize connection
     * pooling to provide specific pool statistics, while other implementations can
     * simply return a default message indicating that pool statistics are not available.
     *
     * @return a string with pool statistics including available/used/total counts
     */
    @Override
    public String getPoolStatistics() {
        return "ConnectionPool{" +
            "available=" + connectionPool.getAvailableConnectionCount() +
            ", used=" + connectionPool.getUsedConnectionCount() +
            ", total=" + connectionPool.getTotalConnectionCount() +
            "}";
    }

    /**
     * Release resources held by the repository, such as closing database connections or
     * clearing caches. This method can be implemented by repository implementations that
     * manage resources to ensure proper cleanup when the repository is no longer needed.
     */
    @Override
    public void releaseResources() {
        if (connectionPool != null) {
            connectionPool.closeAll();
            logger.info("Database connection pool closed via releaseResources().");
        }
        /* Reset singleton so a fresh instance can be created if needed */
        instance = null;
    }

    /**
     * Map ResultSet row to QuantityMeasurementEntity.
     *
     * Reads all columns from the current row of the ResultSet and constructs
     * a QuantityMeasurementEntity using the base (3-arg) constructor to set
     * all fields directly.
     *
     * @param rs ResultSet positioned at the row to read
     * @return populated QuantityMeasurementEntity
     * @throws SQLException if a column cannot be read
     */
    private QuantityMeasurementEntity mapResultSetToEntity(ResultSet rs) throws SQLException {
        /*
         * Build operand QuantityModel-like data from columns — we reconstruct
         * the entity fields directly since we store primitive columns, not serialized objects.
         */
        double thisValue = rs.getDouble("this_value");
        String thisUnit  = rs.getString("this_unit");
        String thisType  = rs.getString("this_measurement_type");

        double thatValue = rs.getDouble("that_value");
        String thatUnit  = rs.getString("that_unit");
        String thatType  = rs.getString("that_measurement_type");

        String operation = rs.getString("operation");

        /*
         * Create a minimal entity using direct field assignment since the database
         * stores flat columns rather than nested QuantityModel objects.
         * The no-arg constructor is used here; all fields are set below.
         */
        QuantityMeasurementEntity entity = new QuantityMeasurementEntity();

        entity.thisValue           = thisValue;
        entity.thisUnit            = thisUnit;
        entity.thisMeasurementType = thisType;
        entity.thatValue           = thatValue;
        entity.thatUnit            = thatUnit;
        entity.thatMeasurementType = thatType;
        entity.operation           = operation;
        entity.resultValue         = rs.getObject("result_value") != null
                                     ? rs.getDouble("result_value") : null;
        entity.resultUnit          = rs.getString("result_unit");
        entity.resultMeasurementType = rs.getString("result_measurement_type");
        entity.resultString        = rs.getString("result_string");
        entity.isError             = rs.getBoolean("is_error");
        entity.errorMessage        = rs.getString("error_message");

        return entity;
    }

    /**
     * Release connection back to pool.
     *
     * Overloaded helper method that closes a ResultSet, Statement, and Connection
     * in the correct order, releasing the connection back to the pool.
     *
     * @param rs   ResultSet to close (may be null)
     * @param stmt Statement to close (may be null)
     * @param conn Connection to release back to the pool (may be null)
     */
    private void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) {
                logger.warning("Error closing ResultSet: " + e.getMessage());
            }
        }
        closeResources(stmt, conn);
    }

    /**
     * Release connection and statement back to pool.
     *
     * Overloaded helper for operations that do not produce a ResultSet (INSERT, DELETE).
     *
     * @param stmt Statement to close (may be null)
     * @param conn Connection to release back to the pool (may be null)
     */
    private void closeResources(Statement stmt, Connection conn) {
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException e) {
                logger.warning("Error closing Statement: " + e.getMessage());
            }
        }
        if (conn != null) {
            connectionPool.releaseConnection(conn);
        }
    }

    /**
     * Main method for testing QuantityMeasurementDatabaseRepository.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            QuantityMeasurementDatabaseRepository repo =
                QuantityMeasurementDatabaseRepository.getInstance();
            logger.getLogger(QuantityMeasurementDatabaseRepository.class.getName())
                  .info("Pool stats: " + repo.getPoolStatistics());
            logger.getLogger(QuantityMeasurementDatabaseRepository.class.getName())
                  .info("Total records: " + repo.getTotalCount());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
