package com.app.qma.enums;


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
