package com.boxhilltravel.payment.domain.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Manager refund request.
 */
@Data
public class RefundOrderBo {

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Refund reason is required")
    private String reason;

}
