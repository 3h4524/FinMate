package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateWalletRequest {
    @NotNull(message = "User id is required")
    private Integer userId;
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Balance")
    private BigDecimal balance;
    private String currency;
}
