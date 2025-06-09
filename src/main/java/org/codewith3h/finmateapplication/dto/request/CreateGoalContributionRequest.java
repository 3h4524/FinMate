package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateGoalContributionRequest {

    @NotNull(message = "GOAL_ID_IS_REQUIRED")
    private Integer goalId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "AMOUNT_MUST_BE_POSITIVE")
    private BigDecimal amount;

    private String note;

    @NotNull(message = "Contribution Date is required")
    private LocalDate contributionDate;
}
