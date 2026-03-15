package com.quantitymeasurement.entity;
import com.quantitymeasurement.model.QuantityModel;
import com.quantitymeasurement.units.IMeasurable;

/**
 * QuantityMeasurementEntity
 *
 * Entity class representing a quantity measurement operation.
 *
 * Stores:
 * - operands
 * - operation type
 * - result
 * - error information
 *
 * This class is Serializable so it can be saved and loaded
 * by the QuantityMeasurementCacheRepository.
 */
public class QuantityMeasurementEntity implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public Double thisValue;
    public String thisUnit;
    public String thisMeasurementType;

    public Double thatValue;
    public String thatUnit;
    public String thatMeasurementType;

    /**
     * Operation type representing the quantity measurement action.
     *
     * Supported operations:
     * - COMPARE
     * - CONVERT
     * - ADD
     * - SUBTRACT
     * - DIVIDE
     */
    public String operation;
    
    public Double resultValue;
    public String resultUnit;
    public String resultMeasurementType;

    public String resultString;

    public boolean isError;
    public String errorMessage;

    /**
     * Constructor for comparison or conversion operations.
     *
     * @param thisQuantity first operand
     * @param thatQuantity second operand
     * @param operation operation type
     * @param result result string such as "Equal" or "Not Equal"
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
     * Constructor for arithmetic operations.
     *
     * @param thisQuantity first operand
     * @param thatQuantity second operand
     * @param operation operation type
     * @param result arithmetic result
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
     * Constructor for error cases.
     *
     * @param thisQuantity first operand
     * @param thatQuantity second operand
     * @param operation operation type
     * @param errorMessage error message description
     * @param isError indicates whether operation failed
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
     * Base constructor used by other constructors.
     *
     * Initializes operand information and operation type.
     *
     * @param thisQuantity first operand
     * @param thatQuantity second operand
     * @param operation operation type
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
     * Override equals() to compare entity objects.
     *
     * Two entities are considered equal if:
     * - operand values match within precision tolerance
     * - units match
     * - operation type matches
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
     * Returns string representation of the entity.
     *
     * This representation is primarily used for logging
     * and debugging purposes.
     *
     * @return formatted entity description
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(isError == true? "[ERROR] " : "[SUCCESS] ")
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
        }
        else if (resultString != null && !resultString.isEmpty()) {
            sb.append(", result=").append(resultString);
        }
        else {
            sb.append(", result=")
              .append(resultValue).append(" ")
              .append(resultUnit).append(" ")
              .append(resultMeasurementType);
        }

        return sb.toString();
    }

    /**
     * Main method for testing entity functionality.
     *
     * Demonstrates:
     * - comparison entity
     * - arithmetic entity
     * - error entity
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        System.out.println("---- Testing QuantityMeasurementEntity ----");

        IMeasurable feet = com.quantitymeasurement.units.LengthUnit.FEET;
        IMeasurable inches = com.quantitymeasurement.units.LengthUnit.INCHES;

        QuantityModel<IMeasurable> q1 =
                new QuantityModel<>(2, feet);

        QuantityModel<IMeasurable> q2 =
                new QuantityModel<>(24, inches);

        QuantityMeasurementEntity comparisonEntity =
                new QuantityMeasurementEntity(
                        q1,
                        q2,
                        "COMPARE",
                        "Equal"
                );

        System.out.println(comparisonEntity);

        QuantityModel<IMeasurable> result =
                new QuantityModel<>(4, feet);

        QuantityMeasurementEntity arithmeticEntity =
                new QuantityMeasurementEntity(
                        q1,
                        q2,
                        "ADD",
                        result
                );

        System.out.println(arithmeticEntity);

        QuantityMeasurementEntity errorEntity =
                new QuantityMeasurementEntity(
                        q1,
                        q2,
                        "DIVIDE",
                        "Division by zero not allowed",
                        true
                );

        System.out.println(errorEntity);

        System.out.println("---- Entity Testing Complete ----");
    }
}