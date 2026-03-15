package com.quantitymeasurement.exception;

/**
 * QuantityMeasurementException
 *
 * Custom runtime exception used in the Quantity Measurement
 * application to represent errors that occur during quantity
 * operations such as comparison, conversion, or arithmetic.
 *
 * This exception extends {@link RuntimeException}, allowing
 * it to be thrown without mandatory handling while still
 * enabling centralized error management within the application.
 *
 * Typical scenarios where this exception may be thrown include:
 * <ul>
 * <li>Invalid unit provided</li>
 * <li>Unsupported measurement conversion</li>
 * <li>Arithmetic errors such as division by zero</li>
 * <li>Invalid quantity values</li>
 * </ul>
 *
 * This custom exception helps maintain consistent error
 * handling across the Controller, Service, and Repository
 * layers of the system.
 */
public class QuantityMeasurementException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new QuantityMeasurementException with
	 * the specified error message.
	 *
	 * @param message detailed error description
	 */
	public QuantityMeasurementException(String message) {
		super(message);
	}

	/**
	 * Constructs a new QuantityMeasurementException with
	 * the specified error message and underlying cause.
	 *
	 * This constructor is useful when wrapping lower-level
	 * exceptions to provide additional context.
	 *
	 * @param message detailed error description
	 * @param cause underlying exception cause
	 */
	public QuantityMeasurementException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Main method for simple testing of the custom exception.
	 *
	 * Demonstrates how the QuantityMeasurementException
	 * can be thrown and caught within the application.
	 *
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		try {
			throw new QuantityMeasurementException(
				"This is a test exception for quantity measurement."
			);
		} catch(QuantityMeasurementException ex) {
			System.out.println("Caught QuantityMeasurementException: " + 
								ex.getMessage());
		} 
	}
}