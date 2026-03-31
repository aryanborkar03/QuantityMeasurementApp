package com.app.quantitymeasurement.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * QuantityInputDTO
 *
 * Request body DTO that wraps the operands for a quantity measurement operation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuantityInputDTO {

    /** First operand. Required and fully validated. */
    @NotNull(message = "thisQuantityDTO must not be null")
    @Valid
    private QuantityDTO thisQuantityDTO;

    /** Second operand. Required and fully validated. */
    @NotNull(message = "thatQuantityDTO must not be null")
    @Valid
    private QuantityDTO thatQuantityDTO;

    /**
     * Optional target unit for the operation result.
     * When omitted, the result uses the unit of {@code thisQuantityDTO}.
     */
    @Valid
    private QuantityDTO targetUnitDTO;
}
