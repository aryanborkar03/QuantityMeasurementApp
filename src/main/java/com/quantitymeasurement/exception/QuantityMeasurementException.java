
package com.quantitymeasurement.exception;

/**
 * QuantityMeasurementException
 *
 * Custom runtime exception used in the Quantity Measurement application to represent
 * errors that occur during quantity operations such as comparison, conversion,
 * or arithmetic.
 *
 * This exception extends RuntimeException, allowing it to be thrown without
 * mandatory handling while still enabling centralized error management.
 *
 * Typical scenarios where this exception may be thrown:
 * - Invalid unit provided
 * - Unsupported measurement conversion
 * - Arithmetic errors such as division by zero
 * - Invalid quantity values
 *
 * This custom exception maintains consistent error handling across the Controller,
 * Service, and Repository layers of the system.
 *
 * @author Developer
 * @version 16.0
 * @since 1.0
 */
public class QuantityMeasurementException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new QuantityMeasurementException with the specified error message.
     *
     * @param message detailed error description
     */
    public QuantityMeasurementException(String message) {
        super(message);
    }

    /**
     * Constructs a new QuantityMeasurementException with the specified error message
     * and underlying cause.
     *
     * This constructor is useful when wrapping lower-level exceptions to provide
     * additional context to upper layers.
     *
     * @param message detailed error description
     * @param cause   underlying exception cause
     */
    public QuantityMeasurementException(String message, Throwable cause) {
        super(message, cause);
    }
}
