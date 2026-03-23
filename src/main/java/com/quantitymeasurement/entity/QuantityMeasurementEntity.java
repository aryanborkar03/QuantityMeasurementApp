package com.quantitymeasurement.entity;

import com.quantitymeasurement.units.IMeasurable;

/**
 * QuantityMeasurementEntity
 *
 * Entity class representing a quantity measurement operation record.
 *
 * This entity is used to persist and retrieve measurement operation history from the
 * database. It stores operands (thisQuantity and thatQuantity), the operation type,
 * and the result (as a model, string, or error message).
 *
 * Stores:
 * - First and second operand values, units, and measurement types
 * - Operation type (COMPARE, CONVERT, ADD, SUBTRACT, DIVIDE)
 * - Result value, unit, and measurement type (for arithmetic operations)
 * - Result string (for comparison/conversion results like "Equal")
 * - Error information (for failed operations)
 *
 * This class is Serializable so it can also be used by the
 * QuantityMeasurementCacheRepository for file-based persistence.
 *
 * @author Developer
 * @version 16.0
 * @since 1.0
 */
public class QuantityMeasurementEntity implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * No-arg constructor for use by the database repository when reconstructing
     * entities from ResultSet rows. Fields are set directly after construction.
     */
    public QuantityMeasurementEntity() {
        /* Fields set by QuantityMeasurementDatabaseRepository.mapResultSetToEntity() */
    }

    /* --- First operand fields --- */
    public Double thisValue;
    public String thisUnit;
    public String thisMeasurementType;

    /* --- Second operand fields --- */
    public Double thatValue;
    public String thatUnit;
    public String thatMeasurementType;

    /**
     * Operation type representing the quantity measurement action.
     *
     * Supported operations: COMPARE, CONVERT, ADD, SUBTRACT, DIVIDE
     */
    public String operation;

    /* --- Result fields --- */
    public Double resultValue;
    public String resultUnit;
    public String resultMeasurementType;

    /**
     * String result used for operations like COMPARE ("Equal" / "Not Equal")
     * or DIVIDE (numeric ratio as string).
     */
    public String resultString;

    /* --- Error fields --- */
    public boolean isError;
    public String errorMessage;

    /**
     * Constructor for comparison or conversion operations where the result is a string.
     *
     * @param thisQuantity first operand
     * @param thatQuantity second operand
     * @param operation    operation type (e.g., "COMPARE")
     * @param result       string result such as "Equal" or "Not Equal"
     */
    public QuantityMeasurementEntity(
            QuantityModel<IMeasurable> thisQuantity,
            QuantityModel<IMeasurable> thatQuantity,
            String operation,
            String result
    ) {
        this(thisQuantity, thatQuantity, operation);
        this.resultString = result;
    }

    /**
     * Constructor for arithmetic operations where the result is a QuantityModel.
     *
     * @param thisQuantity first operand
     * @param thatQuantity second operand
     * @param operation    operation type (e.g., "ADD")
     * @param result       result quantity model with value and unit
     */
    public QuantityMeasurementEntity(
            QuantityModel<IMeasurable> thisQuantity,
            QuantityModel<IMeasurable> thatQuantity,
            String operation,
            QuantityModel<IMeasurable> result
    ) {
        this(thisQuantity, thatQuantity, operation);
        this.resultValue = result.getValue();
        this.resultUnit = result.getUnit().getUnitName();
        this.resultMeasurementType = result.getUnit().getMeasurementType();
    }

    /**
     * Constructor for error cases where an operation fails.
     *
     * @param thisQuantity first operand
     * @param thatQuantity second operand
     * @param operation    operation type
     * @param errorMessage error description
     * @param isError      flag indicating this is an error record
     */
    public QuantityMeasurementEntity(
            QuantityModel<IMeasurable> thisQuantity,
            QuantityModel<IMeasurable> thatQuantity,
            String operation,
            String errorMessage,
            boolean isError
    ) {
        this(thisQuantity, thatQuantity, operation);
        this.errorMessage = errorMessage;
        this.isError = isError;
    }

    /**
     * Base constructor used internally by all other constructors.
     *
     * Initializes operand information and operation type.
     * Validates that neither operand is null.
     *
     * @param thisQuantity first operand
     * @param thatQuantity second operand
     * @param operation    operation type
     * @throws IllegalArgumentException if either operand is null
     */
    public QuantityMeasurementEntity(
            QuantityModel<IMeasurable> thisQuantity,
            QuantityModel<IMeasurable> thatQuantity,
            String operation
    ) {
        if (thisQuantity == null || thatQuantity == null) {
            throw new IllegalArgumentException("Quantities cannot be null");
        }

        this.thisValue = thisQuantity.getValue();
        this.thisUnit = thisQuantity.getUnit().getUnitName();
        this.thisMeasurementType = thisQuantity.getUnit().getMeasurementType();

        this.thatValue = thatQuantity.getValue();
        this.thatUnit = thatQuantity.getUnit().getUnitName();
        this.thatMeasurementType = thatQuantity.getUnit().getMeasurementType();

        this.operation = operation;
    }

    /**
     * Compares this entity with another for equality.
     *
     * Two entities are considered equal if their operand values match within
     * precision tolerance (1e-6), and units and operation type match.
     *
     * @param obj object to compare
     * @return true if entities are equivalent
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        QuantityMeasurementEntity other = (QuantityMeasurementEntity) obj;
        return Math.abs(this.thisValue - other.thisValue) < 1e-6
                && Math.abs(this.thatValue - other.thatValue) < 1e-6
                && this.thisUnit.equals(other.thisUnit)
                && this.thatUnit.equals(other.thatUnit)
                && this.operation.equals(other.operation);
    }

    /**
     * Returns a human-readable string representation of this entity.
     *
     * Includes operation, operand details, and result or error information.
     * Used for logging, debugging, and displaying stored measurements.
     *
     * @return formatted entity description
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(isError ? "[ERROR] " : "[SUCCESS] ")
          .append("operation=").append(operation);

        sb.append(", operand1=")
          .append(thisValue).append(" ")
          .append(thisUnit).append(" ")
          .append(thisMeasurementType);

        sb.append(", operand2=")
          .append(thatValue).append(" ")
          .append(thatUnit).append(" ")
          .append(thatMeasurementType);

        if (isError) {
            sb.append(", message=").append(errorMessage);
        } else if (resultString != null && !resultString.isEmpty()) {
            sb.append(", result=").append(resultString);
        } else {
            sb.append(", result=")
              .append(resultValue).append(" ")
              .append(resultUnit).append(" ")
              .append(resultMeasurementType);
        }

        return sb.toString();
    }
}