package com.app.quantitymeasurement.exception;


public class QuantityMeasurementException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with the given error message.
     *
     * @param message description of the error
     */
    public QuantityMeasurementException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the given message and root cause.
     * Use this constructor when wrapping a lower-level exception so that
     * the original stack trace is preserved.
     *
     * @param message description of the error
     * @param cause   underlying exception
     */
    public QuantityMeasurementException(String message, Throwable cause) {
        super(message, cause);
    }
}
