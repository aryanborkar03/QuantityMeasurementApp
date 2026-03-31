package com.app.quantitymeasurement.model;

/**
 * OperationType
 *
 * Enumeration of the operations supported by the Quantity Measurement application.
 *
 * Using this enum in DTOs and service methods provides compile-time type safety and
 * prevents invalid operation strings from entering the processing pipeline.
 */
public enum OperationType {

    /** Adds two quantities together. */
    ADD,

    /** Subtracts one quantity from another. */
    SUBTRACT,

    /** Multiplies two quantities (reserved for future use). */
    MULTIPLY,

    /** Divides one quantity by another, returning the dimensionless ratio. */
    DIVIDE,

    /** Compares two quantities for equality after base-unit conversion. */
    COMPARE,

    /** Converts a quantity from one unit to another within the same category. */
    CONVERT
}
