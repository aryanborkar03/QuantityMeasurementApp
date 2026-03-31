package com.app.quantitymeasurement.enums;

/**
 * OperationType
 *
 * Enumeration of the operations supported by the Quantity Measurement application.
 *
 * Using this enum in DTOs and service methods provides compile-time type safety and
 * prevents invalid operation strings from entering the processing pipeline.
 */
public enum OperationType {

    ADD,
    
    SUBTRACT,

    MULTIPLY,

    DIVIDE,

    COMPARE,

    CONVERT
}
