package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateGoalRequest {

    @Size(max = 100, message = "EXCEED_MAX_LENGTH_OF_NAME")
    private String name;

    @NotNull(message = "USER_ID_IS_REQUIRED")
    private Integer userId;

    private String description;

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "AMOUNT_MUST_BE_POSITIVE")
    private BigDecimal targetAmount;
    @NotNull(message = "Current amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "AMOUNT_MUST_BE_POSITIVE")
    private BigDecimal currentAmount;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "Target Date is required")
    private LocalDate deadline;

    private Boolean isLongTerm;
}

