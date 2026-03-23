package com.quantitymeasurement.exception;

/**
 * DatabaseException
 *
 * DatabaseException is thrown when database operations fail.
 * This exception encapsulates JDBC errors and provides meaningful context
 * for upper layers of the application.
 *
 * Extends QuantityMeasurementException so it participates in the application's
 * custom exception hierarchy. Upper layers can catch DatabaseException specifically
 * or catch QuantityMeasurementException more broadly as needed.
 *
 * Provides static factory methods for common failure scenarios:
 * - connectionFailed: when database connectivity cannot be established
 * - queryFailed:      when a SQL query execution fails
 * - transactionFailed: when a transaction cannot complete
 *
 * @author Developer
 * @version 16.0
 * @since 16.0
 */
public class DatabaseException extends QuantityMeasurementException {

    /**
     * Constructs a DatabaseException with a descriptive message.
     *
     * @param message error description
     */
    public DatabaseException(String message) {
        super(message);
    }

    /**
     * Constructs a DatabaseException with a descriptive message and the root cause.
     *
     * This is the preferred constructor when wrapping a lower-level SQLException
     * so that the full stack trace is preserved.
     *
     * @param message error description
     * @param cause   underlying exception (typically an SQLException)
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Factory method for database connection failures.
     *
     * Creates a DatabaseException with a standardized message prefix indicating
     * that the database connection could not be established.
     *
     * @param details additional context about what was being connected to
     * @param cause   root cause of the connection failure
     * @return a new DatabaseException for connection failures
     */
    public static DatabaseException connectionFailed(String details, Throwable cause) {
        return new DatabaseException("Database connection failed: " + details, cause);
    }

    /**
     * Factory method for SQL query execution failures.
     *
     * Creates a DatabaseException with a standardized message prefix indicating
     * which query or operation failed during execution.
     *
     * @param query the SQL query or operation description that failed
     * @param cause root cause of the query failure
     * @return a new DatabaseException for query failures
     */
    public static DatabaseException queryFailed(String query, Throwable cause) {
        return new DatabaseException("Query execution failed: " + query, cause);
    }

    /**
     * Factory method for transaction failures.
     *
     * Creates a DatabaseException with a standardized message prefix indicating
     * that a database transaction could not be completed for a given operation.
     *
     * @param operation the name of the operation during which the transaction failed
     * @param cause     root cause of the transaction failure
     * @return a new DatabaseException for transaction failures
     */
    public static DatabaseException transactionFailed(String operation, Throwable cause) {
        return new DatabaseException("Transaction failed during " + operation, cause);
    }
}
