package com.app.quantitymeasurement.entity;

import com.app.quantitymeasurement.model.QuantityModel;
import com.app.quantitymeasurement.unit.IMeasurable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


@Entity
@Table(name = "quantity_measurement")
@Data
@EqualsAndHashCode(of = {"thisValue", "thisUnit", "thatValue", "thatUnit", "operation"})
@NoArgsConstructor
public class QuantityMeasurementEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Auto-generated primary key. Uses the IDENTITY strategy for H2 and MySQL. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // First operand
    // -------------------------------------------------------------------------

    @Column(name = "this_value")
    private Double thisValue;

    @Column(name = "this_unit")
    private String thisUnit;

    @Column(name = "this_measurement_type")
    private String thisMeasurementType;

    // -------------------------------------------------------------------------
    // Second operand
    // -------------------------------------------------------------------------

    @Column(name = "that_value")
    private Double thatValue;

    @Column(name = "that_unit")
    private String thatUnit;

    @Column(name = "that_measurement_type")
    private String thatMeasurementType;

    // -------------------------------------------------------------------------
    // Operation
    // -------------------------------------------------------------------------

    /**
     * Operation type in lowercase (e.g., {@code "compare"}, {@code "add"}).
     */
    @Column(name = "operation")
    private String operation;

    // -------------------------------------------------------------------------
    // Result
    // -------------------------------------------------------------------------

    /** Numeric result for arithmetic and conversion operations; {@code null} for compare. */
    @Column(name = "result_value")
    private Double resultValue;

    /** Unit of the result quantity; {@code null} for compare and divide. */
    @Column(name = "result_unit")
    private String resultUnit;

    /** Measurement category of the result; {@code null} for compare and divide. */
    @Column(name = "result_measurement_type")
    private String resultMeasurementType;

    /**
     * String result for compare operations ({@code "true"} or {@code "false"}).
     * {@code null} for all other operations.
     */
    @Column(name = "result_string")
    private String resultString;

    // -------------------------------------------------------------------------
    // Error
    // -------------------------------------------------------------------------

    /** {@code true} when the operation failed and was not completed successfully. */
    @Column(name = "is_error")
    private boolean error;

    /** Error description when {@code error} is {@code true}; {@code null} otherwise. */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    // -------------------------------------------------------------------------
    // Audit
    // -------------------------------------------------------------------------

    /** Timestamp set automatically when the record is first persisted. */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Sets {@code createdAt} immediately before the entity is inserted into the database.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Model-based constructors
    // -------------------------------------------------------------------------

    /**
     * Creates a record for a compare or convert operation whose result is a string.
     *
     * @param thisQuantity first operand
     * @param thatQuantity second operand
     * @param operation    operation name (e.g., {@code "compare"})
     * @param result       string result (e.g., {@code "true"}, {@code "false"})
     */
    public QuantityMeasurementEntity(
            QuantityModel<IMeasurable> thisQuantity,
            QuantityModel<IMeasurable> thatQuantity,
            String operation,
            String result) {
        this(thisQuantity, thatQuantity, operation);
        this.resultString = result;
    }

    /**
     * Creates a record for an arithmetic operation whose result is a {@link QuantityModel}.
     *
     * @param thisQuantity first operand
     * @param thatQuantity second operand
     * @param operation    operation name (e.g., {@code "add"})
     * @param result       result quantity carrying value, unit, and measurement type
     */
    public QuantityMeasurementEntity(
            QuantityModel<IMeasurable> thisQuantity,
            QuantityModel<IMeasurable> thatQuantity,
            String operation,
            QuantityModel<IMeasurable> result) {
        this(thisQuantity, thatQuantity, operation);
        this.resultValue            = result.getValue();
        this.resultUnit             = result.getUnit().getUnitName();
        this.resultMeasurementType  = result.getUnit().getMeasurementType();
    }

    /**
     * Creates an error record for a failed operation.
     *
     * @param thisQuantity first operand
     * @param thatQuantity second operand
     * @param operation    operation that failed
     * @param errorMessage description of the failure
     * @param isError      must be {@code true}; included for explicitness at call sites
     */
    public QuantityMeasurementEntity(
            QuantityModel<IMeasurable> thisQuantity,
            QuantityModel<IMeasurable> thatQuantity,
            String operation,
            String errorMessage,
            boolean isError) {
        this(thisQuantity, thatQuantity, operation);
        this.errorMessage = errorMessage;
        this.error        = isError;
    }

    /**
     * Base constructor shared by all model-based constructors.
     * Populates operand fields and validates that neither operand is null.
     *
     * @param thisQuantity first operand
     * @param thatQuantity second operand
     * @param operation    operation name
     * @throws IllegalArgumentException if either operand is {@code null}
     */
    public QuantityMeasurementEntity(
            QuantityModel<IMeasurable> thisQuantity,
            QuantityModel<IMeasurable> thatQuantity,
            String operation) {
        if (thisQuantity == null || thatQuantity == null) {
            throw new IllegalArgumentException("Quantities cannot be null");
        }
        this.thisValue           = thisQuantity.getValue();
        this.thisUnit            = thisQuantity.getUnit().getUnitName();
        this.thisMeasurementType = thisQuantity.getUnit().getMeasurementType();
        this.thatValue           = thatQuantity.getValue();
        this.thatUnit            = thatQuantity.getUnit().getUnitName();
        this.thatMeasurementType = thatQuantity.getUnit().getMeasurementType();
        this.operation           = operation;
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    /**
     * Returns a human-readable description of this record for logging and debugging.
     *
     * @return formatted entity string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(error ? "[ERROR] " : "[SUCCESS] ").append("operation=").append(operation);
        sb.append(", operand1=").append(thisValue).append(" ").append(thisUnit);
        sb.append(", operand2=").append(thatValue).append(" ").append(thatUnit);
        if (error) {
            sb.append(", message=").append(errorMessage);
        } else if (resultString != null && !resultString.isEmpty()) {
            sb.append(", result=").append(resultString);
        } else {
            sb.append(", result=").append(resultValue).append(" ").append(resultUnit);
        }
        return sb.toString();
    }
}
